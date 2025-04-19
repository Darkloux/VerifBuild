package me.verifbuild.listeners;

import me.verifbuild.VerifBuild;
import me.verifbuild.verification.TriggerBlock;
import me.verifbuild.verification.VerificationArea;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

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
        
        // Check if this is a trigger block
        TriggerBlock triggerBlock = plugin.getConfigManager().getTriggerBlockByMaterial(material);
        if (triggerBlock != null) {
            // Check if the player has permission
            if (!player.hasPermission("verifbuild.use." + triggerBlock.getId()) && 
                !player.hasPermission("verifbuild.use.*")) {
                player.sendMessage("§cYou don't have permission to use this verification type.");
                event.setCancelled(true);
                return;
            }
            
            // Create a new verification area
            boolean created = plugin.getVerifierManager().createVerificationArea(triggerBlock, block.getLocation());
            if (!created) {
                player.sendMessage("§cThis verification type has reached its maximum uses.");
                event.setCancelled(true);
            }
        } else {
            // Check if the block is in an existing verification area
            VerificationArea area = plugin.getVerifierManager().getVerificationContainingBlock(block);
            if (area != null) {
                // Trigger a verification check (will happen automatically in the next scheduled check)
            }
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
            // Check if the player has permission to cancel verifications
            if (!player.hasPermission("verifbuild.cancel." + block.getType().name()) && 
                !player.hasPermission("verifbuild.cancel.*")) {
                player.sendMessage("§cYou don't have permission to cancel this verification.");
                event.setCancelled(true);
                return;
            }
            
            // Remove the verification area
            plugin.getVerifierManager().removeVerification(area.getId());
        } else {
            // Check if the block is in an existing verification area
            area = plugin.getVerifierManager().getVerificationContainingBlock(block);
            if (area != null) {
                // Trigger a verification check (will happen automatically in the next scheduled check)
            }
        }
    }
}