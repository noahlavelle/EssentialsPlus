package com.noahlavelle.essentialsplus.commnds;

import com.noahlavelle.essentialsplus.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {

    private Main plugin; // Accesses main class from here

    public FlyCommand(Main plugin) {
        this.plugin = plugin;

        plugin.getCommand("fly").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + plugin.getConfig().getString("player_only"));
            return true;
        }

        Player player = (Player) commandSender;

        if (!(player.hasPermission("essentialsplus.fly"))) {
            return true;
        }

        if (player.getAllowFlight()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            commandSender.sendMessage(ChatColor.GREEN + plugin.getConfig().getString("fly.disabled"));
            return true;
        } else {
            player.setAllowFlight(true);
            player.setFlying(true);
            commandSender.sendMessage(ChatColor.GREEN + plugin.getConfig().getString("fly.enabled"));
        }

        return false;
    }
}
