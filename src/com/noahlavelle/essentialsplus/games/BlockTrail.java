package com.noahlavelle.essentialsplus.games;

import com.noahlavelle.essentialsplus.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.UUID;

public class BlockTrail implements Listener, CommandExecutor {

    ArrayList<UUID> enabledPlayers = new ArrayList<UUID>();

    @EventHandler
    public void onPlayerWalk(PlayerMoveEvent event) {

        Player player = event.getPlayer();

        if (enabledPlayers.contains(player.getUniqueId())) {

            int playerCoords[] = {player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()};

            Block block = player.getWorld().getBlockAt(playerCoords[0], playerCoords[1] - 1, playerCoords[2]);

            if (!(block.getType() == Material.AIR)) {
                block.setType(Material.RED_WOOL);
            }
        }
    }

    private Main plugin;

    public BlockTrail(Main plugin) {
        this.plugin = plugin;

        plugin.getCommand("blocktrail").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + plugin.getConfig().getString("player_only"));
            return true;
        }

        Player player = (Player) commandSender;
        UUID uuid = player.getUniqueId();

        if (!(player.hasPermission("essentialsplus.blocktrail"))) return true;

        if (enabledPlayers.contains(uuid)) {
            enabledPlayers.remove(uuid);
            commandSender.sendMessage(ChatColor.GREEN + plugin.getConfig().getString("blocktrail.disabled"));
        } else {
            enabledPlayers.add(uuid);
            commandSender.sendMessage(ChatColor.GREEN + plugin.getConfig().getString("blocktrail.enabled"));
        }

        return false;

    }
}
