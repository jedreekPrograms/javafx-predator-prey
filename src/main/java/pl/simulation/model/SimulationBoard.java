package pl.simulation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/** All board mutations and occupancy decisions use the same monitor. */
public final class SimulationBoard {
    private final int width, height;
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
    }

    public synchronized int spawn(Species species, Position position) {
        Objects.requireNonNull(species); Objects.requireNonNull(position);
        if (!contains(position) || occupant(position) != null) throw new IllegalArgumentException("Cell unavailable");
        if (species == Species.WOLF && animals.stream().anyMatch(a -> a.species == Species.WOLF))
            throw new IllegalArgumentException("The simulation has one wolf");
        int id = animals.size();
        animals.add(new Animal(id, species, position));
        return id;
    }

    public synchronized BoardSnapshot snapshot() {
        return new BoardSnapshot(width, height, animals.stream().map(Animal::state).toList());
    }

    public synchronized boolean isAlive(int id) { return animal(id).alive; }
    public synchronized boolean isPaused(int id) { return animal(id).paused; }
    public boolean contains(Position p) { return p.x() >= 0 && p.y() >= 0 && p.x() < width && p.y() < height; }

    private Animal animal(int id) {
        if (id < 0 || id >= animals.size()) throw new IllegalArgumentException("Unknown animal");
        return animals.get(id);
    }

    private Animal occupant(Position p) {
        return animals.stream().filter(a -> a.alive && a.position.equals(p)).findFirst().orElse(null);
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
