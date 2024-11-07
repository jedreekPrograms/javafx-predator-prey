package pl.simulation.runtime;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import pl.simulation.model.*;
import pl.simulation.ui.SimulationSettings;
import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineTest {
    @Test void closeInterruptsWorkersSleepingForLongDelays() throws Exception {
        var b=BoardFactory.create(5,5,10,new Random(2));var e=new SimulationEngine(b,60000);
        e.start();assertTrue(e.hasLiveWorkers());e.close();awaitStopped(e);
    }
    @Test void closeWakesPausedWorkers() throws Exception {
        var b=BoardFactory.create(5,5,4,new Random(2));
        b.snapshot().animals().forEach(a->b.togglePauseAt(a.position()));
        var e=new SimulationEngine(b,1);e.start();Thread.sleep(20);e.close();awaitStopped(e);
    }
    @Test void emptyHuntWorkersFinishNaturally() throws Exception {
        var e=new SimulationEngine(BoardFactory.create(1,1,0,new Random(2)),1);e.start();awaitStopped(e);e.close();
    }
    @Test void restartAndStartingAfterCloseAreRejected() {
        var e=new SimulationEngine(BoardFactory.create(2,2,1,new Random(2)),10);
        e.start();try{assertThrows(IllegalStateException.class,e::start);}finally{e.close();}
        var closed=new SimulationEngine(BoardFactory.create(2,2,1,new Random(3)),10);closed.close();
        assertThrows(IllegalStateException.class,closed::start);
    }
    @Test void invalidSettingsAreRejected() {
        assertThrows(IllegalArgumentException.class,()->new SimulationSettings(0,2,1,500));
        assertThrows(IllegalArgumentException.class,()->new SimulationSettings(2,2,4,500));
        assertThrows(IllegalArgumentException.class,()->new SimulationSettings(2,2,1,0));
    }
    @Test void concurrentWorkersKeepEveryLivingAnimalInASeparateCell() throws Exception {
        var b=BoardFactory.create(12,12,50,new Random(2));var e=new SimulationEngine(b,1,0);e.start();
        try{for(int i=0;i<100;i++){var alive=b.snapshot().animals().stream().filter(AnimalState::alive).toList();
            assertEquals(alive.size(),alive.stream().map(AnimalState::position).distinct().count());Thread.sleep(1);}}
        finally{e.close();awaitStopped(e);}
    }
    private static void awaitStopped(SimulationEngine engine)throws Exception {
        long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(3);
        while(engine.hasLiveWorkers()&&System.nanoTime()<end)Thread.sleep(2);
        assertFalse(engine.hasLiveWorkers(),"Animal workers did not stop");
    }
}
