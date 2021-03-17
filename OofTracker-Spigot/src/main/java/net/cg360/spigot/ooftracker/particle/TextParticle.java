package net.cg360.spigot.ooftracker.particle;

import net.cg360.spigot.ooftracker.OofTracker;
import net.cg360.spigot.ooftracker.nms.NMS;
import net.cg360.spigot.ooftracker.nms.RawTextBuilder;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

// This could probably get a cross-platform utility class in NSAPI Commons.
public class TextParticle {

    protected ArrayList<Player> visibleToPlayers;

    protected int fakeEntityID;
    protected UUID fakeEntityUUID;
    protected World world;
    protected Location lastPosition;
    protected Location position;

    protected Vector velocity;
    protected Vector acceleration;
    protected Vector terminalVelocity;

    protected int age;
    protected int range;
    protected boolean isSpawned;

    protected String parsedText;


    // Unlike LivingEntityHealthBar, there's less control over viewers. They are only despawned on a dimension transfer.
    public TextParticle(RawTextBuilder text, Location startingPosition, Vector initialVelocity, Vector acceleration, Vector terminalVelocity, int lifespan, int range) {
        this.visibleToPlayers = new ArrayList<>();

        if(startingPosition == null) throw new IllegalArgumentException("Starting position must not be null.");
        if(startingPosition.getWorld() == null) throw new IllegalArgumentException("TextParticle starting position must not have a null world.");

        this.fakeEntityID = NMS.getNewEntityID(); // Steal right from under NMS's nose :)
        this.fakeEntityUUID = UUID.randomUUID();
        this.world = startingPosition.getWorld(); // doing this to shut up possible-NPE warnings.
        this.lastPosition = new Location(
                startingPosition.getWorld(),
                startingPosition.getX(),
                startingPosition.getY(),
                startingPosition.getZ(),
                startingPosition.getYaw(),
                startingPosition.getPitch());
        this.position = new Location(
                startingPosition.getWorld(),
                startingPosition.getX(),
                startingPosition.getY(),
                startingPosition.getZ(),
                startingPosition.getYaw(),
                startingPosition.getPitch());
        this.velocity = initialVelocity == null ?
                new Vector(0d, 1.5d, 0d) :
                new Vector(initialVelocity.getX(), initialVelocity.getY(), initialVelocity.getZ());
        this.acceleration = acceleration == null ?
                new Vector(0d, -0.1d, 0d) :
                new Vector(acceleration.getX(), acceleration.getY(), acceleration.getZ());
        this.terminalVelocity = terminalVelocity == null ?
                new Vector(2d, 2d, 2d) :
                new Vector(Math.abs(terminalVelocity.getX()), Math.abs(terminalVelocity.getX()), Math.abs(terminalVelocity.getX()));

        this.range = range > 0 ? range : 1;
        this.age = lifespan > 0 ? lifespan : 1; // Ensure the lifespan is actually long enough.
        this.isSpawned = false;

        this.parsedText = (text == null ? new RawTextBuilder("null text :D") : text).toString();
    }


    /**
     * Spawns the particle to all players within the particle's range.
     * @return true if the particle was spawned.
     */
    public boolean spawn() {
        if ((!isSpawned) && (age > 0)) {

            for(Player player: world.getPlayers()) {
                if(withinRangeCheck(player)) sendParticleToPlayer(player); // Send to players within range.
            }

            new BukkitRunnable() {

                @Override
                public void run() {

                    if(OofTracker.isRunning()){
                        age--;

                        if(age < 0) {
                            kill(); // Ensure it's dead
                            this.cancel();
                            return;
                        }
                        physicsTick();
                        updateMotionForViewers();
                        return;
                    }
                    this.cancel();
                }

            }.runTaskTimer(OofTracker.get(), 1, 1);

            TextParticleManager.get().addParticle(this);
            return true;
        }
        return false;
    }

