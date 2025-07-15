package me.blobfishhat.randomBlockDrops.commands;

import me.blobfishhat.randomBlockDrops.RandomBlockDrops;
import me.blobfishhat.randomBlockDrops.config.BlockDropConfig;
import me.blobfishhat.randomBlockDrops.config.BlockDropEntry;
import me.blobfishhat.randomBlockDrops.gui.GuiUtils;
import me.blobfishhat.randomBlockDrops.gui.RandomBlockDropsGui;
import me.blobfishhat.randomBlockDrops.util.LootTableManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RandomBlockDropsCommand implements CommandExecutor {
    
    private final RandomBlockDrops plugin;
    private final BlockDropConfig config;
    
    public RandomBlockDropsCommand(RandomBlockDrops plugin) {
        this.plugin = plugin;
        this.config = plugin.getBlockDropConfig();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Open main GUI
            if (player.hasPermission("randomblockdrops.gui")) {
                RandomBlockDropsGui gui = new RandomBlockDropsGui(plugin);
                plugin.getGuiManager().openGui(player, gui);
            } else {
                player.sendMessage(GuiUtils.colorize("&cYou don't have permission to use this command!"));
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "add":
                handleAddCommand(player, args);
                break;
                
            case "remove":
                handleRemoveCommand(player, args);
                break;
                
            case "list":
                handleListCommand(player);
                break;
                
            case "reload":
                handleReloadCommand(player);
                break;
                
            case "toggle":
                handleToggleCommand(player);
                break;

            case "loot":
                handleLootCommand(player, args);
                break;

            case "randomize":
                handleRandomizeCommand(player, args);
                break;

            case "lootlist":
                handleLootListCommand(player);
                break;

            case "startgame":
                handleStartGameCommand(player);
                break;

            case "resetrandom":
                handleResetRandomCommand(player);
                break;

            case "stop":
                handleStopCommand(player);
                break;

            case "help":
            default:
                sendHelpMessage(player);
                break;
        }
        
        return true;
    }
    
    private void handleAddCommand(Player player, String[] args) {
        if (!player.hasPermission("randomblockdrops.admin")) {
            player.sendMessage(GuiUtils.colorize("&cYou don't have permission to use this command!"));
            return;
        }
        
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType() == Material.AIR) {
            player.sendMessage(GuiUtils.colorize("&cYou must hold a block in your hand!"));
            return;
        }
        
        Material blockType = handItem.getType();
        if (!isValidBlockType(blockType)) {
            player.sendMessage(GuiUtils.colorize("&cYou must hold a placeable block or plant!"));
            player.sendMessage(GuiUtils.colorize("&7" + blockType + " is not a valid block type"));
            return;
        }
        
        BlockDropEntry entry = new BlockDropEntry();
        config.saveBlockDropEntry(blockType, entry);

        player.sendMessage(GuiUtils.colorize("&aAdded " + blockType.name().toLowerCase().replace("_", " ") +
            " to the configuration!"));
        player.sendMessage(GuiUtils.colorize("&7Use the GUI to configure what items it drops."));
    }
    
    private void handleRemoveCommand(Player player, String[] args) {
        if (!player.hasPermission("randomblockdrops.admin")) {
            player.sendMessage(GuiUtils.colorize("&cYou don't have permission to use this command!"));
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(GuiUtils.colorize("&cUsage: /rbd remove <block_type>"));
            return;
        }
        
        try {
            Material blockType = Material.valueOf(args[1].toUpperCase());
            if (config.hasBlockConfiguration(blockType)) {
                config.removeBlockConfiguration(blockType);
                player.sendMessage(GuiUtils.colorize("&aRemoved configuration for " + 
                    blockType.name().toLowerCase().replace("_", " ")));
            } else {
                player.sendMessage(GuiUtils.colorize("&cNo configuration found for " + 
                    blockType.name().toLowerCase().replace("_", " ")));
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(GuiUtils.colorize("&cInvalid block type: " + args[1]));
        }
    }
    
    private void handleListCommand(Player player) {
        if (!player.hasPermission("randomblockdrops.list")) {
            player.sendMessage(GuiUtils.colorize("&cYou don't have permission to use this command!"));
            return;
        }
        
        player.sendMessage(GuiUtils.colorize("&6=== Configured Blocks ==="));
        for (Material block : config.getConfiguredBlocks()) {
            BlockDropEntry entry = config.getBlockDropEntry(block);
            String dropInfo = entry.isUsingLootTable() ?
                "Loot Table: " + entry.getLootTable() :
                "Custom Drops: " + entry.getDropCount();
            player.sendMessage(GuiUtils.colorize("&e" + block.name().toLowerCase().replace("_", " ") +
                " &7- " + dropInfo));
        }
    }
    
    private void handleReloadCommand(Player player) {
        if (!player.hasPermission("randomblockdrops.admin")) {
            player.sendMessage(GuiUtils.colorize("&cYou don't have permission to use this command!"));
            return;
        }
        
        config.reloadConfig();
        player.sendMessage(GuiUtils.colorize("&aConfiguration reloaded!"));
    }
    
    private void handleToggleCommand(Player player) {
        if (!player.hasPermission("randomblockdrops.admin")) {
            player.sendMessage(GuiUtils.colorize("&cYou don't have permission to use this command!"));
            return;
        }

        boolean newStatus = !config.isEnabled();
        config.setEnabled(newStatus);
        player.sendMessage(GuiUtils.colorize("&6RandomBlockDrops " +
            (newStatus ? "&aenabled" : "&cdisabled")));
    }

    private void handleLootCommand(Player player, String[] args) {
        if (!player.hasPermission("randomblockdrops.admin")) {
            player.sendMessage(GuiUtils.colorize("&cYou don't have permission to use this command!"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(GuiUtils.colorize("&cUsage: /rbd loot <loot_table>"));
            player.sendMessage(GuiUtils.colorize("&7Example: /rbd loot endcity"));
            player.sendMessage(GuiUtils.colorize("&7Use /rbd lootlist to see available loot tables"));
            return;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType() == Material.AIR) {
            player.sendMessage(GuiUtils.colorize("&cYou must hold a block in your hand!"));
            return;
        }

        Material blockType = handItem.getType();
        if (!isValidBlockType(blockType)) {
            player.sendMessage(GuiUtils.colorize("&cYou must hold a placeable block or plant!"));
            player.sendMessage(GuiUtils.colorize("&7" + blockType + " is not a valid block type"));
            return;
        }

        String lootTableInput = args[1];
        String resolvedLootTable = LootTableManager.resolveLootTable(lootTableInput);

        config.setBlockLootTable(blockType, resolvedLootTable);

        // Debug logging
        plugin.getLogger().info("Set loot table for " + blockType + ": " + resolvedLootTable);

        player.sendMessage(GuiUtils.colorize("&aSet " + blockType.name().toLowerCase().replace("_", " ") +
            " to use loot table: &e" + resolvedLootTable));
        player.sendMessage(GuiUtils.colorize("&7Description: &f" +
            LootTableManager.getLootTableDescription(lootTableInput)));
    }

    private void handleRandomizeCommand(Player player, String[] args) {
        if (!player.hasPermission("randomblockdrops.admin")) {
            player.sendMessage(GuiUtils.colorize("&cYou don't have permission to use this command!"));
            return;
        }

        // Toggle randomization (no rate parameter)
        boolean newStatus = !config.isCompleteRandomization();
        config.setCompleteRandomization(newStatus);

        if (newStatus) {
            player.sendMessage(GuiUtils.colorize("&6&lCOMPLETE CHAOS MODE ACTIVATED!"));
            player.sendMessage(GuiUtils.colorize("&c&lWARNING: &7EVERYTHING now drops 1 random item!"));
            player.sendMessage(GuiUtils.colorize("&7- Block breaking: 1 random item"));
            player.sendMessage(GuiUtils.colorize("&7- Mob kills: 1 random item"));
            player.sendMessage(GuiUtils.colorize("&7- Fishing: 1 random item"));
            player.sendMessage(GuiUtils.colorize("&7- Loot tables: Full drops (multiple items)"));
        } else {
            player.sendMessage(GuiUtils.colorize("&6Complete randomization &cdisabled"));
            player.sendMessage(GuiUtils.colorize("&7Normal game mechanics restored"));
        }
    }

    private void handleLootListCommand(Player player) {
        if (!player.hasPermission("randomblockdrops.list")) {
            player.sendMessage(GuiUtils.colorize("&cYou don't have permission to use this command!"));
            return;
        }

        player.sendMessage(GuiUtils.colorize("&6=== Available Loot Tables ==="));
        player.sendMessage(GuiUtils.colorize("&7Use: /rbd loot <name> [rate]"));
        player.sendMessage("");

        for (String lootTable : LootTableManager.getAvailableLootTables()) {
            player.sendMessage(GuiUtils.colorize("&e" + lootTable + " &7- " +
                LootTableManager.getLootTableDescription(lootTable)));
        }
    }

    private void handleStartGameCommand(Player player) {
        if (!player.hasPermission("randomblockdrops.admin")) {
            player.sendMessage(GuiUtils.colorize("&cYou don't have permission to use this command!"));
            return;
        }

        plugin.getGameManager().startGame();
        player.sendMessage(GuiUtils.colorize("&aGame started! Check the scoreboard for timers."));
    }

    private void handleResetRandomCommand(Player player) {
        if (!player.hasPermission("randomblockdrops.admin")) {
            player.sendMessage(GuiUtils.colorize("&cYou don't have permission to use this command!"));
            return;
        }

        config.resetPersistentRandomDrops();
        player.sendMessage(GuiUtils.colorize("&aReset all persistent random drop mappings!"));
        player.sendMessage(GuiUtils.colorize("&7Blocks will now generate new random items when broken."));
    }

    private void handleStopCommand(Player player) {
        if (!player.hasPermission("randomblockdrops.admin")) {
            player.sendMessage(GuiUtils.colorize("&cYou don't have permission to use this command!"));
            return;
        }

        // Disable everything
        config.setEnabled(false);
        config.setCompleteRandomization(false);
        config.resetPersistentRandomDrops();

        player.sendMessage(GuiUtils.colorize("&c&lSTOPPED ALL RANDOMIZATION!"));
        player.sendMessage(GuiUtils.colorize("&7- Plugin disabled"));
        player.sendMessage(GuiUtils.colorize("&7- Chaos mode disabled"));
        player.sendMessage(GuiUtils.colorize("&7- All random mappings cleared"));
        player.sendMessage(GuiUtils.colorize("&7- Game mechanics restored to normal"));

        // Broadcast to all players
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (!p.equals(player)) {
                p.sendMessage(GuiUtils.colorize("&6RandomBlockDrops has been stopped by " + player.getName()));
                p.sendMessage(GuiUtils.colorize("&7All blocks now drop normally"));
            }
        }
    }
    
    private void sendHelpMessage(Player player) {
        player.sendMessage(GuiUtils.colorize("&6=== RandomBlockDrops Commands ==="));
        player.sendMessage(GuiUtils.colorize("&e/rbd &7- Open the main GUI"));
        player.sendMessage(GuiUtils.colorize("&e/rbd add &7- Add block in hand"));
        player.sendMessage(GuiUtils.colorize("&e/rbd remove <block> &7- Remove block configuration"));
        player.sendMessage(GuiUtils.colorize("&e/rbd list &7- List all configured blocks"));
        player.sendMessage(GuiUtils.colorize("&e/rbd loot <table> &7- Set block to use loot table"));
        player.sendMessage(GuiUtils.colorize("&e/rbd lootlist &7- Show available loot tables"));
        player.sendMessage(GuiUtils.colorize("&e/rbd randomize &7- Toggle COMPLETE CHAOS MODE"));
        player.sendMessage(GuiUtils.colorize("&e/rbd resetrandom &7- Reset persistent random drop mappings"));
        player.sendMessage(GuiUtils.colorize("&c/rbd stop &7- STOP ALL randomization (emergency)"));
        player.sendMessage(GuiUtils.colorize("&e/rbd startgame &7- Start survival game (PvP timer, border)"));
        player.sendMessage(GuiUtils.colorize("&e/rbd reload &7- Reload configuration"));
        player.sendMessage(GuiUtils.colorize("&e/rbd toggle &7- Enable/disable plugin"));
        player.sendMessage(GuiUtils.colorize("&e/rbd help &7- Show this help message"));
        player.sendMessage("");
        player.sendMessage(GuiUtils.colorize("&7Examples:"));
        player.sendMessage(GuiUtils.colorize("&8/rbd loot endcity &7- Grass drops End City loot"));
        player.sendMessage(GuiUtils.colorize("&8/rbd randomize &7- EVERYTHING drops random items (CHAOS MODE)"));
    }

    private boolean isValidBlockType(Material material) {
        // Check if it's a regular block
        if (material.isBlock()) {
            return true;
        }

        // Check for specific plant/flower types that can be broken but aren't considered "blocks"
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
            case SUNFLOWER:
            case LILAC:
            case ROSE_BUSH:
            case PEONY:
            case TALL_GRASS:
            case LARGE_FERN:
            case DEAD_BUSH:
            case SEAGRASS:
            case TALL_SEAGRASS:
            case SEA_PICKLE:
            case KELP:
            case KELP_PLANT:
            case BAMBOO:
            case SUGAR_CANE:
            case CACTUS:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case CRIMSON_FUNGUS:
            case WARPED_FUNGUS:
            case NETHER_SPROUTS:
            case CRIMSON_ROOTS:
            case WARPED_ROOTS:
            case WHEAT:
            case CARROTS:
            case POTATOES:
            case BEETROOTS:
            case SWEET_BERRY_BUSH:
            case COCOA:
                return true;
            default:
                return false;
        }
    }
}
