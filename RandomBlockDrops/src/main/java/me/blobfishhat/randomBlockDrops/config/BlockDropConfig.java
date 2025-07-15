package me.blobfishhat.randomBlockDrops.config;

import me.blobfishhat.randomBlockDrops.RandomBlockDrops;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BlockDropConfig {

    private final RandomBlockDrops plugin;
    private final Map<Material, BlockDropEntry> blockDrops;
    private final Map<Material, ItemStack> persistentRandomDrops;
    private boolean enabled;
    private boolean completeRandomization;

    public BlockDropConfig(RandomBlockDrops plugin) {
        this.plugin = plugin;
        this.blockDrops = new HashMap<>();
        this.persistentRandomDrops = new HashMap<>();
        this.enabled = true;
        this.completeRandomization = false;
    }
    
    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();

        // Load enabled status
        enabled = config.getBoolean("enabled", true);

        // Load randomization settings
        completeRandomization = config.getBoolean("complete-randomization.enabled", false);

        // Load persistent random drops
        loadPersistentRandomDrops(config);

        // Load block configurations
        ConfigurationSection blocksSection = config.getConfigurationSection("blocks");
        if (blocksSection != null) {
            for (String blockName : blocksSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(blockName.toUpperCase());
                    ConfigurationSection blockSection = blocksSection.getConfigurationSection(blockName);
                    
                    if (blockSection != null) {
                        String lootTable = blockSection.getString("loot-table", null);
                        List<Map<?, ?>> dropsList = blockSection.getMapList("drops");

                        BlockDropEntry entry = new BlockDropEntry();

                        // Set loot table if specified
                        if (lootTable != null && !lootTable.isEmpty()) {
                            entry.setLootTable(lootTable);
                        } else {
                            // Load individual drops
                            for (Map<?, ?> dropMap : dropsList) {
                                String materialName = (String) dropMap.get("material");
                                Object amountObj = dropMap.get("amount");
                                int amount = amountObj != null ? (Integer) amountObj : 1;

                                try {
                                    Material dropMaterial = Material.valueOf(materialName.toUpperCase());
                                    entry.addPossibleDrop(new ItemStack(dropMaterial, amount));
                                } catch (IllegalArgumentException e) {
                                    plugin.getLogger().warning("Invalid material in config: " + materialName);
                                }
                            }
                        }

                        blockDrops.put(material, entry);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid block material in config: " + blockName);
                }
            }
        }
        
        // Add default configurations if none exist
        if (blockDrops.isEmpty()) {
            addDefaultConfigurations();
        }
    }
    
    public void saveConfig() {
        FileConfiguration config = plugin.getConfig();

        // Save enabled status
        config.set("enabled", enabled);

        // Save randomization settings
        config.set("complete-randomization.enabled", completeRandomization);

        // Save persistent random drops
        savePersistentRandomDrops(config);

        // Clear existing block configurations
        config.set("blocks", null);

        // Save block configurations
        for (Map.Entry<Material, BlockDropEntry> entry : blockDrops.entrySet()) {
            String blockPath = "blocks." + entry.getKey().name().toLowerCase();
            BlockDropEntry dropEntry = entry.getValue();

            // Save loot table if present
            if (dropEntry.getLootTable() != null) {
                config.set(blockPath + ".loot-table", dropEntry.getLootTable());
            } else {
                // Save individual drops
                List<Map<String, Object>> dropsList = new ArrayList<>();
                for (ItemStack drop : dropEntry.getPossibleDrops()) {
                    Map<String, Object> dropMap = new HashMap<>();
                    dropMap.put("material", drop.getType().name().toLowerCase());
                    dropMap.put("amount", drop.getAmount());
                    dropsList.add(dropMap);
                }

                config.set(blockPath + ".drops", dropsList);
            }
        }

        plugin.saveConfig();
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        blockDrops.clear();
        persistentRandomDrops.clear();
        loadConfig();
    }
    
    private void addDefaultConfigurations() {
        // Stone drops
        BlockDropEntry stoneEntry = new BlockDropEntry();
        stoneEntry.addPossibleDrop(new ItemStack(Material.DIAMOND, 1));
        stoneEntry.addPossibleDrop(new ItemStack(Material.EMERALD, 1));
        stoneEntry.addPossibleDrop(new ItemStack(Material.GOLD_INGOT, 2));
        blockDrops.put(Material.STONE, stoneEntry);

        // Oak log drops
        BlockDropEntry logEntry = new BlockDropEntry();
        logEntry.addPossibleDrop(new ItemStack(Material.APPLE, 3));
        logEntry.addPossibleDrop(new ItemStack(Material.STICK, 5));
        blockDrops.put(Material.OAK_LOG, logEntry);

        // Dirt drops
        BlockDropEntry dirtEntry = new BlockDropEntry();
        dirtEntry.addPossibleDrop(new ItemStack(Material.BONE_MEAL, 2));
        dirtEntry.addPossibleDrop(new ItemStack(Material.WHEAT_SEEDS, 3));
        blockDrops.put(Material.DIRT, dirtEntry);
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Set<Material> getConfiguredBlocks() {
        return new HashSet<>(blockDrops.keySet());
    }
    
    public BlockDropEntry getBlockDropEntry(Material material) {
        return blockDrops.get(material);
    }
    
    public void saveBlockDropEntry(Material material, BlockDropEntry entry) {
        blockDrops.put(material, entry);
        saveConfig();
    }
    
    public void removeBlockConfiguration(Material material) {
        blockDrops.remove(material);
        saveConfig();
    }
    
    public boolean hasBlockConfiguration(Material material) {
        return blockDrops.containsKey(material);
    }
    
    public int getTotalDropConfigurations() {
        return blockDrops.values().stream()
            .mapToInt(entry -> entry.getPossibleDrops().size())
            .sum();
    }
    
    public List<ItemStack> getRandomDrops(Material material) {
        if (!enabled) {
            return new ArrayList<>();
        }

        // Check if this block has a specific loot table configuration (overrides chaos mode)
        BlockDropEntry entry = blockDrops.get(material);
        if (entry != null && entry.isUsingLootTable()) {
            return entry.getRandomDrops(plugin); // Full loot table drops
        }

        // Check for complete randomization
        if (completeRandomization) {
            return getPersistentRandomDrops(material); // Consistent random item
        }

        // Use configured drops if available
        if (entry != null) {
            return entry.getRandomDrops(plugin);
        }

        return new ArrayList<>();
    }

    private List<ItemStack> getCompletelyRandomDrops() {
        Random random = new Random();

        // Get all possible materials that can be items
        Material[] allMaterials = Material.values();
        List<Material> validMaterials = new ArrayList<>();

        for (Material mat : allMaterials) {
            if (mat.isItem() && !mat.isAir() && mat != Material.BARRIER && mat != Material.STRUCTURE_VOID) {
                validMaterials.add(mat);
            }
        }

        if (validMaterials.isEmpty()) {
            return new ArrayList<>();
        }

        // Generate only 1 random item for chaos mode
        List<ItemStack> drops = new ArrayList<>();
        Material randomMaterial = validMaterials.get(random.nextInt(validMaterials.size()));
        int amount = random.nextInt(Math.min(randomMaterial.getMaxStackSize(), 16)) + 1;
        drops.add(new ItemStack(randomMaterial, amount));

        return drops;
    }

    public boolean isCompleteRandomization() {
        return completeRandomization;
    }

    public void setCompleteRandomization(boolean completeRandomization) {
        this.completeRandomization = completeRandomization;

        // Clear persistent drops when toggling chaos mode
        if (!completeRandomization) {
            persistentRandomDrops.clear();
        }
    }

    private List<ItemStack> getPersistentRandomDrops(Material material) {
        // Generate a new random item each time (truly randomized)
        ItemStack randomDrop = generateSingleRandomItem();

        List<ItemStack> drops = new ArrayList<>();
        drops.add(randomDrop);
        return drops;
    }

    private ItemStack generateSingleRandomItem() {
        Random random = new Random();

        // Get only useful/competitive materials that players actually use
        List<Material> usefulMaterials = getUsefulMaterials();

        if (usefulMaterials.isEmpty()) {
            return new ItemStack(Material.STONE, 1);
        }

        // Truly random selection from useful materials
        Material selectedMaterial = usefulMaterials.get(random.nextInt(usefulMaterials.size()));

        // Generate reasonable amount (1-3 for most items, 1 for rare items)
        int amount = 1;
        if (isCommonItem(selectedMaterial)) {
            amount = random.nextInt(3) + 1; // 1-3 for common items
        } else if (isUncommonItem(selectedMaterial)) {
            amount = random.nextInt(2) + 1; // 1-2 for uncommon items
        }
        // Rare items stay at 1

        return new ItemStack(selectedMaterial, amount);
    }

    private List<Material> getUsefulMaterials() {
        List<Material> useful = new ArrayList<>();

        // Weapons & Tools
        useful.add(Material.DIAMOND_SWORD);
        useful.add(Material.NETHERITE_SWORD);
        useful.add(Material.DIAMOND_PICKAXE);
        useful.add(Material.NETHERITE_PICKAXE);
        useful.add(Material.DIAMOND_AXE);
        useful.add(Material.NETHERITE_AXE);
        useful.add(Material.BOW);
        useful.add(Material.CROSSBOW);
        useful.add(Material.TRIDENT);
        useful.add(Material.SHIELD);

        // Armor
        useful.add(Material.DIAMOND_HELMET);
        useful.add(Material.DIAMOND_CHESTPLATE);
        useful.add(Material.DIAMOND_LEGGINGS);
        useful.add(Material.DIAMOND_BOOTS);
        useful.add(Material.NETHERITE_HELMET);
        useful.add(Material.NETHERITE_CHESTPLATE);
        useful.add(Material.NETHERITE_LEGGINGS);
        useful.add(Material.NETHERITE_BOOTS);

        // Valuable Resources
        useful.add(Material.DIAMOND);
        useful.add(Material.EMERALD);
        useful.add(Material.NETHERITE_INGOT);
        useful.add(Material.ANCIENT_DEBRIS);
        useful.add(Material.GOLD_INGOT);
        useful.add(Material.IRON_INGOT);
        useful.add(Material.COPPER_INGOT);

        // Food & Potions
        useful.add(Material.GOLDEN_APPLE);
        useful.add(Material.ENCHANTED_GOLDEN_APPLE);
        useful.add(Material.COOKED_BEEF);
        useful.add(Material.BREAD);
        useful.add(Material.POTION);
        useful.add(Material.SPLASH_POTION);
        useful.add(Material.LINGERING_POTION);

        // Utility Items
        useful.add(Material.ENDER_PEARL);
        useful.add(Material.BLAZE_ROD);
        useful.add(Material.GHAST_TEAR);
        useful.add(Material.NETHER_STAR);
        useful.add(Material.DRAGON_EGG);
        useful.add(Material.ELYTRA);
        useful.add(Material.TOTEM_OF_UNDYING);
        useful.add(Material.ENCHANTED_BOOK);
        useful.add(Material.EXPERIENCE_BOTTLE);

        // Building Blocks (useful ones)
        useful.add(Material.OBSIDIAN);
        useful.add(Material.CRYING_OBSIDIAN);
        useful.add(Material.ANCIENT_DEBRIS);
        useful.add(Material.BEACON);
        useful.add(Material.CONDUIT);

        // Redstone & Technical
        useful.add(Material.REDSTONE);
        useful.add(Material.REDSTONE_BLOCK);
        useful.add(Material.PISTON);
        useful.add(Material.STICKY_PISTON);
        useful.add(Material.OBSERVER);
        useful.add(Material.HOPPER);
        useful.add(Material.DROPPER);
        useful.add(Material.DISPENSER);

        return useful;
    }

    private boolean isCommonItem(Material material) {
        switch (material) {
            case IRON_INGOT:
            case COPPER_INGOT:
            case BREAD:
            case COOKED_BEEF:
            case REDSTONE:
                return true;
            default:
                return false;
        }
    }

    private boolean isUncommonItem(Material material) {
        switch (material) {
            case GOLD_INGOT:
            case DIAMOND:
            case EMERALD:
            case ENDER_PEARL:
            case BLAZE_ROD:
            case GHAST_TEAR:
                return true;
            default:
                return false;
        }
    }

    private void loadPersistentRandomDrops(FileConfiguration config) {
        ConfigurationSection persistentSection = config.getConfigurationSection("persistent-random-drops");
        if (persistentSection != null) {
            for (String blockName : persistentSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(blockName.toUpperCase());
                    ConfigurationSection dropSection = persistentSection.getConfigurationSection(blockName);

                    if (dropSection != null) {
                        String dropMaterialName = dropSection.getString("material");
                        int amount = dropSection.getInt("amount", 1);

                        if (dropMaterialName != null) {
                            try {
                                Material dropMaterial = Material.valueOf(dropMaterialName.toUpperCase());
                                persistentRandomDrops.put(material, new ItemStack(dropMaterial, amount));
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Invalid drop material in persistent config: " + dropMaterialName);
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid block material in persistent config: " + blockName);
                }
            }
        }
    }

    private void savePersistentRandomDrops(FileConfiguration config) {
        // Clear existing persistent drops
        config.set("persistent-random-drops", null);

        // Save current persistent drops
        for (Map.Entry<Material, ItemStack> entry : persistentRandomDrops.entrySet()) {
            String blockPath = "persistent-random-drops." + entry.getKey().name().toLowerCase();
            ItemStack drop = entry.getValue();

            config.set(blockPath + ".material", drop.getType().name().toLowerCase());
            config.set(blockPath + ".amount", drop.getAmount());
        }
    }

    public void resetPersistentRandomDrops() {
        persistentRandomDrops.clear();
        saveConfig();
    }

    public void setBlockLootTable(Material material, String lootTable) {
        BlockDropEntry entry = blockDrops.get(material);
        if (entry == null) {
            entry = new BlockDropEntry();
        }

        if (lootTable != null) {
            entry.setLootTable(lootTable);
        } else {
            entry.setLootTable(null); // Remove loot table, keep existing custom drops
        }

        blockDrops.put(material, entry);
        saveConfig();
    }
}
