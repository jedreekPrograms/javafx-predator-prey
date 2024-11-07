# Predator–Prey Simulation — JavaFX

A concurrent Java 21 simulation of one wolf and multiple hares on a rectangular grid.
Hares move away from the wolf; the wolf pursues active hares. Ties are resolved randomly.
This university project explores threads, synchronization, shared state and desktop visualization.

## Run

Install JDK 21 and Maven 3.8+, then run:

```shell
mvn clean verify
mvn javafx:run
```

The settings dialog asks for columns, rows, hare count and base delay in milliseconds.
Dimensions range from 1 to 100. Leave one cell for the wolf. Delay ranges from 1 to 60000 ms.
Cancelling the dialog exits cleanly. A graphical desktop is required.

## Rules and controls

- The wolf is black; hares are green; empty cells are white.
- Each animal considers up to eight neighboring cells, without wrapping at board edges.
- A hare chooses an empty neighbor farthest from the wolf.
- The wolf chooses a neighbor closest to an active hare and captures a hare by entering its cell.
- Each animal waits a random duration between `k` and `2k` ms, excluding the upper endpoint.
- After a capture, the wolf rests for five seconds.
- Click an animal to pause or resume it. Paused animals have an amber border.
- Paused hares remain on the board and are protected from capture, matching the original rules.
- After all hares are caught, the window stays open. Closing it stops the simulation.

## Design

| Package | Role |
| --- | --- |
| [model](src/main/java/pl/simulation/model/) | Positions, animals, atomic movement/capture and immutable snapshots |
| [runtime](src/main/java/pl/simulation/runtime/) | Collision-free setup, one virtual thread per animal and worker lifecycle |
| [ui](src/main/java/pl/simulation/ui/) | Settings, grid rendering and click controls |
| [app](src/main/java/pl/simulation/app/) | JavaFX entry point and shutdown |

Board operations share one monitor, so destination checks and movement cannot race.
Workers never modify JavaFX controls. The JavaFX thread renders snapshots every 100 ms.
Sleeps happen outside the board lock. See [concurrency notes](docs/CONCURRENCY.md).

## Tests

`mvn clean verify` checks initialization, movement, capture, pause state, occupancy invariants and worker shutdown.
The desktop smoke test runs when `DISPLAY` is available; CI uses Xvfb for the complete suite.
JavaFX is resolved by Maven. Preview language features and manually configured IDE libraries are unnecessary.

## History

Reorganized from [Plansza](https://github.com/jedreekPrograms/Plansza).
The staged history was reconstructed from the imported snapshot; assigned October–November 2024 dates
are not recovered original commit timestamps.
