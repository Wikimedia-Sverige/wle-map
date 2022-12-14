package se.wikimedia.wle.map.prevalence.transactions;

import lombok.Data;
import org.prevayler.TransactionWithQuery;
import se.wikimedia.wle.map.domain.NaturvardsregistretObject;
import se.wikimedia.wle.map.prevalence.Root;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
public class SetObjectUpdatedFromCommons implements TransactionWithQuery<Root, NaturvardsregistretObject>, Serializable {

    public static final long serialVersionUID = 1L;

    private UUID identity;
    private LocalDateTime when;

    public SetObjectUpdatedFromCommons() {
    }

    public static SetObjectUpdatedFromCommons factory(UUID identity, LocalDateTime when) {
        SetObjectUpdatedFromCommons instance = new SetObjectUpdatedFromCommons();
        instance.identity = identity;
        instance.when = when;
        return instance;
    }

    @Override
    public NaturvardsregistretObject executeAndQuery(Root root, Date date) throws Exception {
        NaturvardsregistretObject object = root.getNaturvardsregistretObjects().get(identity);
        object.setUpdatedFromCommons(when);
        return object;
    }
}
