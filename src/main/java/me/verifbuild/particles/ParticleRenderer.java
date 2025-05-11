package me.verifbuild.particles;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import me.verifbuild.VerifBuild;

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
        
        particleTask = plugin.getServer().getScheduler().runTaskTimer(
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
        Particle.DustOptions greenDust = new Particle.DustOptions(org.bukkit.Color.fromRGB(0, 255, 0), 1.5F);
        double step = 0.5;
    
        // Líneas verticales (Y)
        for (int x : new int[]{minX, maxX}) {
            for (int z : new int[]{minZ, maxZ}) {
                for (double y = minY; y <= maxY; y += step) {
                    world.spawnParticle(Particle.REDSTONE, x + 0.5, y + 0.5, z + 0.5, 1, 0, 0, 0, 0, greenDust);
                }
            }
        }
    
        // Líneas horizontales (X) en la base y en la parte superior
        for (int y : new int[]{minY, maxY}) {
            for (int z : new int[]{minZ, maxZ}) {
                for (double x = minX; x <= maxX; x += step) {
                    world.spawnParticle(Particle.REDSTONE, x + 0.5, y + 0.5, z + 0.5, 1, 0, 0, 0, 0, greenDust);
                }
            }
        }
    
        // Líneas horizontales (Z) en la base y en la parte superior
        for (int y : new int[]{minY, maxY}) {
            for (int x : new int[]{minX, maxX}) {
                for (double z = minZ; z <= maxZ; z += step) {
                    world.spawnParticle(Particle.REDSTONE, x + 0.5, y + 0.5, z + 0.5, 1, 0, 0, 0, 0, greenDust);
                }
            }
        }
    }
           
}