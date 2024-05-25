package fr.ht06.skyblockplugin.TabCompleter;

import fr.ht06.skyblockplugin.IslandManager.IslandManager;
import fr.ht06.skyblockplugin.SkyblockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IslandCommandTab implements TabCompleter {

    IslandManager islandManager = SkyblockPlugin.islandManager;

    public IslandCommandTab(SkyblockPlugin skyblockPlugin) {
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (strings.length == 1){
            List<String> listearg0 = new ArrayList<>();
            listearg0.add("setspawn");
            listearg0.add("delete");
            listearg0.add("visit");
            listearg0.add("settings");
            listearg0.add("invite");
            listearg0.add("team");
            listearg0.add("join");
            listearg0.add("decline");
            listearg0.add("leave");
            listearg0.add("promote");
            listearg0.add("demote");
            listearg0.add("kick");

            return listearg0;
        }
        if (strings.length == 2){
            if (strings[0].equalsIgnoreCase("visit") || strings[0].equalsIgnoreCase("invite")){
                List<String> listearg1 = new ArrayList<>();

                for (Player p: Bukkit.getOnlinePlayers())
                        listearg1.add(p.getName());
                return listearg1;
            }
        }
        if (strings.length == 2){
            if (strings[0].equalsIgnoreCase("promote") || strings[0].equalsIgnoreCase("demote")){
                List<String> listearg1 = new ArrayList<>();

                for (Player p: Bukkit.getOnlinePlayers())
                    listearg1.add(p.getName());
                return listearg1;
            }
        }


        return List.of();
    }
}
