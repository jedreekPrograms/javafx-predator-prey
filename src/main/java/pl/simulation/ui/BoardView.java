package pl.simulation.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import pl.simulation.model.*;
import pl.simulation.runtime.SimulationEngine;

public final class BoardView extends BorderPane implements AutoCloseable {
    private final SimulationBoard board;
    private final StackPane[][] cells;
    private final Label status = new Label();
    private final Timeline refresh;

    public BoardView(SimulationBoard board, SimulationEngine engine) {
        this.board = board;
        BoardSnapshot initial = board.snapshot();
        cells = new StackPane[initial.width()][initial.height()];
        GridPane grid = new GridPane();
        for (int x = 0; x < initial.width(); x++) for (int y = 0; y < initial.height(); y++) {
            Position position = new Position(x, y);
            StackPane cell = new StackPane();
            cell.setMinSize(28, 28); cell.setPrefSize(36, 36);
            cell.setOnMouseClicked(event -> { engine.togglePauseAt(position); render(); });
            cells[x][y] = cell; grid.add(cell, x, y);
        }
        setCenter(new ScrollPane(grid));
        setBottom(status);
        render();
        refresh = new Timeline(new KeyFrame(Duration.millis(100), event -> render()));
        refresh.setCycleCount(Timeline.INDEFINITE); refresh.play();
    }

    public void render() {
        BoardSnapshot snapshot = board.snapshot();
        for (var column : cells) for (StackPane cell : column)
            cell.setStyle("-fx-background-color: white; -fx-border-color: #9ca3af;");
        for (AnimalState animal : snapshot.animals()) if (animal.alive()) {
            String color = animal.species() == Species.WOLF ? "#111827" : "#22c55e";
            String border = animal.paused() ? "#f59e0b" : "#9ca3af";
            cells[animal.position().x()][animal.position().y()].setStyle(
                    "-fx-background-color: " + color + "; -fx-border-color: " + border + "; -fx-border-width: 2;");
        }
        status.setText(snapshot.livingHares() == 0 ? "Symulacja zakończona — brak zajęcy."
                : "Żywe zające: " + snapshot.livingHares() + " | Kliknij zwierzę, aby wstrzymać lub wznowić.");
    }

    public StackPane cellAt(int x, int y) { return cells[x][y]; }
    @Override public void close() { refresh.stop(); }
}
