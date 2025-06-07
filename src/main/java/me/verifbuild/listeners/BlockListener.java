package me.verifbuild.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.verifbuild.VerifBuild;
import me.verifbuild.verification.TriggerBlock;
import me.verifbuild.verification.VerificationArea;

public class BlockListener implements Listener {
    
    private final VerifBuild plugin;

    public BlockListener(VerifBuild plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles block placement events to detect trigger blocks.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
    
        Block block = event.getBlock();
        Material material = block.getType();
        Player player = event.getPlayer();
    
        ItemStack item = event.getItemInHand();
        if (!item.hasItemMeta()) return;
    
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "verif_block");
    
        // Validar que el bloque tenga el tag verificador
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            plugin.getLogger().warning("[VerifBuild] Colocación cancelada: bloque sin NBT de verificador.");
            player.sendMessage("§cEste bloque no es un verificador válido.");
            event.setCancelled(true);
            return;
        }
    
        // Obtener el ID del verificador desde el NBT
        String triggerId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        TriggerBlock triggerBlock = plugin.getConfigManager().getTriggerBlocks().get(triggerId);
    
        if (triggerBlock == null) {
            player.sendMessage("§cEste tipo de verificador no es válido o no está configurado.");
            event.setCancelled(true);
            return;
        }
    
        // Verificar permisos
        if (!player.hasPermission("verifbuild.use." + triggerBlock.getId()) &&
            !player.hasPermission("verifbuild.use.*")) {
            player.sendMessage("§cNo tienes permiso para usar este tipo de verificador.");
            event.setCancelled(true);
            return;
        }
    
        // Registrar como ubicación válida
        plugin.getVerifierManager().registerValidTrigger(block.getLocation(), triggerId);
        plugin.getLogger().info("[VerifBuild] Bloque verificador colocado con ID: " + triggerId);
    
        // Crear la verificación
        boolean created = plugin.getVerifierManager().createVerificationArea(triggerBlock, block.getLocation(), player);
        if (!created) {
            player.sendMessage("§cEste verificador alcanzó su número máximo de usos.");
            event.setCancelled(true);
        }
    }
    
    /**
     * Handles block break events to detect trigger blocks being removed.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        // Check if this is a trigger block for an active verification
        VerificationArea area = plugin.getVerifierManager().getVerificationByTriggerLocation(block.getLocation());
        if (area != null) {
            // Si la estructura ya fue verificada, permitir romper normalmente
            if (area.isCompleted()) {
                // Permitir romper el bloque normalmente
                return;
            }
            // Si NO está verificada, solo cancelar si tiene permiso de cancelación
            if (!player.hasPermission("verifbuild.cancel." + block.getType().name()) && 
                !player.hasPermission("verifbuild.cancel.*")) {
                player.sendMessage("§cYou don't have permission to cancel this verification.");
                event.setCancelled(true);
                return;
            }
            // Cancelar el drop normal y dar solo el verificador
            event.setDropItems(false);
            switch (player.getGameMode()) {
                case SURVIVAL:
                case ADVENTURE:
                    player.getInventory().addItem(me.verifbuild.util.ItemUtils.createVerifierItem(plugin, area.getTriggerBlock()));
                    break;
                default:
                    break;
            }
            // Remover el área
            plugin.getVerifierManager().removeVerification(area.getId());
        }
    }
    
    /**
     * Maneja la interacción de clic derecho sobre el executor block (solo si NO hay verificación activa en ese bloque).
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Solo main hand para evitar doble ejecución
        if (event.getHand() != null && event.getHand() != EquipmentSlot.HAND) return;
        if (event.useInteractedBlock() == org.bukkit.event.Event.Result.DENY) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        Player player = event.getPlayer();
        // Si hay una verificación activa en ese bloque, NO ejecutar comandos de interacción
        VerificationArea area = plugin.getVerifierManager().getVerificationByTriggerLocation(block.getLocation());
        if (area != null && !area.isCompleted()) {
            // Está en proceso de verificación, no ejecutar nada
            return;
        }
        // Buscar si el bloque es un executor block de alguna estructura
        for (TriggerBlock triggerBlock : plugin.getConfigManager().getTriggerBlocks().values()) {
            if (block.getType() == triggerBlock.getExecutorMaterial()) {
                // Ejecutar comandos de interacción si existen
                if (triggerBlock.getInteractCommands() != null && !triggerBlock.getInteractCommands().isEmpty()) {
                    for (String command : triggerBlock.getInteractCommands()) {
                        String processedCommand = command.replace("%player%", player.getName())
                                .replace("%x%", String.valueOf(block.getX()))
                                .replace("%y%", String.valueOf(block.getY()))
                                .replace("%z%", String.valueOf(block.getZ()));
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCommand);
                    }
                    // Mensaje eliminado para no estorbar la salida de comandos
                }
                event.setCancelled(true);
                return;
            }
        }
    }
}