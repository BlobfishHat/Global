package me.blobfishhat.randomBlockDrops.gui;

import me.blobfishhat.randomBlockDrops.RandomBlockDrops;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiManager implements Listener {
    
    private final RandomBlockDrops plugin;
    private final Map<UUID, BaseGui> openGuis;
    
    public GuiManager(RandomBlockDrops plugin) {
        this.plugin = plugin;
        this.openGuis = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void openGui(Player player, BaseGui gui) {
        closeGui(player);
        openGuis.put(player.getUniqueId(), gui);
        gui.open(player);
    }
    
    public void closeGui(Player player) {
        BaseGui gui = openGuis.remove(player.getUniqueId());
        if (gui != null) {
            gui.close(player);
        }
    }
    
    public BaseGui getOpenGui(Player player) {
        return openGuis.get(player.getUniqueId());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        BaseGui gui = getOpenGui(player);
        
        if (gui != null && gui.getInventory().equals(event.getInventory())) {
            event.setCancelled(true);
            gui.handleClick(event);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        BaseGui gui = openGuis.get(player.getUniqueId());
        
        if (gui != null && gui.getInventory().equals(event.getInventory())) {
            openGuis.remove(player.getUniqueId());
            gui.onClose(player);
        }
    }
}
