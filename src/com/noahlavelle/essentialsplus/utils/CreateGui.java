package com.noahlavelle.essentialsplus.utils;

import com.noahlavelle.essentialsplus.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CreateGui {

    private final String path;
    private final String title;
    private Main plugin;

    public CreateGui (Main plugin, String path, String title) {
        this.plugin = plugin;
        this.path = path;
        this.title = title;
    }

    public Inventory createGui () {
        Inventory gui = Bukkit.createInventory(null, 27, title);

        for (int i = 0; i <= 26; i++) {
            Material material = Material.getMaterial(plugin.getConfig().getString(path + (i + 1) + ".item"));
            String name = plugin.getConfig().getString(path + (i + 1) + ".name");
            String lore = plugin.getConfig().getString(path + (i + 1) + ".lore");

            ItemStack item = new ItemStack(material, 1);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(name);

            item.setItemMeta(meta);

            gui.setItem(i, item);
        }

        return gui;
    }



}
