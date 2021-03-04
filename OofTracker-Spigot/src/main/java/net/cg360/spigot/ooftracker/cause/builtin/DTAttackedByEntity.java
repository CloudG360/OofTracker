package net.cg360.spigot.ooftracker.cause.builtin;

import net.cg360.nsapi.commons.id.Identifier;
import net.cg360.spigot.ooftracker.cause.DamageTrace;
import net.cg360.spigot.ooftracker.cause.TraceKeys;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
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

        if(attacker instanceof Player) {
            this.rawData.set(TraceKeys.ATTACKING_PLAYER, (Player) this.attacker);

        } else {

            if(attacker instanceof Tameable) {
                Tameable tame = (Tameable) attacker;

                if(tame.getOwner() instanceof Player) {
                    this.rawData.set(TraceKeys.ATTACKING_PLAYER, (Player) tame.getOwner());
                }
            }
        }
    }

    @Override
    public Identifier getTraceType() {
        return TraceKeys.NAME.id("attacked_by_entity");
    }
}
