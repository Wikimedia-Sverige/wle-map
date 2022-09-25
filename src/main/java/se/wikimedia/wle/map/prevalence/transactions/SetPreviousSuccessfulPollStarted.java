package se.wikimedia.wle.map.prevalence.transactions;

import lombok.Data;
import org.prevayler.Transaction;
import se.wikimedia.wle.map.prevalence.Root;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class SetPreviousSuccessfulPollStarted implements Transaction<Root>, Serializable {

    public static final long serialVersionUID = 1L;

    private LocalDateTime previousSuccessfulPollStarted;

    public SetPreviousSuccessfulPollStarted() {
    }

    public static SetPreviousSuccessfulPollStarted factory(LocalDateTime previousSuccessfulPollStarted) {
        SetPreviousSuccessfulPollStarted instance = new SetPreviousSuccessfulPollStarted();
        instance.previousSuccessfulPollStarted = previousSuccessfulPollStarted;
        return instance;
    }

    @Override
    public void executeOn(Root root, Date date) {
        root.setPreviousSuccessfulPollStarted(previousSuccessfulPollStarted);
    }
}
