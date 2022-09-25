package se.wikimedia.wle.map.prevalence.transactions;

import lombok.Data;
import org.prevayler.TransactionWithQuery;
import se.wikimedia.wle.map.domain.NaturvardsregistretObject;
import se.wikimedia.wle.map.prevalence.Root;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@Data
public class RemoveObjectImages implements TransactionWithQuery<Root, NaturvardsregistretObject>, Serializable {

    public static final long serialVersionUID = 1L;

    private UUID identity;
    private Collection<String> wikidataImageNames;

    public RemoveObjectImages() {
    }

    public static RemoveObjectImages factory(UUID identity, Collection<String> wikidataImageNames) {
        RemoveObjectImages instance = new RemoveObjectImages();
        instance.identity = identity;
        instance.wikidataImageNames = wikidataImageNames;
        return instance;
    }

    @Override
    public NaturvardsregistretObject executeAndQuery(Root root, Date date) throws Exception {
        NaturvardsregistretObject object = root.getNaturvardsregistretObjects().get(identity);
        object.getWikidataImageNames().removeAll(wikidataImageNames);
        return object;
    }
}
