package com.noahlavelle.essentialsplus.games;

import com.noahlavelle.essentialsplus.Main;
import com.noahlavelle.essentialsplus.utils.RandomFirework;
import com.noahlavelle.essentialsplus.utils.WorldManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.text.SimpleDateFormat;
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

                if (game.players.size() == 1) { // Change to 16 when done developing

                    for (UUID u : game.players) {
                        Player p = Bukkit.getPlayer(u);
                        p.sendTitle(ChatColor.GREEN + "10", "", 10, 20, 20);
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 0.5F);
                    }


                    for (int i = 5; i >= 0; i--) {
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

    public class Game {

        private Main plugin;
        private ArrayList<String> teams = new ArrayList<>();
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
                teams.add(team);
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
            Objective objective = scoreboard.registerNewObjective("scoreboard", "dummy", ChatColor.YELLOW + "" + ChatColor.BOLD + "BEDWARS");

            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            for (int i = 0; i < teams.size(); i++) {
                ChatColor color = ChatColor.valueOf(plugin.getConfig().getString("bedwars.bw-lighthouse.teams." + teams.get(i)));
                String teamName = teams.get(i).substring(0, 1).toUpperCase() + teams.get(i).substring(1) ;

                Score score = objective.getScore(color + "" + teams.get(i).toUpperCase().charAt(0) + " " + ChatColor.RESET + teamName + ": " + ChatColor.GREEN + "✔");
                score.setScore((teams.size() - i) + 2);
            }

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/YY");
            String formattedDate = format.format(new Date());

            Score dateScore = objective.getScore(ChatColor.GRAY + formattedDate + " " + id);
            Score footer = objective.getScore(ChatColor.YELLOW + plugin.getConfig().getString("bedwars.footer_message"));
            Score blankLineDate = objective.getScore("");
            Score blankLineFooter = objective.getScore(" ");

            footer.setScore(1);
            blankLineFooter.setScore(2);
            blankLineDate.setScore(teams.size() + 3);
            dateScore.setScore(teams.size() + 4);


            for (UUID u : players) {
                Player p = Bukkit.getPlayer(u);
                p.setScoreboard(scoreboard);
            }

        }

        public void createGenerators () {

            for (String gen : plugin.getConfig().getConfigurationSection("bedwars.bw-lighthouse.generators.base").getKeys(false)) {
                Location location = new Location(Bukkit.getWorld("bw-" + id), Double.parseDouble(plugin.getConfig().getString("bedwars.bw-lighthouse.generators.base." + gen + ".x")),
                        Double.parseDouble(plugin.getConfig().getString("bedwars.bw-lighthouse.generators.base." + gen + ".y")),
                        Double.parseDouble(plugin.getConfig().getString("bedwars.bw-lighthouse.generators.base." + gen + ".z")));

                Generator generator = new Generator(location, "base");
                generator.run();
            }

            for (String gen : plugin.getConfig().getConfigurationSection("bedwars.bw-lighthouse.generators.diamond").getKeys(false)) {
                Location location = new Location(Bukkit.getWorld("bw-" + id), Double.parseDouble(plugin.getConfig().getString("bedwars.bw-lighthouse.generators.diamond." + gen + ".x")),
                        Double.parseDouble(plugin.getConfig().getString("bedwars.bw-lighthouse.generators.diamond." + gen + ".y")),
                        Double.parseDouble(plugin.getConfig().getString("bedwars.bw-lighthouse.generators.diamond." + gen + ".z")));

                Generator generator = new Generator(location, "diamond");
                generator.run();
            }

            for (String gen : plugin.getConfig().getConfigurationSection("bedwars.bw-lighthouse.generators.emerald").getKeys(false)) {
                Location location = new Location(Bukkit.getWorld("bw-" + id), Double.parseDouble(plugin.getConfig().getString("bedwars.bw-lighthouse.generators.emerald." + gen + ".x")),
                        Double.parseDouble(plugin.getConfig().getString("bedwars.bw-lighthouse.generators.emerald." + gen + ".y")),
                        Double.parseDouble(plugin.getConfig().getString("bedwars.bw-lighthouse.generators.emerald." + gen + ".z")));

                Generator generator = new Generator(location, "emerald");
                generator.run();
            }

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

    public class Generator {

        private String type;
        private Location location;
        private Random rand = new Random(System.currentTimeMillis());

        public Generator (Location location, String type) {
            this.location = location;
            this.type = type;
        }

        public void run() {
            switch (type) {
                case "base":
                    spawnBase();
                break;
                case "diamond":
                    spawnDiamond();
                break;
                case "emerald":
                    spawnEmerald();
                break;
            }
        }

        public void spawnBase() {

            location.getWorld().dropItem(location, new ItemStack(Material.IRON_INGOT, 1));
            if (rand.nextInt(5) + 1 == 1) {
                location.getWorld().dropItem(location, new ItemStack(Material.GOLD_INGOT, 1));
            }

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                spawnBase();
            }, (Long.parseLong(plugin.getConfig().getString("bedwars.generators.base")) + (rand.nextInt(3))) * 20);
        }

        public void spawnDiamond() {
            location.getWorld().dropItem(location, new ItemStack(Material.DIAMOND, 1));

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                spawnDiamond();
            }, Long.parseLong(plugin.getConfig().getString("bedwars.generators.diamond")) * 20);
        }

        public void spawnEmerald() {
            location.getWorld().dropItem(location, new ItemStack(Material.EMERALD, 1));

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                spawnEmerald();
            }, Long.parseLong(plugin.getConfig().getString("bedwars.generators.emerald")) * 20);
        }

    }
}
