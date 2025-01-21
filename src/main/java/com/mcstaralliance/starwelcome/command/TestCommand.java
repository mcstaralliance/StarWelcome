package com.mcstaralliance.starwelcome.command;

import com.mcstaralliance.starwelcome.StarWelcome;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class TestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration config = StarWelcome.getInstance().getConfig();
        config.set("debug", !config.getBoolean("debug"));
        StarWelcome.getInstance().saveConfig();
        if (config.getBoolean("debug")) {
            sender.sendMessage(ChatColor.RED + "StarWelcome 调试模式已启动");
        } else {
            sender.sendMessage(ChatColor.RED + "StarWelcome 调试模式已关闭");
        }
        return true;
    }
}
