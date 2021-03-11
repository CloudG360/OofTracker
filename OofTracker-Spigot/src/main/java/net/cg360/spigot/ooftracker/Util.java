package net.cg360.spigot.ooftracker;

import net.cg360.nsapi.commons.data.keyvalue.Key;
import net.minecraft.server.v1_16_R3.Entity;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Util {

    /**
     * Shorthand method for checking OofTracker's configuration booleans.
     * @param boolKey the key in the config
     * @param orElse the fallback value
     * @return the value stored, else the fallback value.
     */
    public static boolean check(Key<Boolean> boolKey, boolean orElse) {
        return OofTracker.getConfiguration().getOrElse(boolKey, orElse);
    }

    /**
     * Takes the current version UUID and pretends it's a version
     * 2 UUID for NPCs.
     * @return a version 2 UUID
     */
    public static UUID generateUUIDv2() {
        UUID baseUUID = UUID.randomUUID();

        // "For NPCs UUID v2 should be used. Note:"
        // <+Grum> i will never confirm this as a feature you know that :)
        // https://wiki.vg/Protocol#Spawn_Player

        if (baseUUID.version() != 2) { // Version 4 -> Version 2
            long mostSignificant = baseUUID.getMostSignificantBits();
            mostSignificant &= ~0x0000000000004000L; // Ones compliment + AND
            mostSignificant |= 0x0000000000002000L; // OR version 2 in
            baseUUID = new UUID(mostSignificant, baseUUID.getLeastSignificantBits());
        }

        return baseUUID;
    }

    public static <T extends Enum<T>> T stringToEnum(Class<T> enumBase, String value) {
        return Enum.valueOf(enumBase, value.toUpperCase());
    }
}
