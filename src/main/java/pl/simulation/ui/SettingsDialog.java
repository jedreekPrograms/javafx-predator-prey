package pl.simulation.ui;

import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

public final class SettingsDialog {
    private SettingsDialog() { }
    public static Optional<SimulationSettings> show(Window owner) {
        Dialog<SimulationSettings> dialog = new Dialog<>();
        dialog.initOwner(owner); dialog.setTitle("Wilk i zające");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField width = new TextField("10"), height = new TextField("10");
        TextField hares = new TextField("5"), delay = new TextField("500");
        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setPadding(new Insets(15));
        form.addRow(0, new Label("Kolumny"), width); form.addRow(1, new Label("Wiersze"), height);
        form.addRow(2, new Label("Zające"), hares); form.addRow(3, new Label("Opóźnienie (ms)"), delay);
        Label error = new Label(); error.setStyle("-fx-text-fill: #b91c1c;"); form.add(error, 0, 4, 2, 1);
        dialog.getDialogPane().setContent(form);
        SimulationSettings[] result = new SimulationSettings[1];
        dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                result[0] = new SimulationSettings(Integer.parseInt(width.getText().trim()), Integer.parseInt(height.getText().trim()),
                        Integer.parseInt(hares.getText().trim()), Integer.parseInt(delay.getText().trim()));
            } catch (IllegalArgumentException invalid) { error.setText(invalid.getMessage()); event.consume(); }
        });
        dialog.setResultConverter(button -> button == ButtonType.OK ? result[0] : null);
        return dialog.showAndWait();
    }
}
