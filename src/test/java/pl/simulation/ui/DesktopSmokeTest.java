package pl.simulation.ui;

import java.util.Random;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import pl.simulation.runtime.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="DISPLAY",matches=".+")
class DesktopSmokeTest {
    @BeforeAll static void toolkit()throws Exception {
        CompletableFuture<Void> ready=new CompletableFuture<>();
        Platform.startup(()->{Platform.setImplicitExit(false);ready.complete(null);});ready.get(10,TimeUnit.SECONDS);
    }
    @AfterAll static void exit(){Platform.exit();}
    @Test void boardWindowRendersPausedAnimalsAndCanClose()throws Exception {
        var task=new FutureTask<Void>(()->{
            var board=BoardFactory.create(5,5,3,new Random(2));var engine=new SimulationEngine(board,100);
            var stage=new Stage();var view=new BoardView(board,engine);
            try{stage.setScene(new Scene(view,500,450));stage.show();view.applyCss();view.layout();
                var wolf=board.snapshot().animals().getFirst();engine.togglePauseAt(wolf.position());view.render();
                assertTrue(view.cellAt(wolf.position().x(),wolf.position().y()).getStyle().contains("#f59e0b"));
                assertNotNull(stage.getScene().snapshot(null));
            }finally{view.close();engine.close();stage.close();}return null;
        });Platform.runLater(task);task.get(10,TimeUnit.SECONDS);
    }
}
