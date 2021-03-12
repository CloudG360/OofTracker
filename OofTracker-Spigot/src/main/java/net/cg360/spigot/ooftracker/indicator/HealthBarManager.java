package net.cg360.spigot.ooftracker.indicator;

import net.cg360.spigot.ooftracker.ConfigKeys;
import net.cg360.spigot.ooftracker.OofTracker;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;

public class HealthBarManager implements Listener {

    private static HealthBarManager primaryManager;

    protected HashMap<Integer, LivingEntityHealthBar> healthbars; // OwnerID: Healthbar
    protected HashMap<Integer, Long> lastDamageMillis;

    public HealthBarManager() {
        this.healthbars = new HashMap<>();
        this.lastDamageMillis = new HashMap<>();

        OofTracker.get().getServer().getScheduler().scheduleSyncRepeatingTask(OofTracker.get(), () -> {

            if(OofTracker.isRunning()) {
                for (LivingEntityHealthBar hb : healthbars.values()) {
                    hb.updatePositionAndVelocity();
                }
            }

        }, 1, 1);
    }

    /**
     * Sets the manager the result provided from HealthBarManager#get() and
     * finalizes the instance to an extent.
     *
     * Cannot be changed once initially called.
     */
    public void setAsPrimaryManager(){
        if(primaryManager == null) primaryManager = this;
    }



    protected boolean checkRemovalTicks(int entityID) {
        long currentMilli = System.currentTimeMillis();  // Get current time now so it's consistent if needed.

        // would be true if the entity died or something.
        if(lastDamageMillis.containsKey(entityID)) {
            long lastMilli = lastDamageMillis.get(entityID);
            long delta = currentMilli - lastMilli; // Difference between then and now.
            long maxConfigDelta = OofTracker.getConfiguration().get(ConfigKeys.HEALTH_BAR_VIEW_TICKS) * 50; // Tick is 0.050 seconds = 50 millis

            // TRUE:    Delta is out of the bounds, remove the healthbar.
            // FALSE:   Delta is in meaning a more recent damage occurred. Keep healthbar.
            return delta >= maxConfigDelta;

        } else {
            return true;
        }
    }



    //TODO: Use the custom damage event triggered by DamageProcessing#onEntityDamage() when added.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {

        if(event.getEntity() instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) event.getEntity();

            // Only create/update health bars if enabled (distance > 0)
            if(OofTracker.getConfiguration().getOrElse(ConfigKeys.HEALTH_BAR_VIEW_DISTANCE, 20d) > 0) {
                long currentMilli = System.currentTimeMillis(); // Get current time now so it's consistent if needed.
                int entityID = event.getEntity().getEntityId();
                int viewTicks = OofTracker.getConfiguration().get(ConfigKeys.HEALTH_BAR_VIEW_TICKS);
                this.lastDamageMillis.put(entityID, currentMilli);

                if (!healthbars.containsKey(entityID)) {
                    healthbars.put(entityID, new LivingEntityHealthBar((LivingEntity) event.getEntity()) );
                }

                AttributeInstance maxHealth = living.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                LivingEntityHealthBar health = this.healthbars.get(entityID); // If this fails, hOW??
                health.visible = true; // Set visible and update.
                health.updateDisplay(living.getHealth() - event.getFinalDamage(), maxHealth == null ? 1d : maxHealth.getValue());

                OofTracker.get().getServer().getScheduler().scheduleSyncDelayedTask(OofTracker.get(), () -> {

                    if(checkRemovalTicks(entityID)) {
                        LivingEntityHealthBar hb = this.healthbars.get(entityID); // Shouldn't fail unless someone has messed with it >:(

                        if(hb != null) { // Stops any pesky NPEs if they do somehow happen
                            hb.visible = false; // Set invisible and update.
                            hb.updateDisplay(0, 10); // Removing it. It doesn't matter what value it has.
                        }
                    }

                }, viewTicks + 1); // Ensure the delta will be past the max.
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDestroy(EntityDeathEvent event) {
        int entityID = event.getEntity().getEntityId();

        // If true, invalidate and remove.
        if(healthbars.containsKey(entityID)) {
            LivingEntityHealthBar hb = healthbars.get(entityID);
            hb.visible = false;
            hb.updateDisplay(0, 1);

            healthbars.remove(entityID);
        }
    }

    /** @return the primary instance of the HealthBarManager. */
    public static HealthBarManager get(){
        return primaryManager;
    }

}
