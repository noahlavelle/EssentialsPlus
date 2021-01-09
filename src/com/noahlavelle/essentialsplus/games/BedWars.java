package com.noahlavelle.essentialsplus.games;

import com.noahlavelle.essentialsplus.Main;
import com.noahlavelle.essentialsplus.utils.WorldManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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

        Game game = new Game (id);

        pendingGames.put(id, game);

        World source = Bukkit.getWorld("bw-speedway");
        WorldManager.copyWorld(source, "bw-" + id);

       ;

        String[] spawnCoords =  plugin.getConfig().getString("bedwars.bw-speedway.spawnPoints.wait");

        Location mapTP = new Location(Bukkit.getWorld("bw-" + id), 0, 100, 0);
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

    public void joinGame (Player player, Game game) {
        System.out.println(game.id);
        Location mapTP = new Location(Bukkit.getWorld("bw-" + game.id), 0, 100, 0);
        player.teleport(mapTP);
    }

    public static class Game {

        String id;

        public Game (String id) {
            this.id = id;
        }

        ArrayList<Player> players = new ArrayList<>();

    }
}
