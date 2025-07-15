package me.blobfishhat.randomBlockDrops.gui;

import me.blobfishhat.randomBlockDrops.RandomBlockDrops;
import me.blobfishhat.randomBlockDrops.config.BlockDropConfig;
import me.blobfishhat.randomBlockDrops.util.LootTableManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LootTableSelectionGui extends BaseGui {
    
    private final RandomBlockDrops plugin;
    private final BlockDropConfig config;
    private final Material blockType;
    private int currentPage = 0;
    private final int itemsPerPage = 21;
    
    public LootTableSelectionGui(RandomBlockDrops plugin, Material blockType) {
        super("&6Select Loot Table for " + blockType.name().toLowerCase().replace("_", " "), 54);
        this.plugin = plugin;
        this.config = plugin.getBlockDropConfig();
        this.blockType = blockType;
    }
    
    @Override
    public void setupInventory() {
        GuiUtils.fillBorder(this);
        
        List<String> lootTables = LootTableManager.getAvailableLootTables();
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, lootTables.size());
        
        // Display loot tables
        for (int i = startIndex; i < endIndex; i++) {
            String lootTable = lootTables.get(i);
            String description = LootTableManager.getLootTableDescription(lootTable);
            
            Material iconMaterial = getLootTableIcon(lootTable);
            
            ItemStack lootTableItem = GuiUtils.createItem(
                iconMaterial,
                "&a" + lootTable,
                "&7" + description,
                "",
                "&eClick to select this loot table"
            );
            
            setItem(10 + (i - startIndex), lootTableItem);
        }
        
        // Navigation buttons
        if (currentPage > 0) {
            setItem(45, GuiUtils.createPreviousPageButton());
        }
        
        if (endIndex < lootTables.size()) {
            setItem(53, GuiUtils.createNextPageButton());
        }
        
        // Back button
        setItem(46, GuiUtils.createBackButton());
        
        // Remove loot table button (switch to custom drops)
        ItemStack removeButton = GuiUtils.createItem(
            Material.BARRIER,
            "&cRemove Loot Table",
            "&7Switch back to custom drops",
            "&7for this block",
            "",
            "&eClick to remove loot table"
        );
        setItem(49, removeButton);
        
        // Close button
        setItem(52, GuiUtils.createCloseButton());
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        
        if (slot == 45 && currentPage > 0) { // Previous page
            currentPage--;
            refresh();
        } else if (slot == 53) { // Next page
            List<String> lootTables = LootTableManager.getAvailableLootTables();
            if ((currentPage + 1) * itemsPerPage < lootTables.size()) {
                currentPage++;
                refresh();
            }
        } else if (slot == 46) { // Back
            BlockDropEditGui editGui = new BlockDropEditGui(plugin, blockType);
            plugin.getGuiManager().openGui(player, editGui);
        } else if (slot == 49) { // Remove loot table
            config.setBlockLootTable(blockType, null); // Remove loot table
            player.sendMessage(GuiUtils.colorize("&cRemoved loot table from " +
                blockType.name().toLowerCase().replace("_", " ")));
            BlockDropEditGui editGui = new BlockDropEditGui(plugin, blockType);
            plugin.getGuiManager().openGui(player, editGui);
        } else if (slot == 52) { // Close
            player.closeInventory();
        } else if (slot >= 10 && slot <= 43) { // Loot table selection
            handleLootTableSelection(event, player, slot);
        }
    }
    
    private void handleLootTableSelection(InventoryClickEvent event, Player player, int slot) {
        List<String> lootTables = LootTableManager.getAvailableLootTables();
        int index = (currentPage * itemsPerPage) + (slot - 10);
        
        if (index >= 0 && index < lootTables.size()) {
            String selectedLootTable = lootTables.get(index);
            String resolvedLootTable = LootTableManager.resolveLootTable(selectedLootTable);

            // Set loot table (100% drop rate)
            config.setBlockLootTable(blockType, resolvedLootTable);

            player.sendMessage(GuiUtils.colorize("&aSet " + blockType.name().toLowerCase().replace("_", " ") +
                " to use loot table: &e" + selectedLootTable));
            player.sendMessage(GuiUtils.colorize("&7This will drop loot table items 100% of the time"));
            
            // Return to block edit GUI
            BlockDropEditGui editGui = new BlockDropEditGui(plugin, blockType);
            plugin.getGuiManager().openGui(player, editGui);
        }
    }
    
    private Material getLootTableIcon(String lootTable) {
        switch (lootTable.toLowerCase()) {
            case "endcity":
                return Material.ELYTRA;
            case "stronghold":
                return Material.END_PORTAL_FRAME;
            case "dungeon":
                return Material.MOSSY_COBBLESTONE;
            case "mineshaft":
                return Material.RAIL;
            case "desert_pyramid":
                return Material.SANDSTONE;
            case "jungle_temple":
                return Material.MOSSY_COBBLESTONE;
            case "woodland_mansion":
                return Material.DARK_OAK_LOG;
            case "buried_treasure":
                return Material.HEART_OF_THE_SEA;
            case "shipwreck_treasure":
                return Material.PRISMARINE;
            case "ancient_city":
                return Material.ECHO_SHARD;
            case "bastion_treasure":
                return Material.NETHERITE_INGOT;
            case "pillager_outpost":
                return Material.CROSSBOW;
            case "village_weaponsmith":
                return Material.IRON_SWORD;
            case "village_toolsmith":
                return Material.IRON_PICKAXE;
            case "village_armorer":
                return Material.IRON_CHESTPLATE;
            case "nether_bridge":
                return Material.NETHER_BRICK;
            case "fishing":
                return Material.FISHING_ROD;
            case "fishing_treasure":
                return Material.ENCHANTED_BOOK;
            default:
                return Material.CHEST;
        }
    }
}
