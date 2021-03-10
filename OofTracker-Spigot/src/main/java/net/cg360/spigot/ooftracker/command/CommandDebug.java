package net.cg360.spigot.ooftracker.command;

import net.cg360.spigot.ooftracker.OofTracker;
import net.cg360.spigot.ooftracker.nms.NMS;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandDebug implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof CraftPlayer) {
            int fakeEntityID = NMS.getNewEntityID(); // Steal right from under NMS's nose :)
            UUID fakeEntityUUID = UUID.randomUUID();

            CraftPlayer cPlayer = (CraftPlayer) sender;
            PacketPlayOutSpawnEntityLiving addPacket = new PacketPlayOutSpawnEntityLiving(); // An armour stand is apparently living??
            PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata();

            try {

                // -- ADD ENTITY --

                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "a", fakeEntityID); // Entity ID
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "b", fakeEntityUUID); // Entity UUID
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "c", NMS.NETID_ARMOUR_STAND); // Entity Type

                // Copy host entity's location and follow
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "d", cPlayer.getLocation().getX()); // Location X
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "e", cPlayer.getLocation().getY()); // Location Y
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "f", cPlayer.getLocation().getZ()); // Location Z

                // Copy host entity's motion and follow.
                Vector motion = cPlayer.getVelocity();
                double dX = MathHelper.a(motion.getX(), -3.9D, 3.9D) * 8000D; // I think this is a clamping function + multiply?
                double dY = MathHelper.a(motion.getY(), -3.9D, 3.9D) * 8000D;;
                double dZ = MathHelper.a(motion.getZ(), -3.9D, 3.9D) * 8000D;;
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "g", (int) dX); // Velocity X
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "h", (int) dY); // Location Y
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "i", (int) dZ); // Location Z

                // Technically rotation doesn't matter but let's copy anyway.
                int yaw = (int) (cPlayer.getLocation().getYaw() * 256.0F / 360.0F);
                int pitch = (int) (cPlayer.getLocation().getPitch() * 256.0F / 360.0F);
                int head = (int) (((CraftLivingEntity)cPlayer).getHandle().getHeadRotation() * 256.0F / 360.0F); // Head rotation apparently.
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

                List<DataWatcher.Item<?>> itemList = new ArrayList<>();
                itemList.add(watcherItem);
                NMS.setClassField(PacketPlayOutEntityMetadata.class, metaPacket, "b", itemList);

                cPlayer.getHandle().playerConnection.sendPacket(addPacket);
                cPlayer.getHandle().playerConnection.sendPacket(metaPacket);

            } catch (NoSuchFieldException err) {
                OofTracker.getLog().severe("Error building packet. - No field! Is this the wrong version?");
                err.printStackTrace();
                return false;

            } catch (IllegalAccessException err) {
                OofTracker.getLog().severe("Error building packet - Can't access field! Is something misconfigured?");
                err.printStackTrace();
                return false;

            }
            return true;
        }
        return false;
    }
}