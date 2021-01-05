package com.noahlavelle.essentialsplus.commnds;

import com.noahlavelle.essentialsplus.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpeedCommand implements CommandExecutor {

    private Main plugin;

    public SpeedCommand (Main plugin) {
        this.plugin = plugin;

        plugin.getCommand("speed").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + plugin.getConfig().getString("player_only"));
            return true;
        }

        Player player = (Player) commandSender;

        if (!(player.hasPermission("essentialsplus.speed"))) return true;

        float speed;

        try {
            speed = (float) Math.ceil(Float.parseFloat(strings[0]) + 1) / 10;
        } catch (Exception e) {
            commandSender.sendMessage(ChatColor.RED + plugin.getConfig().getString("value_nan"));
            return true;
        }

        if (speed > 1) {
            speed = 1;
        } else if (speed < -2) {
            speed = -2;
        }

        player.setWalkSpeed(speed);
        player.setFlySpeed(speed);

        commandSender.sendMessage(ChatColor.GREEN + "[EssentialsPlus] Set speed to " + ((speed * 10) - 1));

        return true;

    }
}
