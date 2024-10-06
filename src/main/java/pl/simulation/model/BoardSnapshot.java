package pl.simulation.model;

import java.util.List;
public record BoardSnapshot(int width, int height, List<AnimalState> animals) {
    public BoardSnapshot { animals = List.copyOf(animals); }
    public long livingHares() {
        return animals.stream().filter(a -> a.alive() && a.species() == Species.HARE).count();
    }
}
