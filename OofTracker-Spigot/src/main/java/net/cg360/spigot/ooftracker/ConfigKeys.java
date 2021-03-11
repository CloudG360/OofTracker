package net.cg360.spigot.ooftracker;

import net.cg360.nsapi.commons.data.keyvalue.Key;
import net.cg360.spigot.ooftracker.processing.DeathBroadcastScope;

public final class ConfigKeys {

    // -- Major settings --

    // Should the plugin restrict it's functions to only events + tracking damage lists
    // Overrides a lot of the plugins functions.
    public static final Key<Boolean> DAMAGE_LISTS_ONLY = new Key<>("damage-lists-only"); // default: false



    // -- Damage Lists --

    // Should entities which aren't players be tracked? Does not prevent *direct* custom damage traces being pushed
    // but does prevent *indirect* custom damage traces.
    // Only stops OofTracker events for non-players + registering any visible damage calls.
    public static final Key<Boolean> LIST_NON_PLAYER_ENABLED = new Key<>("non-player-lists-enabled"); // default: true

    // Should a given DamageStack be cleared once it's entity dies?
    public static final Key<Boolean> LIST_CLEAR_ON_DEATH = new Key<>("clear-list-on-death"); // default: true



    // -- Deaths --

    // Are custom death messages enabled?
    public static final Key<Boolean> DEATH_MESSAGE_OVERRIDE = new Key<>("death-message-override"); // default: true

    // Are the bonus tags at the end of assisted deaths enabled?
    public static final Key<Boolean> ASSIST_TAGS_ENABLED = new Key<>("assist-tags-enabled"); // default: true

    // Does the final killer show up in the assist tag? Depends on: ASSIST_TAGS_ENABLED
    public static final Key<Boolean> KILLER_IN_ASSIST_TAG = new Key<>("killer-in-assist"); // default: true

    // Do assists + killers broadcast an audible ping on kill?
    public static final Key<Boolean> PING_ON_KILL = new Key<>("ping-on-kill"); // default: true

    // Do assists + killers broadcast an audible ping on kill?
    public static final Key<DeathBroadcastScope> DEATH_BROADCAST_SCOPE = new Key<>("death-broadcast-scope"); // default: SERVER



    // -- Indicators --

    // Are custom death messages enabled?
    public static final Key<Double> HEALTH_BAR_VIEW_DISTANCE = new Key<>("health-view-distance"); // default: 20

}
