package net.cg360.spigot.ooftracker.processing.builtin;

import net.cg360.spigot.ooftracker.cause.DamageTrace;
import net.cg360.spigot.ooftracker.cause.builtin.DTAttackedByEntity;
import net.cg360.spigot.ooftracker.processing.DamageProcessor;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DPDAttackedByEntity extends DamageProcessor {

    @Override
    protected DamageTrace genDamageTrace(EntityDamageEvent event) {
        return new DTAttackedByEntity(event);
    }

    @Override
    protected boolean checkEvent(EntityDamageEvent event) {
        return event instanceof EntityDamageByEntityEvent;
    }

    @Override
    public int getPriority() {
        return -500000; // Generally, custom processors should go before this.
    }
}
