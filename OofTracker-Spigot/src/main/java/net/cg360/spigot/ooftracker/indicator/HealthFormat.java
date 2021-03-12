package net.cg360.spigot.ooftracker.indicator;

public enum HealthFormat {

    // Mono variants just use 1 base colour.

    TEXT,
    TEXT_SPLIT, // Damage-based colour only affects the health (not the max health text)
    TEXT_MONO,
    BAR,
    BAR_MONO,
    SQUARES,
    SQUARES_MONO, // Squares but it's only red.

}
