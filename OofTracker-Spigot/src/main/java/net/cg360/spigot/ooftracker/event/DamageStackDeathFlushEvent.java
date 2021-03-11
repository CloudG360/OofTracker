package net.cg360.spigot.ooftracker.event;

import net.cg360.spigot.ooftracker.list.DamageStack;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called even if death list clearing is disabled in the config,
 * instead marking it as cancelled.
 */
public class DamageStackDeathFlushEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    protected Entity victim;
    protected DamageStack stack;
    protected boolean isCancelled;

    public DamageStackDeathFlushEvent(Entity victim, DamageStack stack) {
        this.victim = victim;
        this.stack = stack;
        this.isCancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }


    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    public void setCancelled() {
        this.setCancelled(true);
    }
}
