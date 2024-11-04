package pl.simulation.app;

import java.util.Random;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.simulation.runtime.*;
import pl.simulation.ui.*;

public final class SimulationApplication extends Application {
    private SimulationEngine engine;
    private BoardView view;
    @Override public void start(Stage stage) {
        var selected = SettingsDialog.show(stage);
        if (selected.isEmpty()) { Platform.exit(); return; }
        var config = selected.get();
        var board = BoardFactory.create(config.width(), config.height(), config.hares(), new Random());
        engine = new SimulationEngine(board, config.delay());
        view = new BoardView(board, engine);
        stage.setTitle("Wilk i zające — symulacja współbieżna");
        stage.setScene(new Scene(view, 620, 560)); stage.show();
        engine.start();
    }
    @Override public void stop() {
        if (view != null) view.close();
        if (engine != null) engine.close();
    }
    public static void main(String[] args) { launch(args); }
}
