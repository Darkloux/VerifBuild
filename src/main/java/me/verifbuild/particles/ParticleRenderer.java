package me.verifbuild.particles;

import me.verifbuild.VerifBuild;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

public class ParticleRenderer {
    
    private final VerifBuild plugin;
    private final Location minLocation;
    private final Location maxLocation;
    private BukkitTask particleTask;
    
    /**
     * Creates a new particle renderer for a verification area.
     *
     * @param plugin      The plugin instance
     * @param minLocation The minimum corner of the area
     * @param maxLocation The maximum corner of the area
     */
    public ParticleRenderer(VerifBuild plugin, Location minLocation, Location maxLocation) {
        this.plugin = plugin;
        this.minLocation = minLocation;
        this.maxLocation = maxLocation;
    }
    
    /**
     * Starts rendering particles for the verification area.
     */
    public void startRendering() {
        stopRendering(); // Ensure no duplicate tasks
        
        particleTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin, this::renderParticles, 0L, 10L);
    }
    
    /**
     * Stops rendering particles.
     */
    public void stopRendering() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
    }
    
    /**
     * Renders particles around the boundary of the verification area.
     */
    private void renderParticles() {
        World world = minLocation.getWorld();
        
        // Get the dimensions of the area
        int minX = minLocation.getBlockX();
        int minY = minLocation.getBlockY();
        int minZ = minLocation.getBlockZ();
        int maxX = maxLocation.getBlockX();
        int maxY = maxLocation.getBlockY();
        int maxZ = maxLocation.getBlockZ();
        
        // Only show vertices and edges to reduce particle count
        renderEdges(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    /**
     * Renders particles along the edges of the verification area.
     */
    private void renderEdges(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        // Calculate the number of particles along each edge based on length
        int xLength = maxX - minX + 1;
        int yLength = maxY - minY + 1;
        int zLength = maxZ - minZ + 1;
        
        // Create particles along corners and edges
        for (int i = 0; i <= 1; i++) {
            for (int j = 0; j <= 1; j++) {
                // X-direction edges
                for (int x = 0; x <= xLength; x += xLength / 4) {
                    if (x > 0 && x < xLength) continue; // Skip middle points
                    int actualX = minX + (x == xLength ? xLength - 1 : x);
                    int actualY = i == 0 ? minY : maxY;
                    int actualZ = j == 0 ? minZ : maxZ;
                    world.spawnParticle(Particle.CRIT_MAGIC, actualX + 0.5, actualY + 0.5, actualZ + 0.5, 1, 0, 0, 0, 0);
                }
                
                // Z-direction edges
                for (int z = 0; z <= zLength; z += zLength / 4) {
                    if (z > 0 && z < zLength) continue; // Skip middle points
                    int actualX = i == 0 ? minX : maxX;
                    int actualY = j == 0 ? minY : maxY;
                    int actualZ = minZ + (z == zLength ? zLength - 1 : z);
                    world.spawnParticle(Particle.CRIT_MAGIC, actualX + 0.5, actualY + 0.5, actualZ + 0.5, 1, 0, 0, 0, 0);
                }
            }
            
            // Y-direction edges
            for (int y = 0; y <= yLength; y += yLength / 4) {
                if (y > 0 && y < yLength) continue; // Skip middle points
                int actualX = i == 0 ? minX : maxX;
                int actualY = minY + (y == yLength ? yLength - 1 : y);
                int actualZ = i == 0 ? minZ : maxZ;
                world.spawnParticle(Particle.CRIT_MAGIC, actualX + 0.5, actualY + 0.5, actualZ + 0.5, 1, 0, 0, 0, 0);
            }
        }
    }
}