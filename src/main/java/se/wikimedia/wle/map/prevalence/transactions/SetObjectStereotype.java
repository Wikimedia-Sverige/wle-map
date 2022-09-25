package se.wikimedia.wle.map.prevalence.transactions;

import lombok.Data;
import org.prevayler.TransactionWithQuery;
import se.wikimedia.wle.map.domain.NaturvardsregistretObject;
import se.wikimedia.wle.map.prevalence.Root;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Data
public class SetObjectStereotype implements TransactionWithQuery<Root, NaturvardsregistretObject>, Serializable {

    public static final long serialVersionUID = 1L;

    private UUID identity;
    private String stereotype;

    public SetObjectStereotype() {
    }

    public static SetObjectStereotype factory(UUID identity, String stereotype) {
        SetObjectStereotype instance = new SetObjectStereotype();
        instance.identity = identity;
        instance.stereotype = stereotype;
        return instance;
    }

    @Override
    public NaturvardsregistretObject executeAndQuery(Root root, Date date) throws Exception {
        NaturvardsregistretObject object = root.getNaturvardsregistretObjects().get(identity);
        object.setStereotype(root.getStereotypes().intern(stereotype));
        return object;
    }
}
