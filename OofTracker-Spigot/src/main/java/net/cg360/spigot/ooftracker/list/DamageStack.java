package net.cg360.spigot.ooftracker.list;

import net.cg360.nsapi.commons.Check;
import net.cg360.spigot.ooftracker.OofTracker;
import net.cg360.spigot.ooftracker.cause.DamageTrace;

import java.util.LinkedList;
import java.util.UUID;

public class DamageStack {

    protected UUID owner;
    protected LinkedList<DamageTrace> stack;

    public DamageStack(UUID owner) {
        Check.nullParam(owner, "Owner");
        this.owner = owner;
        this.stack = new LinkedList<>();
    }

    protected DamageStack(DamageStack previousStack) {
        this.owner = previousStack.owner;
        this.stack = new LinkedList<>(previousStack.stack);
    }


    /** Adds a DamageTrace to the top of the DamageStack stack. */
    public void push(DamageTrace trace) {
        if(!trace.getVictim().getUniqueId().equals(owner)) throw new IllegalArgumentException("DamageTrace being pushed must belong to the list's owner.");
        stack.add(0, trace);
        OofTracker.getLog().fine(trace.toString());
    }

    /**
     * Removes the most recent element on the DamageStack stack.
     * @return the most recent element.
     */
    public DamageTrace pop() {
        DamageTrace t = peek();
        if(!isEmpty()) stack.remove(0);
        return t;
    }

    /**
     * Checks the the most recent element of the DamageStack
     * stack without removing it.
     * @return the most recent element
     */
    public DamageTrace peek() {
        return isEmpty() ? null : this.stack.get(0);
    }

    /** @return the size of the stack. */
    public int size() { return this.stack.size(); }

    /** Clears the DamageStack stack. */
    public void clear() {
        this.stack.clear();
    }

    /** Duplicates the stack. */
    public DamageStack duplicate() {
        return new DamageStack(this);
    }



    /** @return true if the list is empty. */
    public boolean isEmpty() { return this.stack.size() == 0; }

    /** @return the owning entity id of this DamageStack. */
    public UUID getOwner() { return owner; }
}
