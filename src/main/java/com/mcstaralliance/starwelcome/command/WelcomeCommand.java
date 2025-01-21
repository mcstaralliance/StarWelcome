package com.mcstaralliance.starwelcome.command;

import com.mcstaralliance.starwelcome.StarWelcome;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class WelcomeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration config = StarWelcome.getInstance().getConfig();
        boolean isDebug = config.getBoolean("debug");
        if (!(sender instanceof Player)) {
            return true;
        }
        Player welcomer = (Player) sender;
        boolean isParamInvalid = args.length != 1;
        if (isParamInvalid) {
            return false;
        }
        Player welcomed = Bukkit.getPlayerExact(args[0]);
        /*
         * welcomer 是欢迎了新玩家的玩家
         * welcomed 是被欢迎的新玩家
         */
        boolean isExistingPlayer = welcomed != null;
        if (!isExistingPlayer) {
            welcomer.sendMessage(ChatColor.RED + "玩家不存在或不在线，请联系管理员");
            return true;
        }
        boolean isSamePlayer = isDebug ? false : welcomed.getName().equals(welcomer.getName());
        boolean isOldPlayer = isDebug ? false : welcomed.hasPlayedBefore();
        HashMap<String, HashSet<String>> welcomedPlayers = getWelcomedPlayers();
        boolean hasNotBeenWelcomedBefore = !welcomedPlayers.containsKey(welcomed.getName());
        /*
         * hasNotBeenWelcomedBefore 指此玩家未被任何玩家欢迎过
         */
        if (isSamePlayer) {
            welcomer.sendMessage(ChatColor.RED + "不可以自己欢迎自己");
            return true;
        }
        if (isOldPlayer) { 
            welcomer.sendMessage(ChatColor.RED + "这不是一位新玩家，不能再次欢迎");
            return true;
        }
        if (hasNotBeenWelcomedBefore) {
            welcomedPlayers.put(welcomed.getName(), new HashSet<>());
            // 因从未被欢迎过，所以要加到 HashMap 里，才能进行欢迎
        }

        boolean hasWelcomed = isDebug ? false : welcomedPlayers.get(welcomed.getName()).contains(welcomer.getName());
        /*
         * hasWelcomed 表示老玩家是否已欢迎过此玩家
         */
        if (hasWelcomed) {
            welcomer.sendMessage(ChatColor.RED + "你已经欢迎过这个玩家了");
            return true;
        }

        // 记录欢迎过新玩家的玩家名
        welcomedPlayers.get(welcomed.getName()).add(welcomer.getName());

        // 保存更新后的数据
        saveWelcomeData();

        Bukkit.broadcastMessage(ChatColor.YELLOW + welcomer.getName() + " 欢迎 " + welcomed.getName() + " 加入服务器!");
        StarWelcome.getInstance().getEcon().depositPlayer(welcomed, 1000);
        StarWelcome.getInstance().getEcon().depositPlayer(welcomer, 1000);
        welcomed.sendMessage(ChatColor.GREEN + welcomer.getName() + " 欢迎了你，你因此获得了 1000 硬币的新人奖励!");
        welcomer.sendMessage(ChatColor.GREEN + "你因欢迎新玩家 " + welcomed.getName() + " 而获得了 1000 硬币的奖励!");
        return true;
    }

    private void saveWelcomeData() {
        FileConfiguration welcomeData = YamlConfiguration.loadConfiguration(StarWelcome.getInstance().getWelcomeDataFile());
        HashMap<String, HashSet<String>> welcomedPlayers = getWelcomedPlayers();
        for (String welcomed : welcomedPlayers.keySet()) {
            welcomeData.set(welcomed, new ArrayList<>(welcomedPlayers.get(welcomed)));
        }
        try {
            welcomeData.save(StarWelcome.getInstance().getWelcomeDataFile());
        } catch (IOException e) {
            StarWelcome.getInstance().getLogger().severe("无法保存欢迎数据!");
        }
    }

    private HashMap<String, HashSet<String>> getWelcomedPlayers() {
        FileConfiguration welcomeData = YamlConfiguration.loadConfiguration(StarWelcome.getInstance().getWelcomeDataFile());
        HashMap<String, HashSet<String>> welcomedPlayers = new HashMap<>();
        for (String welcomed : welcomeData.getKeys(false)) {
            HashSet<String> welcomers = new HashSet<>(welcomeData.getStringList(welcomed));
            welcomedPlayers.put(welcomed, welcomers);
        }
        return welcomedPlayers;
    }
}
