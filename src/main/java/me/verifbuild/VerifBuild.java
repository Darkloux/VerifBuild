package me.verifbuild;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.verifbuild.api.VerifBuildAPI;
import me.verifbuild.commands.VerifBuildCommand;
import me.verifbuild.config.ConfigManager;
import me.verifbuild.listeners.BlockListener;
import me.verifbuild.verification.VerifierManager;

public final class VerifBuild extends JavaPlugin {
    
    private static VerifBuild instance;
    private ConfigManager configManager;
    private VerifierManager verifierManager;
    
    @Override
    public void onEnable() {
        VerifBuildAPI.init(this);

        instance = this;
        
        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Initialize verification manager
        verifierManager = new VerifierManager(this);
        
        // Register events
        Bukkit.getPluginManager().registerEvents(new BlockListener(this), this);
        
        // Register commands
        getCommand("verifbuild").setExecutor(new VerifBuildCommand(this));
        
        getLogger().info("VerifBuild has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Clean up verification areas
        if (verifierManager != null) {
            verifierManager.cleanupAllVerifications();
        }
        
        getLogger().info("VerifBuild has been disabled!");
    }
    
    /**
     * Gets the singleton instance of the plugin.
     *
     * @return The plugin instance
     */
    public static VerifBuild getInstance() {
        return instance;
    }
    
    /**
     * Gets the configuration manager.
     *
     * @return The configuration manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Gets the verifier manager.
     *
     * @return The verifier manager
     */
    public VerifierManager getVerifierManager() {
        return verifierManager;
    }
}