package net.cg360.spigot.ooftracker.causes.builtin;

import net.cg360.nsapi.commons.id.Identifier;
import net.cg360.spigot.ooftracker.causes.DamageTrace;
import net.cg360.spigot.ooftracker.causes.TraceKeys;
import org.bukkit.entity.Damageable;
import org.bukkit.event.entity.EntityDamageEvent;

public class DTDefault extends DamageTrace {

    public DTDefault(EntityDamageEvent eventIn) {
        super(eventIn);
    }

    public DTDefault(Damageable victim, double damageDealt) {
        super(victim, damageDealt);
    }

    @Override
    public Identifier getTraceType() {
        return TraceKeys.NAME.id("default");
    }
}
