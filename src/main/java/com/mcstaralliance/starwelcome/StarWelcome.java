package com.mcstaralliance.starwelcome;

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
    private HashMap<String, HashSet<String>> welcomedPlayers = new HashMap<>();
    private File welcomeDataFile;
    private FileConfiguration welcomeData;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(this, this);

        // 初始化欢迎数据文件
        welcomeDataFile = new File(getDataFolder(), "data.yml");
        if (!welcomeDataFile.exists()) {
            saveResource("data.yml", false);
        }
        welcomeData = YamlConfiguration.loadConfiguration(welcomeDataFile);

        // 从配置文件加载数据
        loadWelcomeData();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        econ = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        return econ != null;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player newPlayer = event.getPlayer();
        if (!newPlayer.hasPlayedBefore()) {
            TextComponent message = new TextComponent(ChatColor.GREEN + "欢迎新玩家 " + newPlayer.getName() + "! ");
            TextComponent clickable = new TextComponent(ChatColor.YELLOW + "[点击欢迎]");
            clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/welcome " + newPlayer.getName()));
            clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("点击欢迎新玩家并获得奖励").create()));
            message.addExtra(clickable);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player != newPlayer) {  // 排除新玩家
                    player.spigot().sendMessage(message);
                }
            }

            // 给新玩家发送一个单独的欢迎消息
            newPlayer.sendMessage(ChatColor.GREEN + "欢迎来到服务器！");
        }
    }
    private void loadWelcomeData() {
        for (String welcomed : welcomeData.getKeys(false)) {
            HashSet<String> welcomers = new HashSet<>(welcomeData.getStringList(welcomed));
            welcomedPlayers.put(welcomed, welcomers);
        }
    }

    private void saveWelcomeData() {
        for (String welcomed : welcomedPlayers.keySet()) {
            welcomeData.set(welcomed, new ArrayList<>(welcomedPlayers.get(welcomed)));
        }
        try {
            welcomeData.save(welcomeDataFile);
        } catch (IOException e) {
            getLogger().severe("无法保存欢迎数据!");
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("welcome") && sender instanceof Player) {
            Player welcomer = (Player) sender;
            boolean isParamInvalid = args.length != 1;
            if (isParamInvalid) {
                return false;
            }
            Player welcomed = Bukkit.getPlayerExact(args[0]);
            boolean isExistingPlayer = welcomed == null;
            boolean isSamePlayer = welcomed == welcomer;
            boolean isOldPlayer = welcomed.hasPlayedBefore();
            boolean hasNotBeenWelcomed = !welcomedPlayers.containsKey(welcomed.getName());
            boolean hasBeenWelcomed = welcomedPlayers.get(welcomed.getName()).contains(welcomer.getName());
            if (isExistingPlayer) {
                return false;
            }
            if (isSamePlayer) {
                welcomer.sendMessage(ChatColor.RED + "不可以自己欢迎自己哦");
                return true;
            }
            if (isOldPlayer) {
                return false;
            }

            if (hasNotBeenWelcomed) {
                welcomedPlayers.put(welcomed.getName(), new HashSet<>());
            }
            if (hasBeenWelcomed) {
                welcomer.sendMessage(ChatColor.RED + "你已经欢迎过这个玩家了!");
                return true;
            }

            welcomedPlayers.get(welcomed.getName()).add(welcomer.getName());

            // 保存更新后的数据
            saveWelcomeData();

            Bukkit.broadcastMessage(ChatColor.YELLOW + welcomer.getName() + " 欢迎 " + welcomed.getName() + " 加入服务器!");
            econ.depositPlayer(welcomed, 1000);
            econ.depositPlayer(welcomer, 1000);
            welcomed.sendMessage(ChatColor.GREEN + welcomer.getName() + " 欢迎了你，你因此获得了 1000 硬币的欢迎奖励!");
            welcomer.sendMessage(ChatColor.GREEN + "你获得了 1000 硬币的欢迎奖励!");
            return true;
        }
        return false;
    }
}