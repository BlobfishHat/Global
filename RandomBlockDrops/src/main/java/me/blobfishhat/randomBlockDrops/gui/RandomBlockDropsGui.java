package me.blobfishhat.randomBlockDrops.gui;

import me.blobfishhat.randomBlockDrops.RandomBlockDrops;
import me.blobfishhat.randomBlockDrops.config.BlockDropConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class RandomBlockDropsGui extends BaseGui {
    
    private final RandomBlockDrops plugin;
    private final BlockDropConfig config;
    
    public RandomBlockDropsGui(RandomBlockDrops plugin) {
        super("&6Random Block Drops - Main Menu", 27);
        this.plugin = plugin;
        this.config = plugin.getBlockDropConfig();
    }
    
    @Override
    public void setupInventory() {
        GuiUtils.fillBorder(this);
        
        // Plugin status toggle
        boolean enabled = config.isEnabled();
        ItemStack statusItem = GuiUtils.createToggleButton(
            enabled,
            "&6Plugin Status",
            "&7Click to disable the plugin",
            "&7Click to enable the plugin"
        );
        setItem(10, statusItem);
        
        // Configure block drops
        ItemStack configItem = GuiUtils.createItem(
            Material.CHEST,
            "&aBlock Drop Configuration",
            "&7Configure which blocks have random drops",
            "&7and what items they can drop",
            "",
            "&eClick to open configuration"
        );
        setItem(12, configItem);
        
        // Statistics
        ItemStack statsItem = GuiUtils.createItem(
            Material.BOOK,
            "&bStatistics",
            "&7View plugin statistics and information",
            "",
            "&eClick to view stats"
        );
        setItem(14, statsItem);
        
        // Complete randomization toggle (CHAOS MODE)
        boolean randomizationEnabled = config.isCompleteRandomization();
        ItemStack randomizeItem;
        if (randomizationEnabled) {
            randomizeItem = GuiUtils.createItem(
                Material.TNT,
                "&c&lCHAOS MODE &4[ACTIVE]",
                "&7These systems drop 1 random item:",
                "&8• Block breaking → 1 random item",
                "&8• Mob kills → 1 random item",
                "&8• Fishing → 1 random item",
                "&7(Loot tables give full drops)",
                "",
                "&c&lClick to disable chaos"
            );
        } else {
            randomizeItem = GuiUtils.createItem(
                Material.GRAY_DYE,
                "&c&lCHAOS MODE &7[INACTIVE]",
                "&7When enabled, EVERYTHING in the game",
                "&7will drop completely random items",
                "&7This affects ALL game mechanics",
                "",
                "&e&lClick to activate CHAOS"
            );
        }
        setItem(16, randomizeItem);

        // Quick loot table selector
        ItemStack quickLootItem = GuiUtils.createItem(
            Material.ENDER_CHEST,
            "&dQuick Loot Table Setup",
            "&7Hold a block and click to quickly",
            "&7assign a loot table to it",
            "",
            "&eClick to set up loot table"
        );
        setItem(19, quickLootItem);

        // Reload configuration
        ItemStack reloadItem = GuiUtils.createItem(
            Material.COMMAND_BLOCK,
            "&eReload Configuration",
            "&7Reload the plugin configuration",
            "&7from the config file",
            "",
            "&eClick to reload"
        );
        setItem(21, reloadItem);

        // Close button
        setItem(22, GuiUtils.createCloseButton());
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        
        switch (slot) {
            case 10: // Toggle plugin status
                boolean newStatus = !config.isEnabled();
                config.setEnabled(newStatus);
                player.sendMessage(GuiUtils.colorize("&6RandomBlockDrops &7has been " + 
                    (newStatus ? "&aenabled" : "&cdisabled")));
                refresh();
                break;
                
            case 12: // Open block drop configuration
                ConfigGui configGui = new ConfigGui(plugin);
                plugin.getGuiManager().openGui(player, configGui);
                break;
                
            case 14: // Show statistics
                showStatistics(player);
                break;
                
            case 16: // Toggle CHAOS MODE
                boolean newRandomizationStatus = !config.isCompleteRandomization();
                config.setCompleteRandomization(newRandomizationStatus);

                if (newRandomizationStatus) {
                    player.sendMessage(GuiUtils.colorize("&c&lCHAOS MODE ACTIVATED!"));
                    player.sendMessage(GuiUtils.colorize("&7EVERYTHING now drops random items!"));
                } else {
                    player.sendMessage(GuiUtils.colorize("&6Chaos mode &cdisabled"));
                    player.sendMessage(GuiUtils.colorize("&7Normal game mechanics restored"));
                }
                refresh();
                break;

            case 19: // Quick loot table setup
                handleQuickLootTableSetup(player);
                break;

            case 21: // Reload configuration
                config.reloadConfig();
                player.sendMessage(GuiUtils.colorize("&6Configuration reloaded!"));
                refresh();
                break;

            case 22: // Close
                player.closeInventory();
                break;
        }
    }
    
    private void showStatistics(Player player) {
        player.sendMessage(GuiUtils.colorize("&6=== RandomBlockDrops Statistics ==="));
        player.sendMessage(GuiUtils.colorize("&7Plugin Status: " +
            (config.isEnabled() ? "&aEnabled" : "&cDisabled")));
        player.sendMessage(GuiUtils.colorize("&7Chaos Mode: " +
            (config.isCompleteRandomization() ? "&c&lACTIVE &7(Everything randomized)" : "&7Inactive")));
        player.sendMessage(GuiUtils.colorize("&7Configured Blocks: &e" +
            config.getConfiguredBlocks().size()));
        player.sendMessage(GuiUtils.colorize("&7Total Drop Configurations: &e" +
            config.getTotalDropConfigurations()));
    }

    private void handleQuickLootTableSetup(Player player) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType() == Material.AIR) {
            player.sendMessage(GuiUtils.colorize("&cHold a block in your hand first!"));
            return;
        }

        Material blockType = handItem.getType();
        if (!isValidBlockType(blockType)) {
            player.sendMessage(GuiUtils.colorize("&cYou must hold a placeable block or plant!"));
            return;
        }

        // Open loot table selection for this block
        LootTableSelectionGui lootGui = new LootTableSelectionGui(plugin, blockType);
        plugin.getGuiManager().openGui(player, lootGui);
    }

    private boolean isValidBlockType(Material material) {
        // Check if it's a regular block
        if (material.isBlock()) {
            return true;
        }

        // Check for specific plant/flower types
        switch (material) {
            case DANDELION:
            case POPPY:
            case BLUE_ORCHID:
            case ALLIUM:
            case AZURE_BLUET:
            case RED_TULIP:
            case ORANGE_TULIP:
            case WHITE_TULIP:
            case PINK_TULIP:
            case OXEYE_DAISY:
            case CORNFLOWER:
            case LILY_OF_THE_VALLEY:
            case WITHER_ROSE:
            case TALL_GRASS:
            case LARGE_FERN:
            case DEAD_BUSH:
            case SUGAR_CANE:
            case CACTUS:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case WHEAT:
            case CARROTS:
            case POTATOES:
            case BEETROOTS:
                return true;
            default:
                return false;
        }
    }
}
