package net.cg360.spigot.ooftracker.cause;

import net.cg360.nsapi.commons.data.keyvalue.Key;
import net.cg360.nsapi.commons.id.Namespace;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

public class TraceKeys {

    public static final Namespace NAME = new Namespace("oof");

    public static final Key<Long> TIME = new Key<>(NAME.id("timestamp"));

    public static final Key<Entity> VICTIM = new Key<>(NAME.id("victim_entity"));
    public static final Key<Double> ORIGINAL_DAMAGE = new Key<>(NAME.id("original_damage"));
    public static final Key<Double> FINAL_DAMAGE = new Key<>(NAME.id("final_damage"));
    public static final Key<EntityDamageEvent.DamageCause> VANILLA_CAUSE = new Key<>(NAME.id("vanilla_cause"));

    public static final Key<Entity> ATTACKER_ROOT = new Key<>(NAME.id("attacking_root")); // The root entity of an attack. Could be a pet owner.
    public static final Key<Entity> ATTACKER_ENTITY = new Key<>(NAME.id("attacker")); // The entity that caused an attack

}
