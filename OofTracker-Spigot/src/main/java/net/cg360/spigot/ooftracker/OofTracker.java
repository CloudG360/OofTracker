package net.cg360.spigot.ooftracker;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class OofTracker extends JavaPlugin implements Listener {

    private static OofTracker oofTracker = null;

    private DamageProcessor damageProcessor;

    @Override
    public void onEnable() {

        try {
            oofTracker = this;

            this.damageProcessor = new DamageProcessor();
            this.getServer().getPluginManager().registerEvents(this, this);

        } catch (Exception err){
            oofTracker = null;
            err.printStackTrace();
            // Just making sure everything is properly nulled.
        }
    }

    public static OofTracker get() { return oofTracker; }
    public static DamageProcessor getDamageProcessor() { return get().damageProcessor; }
}
