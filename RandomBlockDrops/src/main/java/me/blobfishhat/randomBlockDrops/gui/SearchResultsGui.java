package me.blobfishhat.randomBlockDrops.gui;

import me.blobfishhat.randomBlockDrops.RandomBlockDrops;
import me.blobfishhat.randomBlockDrops.config.BlockDropConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchResultsGui extends BaseGui {
    
    private final RandomBlockDrops plugin;
    private final BlockDropConfig config;
    private final String searchTerm;
    private int currentPage = 0;
    private final int itemsPerPage = 21;
    
    public SearchResultsGui(RandomBlockDrops plugin, String searchTerm) {
        super("&6Search Results: " + searchTerm, 54);
        this.plugin = plugin;
        this.config = plugin.getBlockDropConfig();
        this.searchTerm = searchTerm.toLowerCase();
    }
    
    @Override
    public void setupInventory() {
        GuiUtils.fillBorder(this);
        
        // Search info
        ItemStack searchInfo = GuiUtils.createItem(
            Material.COMPASS,
            "&eSearch Results",
            "&7Search term: &a" + searchTerm,
            "&7Found " + getSearchResults().size() + " matches"
        );
        setItem(4, searchInfo);
        
        // Get search results
        List<Material> searchResults = getSearchResults();
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, searchResults.size());
        
        // Display search results
        for (int i = startIndex; i < endIndex; i++) {
            Material material = searchResults.get(i);
            
            List<String> lore = new ArrayList<>();
            lore.add("&7Material: &e" + material.name());
            lore.add("&7Type: &e" + (material.isBlock() ? "Block" : "Item"));
            
            if (config.isCompleteRandomization()) {
                lore.add("&c[CHAOS MODE] - Drops random items");
            } else if (config.hasBlockConfiguration(material)) {
                lore.add("&a[CONFIGURED] - Has custom drops");
            } else {
                lore.add("&7[NOT CONFIGURED] - No custom drops");
            }
            
            lore.add("");
            lore.add("&eClick to configure this block");
            
            ItemStack resultItem = GuiUtils.createItem(
                material,
                "&a" + material.name().toLowerCase().replace("_", " "),
                lore.toArray(new String[0])
            );
            
            setItem(10 + (i - startIndex), resultItem);
        }
        
        // Navigation buttons
        if (currentPage > 0) {
            setItem(45, GuiUtils.createPreviousPageButton());
        }
        
        if (endIndex < searchResults.size()) {
            setItem(53, GuiUtils.createNextPageButton());
        }
        
        // New search button
        ItemStack newSearchButton = GuiUtils.createItem(
            Material.COMPASS,
            "&bNew Search",
            "&7Click to start a new search",
            "",
            "&eClick to search"
        );
        setItem(46, newSearchButton);
        
        // Back button
        setItem(47, GuiUtils.createBackButton());
        
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
            List<Material> searchResults = getSearchResults();
            if ((currentPage + 1) * itemsPerPage < searchResults.size()) {
                currentPage++;
                refresh();
            }
        } else if (slot == 46) { // New search
            AnvilSearchGui searchGui = new AnvilSearchGui(plugin);
            plugin.getGuiManager().openGui(player, searchGui);
        } else if (slot == 47) { // Back
            ConfigGui configGui = new ConfigGui(plugin);
            plugin.getGuiManager().openGui(player, configGui);
        } else if (slot == 52) { // Close
            player.closeInventory();
        } else if (slot >= 10 && slot <= 43) { // Material selection
            handleMaterialSelection(event, player, slot);
        }
    }
    
    private void handleMaterialSelection(InventoryClickEvent event, Player player, int slot) {
        List<Material> searchResults = getSearchResults();
        int index = (currentPage * itemsPerPage) + (slot - 10);
        
        if (index >= 0 && index < searchResults.size()) {
            Material selectedMaterial = searchResults.get(index);
            
            // Open block editor for this material
            BlockDropEditGui editGui = new BlockDropEditGui(plugin, selectedMaterial);
            plugin.getGuiManager().openGui(player, editGui);
        }
    }
    
    private List<Material> getSearchResults() {
        List<Material> allMaterials = new ArrayList<>();
        
        // Get all valid materials
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
            
            if (material.isBlock() || isValidPlantType(material)) {
                allMaterials.add(material);
            }
        }
        
        // Filter by search term
        allMaterials = allMaterials.stream()
            .filter(material -> material.name().toLowerCase().contains(searchTerm))
            .collect(Collectors.toList());
        
        // Sort alphabetically
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
