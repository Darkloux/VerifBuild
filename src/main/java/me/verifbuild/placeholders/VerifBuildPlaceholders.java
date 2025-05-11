package me.verifbuild.placeholders;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.verifbuild.VerifBuild;
import me.verifbuild.verification.VerificationArea;
import me.verifbuild.verification.VerifierManager;

public class VerifBuildPlaceholders extends PlaceholderExpansion {

    private final VerifBuild plugin;

    public VerifBuildPlaceholders(VerifBuild plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "verifbuild";
    }

    @Override
    public @NotNull String getAuthor() {
        return "DarkovDev";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        VerifierManager manager = plugin.getVerifierManager();

        if (player == null || !player.isOnline()) return "";

        VerificationArea area = manager.getVerificationByPlayer(player.getPlayer());
        if (area == null) return "";

        switch (identifier.toLowerCase()) {
            case "progress":
                return String.format("%.2f%%", area.getProgressPercentage());
            case "current_blocks":
                return String.valueOf(area.getCurrentBlockCount());
            case "structure_id":
                return area.getTriggerBlock().getId();
            default:
                return null;
        }
    }
}