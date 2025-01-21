package com.mcstaralliance.starwelcome.listener;

import com.mcstaralliance.starwelcome.StarWelcome;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        FileConfiguration config = StarWelcome.getInstance().getConfig();
        boolean isDebug = config.getBoolean("debug");
        if (isDebug) {
            // do nothing
        } else if (event.getPlayer().hasPlayedBefore()) {
            return;
        }

        Player newPlayer = event.getPlayer();
        broadcastWelcomeMessage(newPlayer);

    }

    private void broadcastWelcomeMessage(Player newPlayer) {
        // 创建欢迎消息
        TextComponent message = new TextComponent(ChatColor.GREEN + "欢迎新玩家 " + newPlayer.getName() + "! ");

        // 创建可点击部分
        TextComponent clickable = new TextComponent(ChatColor.YELLOW + "[点我欢迎]");
        clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/welcome " + newPlayer.getName()));

        // 使用新的 HoverEvent 构造方法
        clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("点击欢迎新玩家并获得奖励")));

        // 将可点击内容附加到欢迎消息
        message.addExtra(clickable);

        // 广播消息给所有其他玩家
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getUniqueId().equals(newPlayer.getUniqueId())) { // 除了新玩家
                player.spigot().sendMessage(message);
            }
        }
    }
}
