package net.cg360.spigot.ooftracker.particle;

import net.cg360.spigot.ooftracker.indicator.bar.LivingEntityHealthBar;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class TextParticleManager implements Listener {

    private static TextParticleManager primaryManager;

    protected HashMap<Integer, TextParticle> activeParticles;

    public TextParticleManager() {
        this.activeParticles = new HashMap<>();
    }

    /**
     * Sets the manager the result provided from TextParticleManager#get() and
     * finalizes the instance to an extent.
     *
     * Cannot be changed once initially called.
     */
    public void setAsPrimaryManager(){
        if(primaryManager == null) primaryManager = this;
    }

    protected void addParticle(TextParticle particle) {
        this.activeParticles.put(particle.getID(), particle);
    }

    protected void removeParticle(TextParticle particle) {
        this.activeParticles.remove(particle.getID(), particle);
    }



    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDimensionTransfer(PlayerChangedWorldEvent event) {

        for(TextParticle t: activeParticles.values()) {
            t.despawnFromPlayer(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {

        for(TextParticle t: activeParticles.values()) {
            t.despawnFromPlayer(event.getPlayer());
        }
    }



    public Optional<TextParticle> getParticle(Integer id) {
        return Optional.ofNullable(activeParticles.get(id));
    }

    public ArrayList<TextParticle> getActiveParticles() {
        return new ArrayList<>(activeParticles.values());
    }



    /** @return the primary instance of the HealthBarManager. */
    public static TextParticleManager get(){
        return primaryManager;
    }
}
