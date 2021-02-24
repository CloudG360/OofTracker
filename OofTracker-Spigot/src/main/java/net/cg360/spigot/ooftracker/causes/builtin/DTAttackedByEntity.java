package net.cg360.spigot.ooftracker.causes.builtin;

import net.cg360.nsapi.commons.id.Identifier;
import net.cg360.spigot.ooftracker.causes.DamageTrace;
import net.cg360.spigot.ooftracker.causes.TraceKeys;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DTAttackedByEntity extends DamageTrace {

    protected Entity attacker;


    public DTAttackedByEntity(EntityDamageEvent eventIn) {
        super(eventIn);
        if(!(eventIn instanceof EntityDamageByEntityEvent)) throw new IllegalArgumentException("Event must be of type EntityDamageByEntityEvent");

        EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) eventIn;
        this.attacker = ev.getDamager();
        this.rawData.set(TraceKeys.ATTACKER_ENTITY, this.attacker);
    }

    public DTAttackedByEntity(Damageable victim, Entity attacker, double damageDealt) {
        super(victim, damageDealt);
        this.attacker = attacker;
        this.rawData.set(TraceKeys.ATTACKER_ENTITY, this.attacker);
    }

    @Override
    public Identifier getTraceType() {
        return TraceKeys.NAME.id("attacked_by_entity");
    }
}
