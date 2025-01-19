package com.mcstaralliance.starwelcome;

import com.mcstaralliance.starwelcome.command.WelcomeCommand;
import com.mcstaralliance.starwelcome.listener.PlayerJoinListener;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import net.milkbowl.vault.economy.Economy;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class StarWelcome extends JavaPlugin implements Listener {
    private Economy econ = null;
    private File welcomeDataFile;
    private static StarWelcome instance;

    @Override
    public void onEnable() {
        instance = this;
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        this.getCommand("welcome").setExecutor(new WelcomeCommand());

        // 初始化欢迎数据文件
        welcomeDataFile = new File(getDataFolder(), "data.yml");

        if (!welcomeDataFile.exists()) {
            saveResource("data.yml", false);
        }
    }
    public static StarWelcome getInstance() {
        return instance;
    }

    public File getWelcomeDataFile() {
        return welcomeDataFile;
    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        econ = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        return econ != null;
    }
    public Economy getEcon() {
        return econ;
    }
}