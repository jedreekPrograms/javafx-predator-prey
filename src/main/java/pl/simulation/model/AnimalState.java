package pl.simulation.model;

public record AnimalState(int id, Species species, Position position, boolean alive, boolean paused) { }
