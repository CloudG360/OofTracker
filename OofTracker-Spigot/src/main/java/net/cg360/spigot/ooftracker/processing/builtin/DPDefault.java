package net.cg360.spigot.ooftracker.processing.builtin;

import net.cg360.spigot.ooftracker.cause.DamageTrace;
import net.cg360.spigot.ooftracker.cause.builtin.DTDefault;
import net.cg360.spigot.ooftracker.processing.DamageProcessor;
import org.bukkit.event.entity.EntityDamageEvent;

public class DPDefault extends DamageProcessor {

    @Override
    protected DamageTrace genDamageTrace(EntityDamageEvent event) {
        return new DTDefault(event);
    }

    @Override
    protected boolean checkEvent(EntityDamageEvent event) {
        return true; // This is a fallback, thus, it should cover everything.
    }

    @Override
    public int getPriority() {
        return -1000000; // If anything goes below this, this'll override it.
    }
}
