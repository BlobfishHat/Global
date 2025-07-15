package me.blobfishhat.randomBlockDrops.managers;

import me.blobfishhat.randomBlockDrops.RandomBlockDrops;
import me.blobfishhat.randomBlockDrops.gui.GuiUtils;
import me.blobfishhat.randomBlockDrops.gui.SearchResultsGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SearchManager implements Listener {
    
    private final RandomBlockDrops plugin;
    private final Map<UUID, Boolean> playersSearching;
    
    public SearchManager(RandomBlockDrops plugin) {
        this.plugin = plugin;
        this.playersSearching = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void startSearch(Player player) {
        playersSearching.put(player.getUniqueId(), true);
    }
    
    public void stopSearch(Player player) {
        playersSearching.remove(player.getUniqueId());
    }
    
    public boolean isSearching(Player player) {
        return playersSearching.containsKey(player.getUniqueId());
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (!isSearching(player)) {
            return;
        }
        
        event.setCancelled(true);
        String searchTerm = event.getMessage().trim();
        
        // Handle cancel
        if (searchTerm.equalsIgnoreCase("cancel")) {
            stopSearch(player);
            player.sendMessage(GuiUtils.colorize("&cSearch cancelled."));
            return;
        }
        
        // Process search
        stopSearch(player);
        
        // Run on main thread
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (searchTerm.isEmpty()) {
                player.sendMessage(GuiUtils.colorize("&cSearch term cannot be empty!"));
                return;
            }
            
            player.sendMessage(GuiUtils.colorize("&aSearching for: &e" + searchTerm));

            // Open search results GUI
            SearchResultsGui searchGui = new SearchResultsGui(plugin, searchTerm);
            plugin.getGuiManager().openGui(player, searchGui);
        });
    }
}
