package net.cg360.spigot.ooftracker;

import net.cg360.nsapi.commons.data.Settings;
import net.cg360.spigot.ooftracker.indicator.bar.HealthBarManager;
import net.cg360.spigot.ooftracker.indicator.bar.HealthFormat;
import net.cg360.spigot.ooftracker.list.DamageStackManager;
import net.cg360.spigot.ooftracker.particle.TextParticleManager;
import net.cg360.spigot.ooftracker.processing.DamageProcessing;
import net.cg360.spigot.ooftracker.processing.DeathBroadcastScope;
import net.cg360.spigot.ooftracker.processing.builtin.DPDAttackedByEntity;
import net.cg360.spigot.ooftracker.processing.builtin.DPDefault;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public final class OofTracker extends JavaPlugin implements Listener {

    private static OofTracker oofTracker = null;
    private static boolean isRunning = false;

    private DamageStackManager damageStackManager;
    private HealthBarManager healthBarManager;
    private TextParticleManager textParticleManager;
    private DamageProcessing damageProcessing;

    private YamlConfiguration configurationFile;
    private Settings configuration;

    @Override
    public void onEnable() {

        try {
            oofTracker = this;
            isRunning = true;

            loadConfiguration();

            // -- Set Managers --
            this.damageStackManager = new DamageStackManager();
            this.damageProcessing = new DamageProcessing();

            this.damageStackManager.setAsPrimaryManager();

            if(!Util.check(ConfigKeys.DAMAGE_LISTS_ONLY, false)) {
                this.healthBarManager = new HealthBarManager();
                this.textParticleManager = new TextParticleManager();

                this.healthBarManager.setAsPrimaryManager();
                this.textParticleManager.setAsPrimaryManager();
            }


            // -- Register DamageProcessors --
            this.damageProcessing.addDamageProcessor(new DPDefault());
            this.damageProcessing.addDamageProcessor(new DPDAttackedByEntity());

            // -- Register listeners --
            this.getServer().getPluginManager().registerEvents(damageProcessing, this);

            if(!Util.check(ConfigKeys.DAMAGE_LISTS_ONLY, false)){
                this.getServer().getPluginManager().registerEvents(healthBarManager, this);
                this.getServer().getPluginManager().registerEvents(textParticleManager, this);
            }


            // -- Register Commands --
            //this.getCommand("debugtest").setExecutor(new CommandDebug());

        } catch (Exception err){
            oofTracker = null;
            err.printStackTrace();
            // Just making sure everything is properly nulled.
        }
    }


    private boolean loadConfiguration() {
        this.configuration = new Settings();
        File cfgFile = new File(getDataFolder(), "config.yml");

        if(!cfgFile.exists()){
            cfgFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        YamlConfiguration config = new YamlConfiguration();

        try {
            config.load(cfgFile);
            this.configurationFile = config;

            // Set config to file's settings
            this.configuration
                    .set(ConfigKeys.DAMAGE_LISTS_ONLY, config.getBoolean(ConfigKeys.DAMAGE_LISTS_ONLY.get(), false))

                    .set(ConfigKeys.LIST_NON_PLAYER_ENABLED, config.getBoolean(ConfigKeys.LIST_NON_PLAYER_ENABLED.get(), true))
                    .set(ConfigKeys.LIST_CLEAR_ON_DEATH, config.getBoolean(ConfigKeys.LIST_CLEAR_ON_DEATH.get(), true))

                    .set(ConfigKeys.DEATH_MESSAGE_OVERRIDE, config.getBoolean(ConfigKeys.DEATH_MESSAGE_OVERRIDE.get(), true))
                    .set(ConfigKeys.ASSIST_TAGS_ENABLED, config.getBoolean(ConfigKeys.ASSIST_TAGS_ENABLED.get(), true))
                    .set(ConfigKeys.KILLER_IN_ASSIST_TAG, config.getBoolean(ConfigKeys.KILLER_IN_ASSIST_TAG.get(), true))
                    .set(ConfigKeys.PING_ON_KILL, config.getBoolean(ConfigKeys.PING_ON_KILL.get(), true))
                    .set(ConfigKeys.DEATH_BROADCAST_SCOPE, Util.stringToEnum(DeathBroadcastScope.class, config.getString(ConfigKeys.DEATH_BROADCAST_SCOPE.get()), DeathBroadcastScope.SERVER))

                    .set(ConfigKeys.HEALTH_BAR_VIEW_DISTANCE, config.getDouble(ConfigKeys.HEALTH_BAR_VIEW_DISTANCE.get(), 20d))
                    .set(ConfigKeys.HEALTH_BAR_VIEW_TICKS, config.getInt(ConfigKeys.HEALTH_BAR_VIEW_TICKS.get(), 400))
                    .set(ConfigKeys.HEALTH_BAR_OFFSET, config.getDouble(ConfigKeys.HEALTH_BAR_OFFSET.get(), 0.3d))
                    .set(ConfigKeys.HEALTH_BAR_VIEW_TYPE, Util.stringToEnum(HealthFormat.class, config.getString(ConfigKeys.HEALTH_BAR_VIEW_TYPE.get()), HealthFormat.SQUARES))

            ;

            return true;

        } catch (IOException | InvalidConfigurationException e){
            e.printStackTrace();
            this.configurationFile = null;
            return false;
        }
    }


    @Override
    public void onDisable() {
        isRunning = false;
    }

    public static OofTracker get() { return oofTracker; }
    public static Logger getLog() { return get().getLogger(); }
    public static boolean isRunning() { return isRunning; }

    public static DamageStackManager getDamageStackManager() { return get().damageStackManager; }
    public static HealthBarManager getHealthBarManager() { return get().healthBarManager; }
    public static DamageProcessing getDamageProcessingManager() { return get().damageProcessing; }

    public static Settings getConfiguration() { return get().configuration; }
    public static YamlConfiguration getConfigurationFile() { return get().configurationFile; }
}
