package net.cg360.spigot.ooftracker.processors;

import net.cg360.nsapi.commons.Check;
import net.cg360.spigot.ooftracker.OofTracker;
import net.cg360.spigot.ooftracker.causes.DamageTrace;
import net.cg360.spigot.ooftracker.causes.TraceKeys;
import net.cg360.spigot.ooftracker.lists.DamageList;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DamageProcessing implements Listener {

    protected List<UUID> ignoredEvents; // Used to block events caused by custom damage.
    protected List<DamageProcessor> damageProcessors;

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {

        if(!ignoredEvents.remove(event.getEntity().getUniqueId())) { // If an item gets removed (thus, it existed)

            for (DamageProcessor processor : damageProcessors) {
                Optional<DamageTrace> trace = processor.getDamageTrace(event);

                if(trace.isPresent()) {
                    pushTrace(event.getEntity(), trace.get());
                    return;
                }
            }

            // Panic! The default DamageTrace generator should've been last.
            throw new IllegalStateException("Default DamageProcessor didn't kick in. Why's it broke? :<");
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

            // The following Entity#damage() calls are only to support
            // Spigot damage events + update player health.

            // Add the entity to the list of ignored events
            // Then damage without the event triggering :)
            ignoredEvents.add(entity.getUniqueId());

            if(attacker == null) {
                entity.damage(trace.getFinalDamageDealt());

            } else { // Had an attacker, trigger damage with attacker.
                entity.damage(trace.getFinalDamageDealt(), attacker);
            }

            pushTrace(entity, trace);
            return true;
        }

        return false;
    }

    private static void pushTrace(Entity entity, DamageTrace t) {
        DamageList ls = OofTracker.getDamageListManager().getDamageList(entity);
        ls.push(t);
    }
}
