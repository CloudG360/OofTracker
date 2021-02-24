package net.cg360.spigot.ooftracker;

import net.cg360.nsapi.commons.Check;
import net.cg360.spigot.ooftracker.causes.DamageTrace;
import net.cg360.spigot.ooftracker.causes.TraceKeys;
import net.cg360.spigot.ooftracker.lists.DamageList;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DamageProcessing implements Listener {

    protected List<UUID> ignoredEvents; // Used to block events caused by custom damage.

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {

        if(!ignoredEvents.remove(event.getEntity().getUniqueId())) { // If an item gets removed (thus, it existed)

            //TODO: Process it!

        }
    }

    /**
     * Applies custom damage to an entity, thus skipping the
     * DamageProcessing system and appending the DamageTrace
     * on directly
     * @param trace a damage trace to be applied to the DamageList stack
     * @return true if the damage has been applied.
     */
    public boolean applyCustomDamage(DamageTrace trace) {
        Check.nullParam(trace, "Damage Trace");

        if(trace.getVictim() instanceof Damageable) {
            Damageable entity = (Damageable) trace.getVictim();
            Entity attacker = trace.getData().get(TraceKeys.ATTACKER_ENTITY);

            // Add the entity to the list of ignored events
            // Then damage without the event triggering :)
            ignoredEvents.add(entity.getUniqueId());

            if(attacker == null) {
                entity.damage(trace.getFinalDamageDealt());

            } else { // Had an attacker, trigger damage with attacker.
                entity.damage(trace.getFinalDamageDealt(), attacker);
            }

            DamageList ls = OofTracker.getDamageListManager().getDamageList(entity);
            ls.push(trace);
            return true;
        }

        return false;
    }

}
