package se.wikimedia.wle.map;

import lombok.Data;
import org.constretto.ConstrettoConfiguration;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.prevayler.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wololo.geojson.Feature;
import org.wololo.geojson.Point;
import se.wikimedia.wle.map.domain.NaturvardsregistretObject;
import se.wikimedia.wle.map.domain.WikimediaImage;
import se.wikimedia.wle.map.index.BoundingBox;
import se.wikimedia.wle.map.index.NaturvardsregistretIndex;
import se.wikimedia.wle.map.naturvardsverket.NaturvardsregistretGeometryManager;
import se.wikimedia.wle.map.prevalence.PrevaylerService;
import se.wikimedia.wle.map.prevalence.Root;
import se.wikimedia.wle.map.prevalence.queries.GetWikimediaImage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
public class REST {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final PrevaylerService prevayler;
    private final NaturvardsregistretIndex index;
    private final NaturvardsregistretGeometryManager geometryManager;

    /**
     * If true, then do CPU intensive evaluation of results
     * to only produce results within the viewport,
     * rather than simple envelope bounding box intersections
     * that could produce some results that are not visible.
     */
    private boolean filterOutNonMatchingItems = false;


    @Autowired
    public REST(
            PrevaylerService prevayler,
            NaturvardsregistretIndex index,
            NaturvardsregistretGeometryManager geometryManager,
            @Autowired
            @Qualifier("service.ini")
                    ConstrettoConfiguration constrettoConfiguration

    ) {
        this.prevayler = prevayler;
        this.index = index;
        this.geometryManager = geometryManager;
        this.filterOutNonMatchingItems = constrettoConfiguration.evaluateToBoolean("search.filterOutNonMatchingItems");
    }

    @Data
    public static class SearchResultDTO {
        private UUID identity;
        private String nvrid;
        private String stereotype;
        private Feature feature;
        private Point centroid;
        private String label;
        private List<SearchResultImageDTO> images = new ArrayList<>();
        private String q;
    }

    @Data
    public static class SearchResultImageDTO {
        private String url;
        private String filename;
        private Integer width;
        private Integer height;
    }

    private final Comparator<SearchResultDTO> searchResultDTOComparator = new Comparator<>() {

        private final Map<String, Integer> order = Stream.of(new Object[][]{
                {"biosphere reserve", 1},
                {"national park", 2},
                {"nature reserve", 3},
                {"natural monument", 4},
                {null, 5}
        }).collect(Collectors.toMap(data -> (String) data[0], data -> (Integer) data[1]));

        @Override
        public int compare(SearchResultDTO dto1, SearchResultDTO dto2) {
            return Integer.compare(order.get(dto1.getStereotype()), order.get(dto2.getStereotype()));
        }
    };

    @Data
    public static class SearchEnvelopeRequestDTO {
        private BoundingBox boundingBox;
        // todo use zoom level, limit allowed values, or something. this is a potential out of memory server killer if abused!
        private Double distanceTolerance;

    }

    @PostMapping(
            path = "nvr/search/envelope",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<List<SearchResultDTO>> search(
            @RequestBody SearchEnvelopeRequestDTO request
    ) {

        long started = System.currentTimeMillis();
        try {

            // query rough geometry envelope index for items that match.

            Set<UUID> searchResults = index.search(request.getBoundingBox());
            Set<NaturvardsregistretObject> objects = prevayler.execute(new Query<Root, Set<NaturvardsregistretObject>>() {
                @Override
                public Set<NaturvardsregistretObject> query(Root root, Date date) throws Exception {
                    Set<NaturvardsregistretObject> objects = new HashSet<>(searchResults.size());
                    for (UUID identity : searchResults) {
                        objects.add(root.getNaturvardsregistretObjects().get(identity));
                    }
                    return objects;
                }
            });

            if (filterOutNonMatchingItems) {
                long startedFilterOutNonMatchingItems = System.currentTimeMillis();
                Polygon polygon = new GeometryFactory().createPolygon(new Coordinate[]{
                        new Coordinate(request.getBoundingBox().getWestLongitude(), request.getBoundingBox().getSouthLatitude()),
                        new Coordinate(request.getBoundingBox().getWestLongitude(), request.getBoundingBox().getNorthLatitude()),
                        new Coordinate(request.getBoundingBox().getEastLongitude(), request.getBoundingBox().getNorthLatitude()),
                        new Coordinate(request.getBoundingBox().getEastLongitude(), request.getBoundingBox().getSouthLatitude()),
                        new Coordinate(request.getBoundingBox().getWestLongitude(), request.getBoundingBox().getSouthLatitude()),
                });

                int filteredOutCount = 0;
                for (Iterator<NaturvardsregistretObject> iterator = objects.iterator(); iterator.hasNext(); ) {
                    NaturvardsregistretObject object = iterator.next();
                    Geometry geometry = geometryManager.getJtsGeometry(object);
                    if (!geometry.intersects(polygon) && !polygon.contains(geometry)) {
                        iterator.remove();
                        filteredOutCount++;
                    }
                }
                log.trace("{} milliseconds to filter out {} items outside of the viewport", (System.currentTimeMillis() - startedFilterOutNonMatchingItems), filteredOutCount);
            }

            List<SearchResultDTO> searchResultDTOS = new ArrayList<>();
            for (NaturvardsregistretObject object : objects) {
                SearchResultDTO dto = new SearchResultDTO();
                dto.setIdentity(object.getIdentity());
                dto.setStereotype(object.getStereotype());
                org.wololo.geojson.Geometry geometry;
                if (request.getDistanceTolerance() != null) {
                    geometry = geometryManager.getSimplifiedGeoJsonGeometry(object, request.getDistanceTolerance());
                } else {
                    geometry = geometryManager.getGeoJsonGeometry(object);
                }
                dto.setNvrid(object.getNaturvardsregistretIdentity());
                dto.setFeature(new Feature(geometry, Collections.emptyMap()));
                dto.setCentroid(geometryManager.getGeoJsonFeatureCentroid(object));
                dto.setLabel(object.getWikidataLabel());
                for (String filename : object.getWikidataImageNames()) {
                    WikimediaImage wikimediaImage = prevayler.execute(new GetWikimediaImage(filename));
                    if (wikimediaImage == null) {
                        log.warn("Missing Wikimedia image for filename {}", filename);
                    } else {
                        SearchResultImageDTO image = new SearchResultImageDTO();
                        image.setUrl(wikimediaImage.getThumburl());
                        image.setFilename(wikimediaImage.getFilename());
                        image.setWidth(wikimediaImage.getThumbwidth());
                        image.setHeight(wikimediaImage.getThumbheight());
                        dto.getImages().add(image);
                    }
                }
                dto.setQ(object.getWikidataQ());
                searchResultDTOS.add(dto);
            }

            searchResultDTOS.sort(searchResultDTOComparator);

            log.info("{} results in {} milliseconds", searchResultDTOS.size(), (System.currentTimeMillis() - started));

            return ResponseEntity.ok(searchResultDTOS);

        } catch (Exception e) {
            log.warn("Caught exception", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
