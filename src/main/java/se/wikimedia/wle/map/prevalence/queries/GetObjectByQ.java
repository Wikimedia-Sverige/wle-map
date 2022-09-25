package se.wikimedia.wle.map.prevalence.queries;

import org.prevayler.Query;
import se.wikimedia.wle.map.domain.NaturvardsregistretObject;
import se.wikimedia.wle.map.prevalence.Root;

import java.util.Date;

public class GetObjectByQ implements Query<Root, NaturvardsregistretObject> {

    private String q;

    public GetObjectByQ(String q) {
        this.q = q;
    }

    @Override
    public NaturvardsregistretObject query(Root root, Date date) throws Exception {
        return root.getNaturvardsregistretObjectsByQ().get(q);
    }
}
