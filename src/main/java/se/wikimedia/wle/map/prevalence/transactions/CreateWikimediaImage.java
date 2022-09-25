package se.wikimedia.wle.map.prevalence.transactions;

import lombok.Data;
import org.prevayler.TransactionWithQuery;
import se.wikimedia.wle.map.domain.WikimediaImage;
import se.wikimedia.wle.map.prevalence.Root;

import java.io.Serializable;
import java.util.Date;

@Data
public class CreateWikimediaImage implements TransactionWithQuery<Root, WikimediaImage>, Serializable {

    public static final long serialVersionUID = 1L;

    private String filename;

    private String thumburl;
    private Integer thumbwidth;
    private Integer thumbheight;
    private String url;
    private String descriptionurl;
    private String descriptionshorturl;

    public CreateWikimediaImage() {
    }

    public static CreateWikimediaImage factory(String filename, String thumburl, Integer thumbwidth, Integer thumbheight, String url, String descriptionurl, String descriptionshorturl) {
        CreateWikimediaImage instance = new CreateWikimediaImage();
        instance.filename = filename;
        instance.thumburl = thumburl;
        instance.thumbwidth = thumbwidth;
        instance.thumbheight = thumbheight;
        instance.url = url;
        instance.descriptionurl = descriptionurl;
        instance.descriptionshorturl = descriptionshorturl;
        return instance;
    }

    @Override
    public WikimediaImage executeAndQuery(Root root, Date date) throws Exception {
        WikimediaImage instance = new WikimediaImage();
        instance.setFilename(filename);
        instance.setThumburl(thumburl);
        instance.setThumbwidth(thumbwidth);
        instance.setThumbheight(thumbheight);
        instance.setUrl(url);
        instance.setDescriptionurl(descriptionurl);
        instance.setDescriptionshorturl(descriptionshorturl);
        root.getWikimediaImages().put(filename, instance);
        return instance;
    }
}
