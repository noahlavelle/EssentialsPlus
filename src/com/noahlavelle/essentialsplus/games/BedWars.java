package com.noahlavelle.essentialsplus.games;

import com.noahlavelle.essentialsplus.Main;
import com.noahlavelle.essentialsplus.utils.WorldManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class BedWars implements CommandExecutor {

    private Main plugin;

    public BedWars (Main plugin) {
        this.plugin = plugin;

        plugin.getCommand("bedwars").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        Player player = ((Player) commandSender).getPlayer();

        World source = Bukkit.getWorld("bw-speedway");

        WorldManager.copyWorld(source, "bw-" + player.getUniqueId().toString());

        Location mapTP = new Location(Bukkit.getWorld("bw-" + player.getUniqueId().toString()), 0, 100, 0);

        player.teleport(mapTP);

        return false;
    }
}
