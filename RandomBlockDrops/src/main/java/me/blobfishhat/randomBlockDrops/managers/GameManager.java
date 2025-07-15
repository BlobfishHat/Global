package me.blobfishhat.randomBlockDrops.managers;

import me.blobfishhat.randomBlockDrops.RandomBlockDrops;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.HashSet;
import java.util.Set;

public class GameManager implements Listener {
    
    private final RandomBlockDrops plugin;
    private boolean pvpEnabled = false;
    private int pvpTimer = 0; // seconds until PvP enables
    private int borderShrinkTimer = 0; // seconds until border shrinks
    private WorldBorder worldBorder;
    private double initialBorderSize = 1000.0;
    private double finalBorderSize = 50.0;
    private ScoreboardManager scoreboardManager;
    private Scoreboard scoreboard;
    private Objective objective;
    private final Set<Player> alivePlayers = new HashSet<>();
    
    public GameManager(RandomBlockDrops plugin) {
        this.plugin = plugin;
        this.scoreboardManager = Bukkit.getScoreboardManager();
        this.scoreboard = scoreboardManager.getNewScoreboard();
        
        // Initialize world border
        World world = Bukkit.getWorlds().get(0);
        this.worldBorder = world.getWorldBorder();
        this.worldBorder.setSize(initialBorderSize);
        this.worldBorder.setCenter(0, 0);
        
        setupScoreboard();
        startGameTimer();
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupScoreboard() {
        objective = scoreboard.registerNewObjective("game", "dummy", 
            ChatColor.GOLD + "" + ChatColor.BOLD + "CHAOS SURVIVAL");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        updateScoreboard();
    }
    
    private void updateScoreboard() {
        // Clear existing scores
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }
        
        int line = 15;
        
        // Empty line
        objective.getScore(" ").setScore(line--);
        
        // Players alive
        objective.getScore(ChatColor.YELLOW + "Players Alive: " + ChatColor.WHITE + alivePlayers.size()).setScore(line--);
        
        // Empty line
        objective.getScore("  ").setScore(line--);
        
        // Border size with color coding
        double currentSize = worldBorder.getSize();
        String borderColor = getBorderColor(currentSize);
        objective.getScore(ChatColor.YELLOW + "Border Size: " + borderColor + (int)currentSize).setScore(line--);
        
        // Empty line
        objective.getScore("   ").setScore(line--);
        
        // PvP timer
        if (!pvpEnabled && pvpTimer > 0) {
            int minutes = pvpTimer / 60;
            int seconds = pvpTimer % 60;
            objective.getScore(ChatColor.RED + "PvP in: " + ChatColor.WHITE + 
                String.format("%d:%02d", minutes, seconds)).setScore(line--);
        } else if (pvpEnabled) {
            objective.getScore(ChatColor.RED + "" + ChatColor.BOLD + "PvP: ENABLED").setScore(line--);
        }
        
        // Border shrink timer
        if (borderShrinkTimer > 0) {
            int minutes = borderShrinkTimer / 60;
            int seconds = borderShrinkTimer % 60;
            objective.getScore(ChatColor.GOLD + "Border shrinks in: " + ChatColor.WHITE + 
                String.format("%d:%02d", minutes, seconds)).setScore(line--);
        }
        
        // Empty line
        objective.getScore("    ").setScore(line--);
        
        // Chaos mode status
        if (plugin.getBlockDropConfig().isCompleteRandomization()) {
            objective.getScore(ChatColor.DARK_RED + "" + ChatColor.BOLD + "CHAOS MODE").setScore(line--);
        }
        
        // Apply scoreboard to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }
    }
    
    private String getBorderColor(double borderSize) {
        double percentage = (borderSize - finalBorderSize) / (initialBorderSize - finalBorderSize);
        
        if (percentage > 0.6) {
            return ChatColor.GREEN.toString();
        } else if (percentage > 0.3) {
            return ChatColor.YELLOW.toString();
        } else {
            return ChatColor.RED.toString();
        }
    }
    
    private void startGameTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Update timers
                if (pvpTimer > 0) {
                    pvpTimer--;
                    if (pvpTimer == 0) {
                        enablePvP();
                    }
                }
                
                if (borderShrinkTimer > 0) {
                    borderShrinkTimer--;
                    if (borderShrinkTimer == 0) {
                        startBorderShrink();
                    }
                }
                
                updateScoreboard();
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
    }
    
    public void startGame() {
        pvpEnabled = false;
        pvpTimer = 10 * 60; // 10 minutes
        borderShrinkTimer = 30 * 60; // 30 minutes
        
        // Reset border
        worldBorder.setSize(initialBorderSize);
        
        // Add all online players as alive
        alivePlayers.clear();
        alivePlayers.addAll(Bukkit.getOnlinePlayers());
        
        // Announce game start
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "CHAOS SURVIVAL STARTED!");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "PvP will be enabled in 10 minutes!");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Border will start shrinking in 30 minutes!");
        
        updateScoreboard();
    }
    
    private void enablePvP() {
        pvpEnabled = true;
        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "PvP HAS BEEN ENABLED!");
        Bukkit.broadcastMessage(ChatColor.RED + "Players can now attack each other!");
        
        // Play sound to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
        }
    }
    
    private void startBorderShrink() {
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "BORDER IS NOW SHRINKING!");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "The world border will shrink to " + (int)finalBorderSize + " blocks over 10 minutes!");
        
        // Shrink border over 10 minutes (600 seconds)
        worldBorder.setSize(finalBorderSize, 600);
        
        // Play sound to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.5f);
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!pvpEnabled && event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            event.setCancelled(true);
            Player attacker = (Player) event.getDamager();
            attacker.sendMessage(ChatColor.RED + "PvP is currently disabled! " + 
                ChatColor.YELLOW + "It will be enabled in " + formatTime(pvpTimer));
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        alivePlayers.add(player);
        player.setScoreboard(scoreboard);
        updateScoreboard();
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        alivePlayers.remove(event.getPlayer());
        updateScoreboard();
    }
    
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }
    
    public boolean isPvpEnabled() {
        return pvpEnabled;
    }
    
    public int getPlayersAlive() {
        return alivePlayers.size();
    }
    
    public double getBorderSize() {
        return worldBorder.getSize();
    }
}
