package net.cg360.spigot.ooftracker.nms;

import net.cg360.spigot.ooftracker.OofTracker;
import net.minecraft.server.v1_16_R3.Entity;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class NMS {

    /**
     * Hooks into the Entity NMS to return the current
     * value of the static field "entityCount" (Used for entity IDs),
     * then incrementing it.
     * @return the value of the static AtomicInteger "entityCount" in the NMS Entity
     */
    public static int getNewEntityID() {
        Class<Entity> cls = Entity.class; // Get NMS Entity class

        try {
            Field field = cls.getDeclaredField("entityCount");
            field.setAccessible(true); // Ensure accessible, might as well keep it open.
            AtomicInteger aI = (AtomicInteger) field.get(null); // Get static field.

            return aI.getAndIncrement(); // Get the ID and then increment it

        } catch (NoSuchFieldException err) {
            OofTracker.getLog().severe("Unable to increment entity ID - No field! Is this the wrong version?");
            err.printStackTrace();
            return new Random().nextInt(Integer.MAX_VALUE - 10); // Idk what to return really. Provide a random number instead.

        } catch (IllegalAccessException err) {
            OofTracker.getLog().severe("Unable to increment entity ID - Can't access field! Is something misconfigured?");
            err.printStackTrace();
            return new Random().nextInt(Integer.MAX_VALUE - 10);

        }  catch (ClassCastException err) {
            OofTracker.getLog().severe("Unable to increment entity ID - Wrong type! Is this the wrong version?");
            err.printStackTrace();
            return new Random().nextInt(Integer.MAX_VALUE - 10);
        }
    }

}
