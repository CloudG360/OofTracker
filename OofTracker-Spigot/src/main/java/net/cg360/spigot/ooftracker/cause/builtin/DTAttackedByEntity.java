package net.cg360.spigot.ooftracker.cause.builtin;

import net.cg360.nsapi.commons.id.Identifier;
import net.cg360.spigot.ooftracker.cause.DamageTrace;
import net.cg360.spigot.ooftracker.cause.TraceKeys;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DTAttackedByEntity extends DamageTrace {

    public DTAttackedByEntity(EntityDamageEvent eventIn) {
        super(eventIn);
        if(!(eventIn instanceof EntityDamageByEntityEvent)) throw new IllegalArgumentException("Event must be of type EntityDamageByEntityEvent");

        EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) eventIn;
        this.rawData.set(TraceKeys.ATTACKER_ENTITY, ev.getDamager());
    }

    public DTAttackedByEntity(Damageable victim, Entity attacker, double damageDealt) {
        super(victim, damageDealt);
        this.rawData.set(TraceKeys.ATTACKER_ENTITY, attacker);
        this.rawData.set(TraceKeys.ATTACKER_ROOT, attacker); // Set by default

        if(attacker instanceof Tameable) {
            Tameable tame = (Tameable) attacker;

            if(tame.getOwner() instanceof Entity) {
                this.rawData.set(TraceKeys.ATTACKER_ROOT, (Entity) tame.getOwner());
            }
        }

        if(attacker instanceof Projectile) {
            Projectile projectile = (Projectile) attacker;

            if(projectile.getShooter() instanceof Entity) {
                this.rawData.set(TraceKeys.ATTACKER_ROOT, (Entity) projectile.getShooter());
            }
        }

    }

    /** @return the attacker entity. */
    public Entity getAttacker() {
        return this.rawData.get(TraceKeys.ATTACKER_ENTITY);
    }

    /**
     * May be the same as #getAttacker()
     * @return the root attacking entity (pet owner, projectile firer, etc)
     */
    public Entity getRootAttacker() {
        return this.rawData.get(TraceKeys.ATTACKER_ROOT);
    }

    @Override
    public Identifier getTraceType() {
        return TraceKeys.NAME.id("attacked_by_entity");
    }
}
