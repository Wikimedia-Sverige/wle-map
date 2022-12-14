package se.wikimedia.wle.map.prevalence.transactions;

import lombok.Data;
import org.prevayler.TransactionWithQuery;
import se.wikimedia.wle.map.domain.NaturvardsregistretObject;
import se.wikimedia.wle.map.prevalence.Root;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Data
public class RemoveNaturvardsregistretObject implements TransactionWithQuery<Root, NaturvardsregistretObject>, Serializable {

    public static final long serialVersionUID = 1L;

    private UUID identity;

    public RemoveNaturvardsregistretObject() {
    }

    public static RemoveNaturvardsregistretObject factory(UUID identity) {
        RemoveNaturvardsregistretObject instance = new RemoveNaturvardsregistretObject();
        instance.identity = identity;
        return instance;
    }

    @Override
    public NaturvardsregistretObject executeAndQuery(Root root, Date date) throws Exception {
        NaturvardsregistretObject object = root.getNaturvardsregistretObjects().remove(identity);
        if (object != null) {
            root.getNaturvardsregistretObjectsByQ().remove(object.getWikidataQ());
        }
        return object;
    }

}
