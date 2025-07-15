package me.blobfishhat.randomBlockDrops.config;

import me.blobfishhat.randomBlockDrops.RandomBlockDrops;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class BlockDropEntry {

    private final List<ItemStack> possibleDrops;
    private final Random random;
    private String lootTable;

    public BlockDropEntry() {
        this.possibleDrops = new ArrayList<>();
        this.random = new Random();
        this.lootTable = null;
    }
    
    // Drop rate removed - all drops are now 100%
    
    public List<ItemStack> getPossibleDrops() {
        return new ArrayList<>(possibleDrops);
    }
    
    public void addPossibleDrop(ItemStack item) {
        if (item != null && item.getType() != org.bukkit.Material.AIR) {
            possibleDrops.add(item.clone());
        }
    }
    
    public void removePossibleDrop(int index) {
        if (index >= 0 && index < possibleDrops.size()) {
            possibleDrops.remove(index);
        }
    }
    
    public void clearPossibleDrops() {
        possibleDrops.clear();
    }
    
    public List<ItemStack> getRandomDrops() {
        return getRandomDrops(null);
    }

    public List<ItemStack> getRandomDrops(RandomBlockDrops plugin) {
        List<ItemStack> drops = new ArrayList<>();

        // Use loot table if specified
        if (lootTable != null && plugin != null) {
            return getLootTableDrops(plugin);
        }

        // Use custom drops
        if (possibleDrops.isEmpty()) {
            return drops;
        }

        // Select a random drop from possible drops (100% drop rate)
        ItemStack selectedDrop = possibleDrops.get(random.nextInt(possibleDrops.size()));
        drops.add(selectedDrop.clone());

        return drops;
    }

    private List<ItemStack> getLootTableDrops(RandomBlockDrops plugin) {
        try {
            plugin.getLogger().info("Attempting to generate loot from table: " + lootTable);
            NamespacedKey key = NamespacedKey.minecraft(lootTable);
            LootTable table = Bukkit.getLootTable(key);

            if (table != null) {
                plugin.getLogger().info("Found loot table: " + key);
                // Create a basic loot context
                LootContext.Builder contextBuilder = new LootContext.Builder(new Location(Bukkit.getWorlds().get(0), 0, 0, 0));
                LootContext context = contextBuilder.build();

                Collection<ItemStack> loot = table.populateLoot(random, context);
                plugin.getLogger().info("Generated " + loot.size() + " items from loot table");

                List<ItemStack> result = new ArrayList<>(loot);
                for (ItemStack item : result) {
                    plugin.getLogger().info("Loot item: " + item.getType() + " x" + item.getAmount());
                }
                return result;
            } else {
                plugin.getLogger().warning("Loot table not found: " + key);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to generate loot from table: " + lootTable + " - " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
    
    public boolean isEmpty() {
        return possibleDrops.isEmpty();
    }
    
    public int getDropCount() {
        return lootTable != null ? 1 : possibleDrops.size();
    }

    public String getLootTable() {
        return lootTable;
    }

    public void setLootTable(String lootTable) {
        this.lootTable = lootTable;
        // Clear custom drops when using loot table
        if (lootTable != null) {
            possibleDrops.clear();
        }
    }

    public boolean isUsingLootTable() {
        return lootTable != null && !lootTable.isEmpty();
    }
}
