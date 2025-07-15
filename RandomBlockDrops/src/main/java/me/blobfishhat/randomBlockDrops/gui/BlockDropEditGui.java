package me.blobfishhat.randomBlockDrops.gui;

import me.blobfishhat.randomBlockDrops.RandomBlockDrops;
import me.blobfishhat.randomBlockDrops.config.BlockDropConfig;
import me.blobfishhat.randomBlockDrops.config.BlockDropEntry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BlockDropEditGui extends BaseGui {
    
    private final RandomBlockDrops plugin;
    private final BlockDropConfig config;
    private final Material blockType;
    private BlockDropEntry entry;
    
    public BlockDropEditGui(RandomBlockDrops plugin, Material blockType) {
        super("&6Edit: " + blockType.name().toLowerCase().replace("_", " "), 54);
        this.plugin = plugin;
        this.config = plugin.getBlockDropConfig();
        this.blockType = blockType;
        this.entry = config.getBlockDropEntry(blockType);

        // Create a new entry if none exists
        if (this.entry == null) {
            this.entry = new BlockDropEntry();
            config.saveBlockDropEntry(blockType, this.entry);
        }
    }
    
    @Override
    public void setupInventory() {
        GuiUtils.fillBorder(this);
        
        // Block display
        List<String> blockLore = new ArrayList<>();

        if (entry != null && entry.isUsingLootTable()) {
            blockLore.add("&7Using loot table: &a" + entry.getLootTable());
            blockLore.add("&7This uses Minecraft's built-in loot system");
            blockLore.add("&7Drops: &eAll loot table items");
        } else if (entry != null) {
            blockLore.add("&7Number of possible drops: &e" + entry.getPossibleDrops().size());
            blockLore.add("&7Drop rate: &a100% (always drops)");
        } else {
            blockLore.add("&7No configuration set");
            blockLore.add("&7Drop rate: &a100% (always drops)");
        }

        ItemStack blockDisplay = GuiUtils.createItem(
            blockType,
            "&a" + blockType.name().toLowerCase().replace("_", " "),
            blockLore.toArray(new String[0])
        );
        setItem(13, blockDisplay);
        
        // Drop rate removed - all drops are now 100%
        // These slots are now empty for cleaner GUI layout
        
        // Display possible drops or loot table info
        if (entry != null && entry.isUsingLootTable()) {
            ItemStack lootTableInfo = GuiUtils.createItem(
                Material.CHEST,
                "&aLoot Table: &e" + entry.getLootTable(),
                "&7This block uses Minecraft's built-in",
                "&7loot table system for drops",
                "",
                "&cClick to switch to custom drops"
            );
            setItem(31, lootTableInfo);
        } else if (entry != null) {
            List<ItemStack> drops = entry.getPossibleDrops();
            for (int i = 0; i < Math.min(drops.size(), 21); i++) {
                ItemStack drop = drops.get(i).clone();
                ItemStack displayItem = GuiUtils.createItem(
                    drop.getType(),
                    drop.getAmount(),
                    "&f" + drop.getAmount() + "x " + drop.getType().name().toLowerCase().replace("_", " "),
                    "&7Right-click to remove this drop"
                );
                setItem(28 + i, displayItem);
            }
        } else {
            // Show message for unconfigured blocks
            ItemStack noConfigInfo = GuiUtils.createItem(
                Material.GRAY_STAINED_GLASS_PANE,
                "&7No Drops Configured",
                "&7This block has no custom drops yet",
                "&7Add items or select a loot table",
                "",
                "&eUse the buttons below to configure"
            );
            setItem(31, noConfigInfo);
        }
        
        // Add drop button (only show if not using loot table)
        if (entry == null || !entry.isUsingLootTable()) {
            ItemStack addDrop = GuiUtils.createItem(
                Material.EMERALD,
                "&aAdd Drop Item",
                "&7Hold an item in your hand",
                "&7and click to add it as a drop",
                "",
                "&eClick to add"
            );
            setItem(48, addDrop);
        }

        // Loot table selection button
        ItemStack lootTableButton = GuiUtils.createItem(
            Material.ENDER_CHEST,
            "&dSelect Loot Table",
            "&7Choose from Minecraft's built-in",
            "&7loot tables (End City, Villages, etc.)",
            "",
            "&eClick to browse loot tables"
        );
        setItem(50, lootTableButton);
        
        // Back button
        setItem(45, GuiUtils.createBackButton());
        
        // Save and close
        ItemStack saveButton = GuiUtils.createItem(
            Material.DIAMOND,
            "&aSave Changes",
            "&7Save the current configuration",
            "",
            "&eClick to save"
        );
        setItem(53, saveButton);
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        
        switch (slot) {
            case 45: // Back
                ConfigGui configGui = new ConfigGui(plugin);
                plugin.getGuiManager().openGui(player, configGui);
                break;
                
            case 48: // Add drop (only if not using loot table)
                if (entry != null && !entry.isUsingLootTable()) {
                    ItemStack handItem = player.getInventory().getItemInMainHand();
                    if (handItem.getType() != Material.AIR) {
                        entry.addPossibleDrop(handItem.clone());
                        player.sendMessage(GuiUtils.colorize("&aAdded &e" + handItem.getAmount() +
                            "x " + handItem.getType().name().toLowerCase().replace("_", " ") +
                            " &aas a possible drop!"));
                        refresh();
                    } else {
                        player.sendMessage(GuiUtils.colorize("&cYou must hold an item in your hand!"));
                    }
                } else if (entry == null) {
                    // Create entry and add drop
                    ItemStack handItem = player.getInventory().getItemInMainHand();
                    if (handItem.getType() != Material.AIR) {
                        entry = new BlockDropEntry();
                        entry.addPossibleDrop(handItem.clone());
                        config.saveBlockDropEntry(blockType, entry);
                        player.sendMessage(GuiUtils.colorize("&aAdded &e" + handItem.getAmount() +
                            "x " + handItem.getType().name().toLowerCase().replace("_", " ") +
                            " &aas a possible drop!"));
                        refresh();
                    } else {
                        player.sendMessage(GuiUtils.colorize("&cYou must hold an item in your hand!"));
                    }
                }
                break;

            case 50: // Select loot table
                LootTableSelectionGui lootGui = new LootTableSelectionGui(plugin, blockType);
                plugin.getGuiManager().openGui(player, lootGui);
                break;
                
            case 53: // Save
                config.saveBlockDropEntry(blockType, entry);
                player.sendMessage(GuiUtils.colorize("&aConfiguration saved for " + 
                    blockType.name().toLowerCase().replace("_", " ") + "!"));
                ConfigGui configGui2 = new ConfigGui(plugin);
                plugin.getGuiManager().openGui(player, configGui2);
                break;

            case 31: // Loot table info (switch to custom drops)
                if (entry != null && entry.isUsingLootTable()) {
                    entry.setLootTable(null);
                    player.sendMessage(GuiUtils.colorize("&aSwitched to custom drops mode"));
                    refresh();
                }
                break;

            default:
                // Handle drop removal
                if (slot >= 28 && slot <= 48 && event.isRightClick() && entry != null && !entry.isUsingLootTable()) {
                    int dropIndex = slot - 28;
                    List<ItemStack> drops = entry.getPossibleDrops();
                    if (dropIndex >= 0 && dropIndex < drops.size()) {
                        ItemStack removedDrop = drops.remove(dropIndex);
                        player.sendMessage(GuiUtils.colorize("&cRemoved &e" +
                            removedDrop.getAmount() + "x " +
                            removedDrop.getType().name().toLowerCase().replace("_", " ") +
                            " &cfrom possible drops!"));
                        refresh();
                    }
                }
                break;
        }
    }
}
