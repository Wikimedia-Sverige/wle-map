package se.wikimedia.wle.map.prevalence;

import lombok.Data;
import se.wikimedia.wle.map.domain.NaturvardsregistretObject;
import se.wikimedia.wle.map.domain.WikimediaImage;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class Root implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDateTime previousSuccessfulPollStarted = null;
    private LocalDateTime previousSuccessfulNoValuesPollStarted = null;

    private Map<UUID, NaturvardsregistretObject> naturvardsregistretObjects = new HashMap<>();
    private Map<String, NaturvardsregistretObject> naturvardsregistretObjectsByQ = new HashMap<>();

    private Map<String, WikimediaImage> wikimediaImages = new HashMap<>();

    private Intern<String> stereotypes = new Intern<>();

}
