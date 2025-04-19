package me.verifbuild.verification;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class StructureRequirement {
    
    private final Map<Material, Integer> requiredBlocks;
    
    /**
     * Creates a new structure requirement.
     *
     * @param requiredBlocks A map of materials to required quantities
     */
    public StructureRequirement(Map<Material, Integer> requiredBlocks) {
        this.requiredBlocks = new HashMap<>(requiredBlocks);
    }
    
    /**
     * Checks if a set of blocks satisfies this requirement.
     *
     * @param blocks A map of materials to their quantities
     * @return True if the blocks satisfy the requirement
     */
    public boolean isSatisfiedBy(Map<Material, Integer> blocks) {
        // Check that all required materials are present in sufficient quantities
        for (Map.Entry<Material, Integer> entry : requiredBlocks.entrySet()) {
            Material material = entry.getKey();
            int requiredAmount = entry.getValue();
            int actualAmount = blocks.getOrDefault(material, 0);
            
            if (actualAmount < requiredAmount) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Gets the map of required blocks.
     *
     * @return A map of materials to required quantities
     */
    public Map<Material, Integer> getRequiredBlocks() {
        return new HashMap<>(requiredBlocks);
    }
    
    /**
     * Gets a string representation of the block requirements.
     *
     * @return A string listing all required blocks and quantities
     */
    public String getRequirementsString() {
        StringBuilder builder = new StringBuilder();
        
        for (Map.Entry<Material, Integer> entry : requiredBlocks.entrySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(entry.getValue()).append("x ").append(entry.getKey().name());
        }
        
        return builder.toString();
    }
}