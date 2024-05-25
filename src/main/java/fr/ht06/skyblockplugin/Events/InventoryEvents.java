package fr.ht06.skyblockplugin.Events;

import fr.ht06.skyblockplugin.Inventory.DeleteIslandInventory;
import fr.ht06.skyblockplugin.Inventory.IslandInventory;
import fr.ht06.skyblockplugin.Inventory.IslandSettingsInv;
import fr.ht06.skyblockplugin.Inventory.LeaveIslandInventory;
import fr.ht06.skyblockplugin.IslandManager.Island;
import fr.ht06.skyblockplugin.IslandManager.IslandManager;
import fr.ht06.skyblockplugin.LoadSchematic;
import fr.ht06.skyblockplugin.SkyblockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

public class InventoryEvents implements Listener {
    private Random random = new Random();
    private SkyblockPlugin main;
    private Integer[] coords = {1000, -1000};
    IslandManager islandManager = SkyblockPlugin.islandManager;


    public InventoryEvents(SkyblockPlugin main) {
        this.main = main;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event){

        Player player = (Player) event.getWhoClicked();

        //@NotNull List<HumanEntity> human = event.getViewers();
        if (event.getClickedInventory() == null) return;

        if (event.getClickedInventory().getHolder() instanceof IslandInventory){//Vérification si c'estle bon inventaire
            event.setCancelled(true);

            if (event.getSlot()< SkyblockPlugin.getInstance().getConfig().getConfigurationSection("IS.").getKeys(false).size()){

                Map<String, String> islandlist = SkyblockPlugin.islandList.getIslandBySlot(event.getSlot());
                if (!new File(getServer().getPluginsFolder().getAbsoluteFile() + "/SkyblockPlugin/Schematic/" + islandlist.get("Schematic")).exists()){
                    getLogger().severe("The file :'" + islandlist.get("Schematic")+"' didn't exist");
                    player.sendMessage("The schematic of the island didn't exist, please contact an administrator");
                    return;
                }

                //Si le joueur n'a pas d'île
                if (!islandManager.playerHasIsland(player.getName())) {
                    //Si quelqu'un a déjà une ile
                    if (!islandManager.getAllIsland().isEmpty()) {
                        player.sendMessage("§aCreation of the Island");
                        //Pour que les joueur ai pas ls meme coordonnées (temporaire)
                        boolean find = false;
                        int x = 0;
                        int z = 0;
                        List<Integer> finale = new ArrayList<>();


                        /*while (!find) {
                            x = x + coords[random.nextInt(0, 2)];
                            z = z + coords[random.nextInt(0, 2)];
                            finale.add(x);
                            finale.add(z);
                            if (!islandManager.getAllCoordinate().containsValue(finale)) {
                                loc = new Location(Bukkit.getWorld("world_Skyblock"), x, 70, z);
                                Island island = new Island(player.getName()+"'s Island",player.getName(), finale, loc);
                                islandManager.addIsland(island);
                                //SkyblockPlugin.islandCoordinates.addCoordinates(islandManager.getIslandbyplayer(player.getName()).getIslandName(), finale);

                                find = true;
                            } else {
                                finale = new ArrayList<>();
                            }

                            Island newIsland = new Island(player.getName()+"'s Island",player.getName(), finale, loc);
                            SkyblockPlugin.islandManager.addIsland(newIsland);

                        }*/
                        List<Integer> coord = getIslandCoordinate(player);
                        Location loc = new Location(Bukkit.getWorld("world_Skyblock"),coord.get(0), 70, coord.get(1));

                        Island newIsland = new Island(player.getName()+"'s Island", coord, loc);
                        newIsland.setOwner(player.getName());
                        SkyblockPlugin.islandManager.addIsland(newIsland);

                        //new LoadSchematic(loc,"world_Skyblock", "islandPlain");

                        //a régler
                        new LoadSchematic(loc, "world_Skyblock", islandlist.get("Schematic"));

                        player.closeInventory();
                        player.teleport(islandManager.getIslandbyplayer(player.getName()).getIslandSpawn());

                        SkyblockPlugin.worldBorderApi.setBorder(player, 100,new Location(Bukkit.getWorld("world_Skyblock"),
                                islandManager.getIslandbyplayer(player.getName()).getIslandCoordinates().get(0), 0,
                                islandManager.getIslandbyplayer(player.getName()).getIslandCoordinates().get(1)));

                    }

                    //Si personne n'a d'île donc 1er joueur
                    else {
                        player.sendMessage("You are the first player to create an island, thank you!");
                        player.sendMessage("Creation of the world 'Skyblock'");

                        Location loc = new Location(Bukkit.getWorld("world_Skyblock"), 0, 70, 0);
                        //new LoadSchematic(loc,"world_Skyblock", "IslandPlain");
                        new LoadSchematic(loc, loc.getWorld().getName(), islandlist.get("Schematic"));
                        player.sendMessage("§aCreation of the Island");

                        player.teleport(new Location(Bukkit.getWorld("world_Skyblock"), 0, 70, 0));
                        int x = 0;
                        int z = 0;
                        List<Integer> finale = new ArrayList<>();
                        finale.add(x);
                        finale.add(z);

                        Island island = new Island(player.getName()+"'s Island", finale, loc);
                        island.setOwner(player.getName());
                        islandManager.addIsland(island);
                        //SkyblockPlugin.islandCoordinates.addCoordinates(island.getIslandName(), island.getIslandCoordinates());


                        SkyblockPlugin.worldBorderApi.setBorder(player, 100,new Location(Bukkit.getWorld("world_Skyblock"),
                                islandManager.getIslandbyplayer(player.getName()).getIslandCoordinates().get(0), 0,
                                islandManager.getIslandbyplayer(player.getName()).getIslandCoordinates().get(1)));

                    }

                    //SkyblockPlugin.playerIslandSettings.put(player.getName(), SkyblockPlugin.ItemSettingBool);

                }


                //System.out.println(main.getServer().getWorldContainer().getAbsolutePath()+"/Skyblock/");
            }

        }

        if (event.getClickedInventory().getHolder() instanceof IslandSettingsInv){

            event.setCancelled(true);

            if (event.getSlot() >= islandManager.getIslandbyplayer(player.getName()).getAllSettings().size())return;  //permet de ne pas avoir d'erreurs quand on clique sur rien

            //if (event.getCurrentItem().isEmpty()) return;
            if (islandManager.getIslandbyplayer(player.getName()).getAllSettings().get(event.getCurrentItem().getType().name())){
                    SkyblockPlugin.setFalse(event.getCurrentItem());
                    islandManager.getIslandbyplayer(player.getName()).setSettings(event.getCurrentItem().getType().name(), false);
            }
            else{
                SkyblockPlugin.setTrue(event.getCurrentItem());
                islandManager.getIslandbyplayer(player.getName()).setSettings(event.getCurrentItem().getType().name(), true);

            }
            //player.sendMessage(String.valueOf(SkyblockPlugin.playerIslandSettings.get(player.getName())));

            player.updateInventory();
        }

        if (event.getClickedInventory().getHolder() instanceof DeleteIslandInventory){
            event.setCancelled(true);
            if (event.getSlot() ==  13){
                SkyblockPlugin.deleteIsland(player);
                player.closeInventory();
            }
        }

        if (event.getClickedInventory().getHolder() instanceof LeaveIslandInventory){
            event.setCancelled(true);
            if (event.getSlot() ==  13){
                Island island = islandManager.getIslandbyplayer(player.getName());
                //si il est membre
                if (islandManager.getIslandbyplayer(player.getName()).isMember(player.getName())){
                    player.closeInventory();
                    player.sendMessage("You leave " +  island.getIslandName());
                    //On le dégage
                    islandManager.getIslandbyplayer(player.getName()).removeMember(player.getName());
                    //On broadcast que le joueur à quitté l'île
                    island.BroadcastLeave(player);


                }
                else{  //S'il est modo
                    player.closeInventory();
                    player.sendMessage("You leave " + islandManager.getIslandbyplayer(player.getName()).getIslandName());
                    //On le dégage
                    islandManager.getIslandbyplayer(player.getName()).removeModerator(player.getName());
                    //On broadcast qu'il a quitté
                    island.BroadcastLeave(player);

                }
            }
        }


    }
    public List<Integer> getIslandCoordinate(Player player) {

        List<List<Integer>> liste = new ArrayList<>();
        List<Integer> twoValue = new ArrayList<>();
        int enHaut = 1;
        int aDroite = 0;
        int enBas = 1;
        int aGauche = 0;
        while (true){
            for (int x = -enHaut * 1000; x <= enHaut * 1000; x += 1000) {
                for (int z = -enHaut * 1000; z <= enHaut * 1000; z += 1000) {
                    twoValue.add(x);
                    twoValue.add(z);
                    if (!liste.contains(twoValue) && !islandManager.IslandCoordinateTaken(twoValue)) {
                        return twoValue;
                    }
                    else {
                        liste.add(twoValue);
                    }
                    twoValue = new ArrayList<>();
                }
                twoValue = new ArrayList<>();
            }
            enHaut += 1;
            for (int x = -aDroite * 1000; x <= aDroite * 1000; x += 1000) {
                for (int z = -aDroite * 1000; z <= aDroite * 1000; z += 1000) {
                    twoValue.add(x);
                    twoValue.add(z);
                    if (!liste.contains(twoValue) && !islandManager.IslandCoordinateTaken(twoValue)) {
                        return twoValue;
                    }
                    else {
                        liste.add(twoValue);
                    }
                    twoValue = new ArrayList<>();
                }
                twoValue = new ArrayList<>();
            }
            aDroite += 1;
            for (int x = -enBas * 1000; x <= enBas * 1000; x += 1000) {
                for (int z = -enBas * 1000; z <= enBas * 1000; z += 1000) {
                    twoValue.add(x);
                    twoValue.add(z);
                    if (!liste.contains(twoValue) && !islandManager.IslandCoordinateTaken(twoValue)) {
                        return twoValue;
                    }
                    else {
                        liste.add(twoValue);
                    }
                    twoValue = new ArrayList<>();
                }
                twoValue = new ArrayList<>();
            }
            enBas += 1;
            for (int x = -aGauche * 1000; x <= aGauche * 1000; x += 1000) {
                for (int z = -aGauche * 1000; z <= aGauche * 1000; z += 1000) {
                    twoValue.add(x);
                    twoValue.add(z);
                    if (!liste.contains(twoValue) && !islandManager.IslandCoordinateTaken(twoValue)) {
                        return twoValue;
                    }
                    else {
                        liste.add(twoValue);
                    }
                    twoValue = new ArrayList<>();
                }
                twoValue = new ArrayList<>();
            }
            aGauche += 1;

        }
    }

}