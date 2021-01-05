package com.noahlavelle.essentialsplus.commnds;

import com.noahlavelle.essentialsplus.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HealCommand implements CommandExecutor {

    private Main plugin;

    public HealCommand (Main plugin) {
        this.plugin = plugin;

        plugin.getCommand("heal").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + plugin.getConfig().getString("player_only"));
            return true;
        }

        Player player = (Player) commandSender;

        if (!(player.hasPermission("essentialsplus.heal"))) return true;

        player.setHealth(20);
        commandSender.sendMessage(ChatColor.GREEN + plugin.getConfig().getString("heal.healed"));


        return true;
    }


}
