package me.verifbuild.verification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import me.verifbuild.VerifBuild;
import me.verifbuild.particles.ParticleRenderer;
import me.verifbuild.util.LocationUtils;

public class VerificationArea {
    
    private final VerifBuild plugin;
    private final UUID id;
    private final TriggerBlock triggerBlock;
    private final Location triggerLocation;
    private final Location minLocation;
    private final Location maxLocation;
    private final ParticleRenderer particleRenderer;
    private BukkitTask verificationTask;
    private boolean completed = false;
    
    public VerificationArea(VerifBuild plugin, UUID id, TriggerBlock triggerBlock, Location triggerLocation) {
        this.plugin = plugin;
        this.id = id;
        this.triggerBlock = triggerBlock;
        this.triggerLocation = triggerLocation.clone();
        
        int radius = triggerBlock.getAreaSize() / 2;
        this.minLocation = triggerLocation.clone().add(-radius, 0, -radius);
        this.maxLocation = triggerLocation.clone().add(radius, triggerBlock.getAreaSize(), radius);
        
        this.particleRenderer = new ParticleRenderer(plugin, minLocation, maxLocation);
    }
    
    public void startVerification() {
        sendMessageToNearbyPlayers(plugin.getConfigManager().getMessage("activated"));
        particleRenderer.startRendering();
        verificationTask = Bukkit.getScheduler().runTaskTimer(plugin, this::verifyStructure, 10L, 10L);
    }
    
    public void stopVerification() {
        if (particleRenderer != null) {
            particleRenderer.stopRendering();
        }
        
        if (verificationTask != null) {
            verificationTask.cancel();
            verificationTask = null;
        }
        
        if (triggerLocation.getBlock().getType() == triggerBlock.getMaterial()) {
            triggerLocation.getBlock().setType(Material.AIR);
        }
        
        if (!completed) {
            sendMessageToNearbyPlayers(plugin.getConfigManager().getMessage("deactivated"));
        }
    }
    
    public void verifyStructure() {
        if (completed) return;
        
        Map<Material, Integer> blockCounts = new HashMap<>();
        
        for (int x = minLocation.getBlockX(); x <= maxLocation.getBlockX(); x++) {
            for (int y = minLocation.getBlockY(); y <= maxLocation.getBlockY(); y++) {
                for (int z = minLocation.getBlockZ(); z <= maxLocation.getBlockZ(); z++) {
                    Block block = minLocation.getWorld().getBlockAt(x, y, z);
                    Material material = block.getType();
                    
                    if (material != Material.AIR && material != triggerBlock.getMaterial()) {
                        blockCounts.merge(material, 1, Integer::sum);
                    }
                }
            }
        }
        
        if (triggerBlock.getRequirement().isSatisfiedBy(blockCounts)) {
            completed = true;
            
            // Ejecutar todo en el siguiente tick síncrono
            Bukkit.getScheduler().runTask(plugin, () -> {
                // Ejecutar comandos primero
                executeSuccessCommands();
                
                // Enviar mensaje de completado
                sendMessageToNearbyPlayers(plugin.getConfigManager().getMessage("completed"));
                
                // Detener verificación y partículas
                stopVerification();
                
                // Notificar al manager
                plugin.getVerifierManager().completeVerification(id);
            });
        }
    }
    
    public void executeSuccessCommands() {
        Player nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;
    
        for (Player player : triggerLocation.getWorld().getPlayers()) {
            double distance = player.getLocation().distance(triggerLocation);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestPlayer = player;
            }
        }
    
        if (nearestPlayer == null) {
            plugin.getLogger().info("[VerifBuild] No se encontró ningún jugador cercano para ejecutar comandos.");
            return;
        }
    
        plugin.getLogger().info("[VerifBuild] Jugador más cercano: " + nearestPlayer.getName());
    
        final Player targetPlayer = nearestPlayer;
        List<String> commands = triggerBlock.getSuccessCommands();
    
        if (commands == null || commands.isEmpty()) {
            plugin.getLogger().info("[VerifBuild] No hay comandos configurados en este TriggerBlock.");
            return;
        }
    
        plugin.getLogger().info("[VerifBuild] Comandos a ejecutar: " + commands);
    
        Location commandLocation = triggerLocation.clone().add(
                triggerBlock.getRelativeX(),
                triggerBlock.getRelativeY(),
                triggerBlock.getRelativeZ()
        );
    
        for (int i = 0; i < commands.size(); i++) {
            final String command = commands.get(i);
            final long delay = i * 2L;
    
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                String processedCommand = command
                        .replace("%x%", String.valueOf(commandLocation.getBlockX()))
                        .replace("%y%", String.valueOf(commandLocation.getBlockY()))
                        .replace("%z%", String.valueOf(commandLocation.getBlockZ()))
                        .replace("%player%", targetPlayer.getName());
    
                plugin.getLogger().info("[VerifBuild] Ejecutando comando procesado: " + processedCommand);
    
                try {
                    boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                    if (!success) {
                        plugin.getLogger().warning("[VerifBuild] El comando no se pudo ejecutar: " + processedCommand);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("[VerifBuild] Error ejecutando comando: " + processedCommand);
                    plugin.getLogger().warning("Excepción: " + e.getMessage());
                }
            }, delay);
        }
    }
    
    
    public boolean isTriggerBlock(Location location) {
        return LocationUtils.isSameBlock(triggerLocation, location);
    }
    
    public boolean isWithinArea(Location location) {
        return location.getWorld().equals(minLocation.getWorld()) &&
                location.getBlockX() >= minLocation.getBlockX() && location.getBlockX() <= maxLocation.getBlockX() &&
                location.getBlockY() >= minLocation.getBlockY() && location.getBlockY() <= maxLocation.getBlockY() &&
                location.getBlockZ() >= minLocation.getBlockZ() && location.getBlockZ() <= maxLocation.getBlockZ();
    }
    
    private void sendMessageToNearbyPlayers(String message) {
        double radius = triggerBlock.getAreaSize() * 2;
        for (Player player : triggerLocation.getWorld().getPlayers()) {
            if (player.getLocation().distance(triggerLocation) <= radius) {
                player.sendMessage(message);
            }
        }
    }
    
    public UUID getId() {
        return id;
    }
    
    public Location getTriggerLocation() {
        return triggerLocation.clone();
    }
    
    public boolean isCompleted() {
        return completed;
    }
}