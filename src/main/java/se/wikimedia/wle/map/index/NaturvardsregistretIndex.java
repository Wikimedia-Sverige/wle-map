package se.wikimedia.wle.map.index;

import lombok.Getter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.prevayler.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import org.wololo.jts2geojson.GeoJSONReader;
import se.wikimedia.wle.map.AbstractLifecycle;
import se.wikimedia.wle.map.domain.NaturvardsregistretObject;
import se.wikimedia.wle.map.prevalence.PrevaylerService;
import se.wikimedia.wle.map.prevalence.Root;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A rough index contain geometry envelopes.
 * I.e. it will catch all items that are in scope for the query,
 * but possible also items that are out of scope for the query.
 * Precise filtering is required, and so done in the REST.
 */
@Service
public class NaturvardsregistretIndex extends AbstractLifecycle implements SmartLifecycle {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final PrevaylerService prevayler;
    private final boolean lucenePathMkDirs;

    @Autowired
    public NaturvardsregistretIndex(
            PrevaylerService prevayler,
            @Qualifier("lucenePath") String lucenePath,
            @Qualifier("lucenePath.mkdirs") boolean lucenePathMkDirs
    ) {
        this.prevayler = prevayler;
        this.lucenePath = new File(lucenePath);
        this.lucenePathMkDirs = lucenePathMkDirs;
    }

    private Directory directory;
    private IndexWriter indexWriter;
    private SearcherManager searcherManager;

    @Getter
    private final File lucenePath;

    public static final String FIELD_SOUTH_LATITUDE = "south_latitude";
    public static final String FIELD_WEST_LONGITUDE = "west_longitude";
    public static final String FIELD_NORTH_LATITUDE = "north_latitude";
    public static final String FIELD_EAST_LONGITUDE = "east_longitude";
    public static final String FIELD_IDENTITY = "identity";

    @Override
    public void doStart() throws Exception {
        if (!lucenePath.exists()) {
            if (!lucenePathMkDirs) {
                throw new IOException("Lucene path " + lucenePath.getAbsolutePath() + " does not exists");
            } else if (!lucenePath.mkdirs()) {
                throw new IOException("Could not mkdirs" + lucenePath.getAbsolutePath());
            }
        }
        directory = FSDirectory.open(lucenePath.toPath(), NoLockFactory.INSTANCE);
        IndexWriterConfig config = new IndexWriterConfig();
        config.setCommitOnClose(true);
        indexWriter = new IndexWriter(directory, config);
        searcherManager = new SearcherManager(indexWriter, new SearcherFactory());
    }

    @Override
    public void doStop() throws Exception {
        searcherManager.close();
        indexWriter.close();
        directory.close();
    }

    public void commit() throws Exception {
        indexWriter.commit();
        searcherManager.maybeRefresh();
    }

    public void reconstruct() throws Exception {
        log.info("Reconstructing index...");
        indexWriter.deleteAll();
        for (NaturvardsregistretObject object : prevayler.execute(new Query<Root, Collection<NaturvardsregistretObject>>() {
            @Override
            public Collection<NaturvardsregistretObject> query(Root root, Date date) throws Exception {
                return new HashSet<>(root.getNaturvardsregistretObjects().values());
            }
        })) {
            if (object.getFeatureGeometry() != null) {
                indexWriter.addDocument(documentFactory(object));
            }
        }
        commit();
        log.info("Index has been reconstructed.");
    }

    public void update(NaturvardsregistretObject object) throws Exception {
        indexWriter.updateDocument(new Term(FIELD_IDENTITY, object.getIdentity().toString()), documentFactory(object));
        searcherManager.maybeRefresh();
    }

    private Document documentFactory(NaturvardsregistretObject object) {

        BoundingBox boundingBox;

        try {
            GeoJSONReader geoJSONReader = new GeoJSONReader();
            Geometry geometry = geoJSONReader.read(object.getFeatureGeometry());
            Geometry envelope = geometry.getEnvelope();
            Coordinate[] envelopeCoordinates = envelope.getCoordinates();
            boundingBox = new BoundingBox();
            if (envelopeCoordinates.length == 1) {
                boundingBox.setSouthLatitude(envelopeCoordinates[0].y);
                boundingBox.setWestLongitude(envelopeCoordinates[0].x);
                boundingBox.setNorthLatitude(envelopeCoordinates[0].y);
                boundingBox.setEastLongitude(envelopeCoordinates[0].x);
            } else {
                boundingBox.setSouthLatitude(envelopeCoordinates[0].y);
                boundingBox.setWestLongitude(envelopeCoordinates[0].x);
                boundingBox.setNorthLatitude(envelopeCoordinates[2].y);
                boundingBox.setEastLongitude(envelopeCoordinates[2].x);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Document document = new Document();
        document.add(new DoublePoint(FIELD_SOUTH_LATITUDE, boundingBox.getSouthLatitude()));
        document.add(new DoublePoint(FIELD_WEST_LONGITUDE, boundingBox.getWestLongitude()));
        document.add(new DoublePoint(FIELD_NORTH_LATITUDE, boundingBox.getNorthLatitude()));
        document.add(new DoublePoint(FIELD_EAST_LONGITUDE, boundingBox.getEastLongitude()));
        document.add(new StringField(FIELD_IDENTITY, object.getIdentity().toString(), Field.Store.YES));

        return document;
    }

    public Set<UUID> search(double centroidLongitude, double centroidLatitude, double radiusKilometers) throws Exception {

        int circumferenceResolution = 20;

        double southLatitude = Double.MAX_VALUE;
        double westLongitude = Double.MAX_VALUE;
        double northLatitude = Double.MIN_VALUE;
        double eastLongitude = Double.MIN_VALUE;

        double radiusLatitude = (radiusKilometers / 6378.8d) * (180 / Math.PI);
        double radiusLongitude = radiusLatitude / Math.cos(centroidLatitude * (Math.PI / 180));

        int step = (int) (360d / (double) circumferenceResolution);
        for (int i = 0; i < 360; i += step) {
            double a = i * (Math.PI / 180);
            double latitude = centroidLatitude + (radiusLatitude * Math.sin(a));
            double longitude = centroidLongitude + (radiusLongitude * Math.cos(a));
            if (latitude < southLatitude) {
                southLatitude = latitude;
            }
            if (latitude > northLatitude) {
                northLatitude = latitude;
            }
            if (longitude < westLongitude) {
                westLongitude = longitude;
            }
            if (longitude > eastLongitude) {
                eastLongitude = longitude;
            }
        }

        return search(new BoundingBox(southLatitude, westLongitude, northLatitude, eastLongitude));
    }

    public Set<UUID> search(BoundingBox boundingBox) throws Exception {

        Set<UUID> searchResults = new HashSet<>();
        IndexSearcher searcher = searcherManager.acquire();
        try {
            searcher.search(new BoundingBoxQueryBuilder(boundingBox).build(), new SimpleCollector() {

                private LeafReaderContext context;

                @Override
                protected void doSetNextReader(LeafReaderContext context) throws IOException {
                    this.context = context;
                }

                @Override
                public void collect(int doc) throws IOException {
                    // todo use binary doc values! but i cant get it to advance correct!
                    searchResults.add(UUID.fromString(context.reader().document(doc).get(FIELD_IDENTITY)));
                }

                @Override
                public ScoreMode scoreMode() {
                    return ScoreMode.COMPLETE_NO_SCORES;
                }
            });

            return searchResults;
        } finally {
            searcherManager.release(searcher);
        }
    }


}
