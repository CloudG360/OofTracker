package net.cg360.spigot.ooftracker.cause.indicators;

import net.cg360.spigot.ooftracker.ConfigKeys;
import net.cg360.spigot.ooftracker.OofTracker;
import net.cg360.spigot.ooftracker.Util;
import net.cg360.spigot.ooftracker.nms.NMS;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_16_R3.Vec3D;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;
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




                        cPlayer.getHandle().playerConnection.sendPacket(addPacket);
                    }
                }

            }
        }
    }
}
