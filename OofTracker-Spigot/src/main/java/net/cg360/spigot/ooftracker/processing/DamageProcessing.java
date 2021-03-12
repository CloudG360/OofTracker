package net.cg360.spigot.ooftracker.processing;

import net.cg360.nsapi.commons.Check;
import net.cg360.spigot.ooftracker.ConfigKeys;
import net.cg360.spigot.ooftracker.OofTracker;
import net.cg360.spigot.ooftracker.Util;
import net.cg360.spigot.ooftracker.cause.DamageTrace;
import net.cg360.spigot.ooftracker.cause.TraceKeys;
import net.cg360.spigot.ooftracker.event.DamageStackDeathFlushEvent;
import net.cg360.spigot.ooftracker.list.DamageStack;
import net.cg360.spigot.ooftracker.list.DamageStackManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.*;

public class DamageProcessing implements Listener {

    protected List<UUID> customDamageCalls; // Used to block events caused by custom damage.
    protected List<DamageProcessor> damageProcessors;

    public DamageProcessing() {
        this.customDamageCalls = new ArrayList<>();
        this.damageProcessors = new ArrayList<>();
    }



    /**
     * Applies custom damage to an entity, thus skipping the
     * DamageProcessing system and appending the DamageTrace
     * on directly
     * @param trace a damage trace to be applied to the DamageStack stack
     * @return true if the damage has been applied.
     */
    public boolean applyCustomDamage(DamageTrace trace) {
        Check.nullParam(trace, "Damage Trace");

        if(trace.getVictim() instanceof Player || Util.check(ConfigKeys.LIST_NON_PLAYER_ENABLED, true)) {

            if (trace.getVictim() instanceof Damageable) {
                Damageable entity = (Damageable) trace.getVictim();
                Entity attacker = trace.getData().get(TraceKeys.ATTACKER_ENTITY);

                // The following Entity#damage() calls are only to support
                // Spigot damage events + update player health.

                // Add the entity to the list of ignored events
                // Then damage without the event triggering :)
                customDamageCalls.add(entity.getUniqueId());

                if (attacker == null) {
                    entity.damage(trace.getFinalDamageDealt());

                } else { // Had an attacker, trigger damage with attacker.
                    entity.damage(trace.getFinalDamageDealt(), attacker);
                }

                pushTrace(entity, trace);
                return true;
            }
        }

        return false;
    }


    public void addDamageProcessor(DamageProcessor processor) {
        Check.nullParam(processor, "Damage Processor");

        int priority = processor.getPriority();
        for(int i = 0; i < damageProcessors.size(); i++) {
            DamageProcessor originalProcessor = damageProcessors.get(i);

            if(originalProcessor.getPriority() < priority) {
                damageProcessors.add(i, processor);
                return;
            }
        }
        damageProcessors.add(processor); // Add if not already added.
    }



    // Should be last in the chain, ignoring if the event has been cancelled.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {

