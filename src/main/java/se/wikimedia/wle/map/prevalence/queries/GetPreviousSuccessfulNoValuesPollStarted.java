package se.wikimedia.wle.map.prevalence.queries;

import org.prevayler.Query;
import se.wikimedia.wle.map.prevalence.Root;

import java.time.LocalDateTime;
import java.util.Date;

public class GetPreviousSuccessfulNoValuesPollStarted implements Query<Root, LocalDateTime> {

    @Override
    public LocalDateTime query(Root root, Date date) throws Exception {
        return root.getPreviousSuccessfulNoValuesPollStarted();
    }
}