    /**
     * Kills the particle, preventing it from spawning again.
     */
    public void kill() {

        if(isSpawned) {
            TextParticleManager.get().removeParticle(this);
            this.isSpawned = false;
            this.age = -1;

            for(Player player: new ArrayList<>(visibleToPlayers)) {
                despawnFromPlayer(player);
            }
        }
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
     * Removes the client-side TextParticle for the player.
     * @param player the client to have the particle removed from.
     * @return true if the particle was removed.
     */
    public boolean despawnFromPlayer(Player player) {

        if(visibleToPlayers.remove(player)) {
            PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(fakeEntityID);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(destroyPacket);
            return true;
        }
        return false;
    }



    /**
     * Ran every tick to update the particle's position and velocity.
     */
    protected void physicsTick() {
        velocity.add(acceleration);

        // Ensure velocity is not at terminal velocity.
        velocity.setX(Math.min(Math.max(velocity.getX(), -terminalVelocity.getX()), terminalVelocity.getX()));
        velocity.setY(Math.min(Math.max(velocity.getY(), -terminalVelocity.getY()), terminalVelocity.getY()));
        velocity.setZ(Math.min(Math.max(velocity.getZ(), -terminalVelocity.getZ()), terminalVelocity.getZ()));

        lastPosition = new Location(position.getWorld(), position.getX(), position.getY(), position.getZ(), position.getYaw(), position.getPitch());
        position.add(velocity);
    }

    /**
     * Updates the location of the client-side TextParticle entity.
     */
    protected void updateMotionForViewers() {
        // World doesn't change for this entity. No need to check.
        for (Player player : visibleToPlayers) {

            try {
                PacketPlayOutEntityVelocity packetVelocity = new PacketPlayOutEntityVelocity(fakeEntityID, new Vec3D(velocity.getX(), velocity.getY(), velocity.getZ()));

                if(lastPosition.distance(position) > 8) { // Protocol insists changes of > 8 must teleport
                    PacketPlayOutEntityTeleport packetTeleport = new PacketPlayOutEntityTeleport();

                    NMS.setClassField(PacketPlayOutEntityTeleport.class, packetTeleport, "a", fakeEntityID); // Entity ID

                    NMS.setClassField(PacketPlayOutEntityTeleport.class, packetTeleport, "b", position.getX()); // Pos X
                    NMS.setClassField(PacketPlayOutEntityTeleport.class, packetTeleport, "c", position.getY()); // Pos Y
                    NMS.setClassField(PacketPlayOutEntityTeleport.class, packetTeleport, "d", position.getZ()); // Pos Z

                    int yaw = (int) (position.getYaw() * 256.0F / 360.0F);
                    int pitch = (int) (position.getPitch() * 256.0F / 360.0F);
                    NMS.setClassField(PacketPlayOutEntityTeleport.class, packetTeleport, "e", (byte) yaw); // Y rot (Yaw)
                    NMS.setClassField(PacketPlayOutEntityTeleport.class, packetTeleport, "f", (byte) pitch); // X rot (Pitch)

                    NMS.setClassField(PacketPlayOutEntityTeleport.class, packetTeleport, "g", false); //On Ground?

                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetTeleport);

                } else {
                    short deltaX = (short) ((position.getX() * 32 - lastPosition.getX() * 32) * 128);
                    short deltaY = (short) ((position.getY() * 32 - lastPosition.getY() * 32) * 128);
                    short deltaZ = (short) ((position.getZ() * 32 - lastPosition.getZ() * 32) * 128);
                    PacketPlayOutEntity.PacketPlayOutRelEntityMove packetMove =
                            new PacketPlayOutEntity.PacketPlayOutRelEntityMove(fakeEntityID, deltaX, deltaY, deltaZ, false); // Love how hidden this is.

                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetMove);
                }

                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetVelocity);

            } catch (NoSuchFieldException err) {
                OofTracker.getLog().severe("Error building packet. - No field! Is this the wrong version?");
                err.printStackTrace();
                return;

            } catch (IllegalAccessException err) {
                OofTracker.getLog().severe("Error building packet - Can't access field! Is something misconfigured?");
                err.printStackTrace();
                return;

            }
        }
    }



    /**
     * Spawns the TextParticle fake armourstand on the client
     * if it has not already been spawned.
     * @param player the client to send the TextParticle to.
     * @return true if the particle has been sent.
     */
    private boolean sendParticleToPlayer(Player player) {
        if(!visibleToPlayers.contains(player)) {
            visibleToPlayers.add(player);

            CraftPlayer cPlayer = (CraftPlayer) player;
            PacketPlayOutSpawnEntityLiving addPacket = new PacketPlayOutSpawnEntityLiving(); // An armour stand is apparently living??

            try {

                // -- ADD ENTITY --

                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "a", fakeEntityID); // Entity ID
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "b", fakeEntityUUID); // Entity UUID
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "c", NMS.NETID_ARMOUR_STAND); // Entity Type

                // Set pos
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "d", position.getX()); // Location X
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "e", position.getY()); // Location Y (+ offset)
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "f", position.getZ()); // Location Z

                // Transform velocity the the network's format
                int[] v = calculateNetworkVelocity(velocity);
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "g", v[0]); // Velocity X
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "h", v[1]); // Location Y
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "i", v[2]); // Location Z

                // Rotation is just 0
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "j", (byte) 0); // Yaw
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "k", (byte) 0); // Pitch
                NMS.setClassField(PacketPlayOutSpawnEntityLiving.class, addPacket, "l", (byte) 0); // Head rotate.



                // -- UPDATE ENTITY META --

                DataWatcher dataWatcher = new DataWatcher(null);


                dataWatcher.register(new DataWatcherObject<>(0, DataWatcherRegistry.a), (byte) 0x20); // Is invisible
                dataWatcher.register(new DataWatcherObject<>(2, DataWatcherRegistry.f), Optional.ofNullable(IChatBaseComponent.ChatSerializer.b(parsedText)) ); // Custom name
                dataWatcher.register(new DataWatcherObject<>(3, DataWatcherRegistry.i), true); // Custom name visible
                dataWatcher.register(new DataWatcherObject<>(14, DataWatcherRegistry.a), (byte) 0x10); // Set Marker

                PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(fakeEntityID, dataWatcher, true);

                cPlayer.getHandle().playerConnection.sendPacket(addPacket);
                cPlayer.getHandle().playerConnection.sendPacket(metaPacket);

            } catch (NoSuchFieldException err) {
                OofTracker.getLog().severe("Error building packet. - No field! Is this the wrong version?");
                err.printStackTrace();
                visibleToPlayers.remove(player); // Remove player as it wasn't displayed.
                return false;

            } catch (IllegalAccessException err) {
                OofTracker.getLog().severe("Error building packet - Can't access field! Is something misconfigured?");
                err.printStackTrace();
                visibleToPlayers.remove(player); // Remove player as it wasn't displayed.
                return false;

            }
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



    public ArrayList<Player> getVisibleToPlayers() { return new ArrayList<>(visibleToPlayers); }
    public int getID() { return fakeEntityID; }
    public int getAge() { return age; }
    public int getRange() { return range; }
    public boolean isSpawned() { return isSpawned; }
    public String getParsedText() { return parsedText; }



    /** @return 3 ints using the protocol's velocity format. */
    public static int[] calculateNetworkVelocity(Vector motion) {
        double dX = MathHelper.a(motion.getX(), -3.9D, 3.9D) * 8000D; // I think this is a clamping function + multiply?
        double dY = MathHelper.a(motion.getY(), -3.9D, 3.9D) * 8000D;
        double dZ = MathHelper.a(motion.getZ(), -3.9D, 3.9D) * 8000D;
        return new int[]{ (int)dX, (int)dY, (int)dZ };
    }
}
