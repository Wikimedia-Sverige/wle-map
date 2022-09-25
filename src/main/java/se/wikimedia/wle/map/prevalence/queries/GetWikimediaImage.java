package se.wikimedia.wle.map.prevalence.queries;

import org.prevayler.Query;
import se.wikimedia.wle.map.domain.WikimediaImage;
import se.wikimedia.wle.map.prevalence.Root;

import java.util.Date;

public class GetWikimediaImage implements Query<Root, WikimediaImage> {

    private String filename;

    public GetWikimediaImage(String filename) {
        this.filename = filename;
    }

    @Override
    public WikimediaImage query(Root root, Date date) throws Exception {
        return root.getWikimediaImages().get(filename);
    }
}
