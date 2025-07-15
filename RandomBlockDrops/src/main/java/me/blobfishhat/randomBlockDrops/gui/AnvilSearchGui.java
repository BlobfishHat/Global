package me.blobfishhat.randomBlockDrops.gui;

import me.blobfishhat.randomBlockDrops.RandomBlockDrops;
import me.blobfishhat.randomBlockDrops.config.BlockDropConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnvilSearchGui extends BaseGui {
    
    private final RandomBlockDrops plugin;
    private final BlockDropConfig config;
    
    public AnvilSearchGui(RandomBlockDrops plugin) {
        super("Search for blocks/items", 3);
        this.plugin = plugin;
        this.config = plugin.getBlockDropConfig();
        // Override the inventory to use anvil type
        this.inventory = Bukkit.createInventory(null, InventoryType.ANVIL, "Search for blocks/items");
    }
    
    @Override
    public void setupInventory() {
        // Set up the anvil GUI
        // Slot 0: Input item (paper with instructions)
        ItemStack searchPaper = new ItemStack(Material.PAPER);
        ItemMeta meta = searchPaper.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Type your search here");
            searchPaper.setItemMeta(meta);
        }
        inventory.setItem(0, searchPaper);
        
        // Slot 1: Empty (anvil second slot)
        // Slot 2: Result will appear here when player types
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 2) { // Result slot - when player clicks the result
            event.setCancelled(true);

            // Get the renamed item
            ItemStack resultItem = inventory.getItem(2);
            if (resultItem != null && resultItem.hasItemMeta() && resultItem.getItemMeta().hasDisplayName()) {
                String rawSearchTerm = resultItem.getItemMeta().getDisplayName();

                // Remove color codes and clean up the search term
                final String searchTerm = rawSearchTerm.replaceAll("§[0-9a-fk-or]", "").trim();

                if (!searchTerm.isEmpty() && !searchTerm.equals("Type your search here")) {
                    // Close anvil and open search results
                    player.closeInventory();

                    // Schedule the search results GUI to open after a tick
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        SearchResultsGui resultsGui = new SearchResultsGui(plugin, searchTerm);
                        plugin.getGuiManager().openGui(player, resultsGui);
                    }, 1L);
                    return;
                }
            }
        }

        // For anvil GUIs, we need to be more permissive to allow typing
        if (slot == 0 || slot == 1) {
            // Allow interaction with input slots
            return;
        }

        // Cancel other interactions
        event.setCancelled(true);
    }
    
    @Override
    public void onClose(Player player) {
        // When anvil closes without selection, go back to config GUI
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ConfigGui configGui = new ConfigGui(plugin);
            plugin.getGuiManager().openGui(player, configGui);
        }, 1L);
    }
}
