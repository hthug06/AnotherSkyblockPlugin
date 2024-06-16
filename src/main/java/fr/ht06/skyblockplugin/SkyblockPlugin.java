package fr.ht06.skyblockplugin;

import com.github.yannicklamprecht.worldborder.api.WorldBorderApi;
import fr.ht06.skyblockplugin.Commands.IsAdminCommand;
import fr.ht06.skyblockplugin.Commands.IslandCommand;
import fr.ht06.skyblockplugin.Commands.skyblockpluginCommand;
import fr.ht06.skyblockplugin.Config.DataConfig;
import fr.ht06.skyblockplugin.Config.IslandLevel;
import fr.ht06.skyblockplugin.Events.InventoryEvents;
import fr.ht06.skyblockplugin.Events.PlayerListeners;
import fr.ht06.skyblockplugin.IslandManager.Island;
import fr.ht06.skyblockplugin.IslandManager.IslandManager;
import fr.ht06.skyblockplugin.TabCompleter.IsadminCommandTab;
import fr.ht06.skyblockplugin.TabCompleter.IslandCommandTab;
import fr.ht06.skyblockplugin.placeholder.IslandLevelPlaceholder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public final class SkyblockPlugin extends JavaPlugin {


    public static WorldBorderApi worldBorderApi;
    public static SkyblockPlugin instance;
    public static IslandListByYAML islandList;
    public static IslandManager islandManager;
    public static YamlConfiguration customConfig;


    @Override
    public void onEnable() {
        instance =this;

        islandManager = new IslandManager();

        //Creation du world_skyblock si il existe pas et le loader sinon
        String settings = "{\"structures\":{\"structures\":{}},\"layers\":[{\"height\":9,\"block\":\"air\"},{\"height\":1,\"block\":\"air\"}],\"lakes\":false,\"features\":false,\"biome\":\"plains\"}";
        WorldCreator worldcreator = new WorldCreator("world_Skyblock");
        worldcreator.type(WorldType.FLAT).type(WorldType.FLAT).generatorSettings(settings).generateStructures(false);
        worldcreator.createWorld();
        new WorldCreator("world_Skyblock").createWorld();

        //Pour les worldBorder
        RegisteredServiceProvider<WorldBorderApi> worldBorderApiRegisteredServiceProvider = getServer().getServicesManager().getRegistration(WorldBorderApi.class);
        if (worldBorderApiRegisteredServiceProvider == null) {
            getLogger().info("API not found");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        worldBorderApi = worldBorderApiRegisteredServiceProvider.getProvider();

        //Toute les commandes , event et TabCompleter
        getCommand("is").setExecutor(new IslandCommand(this));
        getCommand("test").setExecutor(new Test(this));
        getCommand("skyblockplugin").setExecutor(new skyblockpluginCommand(this));
        getCommand("isadmin").setExecutor(new IsAdminCommand());
        getCommand("is").setTabCompleter(new IslandCommandTab(this));
        getCommand("isadmin").setTabCompleter(new IsadminCommandTab());
        getServer().getPluginManager().registerEvents(new InventoryEvents(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListeners(this), this);
        saveDefaultConfig();

        registerPlaceholder();

        //Les iles depuis la config
        List<String> IS = new ArrayList<>(getConfig().getConfigurationSection("IS.").getKeys(false));
        List<String> verificationSlot = new ArrayList<>();
        List<String> verificationName = new ArrayList<>();
        islandList = new IslandListByYAML();
        int i =0;

        //Import du schematic
        //saveResource("Schematic/IslandPlains.schem", false);


        //verification si les îles sont valide (vérifiactions des slots)
        for (String caractere: IS){
            islandList.createIsland(caractere);
            if (verificationSlot.contains(islandList.getIsland(caractere).get("Slot"))){
                getLogger().warning("The config isn't valid (you can't have the same slot on 2 items)");
                resetConfig();
                break;
            }
            else{
                verificationSlot.add(islandList.getIsland(caractere).get("Slot"));
            }

            //MArche pas ??
            //verification si les îles sont valide (vérifiactions des id)

            if (verificationName.contains(IS.get(i))){
                getLogger().warning("The config isn't valid (you can't have 2 same id)");
                resetConfig();
                break;
            }
            else{
                verificationName.add(IS.get(i));
            }
            i++;
        };

        //Dossier pour les schematic
        File dossier = new File(getServer().getPluginsFolder().getAbsoluteFile() + "/SkyblockPlugin/Schematic/");

        if (!dossier.exists()){
            dossier.mkdir();
        }

        //rajout des gens dans la hashmap
        DataConfig.setup();
        if (DataConfig.get().contains("Island")) {
            Island island;
            for (String v : DataConfig.get().getConfigurationSection("Island").getKeys(false)) {
                FileConfiguration dataconfig = DataConfig.get();
                Location loc = new Location(Bukkit.getWorld("world_Skyblock"),
                        dataconfig.getDouble("Island."+ v +".LocationSpawn.x"),
                        dataconfig.getDouble("Island."+ v +".LocationSpawn.y"),
                        dataconfig.getDouble("Island."+ v +".LocationSpawn.z"),
                        dataconfig.getInt("Island."+ v +".LocationSpawn.pitch"),
                        dataconfig.getInt("Island."+ v +".LocationSpawn.yaw"));

                //création de l'ile
                island = new Island(v, (List<Integer>) dataconfig.get("Island."+v+".Coordinates"), loc);

                //les joueurs
                island.setOwner((String) DataConfig.get().get("Island."+ v +".Players.Owner"));
                if (DataConfig.get().contains("Island."+ v +".Players.Moderators")){
                    island.setModerator((List<String>) DataConfig.get().getList("Island."+ v +".Players.Moderators"));
                }
                if (DataConfig.get().contains("Island."+ v +".Players.Members")){
                    island.setMember((List<String>) DataConfig.get().getList("Island."+ v +".Players.Members"));
                }

                //les settings
                @NotNull Map<String, Object> maps =  DataConfig.get().getConfigurationSection("Island."+v+".Settings").getValues(true);
                for (Map.Entry<String, Object> m : maps.entrySet()){
                    island.setSettings(m.getKey(), (Boolean) m.getValue());
                }

                //le level de l'île
                island.setLevel(dataconfig.getDouble("Island."+ v +".Level"));

                //ajout de l'île a l'island manager
                islandManager.addIsland(island);
            }
        }

        //On dégage la data.yml
        File dataYML = new File(Bukkit.getServer().getPluginManager().getPlugin("JustSkyblock").getDataFolder(), "data.yml");
        dataYML.delete();

        //Pour les level
        createLevelConfig();
        IslandLevel.save();
    }

    @Override
    public void onDisable() {

        //DataConfig.get().addDefault("Message", "this is the default message");
        DataConfig.setup();
        DataConfig.get().addDefault("WARNING 1", "This is the data file, it's stock data while the server is offline");
        DataConfig.get().addDefault("WARNING 2", "Don't touch this file or everything can be corrupt");
        DataConfig.get().addDefault("WARNING 3", "If you delete this file, every player data will be reset");
        DataConfig.get().setInlineComments("WARNING 1", Collections.singletonList("test"));
        DataConfig.get().createSection("Island");
        DataConfig.get().options().copyDefaults(true);
        //DataConfig.get().createSection("Players");
        DataConfig.save();

        //List<String> configIS = new ArrayList<>(DataConfig.get().getConfigurationSection("Players").getKeys(false));

        for (Island v: islandManager.getAllIsland()){

            DataConfig.get().getConfigurationSection("Island").createSection(v.getIslandName());

            //le Level
            DataConfig.get().getConfigurationSection("Island."+v.getIslandName()).set("Level", v.getLevel()); //le chef


            //les joueur de l'île
            DataConfig.get().getConfigurationSection("Island."+v.getIslandName()).createSection("Players").set("Owner", v.getOwner()); //le chef
            if (!v.getAllModerators().isEmpty()) DataConfig.get().getConfigurationSection("Island."+v.getIslandName()+".Players").set("Moderators",v.getAllModerators()); //les modos
            if (!v.getAllMembers().isEmpty()) DataConfig.get().getConfigurationSection("Island."+v.getIslandName()+".Players").set("Members",v.getAllMembers());  //les membres

            //la location du spawn
            DataConfig.get().getConfigurationSection("Island."+v.getIslandName()).createSection("LocationSpawn", v.getIslandSpawn().serialize());

            //les 2 coordonnée de l'île (en x et en z)
            DataConfig.get().getConfigurationSection("Island."+v.getIslandName()).createSection("Coordinates");
            DataConfig.get().set("Island."+v.getIslandName()+".Coordinates", v.getIslandCoordinates());

            DataConfig.get().getConfigurationSection("Island."+v.getIslandName()).createSection("Settings", v.getAllSettings());
        }
        DataConfig.save();
    }

    public static SkyblockPlugin getInstance(){
        return instance;
    }

    public void resetConfig(){
        File configFile = new File(getDataFolder(), "config.yml");
        configFile.delete();
        saveDefaultConfig();
        reloadConfig();
    }

    public static ItemStack setTrue(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        List<Component> liste = new ArrayList<>();
        liste.add(Component.text("ALLOW").color(TextColor.color(0x2FCC33)).decoration(TextDecoration.ITALIC,false).decorate(TextDecoration.BOLD));
        liste.add(Component.text("DISALLOW").color(TextColor.color(0x898F86)).decoration(TextDecoration.ITALIC,true));
        itemMeta.lore(liste);
        item.setItemMeta(itemMeta);
        return item;
    }

    public static ItemStack setFalse(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        List<Component> liste = new ArrayList<>();
        liste.add(Component.text("ALLOW").color(TextColor.color(0x898F86)).decoration(TextDecoration.ITALIC,true));
        liste.add(Component.text("DISALLOW").color(TextColor.color(0xCC322A)).decoration(TextDecoration.ITALIC,false).decorate(TextDecoration.BOLD));
        itemMeta.lore(liste);
        item.setItemMeta(itemMeta);
        return item;
    }


    public static void deleteIsland(Player player){
        Island island = islandManager.getIslandbyplayer(player.getName()).getIsland();
        Bukkit.dispatchCommand(player, "spawn");
        //y
        for (int y = -64; y < 320/*couche max à couche min*/; y++) {
            Location isLoc1 = new Location(Bukkit.getWorld("world_Skyblock"), island.getIslandCoordinates().get(0), 70, island.getIslandCoordinates().get(1)).clone().add(-50, -200, -50);;
            int x1 = isLoc1.getBlockX();
            int z1 = isLoc1.getBlockZ();

            Location isLoc2 = new Location(Bukkit.getWorld("world_Skyblock"), island.getIslandCoordinates().get(0), 70, island.getIslandCoordinates().get(1)).clone().add(50, 300, 50);;
            int x2 = isLoc2.getBlockX();
            int z2 = isLoc2.getBlockZ();
            //x
            for (int x = x1; x < x2; x++) {
                //z
                for (int z = z1; z < z2; z++) {
                    if (!Bukkit.getWorld("world_Skyblock").getBlockAt(new Location(Bukkit.getWorld("world_Skyblock"), x, y, z)).getType().isAir())
                        Bukkit.getWorld("world_Skyblock").getBlockAt(new Location(Bukkit.getWorld("world_Skyblock"), x, y, z)).setType(Material.AIR);

                    if (x==0 && y== 70 && z== 0) {
                        @NotNull Collection<Entity> livingEntity = new Location(Bukkit.getWorld("world_Skyblock"), x, y, z).getNearbyEntities(50, 400, 50);
                        Player playertarget;
                        for (Entity e : livingEntity) {
                            if (e instanceof Player) {
                                playertarget = ((Player) e).getPlayer();
                                Bukkit.dispatchCommand(playertarget,"spawn");
                            }
                            else e.remove();
                        }
                    }
                }
            }
        }
        islandManager.deleteIsland(island.getIslandName());
        //SkyblockPlugin.islandCoordinates.deleteCoordinates(island.getIslandName());
        player.getInventory().clear();
    }

    public void createLevelConfig(){
        //recup le dossier .yml dans les ressources
        super.saveResource("level.yml", false /* don't replace the file on disk if it exists */);
        //Pourvoir l'utiliser
        customConfig = YamlConfiguration.loadConfiguration(new File(Bukkit.getServer().getPluginManager().getPlugin("JustSkyblock").getDataFolder(), "level.yml"));
        //Utiliser cette class pour simplifier
        IslandLevel.setup();
    }

    private void registerPlaceholder(){
        new IslandLevelPlaceholder().register();
    }

}
