package net.cg360.spigot.ooftracker;

import net.cg360.nsapi.commons.data.keyvalue.Key;

public final class ConfigKeys {

    // -- Major settings --

    // Should the plugin restrict it's functions to only events + tracking damage lists
    // Overrides a lot of the plugins functions.
    public static final Key<Boolean> DAMAGE_LISTS_ONLY = new Key<>("damage-lists-only"); // default: false


    // -- Damage Lists --

    // Should entities which aren't players be tracked?
    public static final Key<Boolean> LIST_NON_PLAYER_ENABLED = new Key<>("non-player-lists-enabled"); // default: true

    // Should a given DamageList be cleared once it's entity dies?
    public static final Key<Boolean> LIST_CLEAR_ON_DEATH = new Key<>("clear-list-on-death"); // default: true

}
