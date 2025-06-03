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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import me.verifbuild.VerifBuild;
import me.verifbuild.particles.ParticleRenderer;
import me.verifbuild.util.ItemUtils;
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
    private final Player placer; // jugador que colocó el bloque verificador
    private BukkitTask expirationTask;

    
    public VerificationArea(VerifBuild plugin, UUID id, TriggerBlock triggerBlock, Location triggerLocation, Player placer) {
        this.plugin = plugin;
        this.id = id;
        this.triggerBlock = triggerBlock;
        this.triggerLocation = triggerLocation.clone();
        this.placer = placer;

        // Usar dimensiones personalizadas
        int halfX = triggerBlock.getAreaX() / 2;
        int halfY = triggerBlock.getAreaY() / 2;
        int halfZ = triggerBlock.getAreaZ() / 2;
        this.minLocation = triggerLocation.clone().add(-halfX, 0, -halfZ);
        this.maxLocation = triggerLocation.clone().add(halfX, triggerBlock.getAreaY(), halfZ);

        this.particleRenderer = new ParticleRenderer(plugin, minLocation, maxLocation);
    }
    
    public void startVerification() {
        sendMessageToNearbyPlayers(plugin.getConfigManager().getMessage("activated"));
        particleRenderer.startRendering();
    
        // Verificación inmediata
        Bukkit.getScheduler().runTask(plugin, this::verifyStructure);
    
        // Verificar estructura cada X ticks
        int interval = plugin.getConfigManager().getVerificationIntervalTicks();
        verificationTask = Bukkit.getScheduler().runTaskTimer(plugin, this::verifyStructure, interval, interval);
    
        // Cancelación automática si no se completa
        int verificationTimeTicks = triggerBlock.getVerificationTimeSeconds() * 20;
        expirationTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!completed) {
                completed = true;
                stopVerification();
    
                if (placer != null && placer.isOnline()) {
                    placer.sendMessage(plugin.getConfigManager().getMessage("expired"));
    
                    switch (placer.getGameMode()) {
                        case SURVIVAL:
                        case ADVENTURE:
                            ItemStack refundItem = ItemUtils.createVerifierItem(plugin, triggerBlock);
                            placer.getInventory().addItem(refundItem);
                            break;
                        default:
                            break;
                    }
                }
    
                plugin.getVerifierManager().removeVerification(id);
            }
        }, verificationTimeTicks);
    }
    
    public void stopVerification() {
        if (particleRenderer != null) {
            particleRenderer.stopRendering();
        }
    
        if (verificationTask != null) {
            verificationTask.cancel();
            verificationTask = null;
        }
    
        if (expirationTask != null) {
            expirationTask.cancel();
            expirationTask = null;
        }
    
        if (!completed && triggerLocation.getBlock().getType() == triggerBlock.getMaterial()) {
            triggerLocation.getBlock().setType(Material.AIR);
        }
    
        if (!completed) {
            sendMessageToNearbyPlayers(plugin.getConfigManager().getMessage("deactivated"));
        }
    }
    
    
    public void verifyStructure() {
        if (completed) return;
    
        if (!plugin.getVerifierManager().isValidTrigger(triggerLocation, triggerBlock.getId())) {
            if (!completed) {
                completed = true; // Marcar como completado para que no se repita
                plugin.getLogger().warning("[VerifBuild] Activación ignorada: bloque no fue colocado por /vb give.");
                stopVerification(); // Detener partículas y tarea repetitiva
            }
            return;
        }
    
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
        
            Bukkit.getScheduler().runTask(plugin, () -> {
                triggerBlock.useOnce(); //Este es el lugar correcto
        
                executeSuccessCommands();
                sendMessageToNearbyPlayers(plugin.getConfigManager().getMessage("completed"));
                stopVerification();
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

    public TriggerBlock getTriggerBlock() {
        return triggerBlock;
    }
    /**
     * Devuelve el jugador que colocó el bloque verificador.
     */
    public Player getPlacer() {
        return placer;
    }
    public int getCurrentBlockCount() {
        Map<Material, Integer> current = new HashMap<>();
    
        for (int x = minLocation.getBlockX(); x <= maxLocation.getBlockX(); x++) {
            for (int y = minLocation.getBlockY(); y <= maxLocation.getBlockY(); y++) {
                for (int z = minLocation.getBlockZ(); z <= maxLocation.getBlockZ(); z++) {
                    Block block = minLocation.getWorld().getBlockAt(x, y, z);
                    Material material = block.getType();
                    if (material != Material.AIR && material != triggerBlock.getMaterial()) {
                        current.merge(material, 1, Integer::sum);
                    }
                }
            }
        }
    
        return current.values().stream().mapToInt(i -> i).sum();
    }
    public double getProgressPercentage() {
        Map<Material, Integer> actualCounts = new HashMap<>();
    
        for (int x = minLocation.getBlockX(); x <= maxLocation.getBlockX(); x++) {
            for (int y = minLocation.getBlockY(); y <= maxLocation.getBlockY(); y++) {
                for (int z = minLocation.getBlockZ(); z <= maxLocation.getBlockZ(); z++) {
                    Block block = minLocation.getWorld().getBlockAt(x, y, z);
                    Material material = block.getType();
                    if (material != Material.AIR && material != triggerBlock.getMaterial()) {
                        actualCounts.merge(material, 1, Integer::sum);
                    }
                }
            }
        }
    
        Map<Material, Integer> required = triggerBlock.getRequirement().getRequiredBlocks();
        int totalRequired = required.values().stream().mapToInt(Integer::intValue).sum();
        int placed = 0;
    
        for (Map.Entry<Material, Integer> entry : required.entrySet()) {
            placed += Math.min(actualCounts.getOrDefault(entry.getKey(), 0), entry.getValue());
        }
    
        return totalRequired == 0 ? 0 : (placed * 100.0) / totalRequired;
    }
}