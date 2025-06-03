package me.verifbuild.verification;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.verifbuild.VerifBuild;

public class VerifierManager {
    
    private final VerifBuild plugin;
    private final Map<UUID, VerificationArea> activeVerifications = new HashMap<>();
    private final Map<Location, String> validTriggers = new HashMap<>();

    public void registerValidTrigger(Location loc, String id) {
        validTriggers.put(loc.getBlock().getLocation(), id);
    }

    public boolean isValidTrigger(Location loc, String expectedId) {
        return validTriggers.containsKey(loc.getBlock().getLocation()) &&
               validTriggers.get(loc.getBlock().getLocation()).equals(expectedId);
    }

    
    public VerifierManager(VerifBuild plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Creates a new verification area.
     *
     * @param triggerBlock  The trigger block configuration
     * @param triggerLocation The location of the placed trigger block
     * @return True if the verification area was created successfully
     */
    public boolean createVerificationArea(TriggerBlock triggerBlock, Location location, Player placer) {
        // Check if the trigger block has reached its max uses
        if (triggerBlock.getMaxUses() > 0 && triggerBlock.getRemainingUses() <= 0) {
            return false;
        }
        
        // Generate a unique ID for this verification area
        UUID verificationId = UUID.randomUUID();
        
        // Create the verification area
        VerificationArea area = new VerificationArea(
            plugin,
            verificationId,
            triggerBlock,
            location,
            placer
        );
        
        // Store the verification area
        activeVerifications.put(verificationId, area);
        
        // Start the verification process
        area.startVerification();
        
        return true;
    }
    
    /**
     * Completes a verification area and executes success commands.
     *
     * @param verificationId The ID of the verification area
     */
    public void completeVerification(UUID verificationId) {
        VerificationArea area = activeVerifications.get(verificationId);
        if (area != null) {
            removeVerification(verificationId);
        }
    }
    
    /**
     * Removes a verification area.
     *
     * @param verificationId The ID of the verification area
     */
    public void removeVerification(UUID verificationId) {
        VerificationArea area = activeVerifications.remove(verificationId);
        if (area != null) {
            area.stopVerification();
        }
    }
    
    /**
     * Gets a verification area by its trigger block location.
     *
     * @param location The location to check
     * @return The verification area, or null if not found
     */
    public VerificationArea getVerificationByTriggerLocation(Location location) {
        return activeVerifications.values().stream()
                .filter(area -> area.isTriggerBlock(location))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Checks if a block is within any active verification area.
     *
     * @param block The block to check
     * @return The verification area containing the block, or null
     */
    public VerificationArea getVerificationContainingBlock(Block block) {
        return activeVerifications.values().stream()
                .filter(area -> area.isWithinArea(block.getLocation()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Cleans up all active verification areas.
     */
    public void cleanupAllVerifications() {
        for (VerificationArea area : activeVerifications.values()) {
            area.stopVerification();
        }
        activeVerifications.clear();
    }
    
    /**
     * Gets all active verification areas.
     *
     * @return A map of verification IDs to verification areas
     */
    public Map<UUID, VerificationArea> getActiveVerifications() {
        return new HashMap<>(activeVerifications);
    }
    public VerificationArea getActiveVerificationByPlayer(Player player) {
        return activeVerifications.values().stream()
                .filter(a -> a.getTriggerLocation().getWorld().equals(player.getWorld()) &&
                             a.getTriggerLocation().distance(player.getLocation()) <= a.getTriggerBlock().getAreaSize() * 2)
                .findFirst().orElse(null);
    }

    /**
     * Obtiene el área de verificación activa de un jugador (si es el "placer").
     * @param player Jugador a buscar
     * @return VerificationArea si el jugador es el colocador, null si no tiene ninguna
     */
    public VerificationArea getVerificationByPlayer(Player player) {
        if (player == null) return null;
        return activeVerifications.values().stream()
                .filter(area -> area != null && area.getPlacer() != null && area.getPlacer().getUniqueId().equals(player.getUniqueId()))
                .findFirst()
                .orElse(null);
    }
}