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

import java.util.*;

public class BedWars implements CommandExecutor {

    private Main plugin;

    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    Map<String, Game> pendingGames = new HashMap<>();

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

        World source = Bukkit.getWorld("bw-speedway");
        WorldManager.copyWorld(source, "bw-" + id);

        Location mapTP = new Location(Bukkit.getWorld("bw-" + id), Integer.parseInt(plugin.getConfig().getString("bedwars.bw_speedway.spawn_points.wait.x")),
                Integer.parseInt(plugin.getConfig().getString("bedwars.bw_speedway.spawn_points.wait.y")),
                Integer.parseInt(plugin.getConfig().getString("bedwars.bw_speedway.spawn_points.wait.z")));
        player.teleport(mapTP);

        game.players.add(player.getUniqueId());

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

    public void joinGame (Player player, Game game) {
        System.out.println(game.id);

        Location mapTP = new Location(Bukkit.getWorld("bw-" + game.id), Integer.parseInt(plugin.getConfig().getString("bedwars.bw_speedway.spawn_points.wait.x")),
                Integer.parseInt(plugin.getConfig().getString("bedwars.bw_speedway.spawn_points.wait.y")),
                Integer.parseInt(plugin.getConfig().getString("bedwars.bw_speedway.spawn_points.wait.z")));
        player.teleport(mapTP);

        game.players.add(player.getUniqueId());
    }

    public static class Game implements Listener {

        private Main plugin;
        private Map<String, String> teams = new HashMap<>();

        String id;

        public Game (String id, Main plugin) {
            this.plugin = plugin;
            this.id = id;
        }

        public Game () {
        }


        ArrayList<UUID> players = new ArrayList<>();

        @EventHandler
        public void playerJoinWorld(PlayerChangedWorldEvent event) {

            if (event.getPlayer().getWorld() == Bukkit.getWorld("bw-" + id)) {
                System.out.println("run");
                initGame();
            }
        }


        public void initGame() {
            populateTeams();
            createGenerators();
            teleportPlayers();

            System.out.println(teams);
        }

        // Game Initialisation

        public void populateTeams () {
            for (String team : plugin.getConfig().getConfigurationSection("bedwars.bw_speedway.teams").getKeys(false)) {
                teams.put(team, plugin.getConfig().getString("bedwars.bw_speedway.teams." + team)); // UNTESTED CODE
            }
        }

        public void createGenerators () {

        }

        public void teleportPlayers () {

        }

    }
}
