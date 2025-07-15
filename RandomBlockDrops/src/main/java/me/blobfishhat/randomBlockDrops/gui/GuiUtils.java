package me.blobfishhat.randomBlockDrops.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class GuiUtils {
    
    public static ItemStack createItem(Material material, String name, String... lore) {
        try {
            // Validate material can be an item
            if (!material.isItem()) {
                // Fallback to a safe material
                material = Material.STONE;
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

                if (lore.length > 0) {
                    List<String> loreList = Arrays.asList(lore);
                    loreList.replaceAll(line -> ChatColor.translateAlternateColorCodes('&', line));
                    meta.setLore(loreList);
                }

                item.setItemMeta(meta);
            }

            return item;
        } catch (Exception e) {
            // Ultimate fallback - return stone with error info
            ItemStack fallback = new ItemStack(Material.STONE);
            ItemMeta meta = fallback.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.RED + "Error: " + material.name());
                meta.setLore(Arrays.asList(ChatColor.GRAY + "This material couldn't be displayed"));
                fallback.setItemMeta(meta);
            }
            return fallback;
        }
    }
    
    public static ItemStack createItem(Material material, int amount, String name, String... lore) {
        ItemStack item = createItem(material, name, lore);
        item.setAmount(amount);
        return item;
    }
    
    public static ItemStack createGlassPane(String name) {
        return createItem(Material.GRAY_STAINED_GLASS_PANE, name);
    }
    
    public static ItemStack createBackButton() {
        return createItem(Material.ARROW, "&cBack", "&7Click to go back");
    }
    
    public static ItemStack createCloseButton() {
        return createItem(Material.BARRIER, "&cClose", "&7Click to close");
    }
    
    public static ItemStack createNextPageButton() {
        return createItem(Material.ARROW, "&aNext Page", "&7Click to go to next page");
    }
    
    public static ItemStack createPreviousPageButton() {
        return createItem(Material.ARROW, "&aPrevious Page", "&7Click to go to previous page");
    }
    
    public static ItemStack createToggleButton(boolean enabled, String name, String enabledLore, String disabledLore) {
        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = enabled ? "&aEnabled" : "&cDisabled";
        String lore = enabled ? enabledLore : disabledLore;
        
        return createItem(material, name, status, "", lore);
    }
    
    public static void fillBorder(BaseGui gui) {
        ItemStack borderItem = createGlassPane(" ");
        
        // Fill top and bottom rows
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, borderItem);
            gui.setItem(gui.getSize() - 9 + i, borderItem);
        }
        
        // Fill left and right columns
        for (int i = 9; i < gui.getSize() - 9; i += 9) {
            gui.setItem(i, borderItem);
            gui.setItem(i + 8, borderItem);
        }
    }
    
    public static String formatPercentage(double percentage) {
        return String.format("%.1f%%", percentage * 100);
    }
    
    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
