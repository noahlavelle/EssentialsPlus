package com.noahlavelle.essentialsplus;

import com.noahlavelle.essentialsplus.commnds.FlyCommand;
import com.noahlavelle.essentialsplus.commnds.HealCommand;
import com.noahlavelle.essentialsplus.commnds.HealthCommand;
import com.noahlavelle.essentialsplus.commnds.SpeedCommand;
import com.noahlavelle.essentialsplus.games.BlockTrail;
import com.noahlavelle.essentialsplus.games.ShrinkingCircle;
import com.noahlavelle.essentialsplus.messages.JoinMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new FlyCommand(this);
        new HealCommand(this);
        new SpeedCommand(this);
        new HealthCommand(this);
        new ShrinkingCircle(this);

        getServer().getPluginManager().registerEvents(new JoinMessage(), this);
        getServer().getPluginManager().registerEvents(new ShrinkingCircle.Game(), this);
        getServer().getPluginManager().registerEvents(new BlockTrail(this), this);
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[CustomEssentials] Plugin is enabled");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[CustomEssentials] Plugin is disabled");
    }

}