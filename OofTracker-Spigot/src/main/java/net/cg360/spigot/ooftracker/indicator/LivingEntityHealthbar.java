package net.cg360.spigot.ooftracker.indicator;

import net.cg360.spigot.ooftracker.ConfigKeys;
import net.cg360.spigot.ooftracker.OofTracker;
import net.cg360.spigot.ooftracker.nms.NMS;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LivingEntityHealthbar {

    protected LivingEntity hostEntity;
    protected ArrayList<Player> visibleToPlayers;

    protected int fakeEntityID;
    protected UUID fakeEntityUUID;


    protected boolean visible;

    // Should only be instantiated from the HealthbarManager
    protected LivingEntityHealthbar(LivingEntity host) {
        this.hostEntity = host;
        this.visibleToPlayers = new ArrayList<>();

        this.fakeEntityID = NMS.getNewEntityID(); // Steal right from under NMS's nose :)
        this.fakeEntityUUID = UUID.randomUUID();

        this.visible = false;
    }

    public void updateVisibility() {
        double maxDistance = OofTracker.getConfiguration().getOrElse(ConfigKeys.HEALTH_BAR_VIEW_DISTANCE, 20d);

        for(Player p: hostEntity.getWorld().getPlayers()) {

            if(p != hostEntity) { // Ensure the player, if they are the entity, are not visible.
                double distance = p.getLocation().distance(hostEntity.getLocation());

                if(visibleToPlayers.contains(p)) {

                    if ((!visible) || (distance > maxDistance)) { // Isn't visible or the distance is now to large, remove
                        visibleToPlayers.remove(p);
                        CraftPlayer cPlayer = (CraftPlayer) p;
                        PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(fakeEntityID);
                        cPlayer.getHandle().playerConnection.sendPacket(destroyPacket);
                    }

                } else {

                    if (visible && (distance <= maxDistance)) { // Is visible + now within distance, add
                        visibleToPlayers.add(p);
                        CraftPlayer cPlayer = (CraftPlayer) p;
                        PacketPlayOutSpawnEntityLiving addPacket = new PacketPlayOutSpawnEntityLiving(); // An armour stand is apparently living??
                        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata();

                        try {

                            // -- ADD ENTITY --

                            NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "a", fakeEntityID); // Entity ID
                            NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "b", fakeEntityUUID); // Entity UUID
                            NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "c", NMS.NETID_ARMOUR_STAND); // Entity Type

                            // Copy host entity's location and follow
                            NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "d", hostEntity.getLocation().getX()); // Location X
                            NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "e", hostEntity.getLocation().getY()); // Location Y
                            NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "f", hostEntity.getLocation().getZ()); // Location Z

                            // Copy host entity's motion and follow.
                            Vector motion = hostEntity.getVelocity();
                            double dX = MathHelper.a(motion.getX(), -3.9D, 3.9D) * 8000D; // I think this is a clamping function + multiply?
                            double dY = MathHelper.a(motion.getY(), -3.9D, 3.9D) * 8000D;;
                            double dZ = MathHelper.a(motion.getZ(), -3.9D, 3.9D) * 8000D;;
                            NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "g", (int) dX); // Velocity X
                            NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "h", (int) dY); // Location Y
                            NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "i", (int) dZ); // Location Z

                            // Technically rotation doesn't matter but let's copy anyway.
                            int yaw = (int) (hostEntity.getLocation().getYaw() * 256.0F / 360.0F);
                            int pitch = (int) (hostEntity.getLocation().getPitch() * 256.0F / 360.0F);
                            int head = (int) (((CraftLivingEntity)hostEntity).getHandle().getHeadRotation() * 256.0F / 360.0F); // Head rotation apparently.
                            NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "j", (byte) yaw); // Yaw
                            NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "k", (byte) pitch); // Pitch
                            NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "l", (byte) head); // Head rotate.



                            // -- UPDATE ENTITY META --

                            NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "a", fakeEntityID); // Entity ID

                            NBTTagCompound compoundTag = new NBTTagCompound();
                            compoundTag.setBoolean("Invisible", true);
                            compoundTag.setBoolean("Marker", true);
                            compoundTag.setString("CustomName", "Ooga Booga");
                            compoundTag.setBoolean("CustomNameVisible", true);

                            DataWatcherObject<NBTTagCompound> tagWatcher = new DataWatcherObject<>(14, DataWatcherRegistry.p);
                            DataWatcher.Item<NBTTagCompound> watcherItem = new DataWatcher.Item<>(tagWatcher, compoundTag);

                            List<DataWatcher.Item<?>> itemList = NMS.getClassField(PacketPlayOutEntityMetadata.class, metaPacket, "b");
                            itemList.add(watcherItem);

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
}
