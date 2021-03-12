package net.cg360.spigot.ooftracker.indicator;

import net.cg360.spigot.ooftracker.ConfigKeys;
import net.cg360.spigot.ooftracker.OofTracker;
import net.cg360.spigot.ooftracker.nms.NMS;
import net.cg360.spigot.ooftracker.nms.RawTextBuilder;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class LivingEntityHealthBar {

    public static final DecimalFormat HEALTH_FORMAT = new DecimalFormat("0.0");
    public static final double THRESHOLD_HEALTHY = 0.85d; // > = green
    public static final double THRESHOLD_OKAY = 0.6d; // > = yellow
    public static final double THRESHOLD_WOUNDED = 0.25d; // > = orange | < = red

    protected Location lastLocation;
    protected LivingEntity hostEntity;
    protected ArrayList<Player> visibleToPlayers;

    protected int fakeEntityID;
    protected UUID fakeEntityUUID;

    protected boolean visible;


    // Should only be instantiated from the HealthBarManager
    protected LivingEntityHealthBar(LivingEntity host) {
        this.lastLocation = new Location(host.getWorld(), 0, 0, 0, 0, 0);
        this.hostEntity = host;
        this.visibleToPlayers = new ArrayList<>();

        this.fakeEntityID = NMS.getNewEntityID(); // Steal right from under NMS's nose :)
        this.fakeEntityUUID = UUID.randomUUID();

        this.visible = false;
    }

    /**
     * Unrecommended as it runs for every player on the server rather
     * than the host entity's world. Should only be used when
     * #updateDisplay() doesn't work.
     * @param health the health of the entity to display.
     * @param maxHealth the max health of the entity to display.
     */
    public void updateDisplayForEveryone(double health, double maxHealth) {
        for (Player p : OofTracker.get().getServer().getOnlinePlayers()) {
            updatePlayerDisplay(p, health, maxHealth);
        }
    }

    public void updateDisplayForViewers(double health, double maxHealth) {
        for (Player p : new ArrayList<>(visibleToPlayers)) {
            updatePlayerDisplay(p, health, maxHealth);
        }
    }

    public void updateDisplayForWorld(double health, double maxHealth) {
        for (Player p : hostEntity.getWorld().getPlayers()) {
            updatePlayerDisplay(p, health, maxHealth);
        }
    }

    public void updatePlayerDisplay(Player p, double health, double maxHealth) {
        double maxDistance = OofTracker.getConfiguration().getOrElse(ConfigKeys.HEALTH_BAR_VIEW_DISTANCE, 20d);

        if(p != hostEntity) { // Ensure the player, if they are the entity, are not visible.
            boolean outOfRange;

            if (p.getWorld().equals(hostEntity.getWorld())) {
                double distance = p.getLocation().distance(hostEntity.getLocation());
                outOfRange = (distance > maxDistance);
            } else {
                outOfRange = true;
            }

            CraftPlayer cPlayer = (CraftPlayer) p;

            if(visibleToPlayers.contains(p)) {

                if ((!visible) || outOfRange) { // Isn't visible or the distance is now to large, remove
                    PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(fakeEntityID);
                    cPlayer.getHandle().playerConnection.sendPacket(destroyPacket);
                    visibleToPlayers.remove(p);

                } else { // Still visible, may include a health update so send new meta.
                    DataWatcher dataWatcher = new DataWatcher(null);


                    dataWatcher.register(new DataWatcherObject<>(0, DataWatcherRegistry.a), (byte) 0x20); // Is invisible
                    dataWatcher.register(new DataWatcherObject<>(2, DataWatcherRegistry.f),  Optional.ofNullable( IChatBaseComponent.ChatSerializer.b( getHealthText(health, maxHealth) ) ) ); // Custom name
                    dataWatcher.register(new DataWatcherObject<>(3, DataWatcherRegistry.i), true); // Custom name visible
                    dataWatcher.register(new DataWatcherObject<>(14, DataWatcherRegistry.a), (byte) 0x10); // Set Marker

                    PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(fakeEntityID, dataWatcher, true);

                    cPlayer.getHandle().playerConnection.sendPacket(metaPacket);
                }

            } else {

                if (visible && (!outOfRange)) { // Is visible + now within distance, add
                    visibleToPlayers.add(p);
                    PacketPlayOutSpawnEntityLiving addPacket = new PacketPlayOutSpawnEntityLiving(); // An armour stand is apparently living??

                    try {

                        // -- ADD ENTITY --

                        NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "a", fakeEntityID); // Entity ID
                        NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "b", fakeEntityUUID); // Entity UUID
                        NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "c", NMS.NETID_ARMOUR_STAND); // Entity Type

                        // Copy host entity's location and follow
                        NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "d", hostEntity.getLocation().getX()); // Location X
                        NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "e", getDisplayYCoordinate()); // Location Y (+ offset)
                        NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "f", hostEntity.getLocation().getZ()); // Location Z

                        // Copy host entity's motion and follow.
                        int[] velocity = calculateNetworkVelocity(hostEntity.getVelocity());
                        NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "g", velocity[0]); // Velocity X
                        NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "h", velocity[1]); // Location Y
                        NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "i", velocity[2]); // Location Z

                        // Technically rotation doesn't matter but let's copy anyway.
                        int yaw = (int) (hostEntity.getLocation().getYaw() * 256.0F / 360.0F);
                        int pitch = (int) (hostEntity.getLocation().getPitch() * 256.0F / 360.0F);
                        int head = (int) (((CraftLivingEntity)hostEntity).getHandle().getHeadRotation() * 256.0F / 360.0F); // Head rotation apparently.
                        NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "j", (byte) yaw); // Yaw
                        NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "k", (byte) pitch); // Pitch
                        NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "l", (byte) head); // Head rotate.



                        // -- UPDATE ENTITY META --

                        DataWatcher dataWatcher = new DataWatcher(null);


                        dataWatcher.register(new DataWatcherObject<>(0, DataWatcherRegistry.a), (byte) 0x20); // Is invisible
                        dataWatcher.register(new DataWatcherObject<>(2, DataWatcherRegistry.f), Optional.ofNullable( IChatBaseComponent.ChatSerializer.b( getHealthText(health, maxHealth) ) ) ); // Custom name
                        dataWatcher.register(new DataWatcherObject<>(3, DataWatcherRegistry.i), true); // Custom name visible
                        dataWatcher.register(new DataWatcherObject<>(14, DataWatcherRegistry.a), (byte) 0x10); // Set Marker

                        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(fakeEntityID, dataWatcher, true);

                        cPlayer.getHandle().playerConnection.sendPacket(addPacket);
                        cPlayer.getHandle().playerConnection.sendPacket(metaPacket);

                    } catch (NoSuchFieldException err) {
                        OofTracker.getLog().severe("Error building packet. - No field! Is this the wrong version?");
                        err.printStackTrace();
                        visibleToPlayers.remove(p); // Remove player as it wasn't displayed.
                        return;

                    } catch (IllegalAccessException err) {
                        OofTracker.getLog().severe("Error building packet - Can't access field! Is something misconfigured?");
                        err.printStackTrace();
                        visibleToPlayers.remove(p); // Remove player as it wasn't displayed.
                        return;

                    }
                }
            }
        }
    }

    // If you find an entity movement event, PLEASE just use that. Running this every tick is stupid.
    public void updatePositionAndVelocity() {
        Optional<Location> oldLoc = checkAndUpdateLastLocation();


        if(oldLoc.isPresent()) { // A movement has occured
            Location oldLocation = oldLoc.get();

            if(!Objects.equals(oldLocation.getWorld(), lastLocation.getWorld())) { // World has changed. Drop all old viewers.
                this.visible = false;
                updateDisplayForEveryone(0d, 1d); // Ensure no one can see the healthbar

                AttributeInstance maxHealth = hostEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);

                this.visible = true;
                updateDisplayForWorld(hostEntity.getHealth(), maxHealth == null ? 1d : maxHealth.getValue()); // Update for viewers of the world.
                oldLocation = lastLocation;
            }

            for (Player player : visibleToPlayers) {

                try {
                    PacketPlayOutEntityVelocity packetVelocity = new PacketPlayOutEntityVelocity(fakeEntityID, new Vec3D(lastLocation.getX(), lastLocation.getY(), lastLocation.getZ()));

                    if(oldLocation.distance(lastLocation) > 8) { // Protocol insists changes of > 8 must teleport
                        PacketPlayOutEntityTeleport packetTeleport = new PacketPlayOutEntityTeleport();

                        NMS.setClassField(PacketPlayOutEntityTeleport.class, packetTeleport, "a", fakeEntityID); // Entity ID

                        NMS.setClassField(PacketPlayOutEntityTeleport.class, packetTeleport, "b", lastLocation.getX()); // Pos X
                        NMS.setClassField(PacketPlayOutEntityTeleport.class, packetTeleport, "c", getDisplayYCoordinate()); // Pos Y
                        NMS.setClassField(PacketPlayOutEntityTeleport.class, packetTeleport, "d", lastLocation.getZ()); // Pos Z

                        int yaw = (int) (lastLocation.getYaw() * 256.0F / 360.0F);
                        int pitch = (int) (lastLocation.getPitch() * 256.0F / 360.0F);
                        NMS.setClassField(PacketPlayOutEntityTeleport.class, packetTeleport, "e", (byte) yaw); // Y rot (Yaw)
                        NMS.setClassField(PacketPlayOutEntityTeleport.class, packetTeleport, "f", (byte) pitch); // X rot (Pitch)

                        NMS.setClassField(PacketPlayOutEntityTeleport.class, packetTeleport, "g", false); //On Ground?

                        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetTeleport);

                    } else {
                        short deltaX = (short) ((lastLocation.getX() * 32 - oldLocation.getX() * 32) * 128);
                        short deltaY = (short) ((lastLocation.getY() * 32 - oldLocation.getY() * 32) * 128);
                        short deltaZ = (short) ((lastLocation.getZ() * 32 - oldLocation.getZ() * 32) * 128);
                        PacketPlayOutEntity.PacketPlayOutRelEntityMove packetMove =
                                new PacketPlayOutEntity.PacketPlayOutRelEntityMove(fakeEntityID, deltaX, deltaY, deltaZ, false); // Love how hidden this is.

                        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetMove);
                    }

                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetVelocity);

                } catch (NoSuchFieldException err) {
                    OofTracker.getLog().severe("Error building packet. - No field! Is this the wrong version?");
                    err.printStackTrace();
                    return;

                } catch (IllegalAccessException err) {
                    OofTracker.getLog().severe("Error building packet - Can't access field! Is something misconfigured?");
                    err.printStackTrace();
                    return;

                }
            }
        }
    }

    public double getDisplayYCoordinate() {
        double hostY = hostEntity.getBoundingBox().getMaxY();
        double offset = OofTracker.getConfiguration().getOrElse(ConfigKeys.HEALTH_BAR_OFFSET, 0.3d);
        return hostY + offset;
    }

    /** @return old location if there has been a change. */
    public Optional<Location> checkAndUpdateLastLocation() {
        Location hL = hostEntity.getLocation();

        if(!lastLocation.equals(hL)) { // Has location changed?
            Location oldLocation = lastLocation;
            lastLocation = new Location(hostEntity.getWorld(), hL.getX(), hL.getY(), hL.getZ(), hL.getYaw(), hL.getPitch());
            return Optional.of(oldLocation);
        }
        return Optional.empty();
    }



    /** @return 3 ints using the protocol's velocity format. */
    public static int[] calculateNetworkVelocity(Vector motion) {
        double dX = MathHelper.a(motion.getX(), -3.9D, 3.9D) * 8000D; // I think this is a clamping function + multiply?
        double dY = MathHelper.a(motion.getY(), -3.9D, 3.9D) * 8000D;
        double dZ = MathHelper.a(motion.getZ(), -3.9D, 3.9D) * 8000D;
        return new int[]{ (int)dX, (int)dY, (int)dZ };
    }

    public static String getHealthText(double health, double maxHealth) {

        switch (OofTracker.getConfiguration().getOrElse(ConfigKeys.HEALTH_BAR_VIEW_TYPE, HealthFormat.SQUARES)) {

            // -- TEXT --

            case TEXT_MONO:
                return genTextFormat(ChatColor.RED, ChatColor.DARK_RED, health, maxHealth);

            case TEXT_SPLIT:
                return genTextFormat(null, ChatColor.DARK_RED, health, maxHealth);

            case TEXT:
                return genTextFormat(null, null, health, maxHealth);


            // -- BAR --

            case BAR_MONO_NO_TEXT:
                return genBarFormat(ChatColor.RED, null, health, maxHealth, false);
            case BAR_MONO:
                return genBarFormat(ChatColor.RED, null, health, maxHealth, true);
            case BAR_DUO_NO_TEXT:
                return genBarFormat(ChatColor.GREEN, ChatColor.RED, health, maxHealth, false);
            case BAR_DUO:
                return genBarFormat(ChatColor.GREEN, ChatColor.RED, health, maxHealth, true);
            case BAR_NO_TEXT:
                return genBarFormat(null, null, health, maxHealth, false);
            case BAR:
                return genBarFormat(null, null, health, maxHealth, true);



            // -- SQUARES --

            case SQUARES_MONO_NO_TEXT:
                return genSquaresFormat(ChatColor.RED, health, maxHealth, false);
            case SQUARES_MONO:
                return genSquaresFormat(ChatColor.RED, health, maxHealth, true);
            case SQUARES_NO_TEXT:
                return genSquaresFormat(null, health, maxHealth, false);

            default: // SQUARES is default.
            case SQUARES:
                return genSquaresFormat(null, health, maxHealth, true);
        }
    }

    /**
     * Generates the text string for "TEXT" variety health bars.
     * @param primary the colour of the health. Colour is based on health if null.
     * @param secondary the colour of the max health. Colour is based on health if null.
     * @param health the entity's health.
     * @param maxHealth the entity's max health
     * @return the built Raw Text string.
     */
    private static String genTextFormat(ChatColor primary, ChatColor secondary, double health, double maxHealth) {
        String healthString = HEALTH_FORMAT.format(health);
        String maxHealthString = HEALTH_FORMAT.format(maxHealth);
        ChatColor genColour = getHealthColour(health, maxHealth);

        return new RawTextBuilder(healthString).setBold(true).setColor(primary == null ? genColour : primary)
                .append(new RawTextBuilder(String.format(" / %s \u2661", maxHealthString)).setColor(secondary == null ? genColour : secondary))
                .toString();
    }

    /**
     * Generates the text string for "SQUARES" variety health bars.
     * @param primary the colour of the health bar. Colour is based on health if null.
     * @param health the entity's health.
     * @param maxHealth the entity's max health
     * @return the built Raw Text string.
     */
    private static String genSquaresFormat(ChatColor primary, double health, double maxHealth, boolean includeText) {
        String healthString = HEALTH_FORMAT.format(health);
        String maxHealthString = HEALTH_FORMAT.format(maxHealth);
        ChatColor barColour = primary == null ? getHealthColour(health, maxHealth) : primary;

        double checkedMaxHealth = maxHealth > 0 ? maxHealth : 1; // Ensure maxHealth is not 0.
        double healthFraction = health / checkedMaxHealth;

        RawTextBuilder barBuilder = new RawTextBuilder().setBold(false).setColor(barColour);

        for(double i = 0; i < 1; i += 0.1d) { // Tbh this could be split into two RawText components rather than 10.
            RawTextBuilder squareBuilder = new RawTextBuilder("\u25A0");
            if(healthFraction < i) squareBuilder.setColor(ChatColor.GRAY); // Override base colour if true
            barBuilder.append(squareBuilder);
        }

        if(includeText) {
            RawTextBuilder fullBuilder = new RawTextBuilder().setBold(true).setColor(barColour);
            // Surround the bar text.
            fullBuilder.append(new RawTextBuilder(healthString + " "));
            fullBuilder.append(barBuilder);
            fullBuilder.append(new RawTextBuilder(" " + maxHealthString));
            return fullBuilder.toString();

        } else {
            return barBuilder.toString(); // The bar will be enough.
        }
    }

    /**
     * Generates the text string for "SQUARES" variety health bars.
     * @param primary the colour of the health bar. Colour is based on health if null.
     * @param health the entity's health.
     * @param maxHealth the entity's max health
     * @return the built Raw Text string.
     */
    private static String genBarFormat(ChatColor primary, ChatColor secondary, double health, double maxHealth, boolean includeText) {
        String healthString = HEALTH_FORMAT.format(health);
        String maxHealthString = HEALTH_FORMAT.format(maxHealth);
        ChatColor barColour = primary == null ? getHealthColour(health, maxHealth) : primary;
        ChatColor barEmptyColour = secondary == null ? ChatColor.GRAY : secondary;

        double checkedMaxHealth = maxHealth > 0 ? maxHealth : 1; // Ensure maxHealth is not 0.
        double healthFraction = health / checkedMaxHealth;

        RawTextBuilder barBuilder = new RawTextBuilder().setBold(false).setColor(barColour);

        barBuilder.append(new RawTextBuilder("[").setColor(ChatColor.DARK_GRAY));

        for(double i = 0; i < 1; i += 0.05d) { // Tbh this could be split into two RawText components rather than 10.
            RawTextBuilder squareBuilder = new RawTextBuilder(":");
            if(healthFraction < i) squareBuilder.setColor(barEmptyColour); // Override base colour if true
            barBuilder.append(squareBuilder);
        }

        barBuilder.append(new RawTextBuilder("]").setColor(ChatColor.DARK_GRAY));

        if(includeText) {
            RawTextBuilder fullBuilder = new RawTextBuilder().setBold(true).setColor(barColour);
            // Surround the bar text.
            fullBuilder.append(new RawTextBuilder(healthString + " "));
            fullBuilder.append(barBuilder);
            fullBuilder.append(new RawTextBuilder(" " + maxHealthString).setColor(secondary == null ? barColour : ChatColor.RED));
            return fullBuilder.toString();

        } else {
            return barBuilder.toString(); // The bar will be enough.
        }
    }


    private static ChatColor getHealthColour(double health, double maxHealth) {
        double checkedMaxHealth = maxHealth > 0 ? maxHealth : 1; // Ensure maxHealth is not 0.
        double fraction = health / checkedMaxHealth;

        if (fraction >= THRESHOLD_HEALTHY) return ChatColor.GREEN;
        if (fraction >= THRESHOLD_OKAY) return ChatColor.YELLOW;
        if(fraction >= THRESHOLD_WOUNDED) return ChatColor.GOLD;

        return ChatColor.RED; // Otherwise it's red cause it's below the threshold
    }
}
