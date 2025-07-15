package me.blobfishhat.randomBlockDrops.listeners;

import me.blobfishhat.randomBlockDrops.RandomBlockDrops;
import me.blobfishhat.randomBlockDrops.config.BlockDropConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
// Crafting and smelting imports removed
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CompleteRandomizationListener implements Listener {
    
    private final RandomBlockDrops plugin;
    private final BlockDropConfig config;
    private final Random random;
    private long lastEventTime = 0;
    private static final long COOLDOWN_MS = 100; // 100ms cooldown between events

    public CompleteRandomizationListener(RandomBlockDrops plugin) {
        this.plugin = plugin;
        this.config = plugin.getBlockDropConfig();
        this.random = new Random();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!config.isEnabled() || !config.isCompleteRandomization()) {
            return;
        }

        // Cooldown check to prevent spam
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastEventTime < COOLDOWN_MS) {
            return;
        }
        lastEventTime = currentTime;

        // Clear original drops and replace with random items
        event.getDrops().clear();
        event.setDroppedExp(0);

        List<ItemStack> randomDrops = generateRandomItems();
        event.getDrops().addAll(randomDrops);

        plugin.getLogger().info("Randomized mob death drops for " + event.getEntity().getType() +
            " - generated " + randomDrops.size() + " random items");
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerFish(PlayerFishEvent event) {
        if (!config.isEnabled() || !config.isCompleteRandomization()) {
            return;
        }
        
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Item) {
            Item caughtItem = (Item) event.getCaught();
            
            // Replace the caught item with random items
            List<ItemStack> randomDrops = generateRandomItems();
            caughtItem.remove(); // Remove original item
            
            // Drop random items at fishing location
            Location fishLocation = caughtItem.getLocation();
            for (ItemStack drop : randomDrops) {
                fishLocation.getWorld().dropItemNaturally(fishLocation, drop);
            }
            
            plugin.getLogger().info("Randomized fishing catch - generated " + randomDrops.size() + " random items");
        }
    }
    
    // Crafting and smelting randomization removed per user request
    
    // ItemSpawn event disabled - was causing server crashes due to infinite loops
    // The other events (block breaking, mob kills, fishing) provide enough chaos
    
    private List<ItemStack> generateRandomItems() {
        List<ItemStack> drops = new ArrayList<>();

        // Get only useful materials (same as block drops)
        List<Material> usefulMaterials = getUsefulMaterials();

        if (usefulMaterials.isEmpty()) {
            drops.add(new ItemStack(Material.STONE, 1));
            return drops;
        }

        // Generate only 1 useful random item
        Material randomMaterial = usefulMaterials.get(random.nextInt(usefulMaterials.size()));

        // Generate reasonable amount based on item rarity
        int amount = 1;
        if (isCommonItem(randomMaterial)) {
            amount = random.nextInt(3) + 1; // 1-3 for common items
        } else if (isUncommonItem(randomMaterial)) {
            amount = random.nextInt(2) + 1; // 1-2 for uncommon items
        }
        // Rare items stay at 1

        drops.add(new ItemStack(randomMaterial, amount));
        return drops;
    }

    private List<Material> getUsefulMaterials() {
        List<Material> useful = new ArrayList<>();

        // Weapons & Tools
        useful.add(Material.DIAMOND_SWORD);
        useful.add(Material.NETHERITE_SWORD);
        useful.add(Material.DIAMOND_PICKAXE);
        useful.add(Material.NETHERITE_PICKAXE);
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

        // Food & Potions
        useful.add(Material.GOLDEN_APPLE);
        useful.add(Material.ENCHANTED_GOLDEN_APPLE);
        useful.add(Material.COOKED_BEEF);
        useful.add(Material.BREAD);

        // Utility Items
        useful.add(Material.ENDER_PEARL);
        useful.add(Material.BLAZE_ROD);
        useful.add(Material.GHAST_TEAR);
        useful.add(Material.NETHER_STAR);
        useful.add(Material.ELYTRA);
        useful.add(Material.TOTEM_OF_UNDYING);
        useful.add(Material.ENCHANTED_BOOK);
        useful.add(Material.EXPERIENCE_BOTTLE);

        // Building Blocks (useful ones)
        useful.add(Material.OBSIDIAN);
        useful.add(Material.BEACON);
        useful.add(Material.CONDUIT);

        return useful;
    }

    private boolean isCommonItem(Material material) {
        switch (material) {
            case IRON_INGOT:
            case BREAD:
            case COOKED_BEEF:
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
}
