package pl.simulation.runtime;

import java.util.ArrayList;
import java.util.random.RandomGenerator;
import pl.simulation.model.*;

public final class BoardFactory {
    private BoardFactory() { }
    public static SimulationBoard create(int width, int height, int hares, RandomGenerator random) {
        SimulationBoard board = new SimulationBoard(width, height);
        if (hares < 0 || hares >= width * height) throw new IllegalArgumentException("Leave one cell for the wolf");
        var positions = new ArrayList<Position>();
        for (int x = 0; x < width; x++) for (int y = 0; y < height; y++) positions.add(new Position(x, y));
        for (int i = positions.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Position value = positions.get(i); positions.set(i, positions.get(j)); positions.set(j, value);
        }
        board.spawn(Species.WOLF, positions.getFirst());
        for (int i = 1; i <= hares; i++) board.spawn(Species.HARE, positions.get(i));
        return board;
    }
}
