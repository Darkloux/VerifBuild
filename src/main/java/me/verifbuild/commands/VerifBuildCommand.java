package me.verifbuild.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.verifbuild.VerifBuild;
import me.verifbuild.verification.TriggerBlock;
import me.verifbuild.verification.VerificationArea;

public class VerifBuildCommand implements CommandExecutor {
    
    private final VerifBuild plugin;
    
    public VerifBuildCommand(VerifBuild plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e=== VerifBuild ===");
            sender.sendMessage("§f/verifbuild list §7- Lista las verificaciones activas");
            sender.sendMessage("§f/verifbuild reload §7- Recarga la configuración");
            if (sender instanceof Player) {
                sender.sendMessage("§f/verifbuild give <tipo> §7- Obtiene un bloque verificador");
            } else {
                sender.sendMessage("§f/verifbuild give <jugador> <tipo> §7- Da un bloque verificador a un jugador");
            }
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "list":
                if (!sender.hasPermission("verifbuild.list")) {
                    sender.sendMessage("§cNo tienes permiso para usar este comando.");
                    return true;
                }
                
                Map<UUID, VerificationArea> activeVerifications = plugin.getVerifierManager().getActiveVerifications();
                sender.sendMessage("§eVerificaciones activas: §f" + activeVerifications.size());
                
                for (VerificationArea area : activeVerifications.values()) {
                    sender.sendMessage(String.format("§7- ID: §f%s §7en §f%d,%d,%d",
                            area.getId().toString().substring(0, 8),
                            area.getTriggerLocation().getBlockX(),
                            area.getTriggerLocation().getBlockY(),
                            area.getTriggerLocation().getBlockZ()));
                }
                return true;
                
            case "reload":
                if (!sender.hasPermission("verifbuild.reload")) {
                    sender.sendMessage("§cNo tienes permiso para usar este comando.");
                    return true;
                }
                
                plugin.getConfigManager().loadConfig();
                sender.sendMessage("§aConfiguración recargada correctamente.");
                return true;
                
            case "give":
                Player targetPlayer;
                String verificationType;
                
                if (sender instanceof Player) {
                    // Comando ejecutado por un jugador
                    if (args.length < 2) {
                        sender.sendMessage("§cUso: /verifbuild give <tipo>");
                        return true;
                    }
                    verificationType = args[1].toLowerCase();
                    targetPlayer = (Player) sender;
                } else {
                    // Comando ejecutado desde la consola
                    if (args.length < 3) {
                        sender.sendMessage("§cUso desde consola: /verifbuild give <jugador> <tipo>");
                        return true;
                    }
                    String playerName = args[1];
                    verificationType = args[2].toLowerCase();
                    targetPlayer = plugin.getServer().getPlayer(playerName);
                    
                    if (targetPlayer == null) {
                        sender.sendMessage("§cJugador '" + playerName + "' no encontrado o no está conectado.");
                        return true;
                    }
                }
                
                Map<String, TriggerBlock> triggerBlocks = plugin.getConfigManager().getTriggerBlocks();
                TriggerBlock triggerBlock = triggerBlocks.get(verificationType);
                
                if (triggerBlock == null) {
                    sender.sendMessage("§cTipo de verificación no encontrado.");
                    return true;
                }
                
                // Verificar permisos solo si el sender es un jugador
                if (sender instanceof Player) {
                    if (!sender.hasPermission("verifbuild.use." + verificationType) && 
                        !sender.hasPermission("verifbuild.use.*")) {
                        sender.sendMessage("§cNo tienes permiso para usar este tipo de verificación.");
                        return true;
                    }
                }
                
                Player player = targetPlayer;
                ItemStack triggerItem = new ItemStack(triggerBlock.getMaterial());
                ItemMeta meta = triggerItem.getItemMeta();
                if (meta != null) {
                    NamespacedKey key = new NamespacedKey(plugin, "verif_block");
                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, triggerBlock.getId());

                    meta.setDisplayName("§6Bloque Verificador: §e" + verificationType);
                    List<String> lore = new ArrayList<>();
                    lore.add("§7Tipo: §f" + verificationType);
                    lore.add("§7Área: §f" + triggerBlock.getAreaX() + "x" + triggerBlock.getAreaY() + "x" + triggerBlock.getAreaZ());
                    lore.add("§7Requisitos:");
                    triggerBlock.getRequirement().getRequiredBlocks().forEach((material, amount) ->
                    lore.add("§7- §f" + amount + "x §e" + material.name())
                );
                meta.setLore(lore);
                triggerItem.setItemMeta(meta);
                }

                
                if (meta != null) {
                    meta.setDisplayName("§6Bloque Verificador: §e" + verificationType);
                    List<String> lore = new ArrayList<>();
                    lore.add("§7Tipo: §f" + verificationType);
                    lore.add("§7Área: §f" + triggerBlock.getAreaX() + "x" + triggerBlock.getAreaY() + "x" + triggerBlock.getAreaZ());
                    lore.add("§7Requisitos:");
                    triggerBlock.getRequirement().getRequiredBlocks().forEach((material, amount) -> 
                        lore.add("§7- §f" + amount + "x §e" + material.name())
                    );
                    meta.setLore(lore);
                    triggerItem.setItemMeta(meta);
                }
                
                player.getInventory().addItem(triggerItem);
                
                if (sender instanceof Player && sender.equals(player)) {
                    player.sendMessage("§aHas recibido un bloque verificador de tipo §e" + verificationType);
                } else {
                    player.sendMessage("§aHas recibido un bloque verificador de tipo §e" + verificationType);
                    sender.sendMessage("§aBloque verificador de tipo §e" + verificationType + " §adado a §f" + player.getName());
                }
                return true;
                
            default:
                sender.sendMessage("§cComando desconocido. Usa /verifbuild para ver la ayuda.");
                return true;
        }
    }
}