package net.cg360.spigot.ooftracker.indicator.particle;

import net.cg360.spigot.ooftracker.nms.NMS;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.UUID;

// This could probably get a cross-platform utility class in NSAPI Commons.
public class TextParticle {

    protected ArrayList<Player> visibleToPlayers;

    protected int fakeEntityID;
    protected UUID fakeEntityUUID;
    protected World world;
    protected Location position;
    protected Vector velocity;

    protected int age;
    protected int range;
    protected boolean isSpawned;

    protected String text;


    public TextParticle(String text, Location startingPosition, Vector initialVelocity, int lifespan, int range) {
        this.visibleToPlayers = new ArrayList<>();

        if(startingPosition == null) throw new IllegalArgumentException("Starting position must not be null.");
        if(startingPosition.getWorld() == null) throw new IllegalArgumentException("TextParticle starting position must not have a null world.");

        this.fakeEntityID = NMS.getNewEntityID(); // Steal right from under NMS's nose :)
        this.fakeEntityUUID = UUID.randomUUID();
        this.world = startingPosition.getWorld(); // doing this to shut up possible-NPE warnings.
        this.position = new Location(
                startingPosition.getWorld(),
                startingPosition.getX(),
                startingPosition.getY(),
                startingPosition.getZ(),
                startingPosition.getYaw(),
                startingPosition.getPitch());
        this.velocity = new Vector(initialVelocity.getX(), initialVelocity.getY(), initialVelocity.getZ());

        this.range = range > 0 ? range : 1;
        this.age = lifespan > 0 ? lifespan : 1; // Ensure the lifespan is actually long enough.
        this.isSpawned = false;

        this.text = text == null || text.length() == 0 ? "null text :D" : text;
    }


    /**
     * Spawns the particle to all players within the particle's range.
     * @return true if the particle was spawned.
     */
    public boolean spawn() {
        if (!isSpawned) {

            for(Player player: world.getPlayers()) {
                if(withinRangeCheck(player)) sendParticleToPlayer(player); // Send to players within range.
            }

            return true;
        }
        return false;
    }

    /**
     * Spawns a text particle on the player's client. If the particle hasn't
     * already been spawned to all nearby viewers, it is spawned.
     * @param player the player
     * @return true if the particle is spawned (false if it's already been spawned)
     */
    public boolean spawnToPlayer(Player player) {
        if(!isSpawned) spawn(); // Spawn to normal players if not visible.
        return sendParticleToPlayer(player); // This checks if it's already been spawned. Don't worry.
    }

    /**
     * Spawns the TextParticle fake armourstand on the client
     * if it has not already been spawned.
     * @param player the client to send the TextParticle to.
     * @return true if the particle has been sent.
     */
    private boolean sendParticleToPlayer(Player player) {
        if(!visibleToPlayers.contains(player)) {

            // Spawn it!

            visibleToPlayers.add(player);
            return true;
        }
        return false;
    }

    /**
     * Checks if a player is within the visibility range of the particle.
     * @param player the player to check.
     * @return true if the player is within range.
     */
    private boolean withinRangeCheck(Player player) {

        if (player.getWorld().equals(world)) {
            double distance = player.getLocation().distance(position);
            return (distance <= range);
        } else {
            return false;
        }
    }



    /** @return 3 ints using the protocol's velocity format. */
    public static int[] calculateNetworkVelocity(Vector motion) {
        double dX = MathHelper.a(motion.getX(), -3.9D, 3.9D) * 8000D; // I think this is a clamping function + multiply?
        double dY = MathHelper.a(motion.getY(), -3.9D, 3.9D) * 8000D;
        double dZ = MathHelper.a(motion.getZ(), -3.9D, 3.9D) * 8000D;
        return new int[]{ (int)dX, (int)dY, (int)dZ };
    }
}
