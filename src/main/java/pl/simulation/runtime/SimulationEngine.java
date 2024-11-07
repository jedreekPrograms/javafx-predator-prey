package pl.simulation.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import pl.simulation.model.*;

/** One independently sleeping virtual thread per animal. No worker touches JavaFX. */
public final class SimulationEngine implements AutoCloseable {
    private final SimulationBoard board;
    private final int delay;
    private final long captureDelay;
    private final List<Thread> workers = new ArrayList<>();
    private final Object control = new Object();
    private volatile boolean running;
    private boolean started, closed;

    public SimulationEngine(SimulationBoard board, int delay) { this(board, delay, 5000); }
    public SimulationEngine(SimulationBoard board, int delay, long captureDelay) {
        if (delay < 1 || delay > 60_000 || captureDelay < 0) throw new IllegalArgumentException("Invalid delay");
        this.board = board; this.delay = delay; this.captureDelay = captureDelay;
    }

    public synchronized void start() {
        if (started || closed) throw new IllegalStateException("Create a new engine to start again");
        started = true; running = true;
        for (AnimalState animal : board.snapshot().animals()) {
            Thread worker = Thread.ofVirtual().name("simulation-animal-" + animal.id()).unstarted(() -> run(animal));
            workers.add(worker); worker.start();
        }
    }

    private void run(AnimalState animal) {
        var random = ThreadLocalRandom.current();
        try {
            while (running && board.isAlive(animal.id())) {
                synchronized (control) {
                    while (running && board.isAlive(animal.id()) && board.isPaused(animal.id())) control.wait(100);
                }
                if (!running || !board.isAlive(animal.id())) break;
                Thread.sleep(delay + random.nextInt(delay));
                if (!running) break;
                StepResult result = animal.species() == Species.WOLF
                        ? board.stepWolf(animal.id(), random) : board.stepHare(animal.id(), random);
                if (result == StepResult.FINISHED) break;
                if (result == StepResult.CAUGHT) {
                    synchronized (control) { control.notifyAll(); }
                    Thread.sleep(captureDelay);
                }
            }
        } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
    }

    public void togglePauseAt(Position position) {
        board.togglePauseAt(position);
        synchronized (control) { control.notifyAll(); }
    }

    public synchronized boolean hasLiveWorkers() { return workers.stream().anyMatch(Thread::isAlive); }

    @Override public synchronized void close() {
        closed = true; running = false;
        workers.forEach(Thread::interrupt);
    }
}
