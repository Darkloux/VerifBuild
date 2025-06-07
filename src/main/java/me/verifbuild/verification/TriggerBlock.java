package me.verifbuild.verification;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

public class TriggerBlock {
    
    private final String id;
    private final Material material;
    private final int areaX;
    private final int areaY;
    private final int areaZ;
    private final StructureRequirement requirement;
    private final List<String> successCommands;
    private final List<String> interactCommands;
    private final int relativeX;
    private final int relativeY;
    private final int relativeZ;
    private final int maxUses;
    private final int verificationTimeSeconds;
    private int remainingUses;
    private final Material executorMaterial;
    
    /**
     * Creates a new trigger block configuration.
     *
     * @param id             The identifier for this trigger block
     * @param material       The material that triggers the verification
     * @param areaX          The size of the verification area in the X dimension
     * @param areaY          The size of the verification area in the Y dimension
     * @param areaZ          The size of the verification area in the Z dimension
     * @param requirement    The structure requirements
     * @param successCommands Commands to execute on success
     * @param relativeX      Relative X coordinate for command execution
     * @param relativeY      Relative Y coordinate for command execution
     * @param relativeZ      Relative Z coordinate for command execution
     * @param maxUses        Maximum number of times this trigger can be used (-1 for unlimited)
     * @param verificationTimeSeconds Time in seconds for verification
     */
    public TriggerBlock(String id, Material material, int areaX, int areaY, int areaZ, StructureRequirement requirement,
                        List<String> successCommands, int relativeX, int relativeY, int relativeZ, int maxUses, int verificationTimeSeconds,
                        List<String> interactCommands, Material executorMaterial) {
        this.id = id;
        this.material = material;
        this.areaX = areaX;
        this.areaY = areaY;
        this.areaZ = areaZ;
        this.requirement = requirement;
        this.successCommands = new ArrayList<>(successCommands);
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.relativeZ = relativeZ;
        this.maxUses = maxUses;
        this.remainingUses = maxUses;
        this.verificationTimeSeconds = verificationTimeSeconds;
        this.interactCommands = interactCommands != null ? new ArrayList<>(interactCommands) : new ArrayList<>();
        this.executorMaterial = executorMaterial != null ? executorMaterial : material;
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
     * Gets the size of the verification area in the X dimension.
     *
     * @return The area size in X
     */
    public int getAreaX() {
        return areaX;
    }
    
    /**
     * Gets the size of the verification area in the Y dimension.
     *
     * @return The area size in Y
     */
    public int getAreaY() {
        return areaY;
    }
    
    /**
     * Gets the size of the verification area in the Z dimension.
     *
     * @return The area size in Z
     */
    public int getAreaZ() {
        return areaZ;
    }
    
    /**
     * Gets the size of the verification area.
     *
     * @return The area size
     */
    public int getAreaSize() {
        // Para retrocompatibilidad, devuelve el mayor de los tres ejes
        return Math.max(Math.max(areaX, areaY), areaZ);
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
     * Gets the commands to execute on interact.
     *
     * @return A list of interact commands
     */
    public List<String> getInteractCommands() {
        return new ArrayList<>(interactCommands);
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
    
    /**
     * Gets the time in seconds for verification.
     *
     * @return The verification time in seconds
     */
    public int getVerificationTimeSeconds() {
        return verificationTimeSeconds;
    }
    
    /**
     * Gets the material of the executor block.
     *
     * @return The executor block material
     */
    public Material getExecutorMaterial() {
        return executorMaterial;
    }
}