package com.noahlavelle.essentialsplus.games;

import com.noahlavelle.essentialsplus.Main;
import com.noahlavelle.essentialsplus.utils.WorldManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class BedWars implements CommandExecutor {

    private Main plugin;

    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    Map<String, Game> pendingGames = new HashMap<>();
    static Map<UUID, Game> games = new HashMap<>();

    public BedWars (Main plugin) {
        this.plugin = plugin;

        plugin.getCommand("bedwars").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        Player player = ((Player) commandSender).getPlayer();

        if (pendingGames.isEmpty()) {
            createGame(player);
        } else {
            joinGame(player, pendingGames.get(pendingGames.keySet().toArray()[0]));
        }


        return false;
    }

    public void createGame (Player player) {
        Random random = new Random(System.currentTimeMillis());
        String id = generateGameID(random);

        Game game = new Game (id, plugin);

        pendingGames.put(id, game);

        World source = Bukkit.getWorld("bw-lighthouse");
        WorldManager.copyWorld(source, "bw-" + id);

        Location mapTP = new Location(Bukkit.getWorld("bw-" + id), Integer.parseInt(plugin.getConfig().getString("bedwars.bw-lighthouse.spawn_points.wait.x")),
                Integer.parseInt(plugin.getConfig().getString("bedwars.bw-lighthouse.spawn_points.wait.y")),
                Integer.parseInt(plugin.getConfig().getString("bedwars.bw-lighthouse.spawn_points.wait.z")));

        games.put(player.getUniqueId(), game);
        game.players.add(player.getUniqueId());
        player.teleport(mapTP);

    }

    public void joinGame (Player player, Game game) {

        Location mapTP = new Location(Bukkit.getWorld("bw-" + game.id), Integer.parseInt(plugin.getConfig().getString("bedwars.bw-lighthouse.spawn_points.wait.x")),
                Integer.parseInt(plugin.getConfig().getString("bedwars.bw-lighthouse.spawn_points.wait.y")),
                Integer.parseInt(plugin.getConfig().getString("bedwars.bw-lighthouse.spawn_points.wait.z")));

        games.put(player.getUniqueId(), game);
        game.players.add(player.getUniqueId());
        player.teleport(mapTP);
    }

    public String generateGameID (Random random) {
        String id = "";

        for (int i = 0; i <= 5; i++) {
            id += alphabet.charAt(random.nextInt(alphabet.length()));
        }

        if (pendingGames.containsKey(id)) {
            generateGameID(random);
            return "";
        } else {
            return id;
        }


    }


    public static class EventHandler implements Listener {

        @org.bukkit.event.EventHandler
        public void playerJoinWorld(PlayerChangedWorldEvent event) {

            Player player = (Player) event.getPlayer();

            Game game = getPlayersGame(player);
            if (game == null) return;


            if (player.getWorld() == Bukkit.getWorld("bw-" + game.id)) {

                if (game.players.size() == 2) { // Change to 16 when done developing

                    for (int i = 5; i >= 0; i--) {
                        System.out.println(i);
                        final int finalI = i;
                        game.plugin.getServer().getScheduler().runTaskLater(game.plugin, () -> game.plugin.getServer().getScheduler().runTaskLater(game.plugin, () -> {
                            for (UUID u : game.players) {
                                Player p = Bukkit.getPlayer(u);
                                if (finalI > 0) {
                                    p.sendTitle(ChatColor.RED + "" + finalI, "", 10, 20, 20);
                                }
                                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 0.5F);
                            }

                            if (finalI == 0) {
                                game.initGame();
                            }
                        }, 20L * (5 - finalI)), 100L);



                    }
                }
            }
        }

        public Game getPlayersGame(Player player) {
            Game game = games.get(player.getUniqueId());
            return game;

        }
    }

    public static class Game {

        private Main plugin;
        private Map<String, String> teams = new HashMap<>();
        private Map<UUID, String> playerTeams = new HashMap<>();

        String id;

        public Game (String id, Main plugin) {
            this.plugin = plugin;
            this.id = id;
        }

        public Game () {
        }


        ArrayList<UUID> players = new ArrayList<>();

        public void initGame() {
            populateTeams();
            createScoreboard();
            createGenerators();
            teleportPlayers();
        }

        // Game Initialisation

        public void populateTeams () {
            List<String> teamAssign = new ArrayList<>();

            for (String team : plugin.getConfig().getConfigurationSection("bedwars.bw-lighthouse.teams").getKeys(false)) {
                teams.put(team, plugin.getConfig().getString("bedwars.bw-lighthouse.teams." + team));
                teamAssign.add(team);
                teamAssign.add(team);
            }

            for (UUID u : players) {
                playerTeams.put(u, teamAssign.get(0));
                teamAssign.remove(0);
            }
        }

        public void createScoreboard() {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective objective = scoreboard.registerNewObjective("scoreboard", "dummy", ChatColor.RED + "BedWars");

            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            String[] keys = teams.keySet().toArray(new String[0]);

            for (int i = keys.length; i > 0; i--) {
                System.out.println(teams);
                Score score = objective.getScore(ChatColor.getByChar(teams.get(i - 1)) + "" + teams.get(i - 1).toUpperCase().charAt(0) + ": ✔");
                score.setScore(i);
            }

            for (UUID u : players) {
                Player p = Bukkit.getPlayer(u);
                p.setScoreboard(scoreboard);
            }

        }

        public void createGenerators () {

        }

        public void teleportPlayers () {
            for (UUID u : playerTeams.keySet()) {
                Player player = Bukkit.getPlayer(u);
                String team = playerTeams.get(u);

                Location location = new Location(player.getWorld(), Integer.parseInt(plugin.getConfig().getString("bedwars.bw-lighthouse.spawn_points." + team + ".x")),
                        Integer.parseInt(plugin.getConfig().getString("bedwars.bw-lighthouse.spawn_points." + team + ".y")),
                        Integer.parseInt(plugin.getConfig().getString("bedwars.bw-lighthouse.spawn_points." + team + ".z")));

                player.teleport(location);
                player.setDisplayName(ChatColor.getByChar(plugin.getConfig().getString("bedwars.bw-lighthouse.teams." + team)) + player.getDisplayName() + ChatColor.RESET);
                player.setPlayerListName(ChatColor.getByChar(plugin.getConfig().getString("bedwars.bw-lighthouse.teams." + team)) + player.getDisplayName() + ChatColor.RESET);

            }
        }

    }
}
