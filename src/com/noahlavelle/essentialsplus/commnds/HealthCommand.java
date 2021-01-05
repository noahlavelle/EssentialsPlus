package com.noahlavelle.essentialsplus.commnds;

import com.noahlavelle.essentialsplus.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HealthCommand implements CommandExecutor {

    private Main plugin;

    public HealthCommand (Main plugin) {
        this.plugin = plugin;

        plugin.getCommand("health").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + plugin.getConfig().getString("player_only"));
            return true;
        }

        Player player = (Player) commandSender;

        if (!(player.hasPermission("essentialsplus.health"))) return true;

        int health;

        try {
            health = (int) Math.ceil(Float.parseFloat(strings[0]));
        } catch (Exception e) {
            commandSender.sendMessage(ChatColor.RED + plugin.getConfig().getString("value_nan"));
            return true;
        }

        if (health < 1) {
            health = 1;
        } else if (health > 20) {
            health = 20;
        }

        player.setHealth(health);

        commandSender.sendMessage(ChatColor.GREEN + "[EssentialsPlus] Set health to " + health);

        return true;

    }
}
