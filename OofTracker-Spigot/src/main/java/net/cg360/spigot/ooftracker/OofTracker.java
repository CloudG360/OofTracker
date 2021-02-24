package net.cg360.spigot.ooftracker;

import net.cg360.spigot.ooftracker.lists.DamageListManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class OofTracker extends JavaPlugin implements Listener {

    private static OofTracker oofTracker = null;

    private DamageListManager damageListManager;
    private DamageListener damageListener;

    @Override
    public void onEnable() {

        try {
            oofTracker = this;

            this.damageListManager = new DamageListManager();
            this.damageListener = new DamageListener();

            this.damageListManager.setAsPrimaryManager();

            this.getServer().getPluginManager().registerEvents(damageListener, this);

        } catch (Exception err){
            oofTracker = null;
            err.printStackTrace();
            // Just making sure everything is properly nulled.
        }
    }

    public static OofTracker get() { return oofTracker; }

    public static Logger getLog() { return get().getLogger(); }
    public static DamageListener getDamageListener() { return get().damageListener; }
    public static DamageListManager getDamageListManager() { return get().damageListManager; }
}
