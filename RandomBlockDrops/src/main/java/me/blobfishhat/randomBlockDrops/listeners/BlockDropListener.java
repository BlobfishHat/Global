package me.blobfishhat.randomBlockDrops.listeners;

import me.blobfishhat.randomBlockDrops.RandomBlockDrops;
import me.blobfishhat.randomBlockDrops.config.BlockDropConfig;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BlockDropListener implements Listener {
    
    private final RandomBlockDrops plugin;
    private final BlockDropConfig config;
    
    public BlockDropListener(RandomBlockDrops plugin) {
        this.plugin = plugin;
        this.config = plugin.getBlockDropConfig();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        
        // Skip if player is in creative mode
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        
        // Skip if plugin is disabled
        if (!config.isEnabled()) {
            return;
        }
        
        plugin.getLogger().info("Processing block break for: " + blockType + " by player: " + player.getName());

        // Get random drops for this block (chaos mode or configured blocks)
        List<ItemStack> randomDrops = config.getRandomDrops(blockType);

        // If no drops and not in chaos mode, skip
        if (randomDrops.isEmpty() && !config.isCompleteRandomization()) {
            plugin.getLogger().info("No configuration found for block: " + blockType + " and chaos mode is off");
            return;
        }

        plugin.getLogger().info("Generated " + randomDrops.size() + " random drops");

        if (!randomDrops.isEmpty()) {
            // Cancel original drops - we only want our random drops
            event.setDropItems(false);

            Location dropLocation = event.getBlock().getLocation().add(0.5, 0.5, 0.5);

            // Drop the random items
            for (ItemStack drop : randomDrops) {
                if (drop != null && drop.getType() != Material.AIR) {
                    plugin.getLogger().info("Dropping item: " + drop.getType() + " x" + drop.getAmount());
                    event.getBlock().getWorld().dropItemNaturally(dropLocation, drop);
                }
            }
            
            // Optional: Send message to player about the random drop
            if (plugin.getConfig().getBoolean("notify-on-drop", false)) {
                StringBuilder message = new StringBuilder("&6Random drop: ");
                for (int i = 0; i < randomDrops.size(); i++) {
                    ItemStack drop = randomDrops.get(i);
                    message.append("&e").append(drop.getAmount()).append("x ")
                           .append(drop.getType().name().toLowerCase().replace("_", " "));
                    
                    if (i < randomDrops.size() - 1) {
                        message.append("&7, ");
                    }
                }
                
                player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message.toString()));
            }
        }
    }
}
