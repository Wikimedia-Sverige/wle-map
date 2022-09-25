package se.wikimedia.wle.map.prevalence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prevayler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import se.wikimedia.wle.map.AbstractLifecycle;

import java.io.File;
import java.io.IOException;

@Service
public class PrevaylerService extends AbstractLifecycle implements SmartLifecycle {

    private final Logger log = LogManager.getLogger(getClass());

    private Prevayler<Root> prevayler;

    private final String prevalenceBase;
    private final boolean prevalenceBaseMkDirs;

    @Autowired
    public PrevaylerService(
            @Qualifier("prevalenceBase") String prevalenceBase,
            @Qualifier("prevalenceBase.mkdirs") boolean prevalenceBaseMkDirs
    ) {
        this.prevalenceBase = prevalenceBase;
        this.prevalenceBaseMkDirs = prevalenceBaseMkDirs;
    }

    @Override
    public void doStart() throws Exception {
        File prevalenceBase = new File(this.prevalenceBase);
        if (!prevalenceBase.exists())
            if (!prevalenceBaseMkDirs)
                throw new IOException("Prevalence base " + prevalenceBase.getAbsolutePath() + " does not exists");
            else if (!prevalenceBase.mkdirs())
                throw new IOException("Unable to mkdirs " + prevalenceBase.getAbsolutePath());
        log.info("Setting up Prevayler...");
        prevayler = PrevaylerFactory.createPrevayler(new Root(), prevalenceBase.getAbsolutePath());
        log.info("Prevayler has been loaded.");
    }

    @Override
    public void doStop() throws Exception {
        prevayler.close();
    }

    public void execute(Transaction<Root> transaction) {
        prevayler.execute(transaction);
    }

    public <R> R execute(TransactionWithQuery<Root, R> transaction) throws Exception {
        return prevayler.execute(transaction);
    }

    public <R> R execute(SureTransactionWithQuery<Root, R> transaction) {
        return prevayler.execute(transaction);
    }

    public <R> R execute(Query<Root, R> query) throws Exception {
        return prevayler.execute(query);
    }

}
