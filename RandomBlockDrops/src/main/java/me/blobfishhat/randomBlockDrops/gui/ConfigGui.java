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

public class ConfigGui extends BaseGui {
    
    private final RandomBlockDrops plugin;
    private final BlockDropConfig config;
    private int currentPage = 0;
    private final int itemsPerPage = 21;
    
    public ConfigGui(RandomBlockDrops plugin) {
        super("&6Block Drop Configuration", 54);
        this.plugin = plugin;
        this.config = plugin.getBlockDropConfig();
    }
    
    @Override
    public void setupInventory() {
        GuiUtils.fillBorder(this);

        // Show chaos mode status if active
        if (config.isCompleteRandomization()) {
            ItemStack chaosInfo = GuiUtils.createItem(
                Material.TNT,
                "&c&lCHAOS MODE ACTIVE",
                "&7EVERYTHING drops random items!",
                "&7Individual block settings are overridden",
                "&7Go back to main menu to disable chaos"
            );
            setItem(4, chaosInfo);
        }

        List<Material> displayBlocks;

        if (config.isCompleteRandomization()) {
            // Show ALL blocks and items when chaos mode is active
            displayBlocks = getAllValidMaterials();
        } else {
            // Show only configured blocks normally
            displayBlocks = new ArrayList<>(config.getConfiguredBlocks());
        }

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, displayBlocks.size());

        // Display blocks
        for (int i = startIndex; i < endIndex; i++) {
            Material block = displayBlocks.get(i);
            BlockDropEntry entry = config.getBlockDropEntry(block);

            List<String> lore = new ArrayList<>();

            // Show if chaos mode overrides this block
            if (config.isCompleteRandomization()) {
                lore.add("&c&lCHAOS MODE ACTIVE");
                lore.add("&7This block drops random items");
                if (entry != null) {
                    lore.add("&7Individual settings below are inactive");
                } else {
                    lore.add("&7No individual configuration");
                }
                lore.add("");
            }

            if (entry != null) {
                if (entry.isUsingLootTable()) {
                    lore.add("&7Using Loot Table: &a" + entry.getLootTable());
                    lore.add("&7This block uses Minecraft's built-in loot system");
                } else {
                    lore.add("&7Possible Drops:");
                    for (ItemStack drop : entry.getPossibleDrops()) {
                        lore.add("&8- &f" + drop.getAmount() + "x " +
                            drop.getType().name().toLowerCase().replace("_", " "));
                    }
                }
            } else if (!config.isCompleteRandomization()) {
                lore.add("&7No configuration set");
                lore.add("&7This block has no custom drops");
            }

            lore.add("");
            lore.add("&eLeft-click to edit");
            lore.add("&cRight-click to remove");

            // Use actual block material but change name color if overridden
            Material displayMaterial = block;
            String displayName = config.isCompleteRandomization() ?
                "&c" + block.name().toLowerCase().replace("_", " ") + " &7[CHAOS]" :
                "&a" + block.name().toLowerCase().replace("_", " ");

            ItemStack blockItem = GuiUtils.createItem(
                displayMaterial,
                displayName,
                lore.toArray(new String[0])
            );

            setItem(10 + (i - startIndex), blockItem);
        }
        
        // Add new block button
        ItemStack addButton = GuiUtils.createItem(
            Material.EMERALD,
            "&aAdd New Block",
            "&7Click to add a new block",
            "&7with random drops",
            "",
            "&eClick to add"
        );
        setItem(48, addButton);

        // Search button
        ItemStack searchButton = GuiUtils.createItem(
            Material.COMPASS,
            "&bSearch Blocks/Items",
            "&7Search for specific blocks or items",
            "&7by typing their name",
            "",
            "&eClick to search"
        );
        setItem(50, searchButton);
        
        // Navigation buttons
        if (currentPage > 0) {
            setItem(45, GuiUtils.createPreviousPageButton());
        }

        if (endIndex < displayBlocks.size()) {
            setItem(53, GuiUtils.createNextPageButton());
        }
        
        // Back button
        setItem(46, GuiUtils.createBackButton());
        
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
            List<Material> displayBlocks = config.isCompleteRandomization() ?
                getAllValidMaterials() : new ArrayList<>(config.getConfiguredBlocks());
            if ((currentPage + 1) * itemsPerPage < displayBlocks.size()) {
                currentPage++;
                refresh();
            }
        } else if (slot == 46) { // Back
            RandomBlockDropsGui mainGui = new RandomBlockDropsGui(plugin);
            plugin.getGuiManager().openGui(player, mainGui);
        } else if (slot == 48) { // Add new block
            player.sendMessage(GuiUtils.colorize("&6Hold a block in your hand and type &e/rbd add &6to add it to the configuration!"));
            player.closeInventory();
        } else if (slot == 50) { // Search
            player.closeInventory();
            player.sendMessage(GuiUtils.colorize("&6Type your search term in chat (or 'cancel' to cancel):"));
            player.sendMessage(GuiUtils.colorize("&7Example: 'stone', 'diamond', 'wood', etc."));
            plugin.getSearchManager().startSearch(player);
        } else if (slot == 52) { // Close
            player.closeInventory();
        } else if (slot >= 10 && slot <= 43) { // Block configuration slots
            handleBlockClick(event, player, slot);
        }
    }
    
    private void handleBlockClick(InventoryClickEvent event, Player player, int slot) {
        List<Material> displayBlocks = config.isCompleteRandomization() ?
            getAllValidMaterials() : new ArrayList<>(config.getConfiguredBlocks());
        int index = (currentPage * itemsPerPage) + (slot - 10);

        if (index >= 0 && index < displayBlocks.size()) {
            Material block = displayBlocks.get(index);
            
            if (event.isRightClick()) {
                // Remove block configuration
                config.removeBlockConfiguration(block);
                player.sendMessage(GuiUtils.colorize("&cRemoved configuration for " + 
                    block.name().toLowerCase().replace("_", " ")));
                refresh();
            } else if (event.isLeftClick()) {
                // Edit block configuration
                BlockDropEditGui editGui = new BlockDropEditGui(plugin, block);
                plugin.getGuiManager().openGui(player, editGui);
            }
        }
    }

    private List<Material> getAllValidMaterials() {
        List<Material> allMaterials = new ArrayList<>();

        for (Material material : Material.values()) {
            // Skip materials that can't be items
            if (!material.isItem()) {
                continue;
            }

            // Skip air and technical materials
            if (material.isAir() || material == Material.BARRIER ||
                material == Material.STRUCTURE_VOID || material == Material.STRUCTURE_BLOCK ||
                material == Material.COMMAND_BLOCK || material == Material.CHAIN_COMMAND_BLOCK ||
                material == Material.REPEATING_COMMAND_BLOCK || material == Material.JIGSAW ||
                material == Material.SPAWNER) {
                continue;
            }

            // Include blocks and breakable items that are valid items
            if (material.isBlock() || isValidPlantType(material)) {
                allMaterials.add(material);
            }
        }

        // Sort alphabetically for easier browsing
        allMaterials.sort((a, b) -> a.name().compareTo(b.name()));

        return allMaterials;
    }

    private boolean isValidPlantType(Material material) {
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
