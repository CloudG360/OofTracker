package net.cg360.spigot.ooftracker.indicator;

public enum HealthFormat {

    // _NO_TEXT variants remove the text on either ends of the bar.

    // -- TEXT --

    TEXT,
    TEXT_SPLIT, // Damage-based colour only affects the health (not the max health text)
    TEXT_MONO,


    // -- BAR --

    BAR,      // Health Colour on gray
    BAR_MONO, // Red on gray
    BAR_DUO,  // Green on Red

    BAR_NO_TEXT,
    BAR_MONO_NO_TEXT,
    BAR_DUO_NO_TEXT,


    // -- SQUARES --

    SQUARES,      // Health Colour on gray
    SQUARES_MONO, // Red on gray

    SQUARES_NO_TEXT,
    SQUARES_MONO_NO_TEXT,

}
