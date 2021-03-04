package net.cg360.spigot.ooftracker.list;

import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.UUID;

public class DamageStackManager {

    private static DamageStackManager primaryManager;

    private HashMap<UUID, DamageStack> damageLists;



    public DamageStackManager() {
        this.damageLists = new HashMap<>();
    }



    /**
     * Sets the manager the result provided from DamageStackManager#get() and
     * finalizes the instance to an extent.
     *
     * Cannot be changed once initially called.
     */
    public void setAsPrimaryManager(){
        if(primaryManager == null) primaryManager = this;
    }


    /**
     * Gets an existing Damage List or creates one if a list is not present.
     * @param entity the entity to fetch the list for.
     * @return the Damage List for the entity. (Non Null)
     */
    public DamageStack getDamageList(Entity entity) {
        return getDamageList(entity.getUniqueId());
    }

    /**
     * Gets an existing Damage List or creates one if a list is not present.
     * @param entityUUID the uuid of the entity to fetch the list for.
     * @return the Damage List for the entity. (Non Null)
     */
    public DamageStack getDamageList(UUID entityUUID) {
        // Create a damagelist if it doesn't exist.
        if(!damageLists.containsKey(entityUUID)) {
            damageLists.put(entityUUID, new DamageStack(entityUUID));
        }

        return damageLists.get(entityUUID);
    }
    // No setter method as getDamageList creates one.


    /** @return the primary instance of the DamageStackManager. */
    public static DamageStackManager get(){
        return primaryManager;
    }
}
