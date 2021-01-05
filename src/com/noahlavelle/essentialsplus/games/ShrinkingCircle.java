package com.noahlavelle.essentialsplus.games;

import com.noahlavelle.essentialsplus.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class ShrinkingCircle implements CommandExecutor, Listener {

    ArrayList<Block> fallingBlocks = new ArrayList<Block>();

    private Main plugin;
    int radius;
    int playerX, playerY, playerZ;

    public ShrinkingCircle (Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("shrinkingcircle").setExecutor(this);
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + plugin.getConfig().getString("player_only"));
            return true;
        }

        Player player = (Player) commandSender;

        if (!(player.hasPermission("essentialsplus.games.shrinkingcircle"))) return true;

        playerX = player.getLocation().getBlockX();
        playerY = player.getLocation().getBlockY();
        playerZ = player.getLocation().getBlockZ();

        radius = Integer.parseInt(strings[0]);

        drawCircle(radius + 1, player, new int[] {playerX, playerY, playerZ}, Material.RED_CONCRETE);
        drawCircle(radius, player, new int[] {playerX, playerY, playerZ}, Material.WHITE_CONCRETE);

        return true;
    }

    void drawCircle(int radius, Player player, int[] playerCoords, Material material) {
        int radiusSquared = radius * radius;

        for(int x = -radius; x <= radius; x++) {
            for(int z = -radius; z <= radius; z++) {
                if((x*x) + (z*z) <= radiusSquared) {
                    player.getWorld().getBlockAt(playerCoords[0] + x, playerCoords[1], playerCoords[2] + z).setType(material);
                }

            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.getInventory().getItemInMainHand().getType() == Material.RED_DYE) {
            player.sendMessage("Shrunk");
            drawCircle(radius + 1, player, new int[] {playerX, playerY, playerZ}, Material.ORANGE_CONCRETE);
            radius -= 4;
            drawCircle(radius + 1, player, new int[] {playerX, playerY, playerZ}, Material.RED_CONCRETE);
            drawCircle(radius, player, new int[] {playerX, playerY, playerZ}, Material.WHITE_CONCRETE);

            radius += 6;

            for(int x = -radius; x <= radius; x++) {
                for(int z = -radius; z <= radius; z++) {
                    if((x*x) + (z*z) <= (radius * radius)) {
                        if (player.getWorld().getBlockAt(playerX + x, playerY, playerZ + z).getType() == Material.ORANGE_CONCRETE) {
                            Block block = player.getWorld().getBlockAt(playerX + x, playerY, playerZ + z);
                            fallingBlocks.add(block);
                        }
                    }

                }
            }
            dropRandom(plugin, fallingBlocks, player);

            radius -= 6;
        }
    }

    public static void dropRandom(Main plugin, ArrayList<Block> fallingBlocks, Player player) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Random rand = new Random(); // REPLACE WITH SEED FOR TIME
            Block block = fallingBlocks.get(rand.nextInt(fallingBlocks.size()));
            fallingBlocks.remove(block);
            block.setType(Material.AIR);
            player.getWorld().spawnFallingBlock(block.getLocation(), Material.ORANGE_CONCRETE, (byte) 0);

            if (!fallingBlocks.isEmpty()) {
                dropRandom(plugin, fallingBlocks, player);
            }
        }, 1L);
    }
}
