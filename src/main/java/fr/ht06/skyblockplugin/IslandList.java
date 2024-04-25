package fr.ht06.skyblockplugin;

import java.util.*;

public class IslandList {
    private SkyblockPlugin main = SkyblockPlugin.getInstance();
    private Map<String, Map<String, String>> listeIS = new HashMap<>();

    //String id;

    public IslandList(/* id, String block, String name, String lore, String schem, Integer slot*/){
        //this.id = id;
    }

    public void createIsland(String id){
        Map<String, String> map1 = new HashMap<>();

        map1.put("Block", String.valueOf(main.getConfig().get("IS." + id + "." + "Block")));

        map1.put("Name", String.valueOf(main.getConfig().get("IS." + id + "." + "Name")));

        map1.put("Schematic", String.valueOf(main.getConfig().get("IS." + id + "." + "Schematic")));

        map1.put("Slot", String.valueOf(main.getConfig().get("IS." + id + "." + "Slot")));

        listeIS.put(id,map1);
    }

    public Map<String, Map<String, String>> seeallIsland(){
        return listeIS;
    }

    public Map<String, String> getIsland(String id){
        return listeIS.get(id);
    }

    public Map<String, String> getIslandBySlot(Integer slot){
        Map<String, String> liste;
        if (listeIS.size() < slot) SkyblockPlugin.getInstance().resetConfig();

        for (Map.Entry<String, Map<String, String>> a : listeIS.entrySet()){
            if (a.getValue().get("Slot").equals(slot.toString())){
                liste = listeIS.get(a.getKey());
                return liste;
            }
        }

        return null;
    }



}