        // If it's a player or if non-player lists are enabled, do.
        if(event.getEntity() instanceof Player || Util.check(ConfigKeys.LIST_NON_PLAYER_ENABLED, true)) {

            if (!customDamageCalls.remove(event.getEntity().getUniqueId())) {
                // ^ If an item gets removed, it must've existed.
                // Thus, only run the following if nothing was removed.

                for (DamageProcessor processor : damageProcessors) {
                    Optional<DamageTrace> trace = processor.getDamageTrace(event);

                    if (trace.isPresent()) {
                        pushTrace(event.getEntity(), trace.get());
                        return;
                    }
                }

                // Panic! The default DamageTrace generator should've been last.
                throw new IllegalStateException("Default DamageProcessor didn't kick in. Why's it broke? :<");
            }
        }
    }

    // Called to set death message.
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDeathLow(EntityDeathEvent event) {

    }

    // Like damage, it should be within the end of the chain.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeathHigh(EntityDeathEvent event) {

        // Doesn't seem to have a named animal death event.
        // If there's a death of a pet, there'll be no custom death message as that doesn't
        // seem to have a setter.
        if (event instanceof PlayerDeathEvent) {
            PlayerDeathEvent e = (PlayerDeathEvent) event;

            if (Util.check(ConfigKeys.ASSIST_TAGS_ENABLED, true)  && (!Util.check(ConfigKeys.DAMAGE_LISTS_ONLY, false))) {
                TextComponent text = new TextComponent(e.getDeathMessage());
                e.setDeathMessage(""); // As we're sending raw, do not send original.

                DamageStack stack = DamageStackManager.get().getDamageList(event.getEntity()).duplicate();
                StringBuilder assistBuilder = new StringBuilder();

                ArrayList<Player> previousDamagers = new ArrayList<>();

                while (!stack.isEmpty()) {
                    DamageTrace t = stack.pop();
                    Entity root = t.getData().get(TraceKeys.ATTACKER_ROOT);

                    if(root instanceof Player) {
                        Player p = (Player) root;

                        if(!previousDamagers.contains(p)) {

                            if (Util.check(ConfigKeys.PING_ON_KILL, true)) {
                                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                            }

                            if(Util.check(ConfigKeys.KILLER_IN_ASSIST_TAG, true)) {
                                assistBuilder.append(p.getName()); // Only add killer to assist tag if true
                                assistBuilder.append(", ");
                            }

                            previousDamagers.add(p);
                        }
                        continue;
                    }
                }

                if(assistBuilder.length() > 0) {
                    assistBuilder.delete(assistBuilder.length() - 2, assistBuilder.length());
                }

                String buildString = assistBuilder.toString();

                if (buildString.length() > 0) {
                    TextComponent assistInfo = new TextComponent(buildString);
                    assistInfo.setColor(ChatColor.DARK_AQUA);

                    TextComponent assistAddon = new TextComponent(" (+ Assist)");
                    assistAddon.setColor(ChatColor.AQUA);
                    assistAddon.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(assistInfo).create())));
                    text.addExtra(assistAddon);
                }


                OofTracker.getLog().info(text.getText());
                Collection<? extends Player> recipientPlayers;  // Weird type cause of the SERVER scope.

                // Select broadcast type with a switch.
                // Sends the message as raw despite if it's been edited or not.
                switch (OofTracker.getConfiguration().getOrElse(ConfigKeys.DEATH_BROADCAST_SCOPE, DeathBroadcastScope.SERVER)) {

                    case WORLD:
                        // Send to everyone in the world where the death occurred.
                        recipientPlayers = e.getEntity().getWorld().getPlayers();
                        break;

                    case PARTICIPANTS:
                        // Send to everyone in the world where the death occurred.
                        previousDamagers.add(e.getEntity());
                        recipientPlayers = previousDamagers;
                        break;

                    case NONE:
                        // Empty, send to no one.
                        recipientPlayers = new ArrayList<>();
                        break;

                    default: // Server is the default!
                    case SERVER:
                        // Send to everyone on the server
                        recipientPlayers = OofTracker.get().getServer().getOnlinePlayers();
                        break;
                }

                // Send message to whatever list is picked.
                for (Player p: recipientPlayers) {
                    p.spigot().sendMessage(text);
                }

            }
        }

        DamageStackDeathFlushEvent stackFlushEvent = new DamageStackDeathFlushEvent(
                event.getEntity(),
                DamageStackManager.get().getDamageList(event.getEntity())
        );

        stackFlushEvent.setCancelled(!Util.check(ConfigKeys.LIST_CLEAR_ON_DEATH, true)); // Cancel if list clearing is disabled.
        OofTracker.get().getServer().getPluginManager().callEvent(stackFlushEvent);

        if(stackFlushEvent.isCancelled()) { DamageStackManager.get().getDamageList(event.getEntity()).clear(); }
    }


    private static void pushTrace(Entity entity, DamageTrace t) {
        DamageStack ls = OofTracker.getDamageStackManager().getDamageList(entity);
        ls.push(t);
    }
}
