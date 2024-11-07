# Concurrency model

One virtual thread belongs to each animal. Movement and capture execute under one board monitor;
checking a destination and updating occupancy form one operation. The GUI only receives immutable snapshots.
No animal worker calls JavaFX APIs. A 100 ms JavaFX timeline renders the current state.

Pausing is an animal state guarded by the board monitor. Workers wait on a separate control monitor,
and recheck state after waking. A move also rechecks pause/alive state while holding the board lock.
The engine never sleeps while holding the board lock. Capturing a hare marks it dead in that same operation.

Closing interrupts all worker waits and sleeps; virtual workers cannot keep the process alive.
The view stops its refresh timeline separately. Finishing the hunt leaves the window available for inspection.
