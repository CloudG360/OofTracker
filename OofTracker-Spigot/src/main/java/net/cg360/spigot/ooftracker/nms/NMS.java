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
            AtomicInteger atomicInteger = getStaticClassField(cls, "entityCount"); // Assign to var cause generics.
            return atomicInteger.getAndIncrement(); // Get the ID and then increment it

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


    public static <T> T getStaticClassField(Class<?> cls, String fieldName) throws NoSuchFieldException, IllegalAccessException, ClassCastException {
        return getClassField(cls, null, fieldName); // Get the ID and then increment it
    }

    @SuppressWarnings("unchecked") // We're throwing the error out anyway.
    public static <T, O> T getClassField(Class<O> cls, O object, String fieldName) throws NoSuchFieldException, IllegalAccessException, ClassCastException {
        Field field = cls.getDeclaredField(fieldName);
        field.setAccessible(true); // Ensure accessible, might as well keep it open.

        return (T) field.get(object); // Get the field
    }


    public static <T> void setStaticClassField(Class<?> cls, String fieldName, T value) throws NoSuchFieldException, IllegalAccessException {
        setClassField(cls, null, fieldName, value); // Get the ID and then increment it
    }

    @SuppressWarnings("unchecked") // We're throwing the error out anyway.
    public static <T, O> void setClassField(Class<O> cls, O object, String fieldName, T value) throws NoSuchFieldException, IllegalAccessException {
        Field field = cls.getDeclaredField(fieldName);
        field.setAccessible(true); // Ensure accessible, might as well keep it open.
        field.set(object, value); // Set the field to the value
    }

}
