package pl.simulation.model;

public record Position(int x, int y) {
    public long squaredDistance(Position other) {
        long dx = (long) x - other.x, dy = (long) y - other.y;
        return dx * dx + dy * dy;
    }
}
