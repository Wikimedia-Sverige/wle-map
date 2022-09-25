package se.wikimedia.wle.map;

import org.springframework.context.Lifecycle;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractLifecycle implements Lifecycle {

    private final AtomicBoolean starting = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean stopping = new AtomicBoolean(false);

    public abstract void doStart() throws Exception;

    public abstract void doStop() throws Exception;

    @Override
    public final void start() {
        if (running.get()) throw new IllegalStateException("Already running");
        if (stopping.get()) throw new IllegalStateException("Currently shutting down");
        if (!starting.compareAndSet(false, true)) throw new IllegalStateException("Already starting");
        try {
            doStart();
            running.set(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            starting.set(false);
        }
    }

    @Override
    public final void stop() {
        if (starting.get()) throw new IllegalStateException("Currently starting up");
        if (!running.get()) throw new IllegalStateException("Not running");
        if (!stopping.compareAndSet(false, true)) throw new IllegalStateException("Already stopping");
        try {
            doStop();
            running.set(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            stopping.set(false);
        }
    }

    @Override
    public final boolean isRunning() {
        return running.get();
    }

    public final boolean isStopping() {
        return stopping.get();
    }
}
