package me.blobfishhat.randomBlockDrops;

import me.blobfishhat.randomBlockDrops.commands.RandomBlockDropsCommand;
import me.blobfishhat.randomBlockDrops.config.BlockDropConfig;
import me.blobfishhat.randomBlockDrops.gui.GuiManager;
import me.blobfishhat.randomBlockDrops.listeners.BlockDropListener;
import me.blobfishhat.randomBlockDrops.listeners.CompleteRandomizationListener;
import me.blobfishhat.randomBlockDrops.managers.GameManager;
import me.blobfishhat.randomBlockDrops.managers.SearchManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class RandomBlockDrops extends JavaPlugin {

    private static RandomBlockDrops instance;
    private GuiManager guiManager;
    private BlockDropConfig blockDropConfig;
    private GameManager gameManager;
    private SearchManager searchManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize configuration
        saveDefaultConfig();
        blockDropConfig = new BlockDropConfig(this);
        blockDropConfig.loadConfig();

        // Initialize managers
        guiManager = new GuiManager(this);
        gameManager = new GameManager(this);
        searchManager = new SearchManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new BlockDropListener(this), this);
        getServer().getPluginManager().registerEvents(new CompleteRandomizationListener(this), this);

        // Register commands
        getCommand("randomblockdrops").setExecutor(new RandomBlockDropsCommand(this));

        getLogger().info("RandomBlockDrops has been enabled!");
        getLogger().info("Game features: PvP timer, Border shrinking, Scoreboard, Search system");
    }

    @Override
    public void onDisable() {
        if (blockDropConfig != null) {
            blockDropConfig.saveConfig();
        }
        getLogger().info("RandomBlockDrops has been disabled!");
    }

    public static RandomBlockDrops getInstance() {
        return instance;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public BlockDropConfig getBlockDropConfig() {
        return blockDropConfig;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public SearchManager getSearchManager() {
        return searchManager;
    }
}
