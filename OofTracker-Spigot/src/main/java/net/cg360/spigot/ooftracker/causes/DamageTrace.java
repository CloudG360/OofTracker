package net.cg360.spigot.ooftracker.causes;

import net.cg360.nsapi.commons.data.Settings;
import net.cg360.nsapi.commons.id.Identifier;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

public abstract class DamageTrace {

    protected Settings rawData; // Don't lock this unless you're mean :<

    protected long time;

    protected final Entity victim; // Why would this change? Give me a good reason :)
    protected double originalDamageDealt;
    protected double finalDamageDealt;
    protected EntityDamageEvent.DamageCause vanillaCause;

    public DamageTrace(EntityDamageEvent eventIn) {
        this.rawData = new Settings();

        // Time probably shouldn't be changed else
        // the stack will be wonky.
        // Only change it if you're copying another damage trace maybe?
        this.time = System.currentTimeMillis();

        this.victim = eventIn.getEntity();
        this.originalDamageDealt = eventIn.getDamage();
        this.finalDamageDealt = eventIn.getFinalDamage();
        this.vanillaCause = eventIn.getCause();


        this.rawData.set(TraceKeys.TIME, this.time);

        this.rawData.set(TraceKeys.VICTIM, this.victim);
        this.rawData.set(TraceKeys.ORIGINAL_DAMAGE, this.originalDamageDealt);
        this.rawData.set(TraceKeys.FINAL_DAMAGE, this.finalDamageDealt);
        this.rawData.set(TraceKeys.VANILLA_CAUSE, this.vanillaCause);

    }

    /**
     * Sets the final damage applied, overriding all modifiers
     * to the base damage.
     * @param finalDamageDealt the amount to be dealt
     */
    public void setFinalDamage(double finalDamageDealt) {
        this.finalDamageDealt = finalDamageDealt;
        this.rawData.set(TraceKeys.FINAL_DAMAGE, this.finalDamageDealt);
    }

    /**
     * Sets the spigot DamageCause of the damage.
     * @param vanillaCause
     */
    public void setVanillaCause(EntityDamageEvent.DamageCause vanillaCause) {
        this.vanillaCause = vanillaCause;
        this.rawData.set(TraceKeys.VANILLA_CAUSE, this.vanillaCause);
    }



    /** @return the type of DamageTrace this instance is. (Similar to Spigot's DamageCause) */
    public abstract Identifier getTraceType();

    /** @return a copied key+value representation of the damage trace. */
    public Settings getData() {
        return rawData.getUnlockedCopy();
        // Ensure public version is a COPY.
        // The raw data should not be updated directly. It's just
        // a good universal interface.
    }

    /** @return the time at which this trace was created. */
    public long getTime() { return time; }

    /** @return the victim entity of the damage. */
    public Entity getVictim() { return victim; }

    /** @return the damage dealt without modifiers. */
    public double getOriginalDamageDealt() { return originalDamageDealt; }

    /** @return the actual damage applied to the entity after modifiers. */
    public double getFinalDamageDealt() { return finalDamageDealt; }

    /** @return the vanilla cause of the damage. */
    public EntityDamageEvent.DamageCause getVanillaCause() { return vanillaCause; }
}
