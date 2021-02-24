package net.cg360.spigot.ooftracker.processors;

public enum Priority {

    LOWEST(-5000), // Run at the end
    LOWER(-3000),
    LOW(-1000),
    NORMAL(0),
    HIGH(1000),
    HIGHER(3000),
    HIGHEST(5000); // Run at the beginning.

    private int value;

    Priority(int value) { this.value = value; }
    public int getValue() { return value; }
}
