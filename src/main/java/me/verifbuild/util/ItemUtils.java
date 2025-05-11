package me.verifbuild.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.verifbuild.VerifBuild;
import me.verifbuild.verification.TriggerBlock;

public class ItemUtils {

    public static ItemStack createVerifierItem(VerifBuild plugin, TriggerBlock triggerBlock) {
        ItemStack item = new ItemStack(triggerBlock.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName("§6Bloque Verificador: §e" + triggerBlock.getId());

        List<String> lore = new ArrayList<>();
        lore.add("§7Tipo: §f" + triggerBlock.getId());
        lore.add("§7Área: §f" + triggerBlock.getAreaSize() + "x" + triggerBlock.getAreaSize());
        lore.add("§7Requisitos:");
        triggerBlock.getRequirement().getRequiredBlocks().forEach((mat, amount) ->
            lore.add("§7- §f" + amount + "x §e" + mat.name())
        );
        meta.setLore(lore);

        // Tag NBT para identificarlo
        NamespacedKey key = new NamespacedKey(plugin, "verif_block");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, triggerBlock.getId());

        item.setItemMeta(meta);
        return item;
    }
}
