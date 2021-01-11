package com.noahlavelle.essentialsplus.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class WorldManager {

    private World world;

    public static void copyFileStructure(File source, File target) {
        try {
            ArrayList<String> ignore = new ArrayList(Arrays.asList("uid.dat", "session.lock"));
            if (!ignore.contains(source.getName())) {
                int length;
                if (source.isDirectory()) {
                    if (!target.exists() && !target.mkdirs()) {
                        throw new IOException("Couldn't create world directory!");
                    }

                    String[] files = source.list();
                    String[] var4 = files;
                    int var5 = files.length;

                    for(length = 0; length < var5; ++length) {
                        String file = var4[length];
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        copyFileStructure(srcFile, destFile);
                    }
                } else {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);
                    byte[] buffer = new byte[1024];

                    while((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }

                    in.close();
                    out.close();
                }
            }

        } catch (IOException var10) {
            throw new RuntimeException(var10);
        }
    }

    public static void copyWorld(World originalWorld, String newWorldName) {
        File copiedFile = new File(Bukkit.getWorldContainer(), newWorldName);
        copyFileStructure(originalWorld.getWorldFolder(), copiedFile);
        (new WorldCreator(newWorldName)).createWorld();
    }
}
