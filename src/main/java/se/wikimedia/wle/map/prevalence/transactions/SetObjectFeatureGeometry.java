package se.wikimedia.wle.map.prevalence.transactions;

import lombok.Data;
import org.prevayler.TransactionWithQuery;
import se.wikimedia.wle.map.domain.NaturvardsregistretObject;
import se.wikimedia.wle.map.prevalence.Root;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Data
public class SetObjectFeatureGeometry implements TransactionWithQuery<Root, NaturvardsregistretObject>, Serializable {

    public static final long serialVersionUID = 1L;

    private UUID identity;
    private String featureGeometry;
    private String commonsMapRevisionId;

    public SetObjectFeatureGeometry() {
    }

    public static SetObjectFeatureGeometry factory(UUID identity, String commonsMapRevisionId, String featureGeometry) {
        SetObjectFeatureGeometry instance = new SetObjectFeatureGeometry();
        instance.identity = identity;
        instance.commonsMapRevisionId = commonsMapRevisionId;
        instance.featureGeometry = featureGeometry;
        return instance;
    }

    @Override
    public NaturvardsregistretObject executeAndQuery(Root root, Date date) throws Exception {
        NaturvardsregistretObject object = root.getNaturvardsregistretObjects().get(identity);
        object.setFeatureGeometry(featureGeometry);
        object.setCommonsMapRevisionId(commonsMapRevisionId);
        return object;
    }
}
