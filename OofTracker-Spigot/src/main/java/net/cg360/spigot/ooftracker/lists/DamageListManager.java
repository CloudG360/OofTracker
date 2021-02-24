package net.cg360.spigot.ooftracker.lists;

import java.util.HashMap;
import java.util.UUID;

public class DamageListManager {

    private static DamageListManager primaryManager;

    private HashMap<UUID, DamageList> damageLists;


    public DamageListManager() {
        this.damageLists = new HashMap<>();
    }


    /**
     * Sets the manager the result provided from KitRegistry#get() and
     * finalizes the instance to an extent.
     *
     * Cannot be changed once initially called.
     */
    public void setAsPrimaryManager(){
        if(primaryManager == null) primaryManager = this;
    }




    /** @return the primary instance of the EventManager. */
    public static DamageListManager get(){
        return primaryManager;
    }
}
