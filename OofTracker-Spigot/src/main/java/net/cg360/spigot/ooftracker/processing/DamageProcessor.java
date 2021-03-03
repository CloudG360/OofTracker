package net.cg360.spigot.ooftracker.processing;

import net.cg360.spigot.ooftracker.cause.DamageTrace;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Optional;

/**
 * A class which processes damage events if a condition is passed.
 */
public abstract class DamageProcessor {

    /**
     * Generates a DamageTrace if the processor can produce a specialised entry.
     * @param event the event triggering this event
     * @return an optional specialised DamageTrace.
     */
    public final Optional<DamageTrace> getDamageTrace(EntityDamageEvent event) {

        if(checkEvent(event)) {
            return Optional.ofNullable(genDamageTrace(event));
        }
        return Optional.empty();
    }


    /**
     * Generates a specialised DamageTrace after DamageProcessor#checkEvent()
     * is called and returns true.
     * @return a specialised DamageTrace
     */
    protected abstract DamageTrace genDamageTrace(EntityDamageEvent event);

    /**
     * @return true if this DamageProcessor can generate a specialised
     * DamageTrace for this event.
     */
    protected abstract boolean checkEvent(EntityDamageEvent event);
    // ^ This could technically modify the event. I could encase it in another class
    //   but I feel like that's overcomplicating access.
    //   Just don't mess with it :)


    /**
     * Returns the priority of this DamageProcessor. Can be an int value
     * but using the Priority enum as guidance is advised.
     * @return the priority of this DamageProcessor.
     */
    public abstract int getPriority();

}
