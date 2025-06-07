package me.verifbuild.config;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.verifbuild.VerifBuild;
import me.verifbuild.verification.StructureRequirement;
import me.verifbuild.verification.TriggerBlock;


public class ConfigManager {
    
    private final VerifBuild plugin;
    private FileConfiguration config;
    private final Map<String, TriggerBlock> triggerBlockMap = new HashMap<>();
    private final Map<String, String> messages = new HashMap<>();
    private int verificationIntervalTicks = 200; // valor por defecto: 10 segundos
    
    public ConfigManager(VerifBuild plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Loads the configuration from the config.yml file.
     */
    public void loadConfig() {
        // Create default config if it doesn't exist
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Load verifications
        loadVerifications();
        
        // Load messages
        loadMessages();
        
        plugin.getLogger().info("Configuration loaded successfully!");
        int seconds = config.getInt("time_verif", 10);
        verificationIntervalTicks = Math.max(1, seconds * 20);
    }
    
    /**
     * Loads verification configurations from the config file.
     */
    private void loadVerifications() {
        triggerBlockMap.clear();
        
        ConfigurationSection verificationsSection = config.getConfigurationSection("verifications");
        if (verificationsSection == null) {
            plugin.getLogger().warning("No verifications found in config.yml");
            return;
        }
        
        for (String key : verificationsSection.getKeys(false)) {
            ConfigurationSection verificationSection = verificationsSection.getConfigurationSection(key);
            if (verificationSection == null) continue;
            try {
                // Load trigger block
                String triggerBlockStr = verificationSection.getString("trigger-block");
                Material triggerMaterial = Material.valueOf(triggerBlockStr);

                // Leer dimensiones del área (x, y, z) o area-size para retrocompatibilidad
                int areaX = verificationSection.getInt("area-x", -1);
                int areaY = verificationSection.getInt("area-y", -1);
                int areaZ = verificationSection.getInt("area-z", -1);
                int areaSize = verificationSection.getInt("area-size", -1);
                if (areaX <= 0 || areaY <= 0 || areaZ <= 0) {
                    // Si no están definidos, usar area-size
                    areaX = areaY = areaZ = areaSize > 0 ? areaSize : 4;
                }

                // Load max uses
                int maxUses = verificationSection.getInt("max-uses", -1);

                // Load structure requirements
                ConfigurationSection structureSection = verificationSection.getConfigurationSection("structure");
                if (structureSection == null) {
                    plugin.getLogger().warning("No structure requirements found for verification: " + key);
                    continue;
                }

                Map<Material, Integer> requiredBlocks = new HashMap<>();
                for (String blockKey : structureSection.getKeys(false)) {
                    Material material = Material.valueOf(blockKey);
                    int amount = structureSection.getInt(blockKey);
                    requiredBlocks.put(material, amount);
                }

                StructureRequirement requirement = new StructureRequirement(requiredBlocks);

                // Load commands
                ConfigurationSection successSection = verificationSection.getConfigurationSection("on-success");
                if (successSection == null) {
                    plugin.getLogger().warning("No success actions found for verification: " + key);
                    continue;
                }
                String executeAt = successSection.getString("execute-at", "0,0,0");
                String[] coords = executeAt.split(",");
                int relX = Integer.parseInt(coords[0]);
                int relY = Integer.parseInt(coords[1]);
                int relZ = Integer.parseInt(coords[2]);

                // Leer tiempo personalizado o usar el general
                int generalTime = config.getInt("time", 120);
                int customTime = verificationSection.getInt("time", -1);
                int finalTime = (customTime <= 0) ? generalTime : customTime;

                // Leer comandos de interacción (on-interact)
                List<String> interactCommands = new java.util.ArrayList<>();
                ConfigurationSection interactSection = verificationSection.getConfigurationSection("on-interact");
                if (interactSection != null) {
                    interactCommands = interactSection.getStringList("commands");
                }

                // Leer executor-block (opcional)
                String executorBlockStr = verificationSection.getString("executor-block");
                Material executorMaterial = null;
                if (executorBlockStr != null && !executorBlockStr.isEmpty()) {
                    try {
                        executorMaterial = Material.valueOf(executorBlockStr);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Executor block inválido para " + key + ": " + executorBlockStr + ". Usando el mismo que el trigger block.");
                    }
                }

                TriggerBlock triggerBlock = new TriggerBlock(
                        key,
                        triggerMaterial,
                        areaX, areaY, areaZ,
                        requirement,
                        successSection.getStringList("commands"),
                        relX, relY, relZ,
                        maxUses,
                        finalTime,
                        interactCommands,
                        executorMaterial
                );

                triggerBlockMap.put(key, triggerBlock);
                plugin.getLogger().info("Loaded verification: " + key);

            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.SEVERE, "Error loading verification: " + key, e);
            }
        }
    }
    
    /**
     * Loads messages from the config file.
     */
    private void loadMessages() {
        messages.clear();
        
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection == null) {
            plugin.getLogger().warning("No messages found in config.yml");
            return;
        }
        
        for (String key : messagesSection.getKeys(false)) {
            String message = messagesSection.getString(key);
            if (message != null) {
                messages.put(key, ChatColor.translateAlternateColorCodes('&', message));
            }
        }
    }
    
    /**
     * Gets a trigger block by its material.
     *
     * @param material The material to look up
     * @return The TriggerBlock, or null if not found
     */
    public TriggerBlock getTriggerBlockByMaterial(Material material) {
        return triggerBlockMap.values().stream()
                .filter(trigger -> trigger.getMaterial() == material)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Gets a message from the configuration.
     *
     * @param key The message key
     * @return The formatted message, or a default if not found
     */
    public String getMessage(String key) {
        return messages.getOrDefault(key, "Message not found: " + key);
    }
    
    /**
     * Gets all configured trigger blocks.
     *
     * @return A map of trigger block IDs to TriggerBlock objects
     */
    public Map<String, TriggerBlock> getTriggerBlocks() {
        return new HashMap<>(triggerBlockMap);
    }

    public int getVerificationIntervalTicks() {
        return 1;
    }

    /**
 * Devuelve el tiempo máximo de verificación en ticks.
 * Calculado a partir del valor en segundos del config.yml.
 */
    public int getVerificationTimeoutTicks() {
        int seconds = config.getInt("time_verif", 120); // Valor por defecto: 120 segundos
        return seconds * 20; // 20 ticks por segundo
    }

    
    
}