package net.cg360.spigot.ooftracker.indicator.bar;

import net.cg360.spigot.ooftracker.ConfigKeys;
import net.cg360.spigot.ooftracker.OofTracker;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.DecimalFormat;
import java.util.HashMap;

public class HealthIndicatorManager implements Listener {

    private static HealthIndicatorManager primaryManager;

    public static final DecimalFormat HEALTH_FORMAT = new DecimalFormat("0.0");
    public static final double THRESHOLD_HEALTHY = 0.85d; // > = green
    public static final double THRESHOLD_OKAY = 0.6d; // > = yellow
    public static final double THRESHOLD_WOUNDED = 0.25d; // > = orange | < = red

    protected HashMap<Integer, LivingEntityHealthBar> healthbars; // OwnerID: Healthbar
    protected HashMap<Integer, Long> lastDamageMillis;


    public HealthIndicatorManager() {
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
     * Sets the manager the result provided from HealthIndicatorManager#get() and
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
                health.updateDisplayForWorld(living.getHealth() - event.getFinalDamage(), maxHealth == null ? 1d : maxHealth.getValue());

                OofTracker.get().getServer().getScheduler().scheduleSyncDelayedTask(OofTracker.get(), () -> {

                    if(checkRemovalTicks(entityID)) {
                        LivingEntityHealthBar hb = this.healthbars.get(entityID); // Shouldn't fail unless someone has messed with it >:(

                        if(hb != null) { // Stops any pesky NPEs if they do somehow happen
                            hb.visible = false; // Set invisible and update.
                            hb.updateDisplayForViewers(0d, 1d); // Removing it. It doesn't matter what value it has.
                        }
                    }

                }, viewTicks + 1); // Ensure the delta will be past the max.
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityHeal(EntityRegainHealthEvent event) {
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
            health.updateDisplayForWorld(living.getHealth() + event.getAmount(), maxHealth == null ? 1d : maxHealth.getValue());

            OofTracker.get().getServer().getScheduler().scheduleSyncDelayedTask(OofTracker.get(), () -> {

                if(checkRemovalTicks(entityID)) {
                    LivingEntityHealthBar hb = this.healthbars.get(entityID); // Shouldn't fail unless someone has messed with it >:(

                    if(hb != null) { // Stops any pesky NPEs if they do somehow happen
                        hb.visible = false; // Set invisible and update.
                        hb.updateDisplayForViewers(0d, 1d); // Removing it. It doesn't matter what value it has.
                    }
                }

            }, viewTicks + 1); // Ensure the delta will be past the max.
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTransform(EntityTransformEvent event) { // Clear original entity health-bars on transform
        int entityID = event.getEntity().getEntityId();

        if(healthbars.containsKey(entityID)) {
            LivingEntityHealthBar hb = healthbars.get(entityID);
            hb.visible = false;
            hb.updateDisplayForViewers(0, 1);

            healthbars.remove(entityID);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDestroy(EntityDeathEvent event) {
        int entityID = event.getEntity().getEntityId();

        // If true, invalidate and remove.
        if(healthbars.containsKey(entityID)) {
            LivingEntityHealthBar hb = healthbars.get(entityID);
            hb.visible = false;
            hb.updateDisplayForViewers(0, 1);

            healthbars.remove(entityID);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDimensionTransfer(PlayerChangedWorldEvent event) {

        for(LivingEntityHealthBar hb: healthbars.values()) {
            AttributeInstance maxHealth = hb.hostEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            hb.updatePlayerDisplay(event.getPlayer(), hb.hostEntity.getHealth(), maxHealth == null ? 1 : maxHealth.getValue());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        int entityID = event.getPlayer().getEntityId();

        // If true, invalidate and remove.
        if(healthbars.containsKey(entityID)) {
            LivingEntityHealthBar hb = healthbars.get(entityID);
            hb.visible = false;
            hb.updateDisplayForViewers(0, 1);

            healthbars.remove(entityID);
        }

        for(LivingEntityHealthBar hb: healthbars.values()) {
            hb.removePlayer(event.getPlayer());
        }
    }




    public static ChatColor getHealthColour(double health, double maxHealth) {
        double checkedMaxHealth = maxHealth > 0 ? maxHealth : 1; // Ensure maxHealth is not 0.
        double fraction = health / checkedMaxHealth;

        if (fraction >= THRESHOLD_HEALTHY) return ChatColor.GREEN;
        if (fraction >= THRESHOLD_OKAY) return ChatColor.YELLOW;
        if(fraction >= THRESHOLD_WOUNDED) return ChatColor.GOLD;

        return ChatColor.RED; // Otherwise it's red cause it's below the threshold
    }

    /** @return the primary instance of the HealthIndicatorManager. */
    public static HealthIndicatorManager get(){
        return primaryManager;
    }

}
