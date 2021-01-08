package com.noahlavelle.essentialsplus.games;

import com.noahlavelle.essentialsplus.Main;
import com.noahlavelle.essentialsplus.utils.CreateGui;
import com.noahlavelle.essentialsplus.utils.RandomFirework;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class LavaFloor  implements CommandExecutor, Listener {

    private Main plugin;

    static Map<UUID, Game> games = new HashMap<>();
    static Map<UUID, Inventory> guis = new HashMap<>();

    ArrayList<UUID> players = new ArrayList<>();
    ArrayList<UUID> deadPlayers = new ArrayList<>();

    private Inventory gui;
    private String[] strings = {"lava", "100", "40", "10"};
    private int awaitingValueStage;

    public LavaFloor (Main plugin) {
        this.plugin = plugin;

        plugin.getCommand("lavafloor").setExecutor(this);
    }

    @org.bukkit.event.EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        gui = guis.get(event.getWhoClicked().getUniqueId());
        if (event.getInventory() != gui) return;

        event.setCancelled(true);

        final ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();

        switch (event.getRawSlot()) {
            case 10:
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "[Floor is Lava] Please enter a valid material:");
                awaitingValueStage = 1;
            break;
            case 12:
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "[Floor is Lava] Please enter a start layer::");
                awaitingValueStage = 3;
            break;
            case 14:
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "[Floor is Lava] Please enter a valid rise delay");
                awaitingValueStage = 4;
            break;
            case 16:
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "[Floor is Lava] Please enter a valid rise size:");
                awaitingValueStage = 2;
            break;
            case 26:
                player.closeInventory();
                Game game = new Game(players, deadPlayers, Material.getMaterial(strings[0].toUpperCase()), Integer.parseInt(strings[1]), Integer.parseInt(strings[2]), player.getLocation(),
                    player, plugin, Integer.parseInt(strings[3]));
                for (UUID u : game.players) {
                    games.put(u, game);
                }

                game.run();
            break;
        }

    }

    @org.bukkit.event.EventHandler
    public void onAsyncChatEvent(PlayerChatEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = event.getPlayer();

        if (player != player) return;

        switch (awaitingValueStage) {
            case 1:
                try {

                    Material.getMaterial(event.getMessage().toUpperCase());

                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "[Floor is Lava] Invalid value\n" + ChatColor.GREEN + "Please enter a valid material:");
                    event.setCancelled(true);
                    return;
                }

                strings[0] = event.getMessage();
                player.openInventory(gui);
                event.setCancelled(true);
                break;
            case 2:
                try {

                    Integer.parseInt(event.getMessage());

                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "[Floor is Lava] Invalid value\n" + ChatColor.GREEN + "Please enter a valid size:");
                    event.setCancelled(true);
                    return;
                }

                strings[1] = event.getMessage();
                player.openInventory(gui);
                event.setCancelled(true);
                break;
            case 3:
                try {

                    Integer.parseInt(event.getMessage());

                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "[Floor is Lava] Invalid value\n" + ChatColor.GREEN + "Please enter a valid start layer:");
                    event.setCancelled(true);
                    return;
                }

                strings[2] = event.getMessage();
                player.openInventory(gui);
                event.setCancelled(true);
                break;
            case 4:
                try {

                    Integer.parseInt(event.getMessage());

                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "[Floor is Lava] Invalid value\n" + ChatColor.GREEN + "Please enter a valid rise delay (seconds):");
                    event.setCancelled(true);
                    return;
                }

                strings[3] = event.getMessage();
                player.openInventory(gui);
                event.setCancelled(true);

                break;
        }

        player.openInventory(gui);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + plugin.getConfig().getString("player_only"));
            return true;
        }

        Player player = ((Player) commandSender).getPlayer();

        Material material = Material.LAVA;
        int size = 100;
        int startLevel = 40;
        int delay = 10;
        Location centerLocation = player.getLocation();

       for (Entity e : player.getNearbyEntities(50, 50, 50)) {
           if (e instanceof Player) {
               players.add(e.getUniqueId());
           }
       }

       if (strings.length < 4) {
           gui = new CreateGui(plugin, "lavafloor.gui.", "The Floor Is Lava").createGui();

           player.openInventory(gui);
           guis.put(player.getUniqueId(), gui);
       } else {

           try {
               material = Material.getMaterial(strings[0].toUpperCase());
               size = Integer.parseInt(strings[1]);
               startLevel = Integer.parseInt(strings[2]);
               delay = Integer.parseInt(strings[3]);
           } catch (Exception e) {
               commandSender.sendMessage(ChatColor.RED + "[Lava Floor] Please enter a valid material, size, start level and rise delay");
           }

           players.add(player.getUniqueId());


           Game game = new Game(players, deadPlayers, material, size, startLevel, centerLocation, player, plugin, delay);

           for (UUID u : players) {
               games.put(u, game);
               Bukkit.getPlayer(u).teleport(player);
           }

           game.run();
       }

       return true;
    }

    public static class EventHandler implements Listener {

        @org.bukkit.event.EventHandler
        public void onEntityDamageEvent(EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player)) return;

            Player player = (Player) event.getEntity();

            Game game = getPlayersGame(player.getUniqueId());
            if (game == null) return;

            game.entityDamage(event);
        }

        @org.bukkit.event.EventHandler
        public void onPlayerDeath(PlayerDeathEvent event) {
            Player player = event.getEntity();

            Game game = getPlayersGame(player.getUniqueId());
            if (game == null) return;

            game.playerDeath(event);
        }

        public Game getPlayersGame(UUID u) {
            Game game = games.get(u);
            return game;

        }
    }

    public static class Game {

        private int delay;
        private Main plugin;
        private Player player;
        private Location centerLocation;
        private int startLevel;
        private int size;
        private Material material;
        private ArrayList<UUID> deadPlayers;
        private ArrayList<UUID> players;
        private World world;

        private boolean running = true;
        private BossBar bossBar;

        public Game (ArrayList<UUID> players, ArrayList<UUID> deadPlayers, Material material, int size, int startLevel, Location centerLocation, Player player, Main plugin, int delay) {
            this.players = players;
            this.deadPlayers = deadPlayers;
            this.material = material;
            this.size = size;
            this.startLevel = startLevel;
            this.centerLocation = centerLocation;
            this.player = player;
            this.world = player.getWorld();
            this.plugin = plugin;
            this.delay = delay;
        }

        public void run () {

            WorldBorder worldBorder = player.getWorld().getWorldBorder();
            worldBorder.setCenter(centerLocation);
            worldBorder.setSize(size);

            bossBar = Bukkit.createBossBar(ChatColor.LIGHT_PURPLE + "Blocks rising in " + delay + " seconds", BarColor.PINK, BarStyle.SOLID);

            for (UUID u : players) {
                bossBar.addPlayer(Bukkit.getPlayer(u));
            }

            bossBar.addPlayer(player);

            int step = 1;


            for (int y = startLevel; y <= 256; y++) {
                int finalY = y;
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    fillLayer(finalY);

                    if (finalY == 256) {
                        running = false;
                    }
                }, (delay * 20) * step);

                step++;
            }

            float[] timePassed = {0};

            updateBossBar(timePassed, bossBar);
        }

        public void updateBossBar (float[] timePassed, BossBar bossBar) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                timePassed[0] += 0.05F;

                if (timePassed[0] >= delay && running) {
                    timePassed[0] = 0;
                    bossBar.setProgress(0);
                    updateBossBar(timePassed, bossBar);
                    return;
                }


                bossBar.setProgress(timePassed[0] / delay);
                bossBar.setTitle(ChatColor.LIGHT_PURPLE + "Blocks rise in " + (int) Math.ceil(delay - timePassed[0]) + " seconds");

                if (running) {
                    updateBossBar(timePassed, bossBar);
                }
            }, 1L);
        }

        public void fillLayer (int y) {

            int lx = centerLocation.getBlockX() - (size / 2);
            int rx = centerLocation.getBlockX() + (size / 2);
            int lz = centerLocation.getBlockZ() - (size / 2);
            int rz = centerLocation.getBlockZ() + (size / 2);

            for (int x = lx; x <= rx; x++) {
                for (int z = lz; z <= rz; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material blockType = block.getType();

                    if (blockType == Material.AIR | blockType == Material.GRASS |blockType == Material.TALL_GRASS | blockType == Material.SNOW) {
                        block.setType(material);
                    }
                }
            }
        }


        public void playerDeath(PlayerDeathEvent event) {

            Player killed = event.getEntity();
            Player killer = event.getEntity().getKiller();

            if (killer instanceof Player) {
                killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 0.5F);
            }

            killed.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1F, 0.5F);
            killed.setHealth(20);

            setPlayerSpectate(event.getEntity());
        }

        public void entityDamage(EntityDamageEvent event) {
            if (deadPlayers.contains(event.getEntity().getUniqueId())) {
                event.setCancelled(true);
            }
        }

        public void setPlayerSpectate(Player player) {
            player.getInventory().clear();

            player.setWalkSpeed(0.4F);
            player.setFlySpeed(0.4F);

            player.setInvisible(true);
            player.setGameMode(GameMode.ADVENTURE);


            player.setDisplayName(ChatColor.GRAY + "[Floor is Lava]  " + player.getName() + ChatColor.RESET);
            player.setPlayerListName(ChatColor.GRAY + "[Floor is Lava] " + player.getName() + ChatColor.RESET);

            players.remove(player.getUniqueId());
            deadPlayers.add(player.getUniqueId());

            player.setAllowFlight(true);
            player.setFlying(true);

            if (players.size() == 1) {
                win(players.get(0));
            }
        }

        public void win(UUID uuid) {

            bossBar.removeAll();
            running = false;

            Player player = Bukkit.getPlayer(uuid);
            for (int i = 0; i < 25; i++) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    RandomFirework.spawnRandomFirework(player.getLocation());
                }, 5 * i);
            }


            player.sendTitle(ChatColor.GOLD + player.getName() + " has won!", ChatColor.GOLD + "Congratulations!", 10, 100, 20);
            player.setInvisible(false);
            players.remove(player);
            player.setDisplayName(player.getName());
            player.setPlayerListName(player.getName());
            games.remove(player.getUniqueId());
            for (UUID u : deadPlayers) {
                Player p = Bukkit.getPlayer(u);
                p.teleport(player);
                p.setInvisible(false);
                p.setDisplayName(p.getName());
                p.setPlayerListName(p.getName());
                p.setAllowFlight(false);
                p.setFlying(false);
                p.setGameMode(GameMode.SURVIVAL);
                deadPlayers.remove(p);
                games.remove(p.getUniqueId());
            }
        }

    }
}
