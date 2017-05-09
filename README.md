Aggalife
=====

A Conway's Game of Life, used as an Akka playground. There's one `WorldActor` that manage every `CellActor`, when started, each cells ask its neighbors their status, so it knows if it will live or die on the next iteration.

Currently the output is printed into the terminal, but there're several thing I'll like to try:

- [ ] Use `akka-http` to create a WS with the cells' status
- [ ] Create an `Elm` app to draw the result
- [ ] Add possibility to control the game from the app (init cells' position, start, stop, pause etc), using REST calls

