package pl.simulation.model;

import java.util.Random;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.simulation.runtime.BoardFactory;
import static org.junit.jupiter.api.Assertions.*;

class SimulationBoardTest {
    @Test void initialPositionsAreUniqueEvenOnFullBoard() {
        var b = BoardFactory.create(5, 4, 19, new Random(7));
        assertEquals(20, new HashSet<>(b.snapshot().animals().stream().map(AnimalState::position).toList()).size());
    }
    @Test void duplicateAndOutOfBoundsSpawnsAreRejected() {
        var b = new SimulationBoard(3, 3); b.spawn(Species.WOLF, new Position(0, 0));
        assertThrows(IllegalArgumentException.class, () -> b.spawn(Species.HARE, new Position(0, 0)));
        assertThrows(IllegalArgumentException.class, () -> b.spawn(Species.HARE, new Position(3, 0)));
        assertThrows(IllegalArgumentException.class, () -> b.spawn(Species.WOLF, new Position(2, 2)));
    }
    @ParameterizedTest @ValueSource(ints={-1,0,101})
    void invalidDimensionsAreRejected(int size) { assertThrows(IllegalArgumentException.class, () -> new SimulationBoard(size, 5)); }
    @Test void tooManyHaresCannotHangInitialization() {
        assertThrows(IllegalArgumentException.class, () -> BoardFactory.create(2, 2, 4, new Random(1)));
    }
    @Test void hareMovesToFarthestAvailableNeighbor() {
        var b = new SimulationBoard(4, 4); b.spawn(Species.WOLF, new Position(0, 0));
        int hare = b.spawn(Species.HARE, new Position(1, 1));
        b.stepHare(hare, new Random(1));
        assertEquals(new Position(2, 2), b.snapshot().animals().get(hare).position());
    }
    @Test void wolfCapturesAdjacentHareAndOccupiesItsCell() {
        var b = new SimulationBoard(3, 3); int wolf=b.spawn(Species.WOLF,new Position(0,0));
        int hare=b.spawn(Species.HARE,new Position(1,0));
        assertEquals(StepResult.CAUGHT,b.stepWolf(wolf,new Random(3)));
        assertFalse(b.isAlive(hare));
        assertEquals(new Position(1,0),b.snapshot().animals().get(wolf).position());
        assertEquals(StepResult.FINISHED,b.stepWolf(wolf,new Random(3)));
    }
    @Test void pausedHareIsProtectedAndDoesNotMove() {
        var b=new SimulationBoard(3,3);int wolf=b.spawn(Species.WOLF,new Position(0,0));
        int hare=b.spawn(Species.HARE,new Position(1,0));b.togglePauseAt(new Position(1,0));
        assertEquals(StepResult.WAITING,b.stepHare(hare,new Random(1)));
        assertEquals(StepResult.WAITING,b.stepWolf(wolf,new Random(1)));
        assertTrue(b.isAlive(hare));
    }
    @Test void pausedWolfStaysInPlace() {
        var b=BoardFactory.create(4,4,3,new Random(2));var wolf=b.snapshot().animals().getFirst();
        b.togglePauseAt(wolf.position());assertEquals(StepResult.WAITING,b.stepWolf(wolf.id(),new Random(3)));
        assertEquals(wolf.position(),b.snapshot().animals().getFirst().position());
    }
    @Test void surroundedHareWaitsWithoutOverwritingAnotherAnimal() {
        var b=new SimulationBoard(2,2);b.spawn(Species.WOLF,new Position(0,0));
        int hare=b.spawn(Species.HARE,new Position(1,1));b.spawn(Species.HARE,new Position(0,1));b.spawn(Species.HARE,new Position(1,0));
        assertEquals(StepResult.WAITING,b.stepHare(hare,new Random(3)));
        assertEquals(4,b.snapshot().animals().stream().map(AnimalState::position).distinct().count());
    }
    @Test void snapshotsDoNotExposeMutableState() {
        var b=BoardFactory.create(4,4,2,new Random(2));var before=b.snapshot();
        b.stepWolf(0,new Random(3));assertThrows(UnsupportedOperationException.class,()->before.animals().clear());
        assertEquals(3,before.animals().size());
    }
    @Test void repeatedRandomStepsPreserveBoundsAndExclusiveOccupancy() {
        var b=BoardFactory.create(10,10,30,new Random(8));var random=new Random(11);
        for(int step=0;step<1000;step++) {
            int id=random.nextInt(31);
            if(id==0)b.stepWolf(id,random);else b.stepHare(id,random);
            var active=b.snapshot().animals().stream().filter(AnimalState::alive).toList();
            assertTrue(active.stream().allMatch(a->b.contains(a.position())));
            assertEquals(active.size(),active.stream().map(AnimalState::position).distinct().count());
        }
    }
    @Test void emptyHuntFinishesWithoutExitingTheApplication() {
        var b=BoardFactory.create(1,1,0,new Random(1));assertEquals(StepResult.FINISHED,b.stepWolf(0,new Random(2)));
    }
}
