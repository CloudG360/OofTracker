package net.cg360.spigot.ooftracker;

import net.cg360.nsapi.commons.data.keyvalue.Key;

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

}
