package me.blobfishhat.randomBlockDrops.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class BaseGui {
    
    protected final String title;
    protected final int size;
    protected Inventory inventory;
    
    public BaseGui(String title, int size) {
        this.title = title;
        this.size = size;
        this.inventory = Bukkit.createInventory(null, size, title);
    }
    
    public void open(Player player) {
        setupInventory();
        player.openInventory(inventory);
    }
    
    public void close(Player player) {
        player.closeInventory();
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public String getTitle() {
        return title;
    }
    
    public int getSize() {
        return size;
    }
    
    protected void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }
    
    protected ItemStack getItem(int slot) {
        return inventory.getItem(slot);
    }
    
    public abstract void setupInventory();
    
    public abstract void handleClick(InventoryClickEvent event);
    
    public void onClose(Player player) {
        // Override if needed
    }
    
    public void refresh() {
        inventory.clear();
        setupInventory();
    }
}
