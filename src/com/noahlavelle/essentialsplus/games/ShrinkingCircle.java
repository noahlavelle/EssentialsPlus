package com.noahlavelle.essentialsplus.games;

import com.noahlavelle.essentialsplus.Main;
import com.noahlavelle.essentialsplus.utils.RandomFirework;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ShrinkingCircle implements CommandExecutor {

    private Main plugin;

    static Map<UUID, Game> games = new HashMap<>();

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

        Player player = ((Player) commandSender).getPlayer();

        if (Integer.parseInt(strings[0]) > 150) {
            strings[0] = "150";
        }  else if (Integer.parseInt(strings[0]) < 1) {
            strings[0] = "1";
        }

        if (Integer.parseInt(strings[1]) > 150) {
            strings[0] = "150";
        }  else if (Integer.parseInt(strings[1]) < 1) {
            strings[0] = "1";
        }

        Game game = new Game(commandSender, command, s, strings, plugin, player.getLocation());
        for (Player p : game.players) {
            games.put(p.getUniqueId(), game);
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
        public void onPlayerMove(PlayerMoveEvent event) {

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

        public Game getPlayersGame(Player player) {
            Game game = games.get(player.getUniqueId());
            return game;

        }

    }

    public static class Game implements Listener {

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

                selectedPlayer.setDisplayName(ChatColor.AQUA + "[Shrinking Circle] " + selectedPlayer.getName() + ChatColor.RESET);
                selectedPlayer.setPlayerListName(ChatColor.AQUA + "[Shrinking Circle] " + selectedPlayer.getName() + ChatColor.RESET);
            }

            commandSender.sendMessage(ChatColor.GREEN + "[EssentialsPlus] You have successfully started a game of Shrinking Circle with:\nA size of " + strings[0]
                    + "\nA shrink size of " + strings[1] + "\n" + players.size() + " players");

            if (strings[2] != null && !strings[2].equals("death") ) {
                try {
                    Integer.parseInt(strings[2]);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "[EssentialsPlus] Please enter a valid shrink size - number or death");
                    return;
                }

                for (int i = 0; i < Math.floor(radius / shrinkSize); i++) {
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

            if (movedPlayer.getLocation().getBlockY() < drawLocation.getBlockY() - 10) {
                setPlayerSpectate(movedPlayer);
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

            if (players.size() == 1) {
                win(players.get(0));
            }
        }

        public void dropRandom(Player player, Random rand) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!fallingBlocks.isEmpty()) {
                Block block = fallingBlocks.get(rand.nextInt(fallingBlocks.size()));
                block.setType(Material.AIR);
                player.getWorld().spawnFallingBlock(block.getLocation(), Material.ORANGE_CONCRETE, (byte) 0);
                fallingBlocks.remove(block);
                    dropRandom(player, rand);
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