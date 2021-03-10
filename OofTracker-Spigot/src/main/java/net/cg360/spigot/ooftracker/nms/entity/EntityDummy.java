package net.cg360.spigot.ooftracker.nms.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.cg360.spigot.ooftracker.Util;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class EntityDummy {

    // the base64 found in the commands at https://minecraft-heads.com/custom-heads
    // is already correct. Just need to turn the command string into bytes.
    public static final byte[] DEFAULT_SKIN_DATA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzk2ZTM3OTQ1M2IwYTUyMWRkYWM2N2JhYzZhYTliNzcxNWZkNjRlYzBhMWU4Y2RiMzdkZjYzNTI2MGMwMzFjYiJ9fX0=".getBytes(StandardCharsets.UTF_8);

    public static EntityPlayerDummy createDummy(Location location) {
        if(location.getWorld() == null) throw new IllegalArgumentException("Location's 'world' property must not be null");
        return createDummy( ((CraftWorld)location.getWorld()).getHandle(), location);
    }

    public static EntityPlayerDummy createDummy(World world, Location location) {
        UUID uuid = Util.generateUUIDv2();
        GameProfile profile = new GameProfile(uuid, null);

        profile.getProperties().put("textures", new Property("textures", new String(DEFAULT_SKIN_DATA)));

        EntityPlayerDummy dummyEntity = new EntityPlayerDummy(world, location, profile);

        return dummyEntity;
    }



    public static class EntityPlayerDummy extends EntityHuman {

        protected EntityPlayerDummy(World world, Location position, GameProfile profile) {
            super( world, new BlockPosition(position.getBlockX(), position.getBlockY(), position.getBlockZ()), position.getYaw(), profile);
        }



        @Override
        public boolean isSpectator() {
            return false;
        }

        @Override
        public boolean isCreative() {
            return false;
        }
    }



}
