package se.wikimedia.wle.map;

import lombok.Getter;
import net.sourceforge.jwbf.core.actions.HttpActionClient;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class WikimediaCommons extends AbstractLifecycle implements SmartLifecycle {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Getter
    private MediaWikiBot bot;

    private String userAgent = "wle2020-wmse-map";
    private String userAgentVersion = "0.1.0";
    //  private String username = "Karl Wettin (WMSE)";
//  private String password = "";
    private String emailAddress = "karl.wettin+bot@wikimedia.se";

    @Override
    public void doStart() throws Exception {
        HttpActionClient client = HttpActionClient.builder() //
                .withUrl("https://commons.wikimedia.org/w/") //
                .withUserAgent(userAgent, userAgentVersion, emailAddress) //
                .withRequestsPerUnit(60, TimeUnit.MINUTES) //
                .build();
        bot = new MediaWikiBot(client);
    }

    @Override
    public void doStop() throws Exception {
    }

}
