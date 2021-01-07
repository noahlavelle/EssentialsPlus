package com.noahlavelle.essentialsplus.games;

import com.noahlavelle.essentialsplus.Main;
import com.noahlavelle.essentialsplus.utils.CreateGui;
import com.noahlavelle.essentialsplus.utils.RandomFirework;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Array;
import java.util.*;

public class ShrinkingCircle implements CommandExecutor, Listener {

    private Main plugin;

    static Map<UUID, Game> games = new HashMap<>();
    static Map<UUID, Inventory> guis = new HashMap<>();

    private Inventory gui;
    private int awaitingValueStage = 0;
    private Player activeGuiPlayer;
    private String[] args = {"50", "1", ""};

    public ShrinkingCircle (Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("shrinkingcircle").setExecutor(this);
    }

    @org.bukkit.event.EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        gui = guis.get(event.getWhoClicked().getUniqueId());
        if (event.getInventory() != gui) return;

        event.setCancelled(true);

        final ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        activeGuiPlayer = player;

        switch (event.getRawSlot()) {
            case 11:
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Please enter a value for the radius:");
                awaitingValueStage = 1;
            break;
            case 13:
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Please enter a value for the shrink size:");
                awaitingValueStage = 2;
            break;
            case 6:
            case 15:
            case 24:
                ItemStack item = new ItemStack(Material.getMaterial(plugin.getConfig().getString("shrinkingcircle.gui." + + (event.getRawSlot() + 1) + ".item")), 1);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(plugin.getConfig().getString("shrinkingcircle.gui." + + (event.getRawSlot() + 1) + ".name"));
                item.setItemMeta(meta);
                gui.setItem(event.getRawSlot(), item);

                gui.setItem(event.getRawSlot(), item);

                for (int slot = 6; slot <= 24; slot += 9) {
                    if (slot != event.getRawSlot()) {
                        item = new ItemStack(Material.getMaterial(plugin.getConfig().getString("shrinkingcircle.gui." + + (slot + 1) + ".item_off")), 1);
                        meta = item.getItemMeta();
                        meta.setDisplayName(plugin.getConfig().getString("shrinkingcircle.gui." + + (slot + 1) + ".name"));
                        item.setItemMeta(meta);
                        gui.setItem(slot, item);
                    }
                }

                if (event.getRawSlot() == 6) {
                    args[2] = "";
                } else if (event.getRawSlot() == 15) {
                    player.closeInventory();
                    player.sendMessage(ChatColor.GREEN + "Please enter a time value (seconds) for the automatic shrink:");
                    awaitingValueStage = 3;
            } else if (event.getRawSlot() == 24) {
                    args[2] = "death";
                }

            break;


            case 26:
                Game game = new Game(player, args, plugin, player.getLocation());
                for (Player p : game.players) {
                    games.put(p.getUniqueId(), game);
                }
            break;
        }

    }

    @org.bukkit.event.EventHandler
    public void onAsyncChatEvent(PlayerChatEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = event.getPlayer();

        if (player != activeGuiPlayer) return;

        switch (awaitingValueStage) {
            case 1:
                try {

                    Integer.parseInt(event.getMessage());

                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "[Shrinking Circle] Invalid value\n" + ChatColor.GREEN + "Please enter a value for the radius:");
                    event.setCancelled(true);
                    return;
                }

                args[0] = event.getMessage();
                activeGuiPlayer.openInventory(gui);
                event.setCancelled(true);
            break;
            case 2:
                try {

                    Integer.parseInt(event.getMessage());

                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "[Shrinking Circle] Invalid value\n" + ChatColor.GREEN + "Please enter a value for the shrink size:");
                    event.setCancelled(true);
                    return;
                }

                args[1] = event.getMessage();
                activeGuiPlayer.openInventory(gui);
                event.setCancelled(true);
            break;
            case 3:
                try {

                    Integer.parseInt(event.getMessage());

                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "[Shrinking Circle] Invalid value\n" + ChatColor.GREEN + "Please enter a time value (seconds) for the automatic shrink:");
                    event.setCancelled(true);
                    return;
                }

                args[2] = event.getMessage();
                activeGuiPlayer.openInventory(gui);
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

        if (strings.length == 0) {
            gui = new CreateGui(plugin, "shrinkingcircle.gui.", "Shrinking Circle").createGui();

            ItemStack replacementItem = new ItemStack(Material.getMaterial(plugin.getConfig().getString("shrinkingcircle.gui.16.item_off")));
            gui.setItem(15, replacementItem);
            replacementItem = new ItemStack(Material.getMaterial(plugin.getConfig().getString("shrinkingcircle.gui.25.item_off")));
            gui.setItem(24, replacementItem);

            player.openInventory(gui);

            guis.put(player.getUniqueId(), gui);
        } else {

            if (Integer.parseInt(strings[0]) > 150) {
                strings[0] = "150";
            } else if (Integer.parseInt(strings[0]) < 1) {
                strings[0] = "1";
            }

            if (Integer.parseInt(strings[1]) > 150) {
                strings[0] = "150";
            } else if (Integer.parseInt(strings[1]) < 1) {
                strings[0] = "1";
            }

            Game game = new Game(commandSender, command, s, strings, plugin, player.getLocation());
            for (Player p : game.players) {
                games.put(p.getUniqueId(), game);
            }
        }
        return true;
    }

    public static class EventHandler implements Listener {

        @org.bukkit.event.EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {

            Player player = event.getPlayer();

            Game game = getPlayersGame(player);
            if (game == null) return;

            game.shrinkCircle(event);
        }

        @org.bukkit.event.EventHandler
        public void onPlayerMoveEvent(PlayerMoveEvent event) {

            Player player = event.getPlayer();

            Game game = getPlayersGame(player);
            if (game == null) return;

            game.playerMove(event);
        }

        @org.bukkit.event.EventHandler
        public void onPlayerDeathEvent(PlayerDeathEvent event) {

            Player player = event.getEntity();

            Game game = getPlayersGame(player);
            if (game == null) return;

            game.playerDeath(event);

        }

        @org.bukkit.event.EventHandler
        public void onPlayerDamageEvent(EntityDamageEvent event) {

            if (!(event.getEntity() instanceof Player)) return;

            Player player = (Player) event.getEntity();

            Game game = getPlayersGame(player);
            if (game == null) return;

            game.playerDamageEvent(event);
        }

        @org.bukkit.event.EventHandler
        public void onItemPickupEvent(EntityPickupItemEvent event) {

            if (!(event.getEntity() instanceof Player)) return;

            Player player = (Player) event.getEntity();

            Game game = getPlayersGame(player);
            if (game == null) return;

            if (game.deadPlayers.contains(player)) event.setCancelled(true);
        }

        @org.bukkit.event.EventHandler
        public void onBlockPLaceEvent(BlockPlaceEvent event) {

            Player player = (Player) event.getPlayer();

            Game game = getPlayersGame(player);
            if (game == null) return;

            game.removeOutsideBlocks(event.getPlayer(), event.getBlock().getY());
        }

        public Game getPlayersGame(Player player) {
            Game game = games.get(player.getUniqueId());
            return game;

        }

    }

    public static class Game implements Listener {

        public Player player;
        Command command;
        String[] strings;
        CommandSender commandSender;
        Main plugin;

        ArrayList<Block> fallingBlocks = new ArrayList<Block>();
        ArrayList<Player> players = new ArrayList<Player>();
        ArrayList<Player> deadPlayers = new ArrayList<Player>();

        int shrinkSize;
        int radius;
        Location drawLocation;

        public Game(CommandSender commandSender, Command command, String s, String[] strings, Main plugin, Location drawLocation) {
            this.commandSender = commandSender;
            this.command = command;
            this.strings = strings;
            this.plugin = plugin;

            this.drawLocation = drawLocation;
            this.shrinkSize = Integer.parseInt(strings[1]);
            this.radius = Integer.parseInt(strings[0]);

            run();
        }

        public Game(Player commandSender, String[] strings, Main plugin, Location drawLocation) {
            this.commandSender = commandSender;
            this.strings = strings;
            this.plugin = plugin;

            this.drawLocation = drawLocation;
            this.shrinkSize = Integer.parseInt(strings[1]);
            this.radius = Integer.parseInt(strings[0]);

            run();
        }

        public Game() {
            // Overloading game
        }

        public void run() {

            Player player = (Player) commandSender;

            new EventHandler();

            for (Entity entity : player.getNearbyEntities(50, 50, 50)) {
                if (entity instanceof Player) players.add((Player) entity);
            }

            players.add(player);

            ItemStack item = new ItemStack(Material.RED_DYE, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "Shrink Circle");
            meta.addEnchant(Enchantment.MENDING, 1, true);
            item.setItemMeta(meta);

            player.getInventory().addItem(item);

            if (!(player.hasPermission("essentialsplus.games.shrinkingcircle"))) return;

            drawCircle(drawLocation, Material.RED_CONCRETE, radius + 1);
            drawCircle(drawLocation, Material.WHITE_CONCRETE, radius);

            for (Player selectedPlayer : players) {
                Location location = new Location(player.getWorld(), drawLocation.getBlockX(), drawLocation.getBlockY() + 1, drawLocation.getBlockZ(),
                        selectedPlayer.getLocation().getYaw(), 0);
                selectedPlayer.teleport(location);

                if (selectedPlayer != player) {
                    selectedPlayer.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD, 1));
                    selectedPlayer.getInventory().addItem(new ItemStack(Material.DIAMOND_AXE, 1));
                    selectedPlayer.getInventory().addItem(new ItemStack(Material.BOW, 1));
                    selectedPlayer.getInventory().addItem(new ItemStack(Material.SHIELD, 1));
                    selectedPlayer.getInventory().addItem(new ItemStack(Material.ARROW, 64));
                    selectedPlayer.getInventory().addItem(new ItemStack(Material.RED_WOOL, 128));

                    selectedPlayer.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
                    selectedPlayer.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                    selectedPlayer.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                    selectedPlayer.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
                }

                selectedPlayer.setDisplayName(ChatColor.AQUA + "[Shrinking Circle] " + selectedPlayer.getName() + ChatColor.RESET);
                selectedPlayer.setPlayerListName(ChatColor.AQUA + "[Shrinking Circle] " + selectedPlayer.getName() + ChatColor.RESET);

                if (selectedPlayer == player) {
                    selectedPlayer.setDisplayName(ChatColor.GOLD + "[Shrinking Circle] [Host] " + selectedPlayer.getName() + ChatColor.RESET);
                    selectedPlayer.setPlayerListName(ChatColor.GOLD + "[Shrinking Circle] [Host] " + selectedPlayer.getName() + ChatColor.RESET);
                }
            }

            commandSender.sendMessage(ChatColor.GREEN + "[Shrinking Circle] You have successfully started a game of Shrinking Circle with:\nA size of " + strings[0]
                    + "\nA shrink size of " + strings[1] + "\n" + players.size() + " players");

            if (strings.length < 3) return;

            if (!strings[2].equals("death") ) {
                try {
                    Integer.parseInt(strings[2]);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "[Shrinking Circle] Please enter a valid shrink size - number or death");
                    return;
                }

                for (int i = 0; i < Math.floor(radius / shrinkSize) + 1; i++) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        shrinkCircleRoot(player);
                    }, Integer.parseInt(strings[2]) * 20*i);
                }
            }

        }

        public void drawCircle(Location loc, Material mat, int r) {
            int cx = loc.getBlockX();
            int cy = loc.getBlockY();
            int cz = loc.getBlockZ();
            World w = loc.getWorld();
            int rSquared = r * r;
            for (int x = cx - r; x <= cx + r; x++) {
                for (int z = cz - r; z <= cz + r; z++) {

                    if (((cx - x) * (cx - x) == 0 & (cz - z) * (cz - z) == rSquared) | ((cx - x) * (cx - x) == rSquared & (cz - z) * (cz - z) == 0)) {
                        continue;
                    }

                    if ((cx - x) * (cx - x) + (cz - z) * (cz - z) <= rSquared) {
                        w.getBlockAt(x, cy, z).setType(mat);
                    }
                }
            }
        }

        public void shrinkCircle(PlayerInteractEvent event) {
            Player player = event.getPlayer();


            if (!(player.hasPermission("essentialsplus.games.shrinkingcircle"))) return;

            if (player.getInventory().getItemInMainHand().getType() == Material.RED_DYE) {
                shrinkCircleRoot(player);
            }
            return;
        }

        public void removeOutsideBlocks(Player player, int y) {
            int cx = drawLocation.getBlockX();
            int cz = drawLocation.getBlockZ();
            World w = drawLocation.getWorld();

            int r = radius + 1;
            int rSquared = r * r;
            for (int x = cx - Integer.parseInt(strings[0] + 1); x <= cx + Integer.parseInt(strings[0] + 1); x++) {
                for (int z = cz - Integer.parseInt(strings[0] + 1); z <= cz + Integer.parseInt(strings[0] + 1); z++) {
                    if (((cx - x) * (cx - x) + (cz - z) * (cz - z) > rSquared | (((cx - x) * (cx - x) == 0 & (cz - z) * (cz - z) == rSquared) | ((cx - x) * (cx - x) == rSquared & (cz - z) * (cz - z) == 0)))) {
                        Block block = w.getBlockAt(x, y, z);
                        Material blockType = block.getType();
                        Location blockLocation = block.getLocation();
                        if (!(blockType == Material.AIR)) {
                            block.setType(Material.AIR);
                            Location location = new Location(player.getWorld(), blockLocation.getBlockX() + 0.5, blockLocation.getBlockY() + 0.0, blockLocation.getBlockZ() + 0.5);
                            player.getWorld().spawnFallingBlock(location, blockType, (byte) 0);
                        }
                    }
                }
            }
        }

        public void shrinkCircleRoot(Player player) {

            drawCircle(drawLocation, Material.ORANGE_CONCRETE, radius + 1);

            radius -= shrinkSize;
            drawCircle(drawLocation, Material.RED_CONCRETE, radius + 1);
            drawCircle(drawLocation, Material.WHITE_CONCRETE, radius);

            radius += 1 + shrinkSize;

            for(int x = -radius; x <= radius; x++) {
                for(int z = -radius; z <= radius; z++) {
                    if((x*x) + (z*z) <= (radius * radius)) {
                        if (player.getWorld().getBlockAt(drawLocation.getBlockX() + x, drawLocation.getBlockY() ,
                                drawLocation.getBlockZ()  + z).getType() == Material.ORANGE_CONCRETE) {
                            Block block = player.getWorld().getBlockAt(drawLocation.getBlockX()  + x, drawLocation.getBlockY() ,
                                    drawLocation.getBlockZ()  + z);
                            fallingBlocks.add(block);
                        }
                    }

                }
            }

            Random rand = new Random(System.currentTimeMillis());

            radius -= 1 + shrinkSize;

            dropRandom(player, rand);
        }

        public void playerMove(PlayerMoveEvent event) {
            Player movedPlayer = event.getPlayer();

            if (!players.contains(movedPlayer)) {
                return;
            }

            if (movedPlayer != commandSender) {
                if (movedPlayer.getLocation().getBlockY() < drawLocation.getBlockY() - 10) {
                    setPlayerSpectate(movedPlayer);
                }

                if (movedPlayer.getLocation().getBlockY() == drawLocation.getBlockY() + 12) {
                    movedPlayer.teleport(new Location (movedPlayer.getWorld(), movedPlayer.getLocation().getBlockX(), movedPlayer.getLocation().getBlockY() - 10, movedPlayer.getLocation().getBlockZ()));
                }
            }
        }

        public void playerDeath(PlayerDeathEvent event) {
            if (!(event.getEntity().getKiller() instanceof Player)) return;

            Player killed = event.getEntity();
            Player killer = event.getEntity().getKiller();

            if (strings[2].equals("death")) {
                shrinkCircleRoot(killer);
            }

            killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 0.5F);
            killed.setHealth(20);

            setPlayerSpectate(event.getEntity());
        }

        public void playerDamageEvent(EntityDamageEvent event) {
            if (deadPlayers.contains(event.getEntity())) {
                event.setCancelled(true);
            }
        }

        public void setPlayerSpectate(Player player) {
            player.getInventory().clear();

            player.setWalkSpeed(0.4F);
            player.setFlySpeed(0.4F);

            player.setInvisible(true);
            player.setGameMode(GameMode.ADVENTURE);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1F, 0.5F);

            player.setDisplayName(ChatColor.GRAY + "[Shrinking Circle]  " + player.getName() + ChatColor.RESET);
            player.setPlayerListName(ChatColor.GRAY + "[Shrinking Circle] " + player.getName() + ChatColor.RESET);

            players.remove(player);
            deadPlayers.add(player);

            player.setAllowFlight(true);
            player.setFlying(true);

            if (players.size() == 2) {
                win(players.get(1));
            }
        }

        public void dropRandom(Player player, Random rand) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!fallingBlocks.isEmpty()) {
                Block block = fallingBlocks.get(rand.nextInt(fallingBlocks.size()));
                block.setType(Material.AIR);
                Location location = new Location(player.getWorld(), block.getLocation().getBlockX() + 0.5, block.getLocation().getBlockY() + 0.0, block.getLocation().getBlockZ() + 0.5);
                player.getWorld().spawnFallingBlock(location, Material.ORANGE_CONCRETE, (byte) 0);
                fallingBlocks.remove(block);
                dropRandom(player, rand);
                } else {
                    for (int y = drawLocation.getBlockY(); y <= drawLocation.getBlockY() + 10; y++) {
                        removeOutsideBlocks(player, y);
                    }
                }
            }, 1L);
        }
        public void win(Player player) {
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
            for (Player p : deadPlayers) {
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