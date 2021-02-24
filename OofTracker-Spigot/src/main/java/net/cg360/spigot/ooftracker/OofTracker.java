package net.cg360.spigot.ooftracker;

import net.cg360.spigot.ooftracker.lists.DamageListManager;
import net.cg360.spigot.ooftracker.processors.DamageProcessing;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class OofTracker extends JavaPlugin implements Listener {

    private static OofTracker oofTracker = null;

    private DamageListManager damageListManager;
    private DamageProcessing damageProcessing;

    @Override
    public void onEnable() {

        try {
            oofTracker = this;

            this.damageListManager = new DamageListManager();
            this.damageProcessing = new DamageProcessing();

            this.damageListManager.setAsPrimaryManager();

            this.getServer().getPluginManager().registerEvents(damageProcessing, this);

        } catch (Exception err){
            oofTracker = null;
            err.printStackTrace();
            // Just making sure everything is properly nulled.
        }
    }

    public static OofTracker get() { return oofTracker; }

    public static Logger getLog() { return get().getLogger(); }
    public static DamageProcessing getDamageListener() { return get().damageProcessing; }
    public static DamageListManager getDamageListManager() { return get().damageListManager; }
}
