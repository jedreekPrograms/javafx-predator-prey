package pl.simulation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/** All board mutations and occupancy decisions use the same monitor. */
public final class SimulationBoard {
    private final int width, height;
    private final Animal[][] occupied;
    private final List<Animal> animals = new ArrayList<>();

    private static final class Animal {
        final int id;
        final Species species;
        Position position;
        boolean alive = true, paused;
        Animal(int id, Species species, Position position) {
            this.id = id; this.species = species; this.position = position;
        }
        AnimalState state() { return new AnimalState(id, species, position, alive, paused); }
    }

    public SimulationBoard(int width, int height) {
        if (width < 1 || width > 100 || height < 1 || height > 100)
            throw new IllegalArgumentException("Board dimensions must be between 1 and 100");
        this.width = width; this.height = height;
        occupied = new Animal[width][height];
    }

    public synchronized int spawn(Species species, Position position) {
        Objects.requireNonNull(species); Objects.requireNonNull(position);
        if (!contains(position) || occupant(position) != null) throw new IllegalArgumentException("Cell unavailable");
        if (species == Species.WOLF && animals.stream().anyMatch(a -> a.species == Species.WOLF))
            throw new IllegalArgumentException("The simulation has one wolf");
        int id = animals.size();
        Animal added = new Animal(id, species, position);
        animals.add(added);
        occupied[position.x()][position.y()] = added;
        return id;
    }

    public synchronized BoardSnapshot snapshot() {
        return new BoardSnapshot(width, height, animals.stream().map(Animal::state).toList());
    }

    public synchronized boolean isAlive(int id) { return animal(id).alive; }
    public synchronized boolean isPaused(int id) { return animal(id).paused; }
    public boolean contains(Position p) { return p.x() >= 0 && p.y() >= 0 && p.x() < width && p.y() < height; }

    public synchronized StepResult stepHare(int id, RandomGenerator random) {
        Animal hare = animal(id);
        if (hare.species != Species.HARE) throw new IllegalArgumentException("Expected a hare");
        if (!hare.alive) return StepResult.FINISHED;
        if (hare.paused) return StepResult.WAITING;
        Animal wolf = animals.stream().filter(a -> a.species == Species.WOLF).findFirst().orElseThrow();
        List<Position> best = new ArrayList<>();
        long distance = -1;
        for (Position next : neighbors(hare.position)) {
            if (occupant(next) != null) continue;
            long candidate = next.squaredDistance(wolf.position);
            if (candidate > distance) { best.clear(); distance = candidate; }
            if (candidate == distance) best.add(next);
        }
        if (best.isEmpty()) return StepResult.WAITING;
        occupied[hare.position.x()][hare.position.y()] = null;
        hare.position = best.get(random.nextInt(best.size()));
        occupied[hare.position.x()][hare.position.y()] = hare;
        return StepResult.MOVED;
    }

    public synchronized StepResult stepWolf(int id, RandomGenerator random) {
        Animal wolf = animal(id);
        if (wolf.species != Species.WOLF) throw new IllegalArgumentException("Expected the wolf");
        List<Animal> living = animals.stream().filter(a -> a.alive && a.species == Species.HARE).toList();
        if (living.isEmpty()) return StepResult.FINISHED;
        if (wolf.paused) return StepResult.WAITING;
        List<Animal> targets = living.stream().filter(a -> !a.paused).toList();
        if (targets.isEmpty()) return StepResult.WAITING;
        List<Position> best = new ArrayList<>();
        long distance = Long.MAX_VALUE;
        for (Position next : neighbors(wolf.position)) {
            Animal occupied = occupant(next);
            if (occupied != null && (occupied.species == Species.WOLF || occupied.paused)) continue;
            long candidate = targets.stream().mapToLong(a -> next.squaredDistance(a.position)).min().orElseThrow();
            if (candidate < distance) { best.clear(); distance = candidate; }
            if (candidate == distance) best.add(next);
        }
        if (best.isEmpty()) return StepResult.WAITING;
        Position next = best.get(random.nextInt(best.size()));
        Animal caught = occupant(next);
        if (caught != null) caught.alive = false;
        occupied[wolf.position.x()][wolf.position.y()] = null;
        wolf.position = next;
        occupied[next.x()][next.y()] = wolf;
        return caught == null ? StepResult.MOVED : StepResult.CAUGHT;
    }

    public synchronized boolean togglePauseAt(Position position) {
        Animal selected = occupant(position);
        if (selected == null) return false;
        selected.paused = !selected.paused;
        return true;
    }

    private Animal animal(int id) {
        if (id < 0 || id >= animals.size()) throw new IllegalArgumentException("Unknown animal");
        return animals.get(id);
    }

    private Animal occupant(Position p) {
        return contains(p) ? occupied[p.x()][p.y()] : null;
    }

    private List<Position> neighbors(Position p) {
        List<Position> result = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) {
            if (dx == 0 && dy == 0) continue;
            Position next = new Position(p.x() + dx, p.y() + dy);
            if (contains(next)) result.add(next);
        }
        return result;
    }
}
