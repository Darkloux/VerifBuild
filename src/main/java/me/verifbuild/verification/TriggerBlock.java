package me.verifbuild.verification;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

public class TriggerBlock {
    
    private final String id;
    private final Material material;
    private final int areaSize;
    private final StructureRequirement requirement;
    private final List<String> successCommands;
    private final int relativeX;
    private final int relativeY;
    private final int relativeZ;
    private final int maxUses;
    private int remainingUses;
    
    /**
     * Creates a new trigger block configuration.
     *
     * @param id             The identifier for this trigger block
     * @param material       The material that triggers the verification
     * @param areaSize       The size of the verification area
     * @param requirement    The structure requirements
     * @param successCommands Commands to execute on success
     * @param relativeX      Relative X coordinate for command execution
     * @param relativeY      Relative Y coordinate for command execution
     * @param relativeZ      Relative Z coordinate for command execution
     * @param maxUses        Maximum number of times this trigger can be used (-1 for unlimited)
     */
    public TriggerBlock(String id, Material material, int areaSize, StructureRequirement requirement,
                        List<String> successCommands, int relativeX, int relativeY, int relativeZ, int maxUses) {
        this.id = id;
        this.material = material;
        this.areaSize = areaSize;
        this.requirement = requirement;
        this.successCommands = new ArrayList<>(successCommands);
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.relativeZ = relativeZ;
        this.maxUses = maxUses;
        this.remainingUses = maxUses;
    }
    
    /**
     * Decrements the remaining uses by one.
     */
    public void useOnce() {
        if (maxUses > 0) {
            remainingUses = Math.max(0, remainingUses - 1);
        }
    }
    
    /**
     * Gets the ID of this trigger block.
     *
     * @return The trigger block ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the material of this trigger block.
     *
     * @return The trigger material
     */
    public Material getMaterial() {
        return material;
    }
    
    /**
     * Gets the size of the verification area.
     *
     * @return The area size
     */
    public int getAreaSize() {
        return areaSize;
    }
    
    /**
     * Gets the structure requirement.
     *
     * @return The structure requirement
     */
    public StructureRequirement getRequirement() {
        return requirement;
    }
    
    /**
     * Gets the commands to execute on success.
     *
     * @return A list of commands
     */
    public List<String> getSuccessCommands() {
        return new ArrayList<>(successCommands);
    }
    
    /**
     * Gets the relative X coordinate for command execution.
     *
     * @return The relative X coordinate
     */
    public int getRelativeX() {
        return relativeX;
    }
    
    /**
     * Gets the relative Y coordinate for command execution.
     *
     * @return The relative Y coordinate
     */
    public int getRelativeY() {
        return relativeY;
    }
    
    /**
     * Gets the relative Z coordinate for command execution.
     *
     * @return The relative Z coordinate
     */
    public int getRelativeZ() {
        return relativeZ;
    }
    
    /**
     * Gets the maximum number of times this trigger can be used.
     *
     * @return The max uses, or -1 if unlimited
     */
    public int getMaxUses() {
        return maxUses;
    }
    
    /**
     * Gets the remaining number of uses.
     *
     * @return The remaining uses, or -1 if unlimited
     */
    public int getRemainingUses() {
        return maxUses > 0 ? remainingUses : -1;
    }
}