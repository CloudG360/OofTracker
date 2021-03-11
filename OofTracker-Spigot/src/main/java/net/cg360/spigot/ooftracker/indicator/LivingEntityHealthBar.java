package net.cg360.spigot.ooftracker.indicator;

import net.cg360.spigot.ooftracker.ConfigKeys;
import net.cg360.spigot.ooftracker.OofTracker;
import net.cg360.spigot.ooftracker.nms.NMS;
import net.cg360.spigot.ooftracker.nms.RawTextBuilder;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class LivingEntityHealthBar {

    private static final DecimalFormat HEALTH_FORMAT = new DecimalFormat("0.0");

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



    public void updateDisplay() {
        double maxDistance = OofTracker.getConfiguration().getOrElse(ConfigKeys.HEALTH_BAR_VIEW_DISTANCE, 20d);

        for(Player p: hostEntity.getWorld().getPlayers()) {

            if(p != hostEntity) { // Ensure the player, if they are the entity, are not visible.
                double distance = p.getLocation().distance(hostEntity.getLocation());
                CraftPlayer cPlayer = (CraftPlayer) p;

                if(visibleToPlayers.contains(p)) {

                    if ((!visible) || (distance > maxDistance)) { // Isn't visible or the distance is now to large, remove
                        PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(fakeEntityID);
                        cPlayer.getHandle().playerConnection.sendPacket(destroyPacket);
                        visibleToPlayers.remove(p);

                    } else { // Still visible, may include a health update so send new meta.
                        DataWatcher dataWatcher = new DataWatcher(null);


                        dataWatcher.register(new DataWatcherObject<>(0, DataWatcherRegistry.a), (byte) 0x20); // Is invisible
                        dataWatcher.register(new DataWatcherObject<>(2, DataWatcherRegistry.f),  Optional.ofNullable( IChatBaseComponent.ChatSerializer.b( getHealthText( hostEntity.getHealth() ) ) ) ); // Custom name
                        dataWatcher.register(new DataWatcherObject<>(3, DataWatcherRegistry.i), true); // Custom name visible
                        dataWatcher.register(new DataWatcherObject<>(14, DataWatcherRegistry.a), (byte) 0x10); // Set Marker

                        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(fakeEntityID, dataWatcher, true);

                        cPlayer.getHandle().playerConnection.sendPacket(metaPacket);
                    }

                } else {

                    if (visible && (distance <= maxDistance)) { // Is visible + now within distance, add
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
                            dataWatcher.register(new DataWatcherObject<>(2, DataWatcherRegistry.f), Optional.ofNullable( IChatBaseComponent.ChatSerializer.b( getHealthText( hostEntity.getHealth() ) ) ) ); // Custom name
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
    }

    // If you find an entity movement event, PLEASE just use that. Running this every tick is stupid.
    public void updatePositionAndVelocity() {
        Optional<Location> oldLoc = checkAndUpdateLastLocation();


        if(oldLoc.isPresent()) { // A movement has occured
            Location oldLocation = oldLoc.get();

            for (Player player : visibleToPlayers) {

                try {
                    PacketPlayOutEntityVelocity packetVelocity = new PacketPlayOutEntityVelocity(fakeEntityID, new Vec3D(lastLocation.getX(), lastLocation.getY(), lastLocation.getZ()));

                    if(oldLocation.distance(lastLocation) > 8) { // Protocol insists changes of > 8 must teleport
                        PacketPlayOutEntityTeleport packetTeleport = new PacketPlayOutEntityTeleport();

                        NMS.setClassField(PacketPlayOutEntityTeleport.class, packetTeleport, "b", lastLocation.getX()); // Pos X
                        NMS.setClassField(PacketPlayOutEntityTeleport.class, packetTeleport, "c", lastLocation.getY()); // Pos Y
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

    public static String getHealthText(double health) {
        return new RawTextBuilder(String.valueOf(Math.ceil(health))).setBold(true).setColor("red").toString();
    }
}
