package net.cg360.spigot.ooftracker.processing;

import net.cg360.nsapi.commons.Check;
import net.cg360.spigot.ooftracker.OofTracker;
import net.cg360.spigot.ooftracker.cause.DamageTrace;
import net.cg360.spigot.ooftracker.cause.TraceKeys;
import net.cg360.spigot.ooftracker.list.DamageList;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DamageProcessing implements Listener {

    protected List<UUID> ignoredVanillaEvents; // Used to block events caused by custom damage.
    protected List<DamageProcessor> damageProcessors;


    public DamageProcessing() {
        this.ignoredVanillaEvents = new ArrayList<>();
        this.damageProcessors = new ArrayList<>();
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
            ignoredVanillaEvents.add(entity.getUniqueId());

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


    public void addDamageProcessor(DamageProcessor processor) {
        Check.nullParam(processor, "Damage Processor");

        int priority = processor.getPriority();
        for(int i = 0; i < damageProcessors.size(); i++) {
            DamageProcessor originalProcessor = damageProcessors.get(i);

            if(originalProcessor.getPriority() < priority) {
                damageProcessors.add(i, processor);
                return;
            }
        }
        damageProcessors.add(processor); // Add if not already added.
    }



    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {

        if(!ignoredVanillaEvents.remove(event.getEntity().getUniqueId())) {
            // ^ If an item gets removed, it must've existed.
            // Thus, only run the following if nothing was removed.

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



    private static void pushTrace(Entity entity, DamageTrace t) {
        DamageList ls = OofTracker.getDamageListManager().getDamageList(entity);
        ls.push(t);
    }
}
