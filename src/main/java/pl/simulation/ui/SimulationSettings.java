package pl.simulation.ui;

public record SimulationSettings(int width, int height, int hares, int delay) {
    public SimulationSettings {
        if (width < 1 || height < 1 || width > 100 || height > 100)
            throw new IllegalArgumentException("Wymiary planszy: 1–100.");
        if (hares < 0 || hares >= width * height)
            throw new IllegalArgumentException("Zostaw co najmniej jedno pole dla wilka.");
        if (delay < 1 || delay > 60_000) throw new IllegalArgumentException("Opóźnienie: 1–60000 ms.");
    }
}
