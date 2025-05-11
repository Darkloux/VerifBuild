package me.verifbuild.api;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.verifbuild.VerifBuild;
import me.verifbuild.util.ItemUtils;
import me.verifbuild.verification.TriggerBlock;
import me.verifbuild.verification.VerificationArea;

public class VerifBuildAPI {

    private static VerifBuild plugin;

    /**
     * Inicializa la API con la instancia principal del plugin.
     */
    public static void init(VerifBuild instance) {
        plugin = instance;
    }

    /**
     * Obtiene el TriggerBlock (configuración) por su ID.
     */
    public static TriggerBlock getTriggerBlock(String id) {
        return plugin.getConfigManager().getTriggerBlocks().get(id);
    }

    /**
     * Crea un ítem verificador con NBT, lore y nombre.
     */
    public static ItemStack createVerifierItem(String id) {
        TriggerBlock block = getTriggerBlock(id);
        if (block == null) return null;
        return ItemUtils.createVerifierItem(plugin, block);
    }

    /**
     * Inicia una verificación en una ubicación específica.
     */
    public static boolean startVerification(String triggerId, Location location, Player placer) {
        TriggerBlock triggerBlock = getTriggerBlock(triggerId);
        if (triggerBlock == null) return false;

        plugin.getVerifierManager().createVerificationArea(triggerBlock, location, placer);
        return true;
    }

    /**
     * Cancela una verificación activa por ID.
     */
    public static void cancelVerification(UUID verificationId) {
        plugin.getVerifierManager().removeVerification(verificationId);
    }

    /**
     * Devuelve todas las verificaciones activas.
     */
    public static Map<UUID, VerificationArea> getActiveVerifications() {
        return plugin.getVerifierManager().getActiveVerifications();
    }
}
