package me.blobfishhat.randomBlockDrops.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootTableManager {
    
    private static final Map<String, String> LOOT_TABLE_ALIASES = new HashMap<>();
    
    static {
        // Structure loot tables
        LOOT_TABLE_ALIASES.put("endcity", "chests/end_city_treasure");
        LOOT_TABLE_ALIASES.put("end_city", "chests/end_city_treasure");
        LOOT_TABLE_ALIASES.put("stronghold", "chests/stronghold_corridor");
        LOOT_TABLE_ALIASES.put("dungeon", "chests/simple_dungeon");
        LOOT_TABLE_ALIASES.put("mineshaft", "chests/abandoned_mineshaft");
        LOOT_TABLE_ALIASES.put("desert_pyramid", "chests/desert_pyramid");
        LOOT_TABLE_ALIASES.put("jungle_temple", "chests/jungle_temple");
        LOOT_TABLE_ALIASES.put("woodland_mansion", "chests/woodland_mansion");
        LOOT_TABLE_ALIASES.put("ocean_ruin_cold", "chests/underwater_ruin_big");
        LOOT_TABLE_ALIASES.put("ocean_ruin_warm", "chests/underwater_ruin_small");
        LOOT_TABLE_ALIASES.put("shipwreck_treasure", "chests/shipwreck_treasure");
        LOOT_TABLE_ALIASES.put("shipwreck_supply", "chests/shipwreck_supply");
        LOOT_TABLE_ALIASES.put("buried_treasure", "chests/buried_treasure");
        LOOT_TABLE_ALIASES.put("pillager_outpost", "chests/pillager_outpost");
        LOOT_TABLE_ALIASES.put("bastion_treasure", "chests/bastion_treasure");
        LOOT_TABLE_ALIASES.put("bastion_other", "chests/bastion_other");
        LOOT_TABLE_ALIASES.put("bastion_bridge", "chests/bastion_bridge");
        LOOT_TABLE_ALIASES.put("bastion_hoglin_stable", "chests/bastion_hoglin_stable");
        LOOT_TABLE_ALIASES.put("ruined_portal", "chests/ruined_portal");
        LOOT_TABLE_ALIASES.put("ancient_city", "chests/ancient_city");
        LOOT_TABLE_ALIASES.put("ancient_city_ice_box", "chests/ancient_city_ice_box");
        
        // Village loot tables
        LOOT_TABLE_ALIASES.put("village_weaponsmith", "chests/village/village_weaponsmith");
        LOOT_TABLE_ALIASES.put("village_toolsmith", "chests/village/village_toolsmith");
        LOOT_TABLE_ALIASES.put("village_armorer", "chests/village/village_armorer");
        LOOT_TABLE_ALIASES.put("village_butcher", "chests/village/village_butcher");
        LOOT_TABLE_ALIASES.put("village_cartographer", "chests/village/village_cartographer");
        LOOT_TABLE_ALIASES.put("village_mason", "chests/village/village_mason");
        LOOT_TABLE_ALIASES.put("village_shepherd", "chests/village/village_shepherd");
        LOOT_TABLE_ALIASES.put("village_tannery", "chests/village/village_tannery");
        LOOT_TABLE_ALIASES.put("village_temple", "chests/village/village_temple");
        LOOT_TABLE_ALIASES.put("village_desert_house", "chests/village/village_desert_house");
        LOOT_TABLE_ALIASES.put("village_plains_house", "chests/village/village_plains_house");
        LOOT_TABLE_ALIASES.put("village_savanna_house", "chests/village/village_savanna_house");
        LOOT_TABLE_ALIASES.put("village_snowy_house", "chests/village/village_snowy_house");
        LOOT_TABLE_ALIASES.put("village_taiga_house", "chests/village/village_taiga_house");
        
        // Nether fortress
        LOOT_TABLE_ALIASES.put("nether_bridge", "chests/nether_bridge");
        
        // Igloo
        LOOT_TABLE_ALIASES.put("igloo", "chests/igloo_chest");
        
        // Fishing
        LOOT_TABLE_ALIASES.put("fishing", "gameplay/fishing");
        LOOT_TABLE_ALIASES.put("fishing_treasure", "gameplay/fishing/treasure");
        LOOT_TABLE_ALIASES.put("fishing_junk", "gameplay/fishing/junk");
        LOOT_TABLE_ALIASES.put("fishing_fish", "gameplay/fishing/fish");
        
        // Entity drops
        LOOT_TABLE_ALIASES.put("zombie", "entities/zombie");
        LOOT_TABLE_ALIASES.put("skeleton", "entities/skeleton");
        LOOT_TABLE_ALIASES.put("creeper", "entities/creeper");
        LOOT_TABLE_ALIASES.put("spider", "entities/spider");
        LOOT_TABLE_ALIASES.put("enderman", "entities/enderman");
        LOOT_TABLE_ALIASES.put("blaze", "entities/blaze");
        LOOT_TABLE_ALIASES.put("ghast", "entities/ghast");
        LOOT_TABLE_ALIASES.put("wither_skeleton", "entities/wither_skeleton");
        LOOT_TABLE_ALIASES.put("piglin", "entities/piglin");
        LOOT_TABLE_ALIASES.put("hoglin", "entities/hoglin");
        LOOT_TABLE_ALIASES.put("strider", "entities/strider");
        LOOT_TABLE_ALIASES.put("ender_dragon", "entities/ender_dragon");
        LOOT_TABLE_ALIASES.put("wither", "entities/wither");
    }
    
    public static String resolveLootTable(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        
        String lowerInput = input.toLowerCase().replace(" ", "_");
        
        // Check if it's an alias
        if (LOOT_TABLE_ALIASES.containsKey(lowerInput)) {
            return LOOT_TABLE_ALIASES.get(lowerInput);
        }
        
        // If it already looks like a full loot table path, return as is
        if (input.contains("/")) {
            return input;
        }
        
        // Try to find partial matches
        for (Map.Entry<String, String> entry : LOOT_TABLE_ALIASES.entrySet()) {
            if (entry.getKey().contains(lowerInput) || entry.getValue().contains(lowerInput)) {
                return entry.getValue();
            }
        }
        
        return input; // Return as-is if no match found
    }
    
    public static List<String> getAvailableLootTables() {
        return Arrays.asList(
            // Popular structure loot tables
            "endcity", "stronghold", "dungeon", "mineshaft", "desert_pyramid", 
            "jungle_temple", "woodland_mansion", "buried_treasure", "shipwreck_treasure",
            "ancient_city", "bastion_treasure", "pillager_outpost", "ruined_portal",
            
            // Village loot tables
            "village_weaponsmith", "village_toolsmith", "village_armorer", "village_butcher",
            "village_cartographer", "village_mason", "village_shepherd", "village_temple",
            
            // Other
            "nether_bridge", "igloo", "fishing", "fishing_treasure"
        );
    }
    
    public static String getLootTableDescription(String alias) {
        switch (alias.toLowerCase()) {
            case "endcity": return "End City treasure chests - Elytra, Shulker Shells, enchanted gear";
            case "stronghold": return "Stronghold corridor chests - Books, apples, bread, iron";
            case "dungeon": return "Simple dungeon chests - Saddles, name tags, enchanted books";
            case "mineshaft": return "Abandoned mineshaft chests - Rails, torches, bread, melon seeds";
            case "desert_pyramid": return "Desert pyramid chests - Diamonds, emeralds, gold, enchanted books";
            case "jungle_temple": return "Jungle temple chests - Diamonds, emeralds, gold, bones";
            case "woodland_mansion": return "Woodland mansion chests - Diamonds, emeralds, enchanted gear";
            case "buried_treasure": return "Buried treasure chests - Heart of the Sea, diamonds, emeralds";
            case "shipwreck_treasure": return "Shipwreck treasure chests - Diamonds, emeralds, lapis lazuli";
            case "ancient_city": return "Ancient City chests - Echo Shards, enchanted golden apples, diamonds";
            case "bastion_treasure": return "Bastion treasure chests - Netherite, ancient debris, gold";
            case "pillager_outpost": return "Pillager outpost chests - Crossbows, arrows, dark oak logs";
            case "village_weaponsmith": return "Village weaponsmith chests - Iron tools, weapons, armor";
            case "village_toolsmith": return "Village toolsmith chests - Stone/iron tools, coal, sticks";
            case "village_armorer": return "Village armorer chests - Iron armor, bread, apples";
            case "nether_bridge": return "Nether fortress chests - Nether wart, diamonds, iron";
            case "fishing": return "General fishing loot - Fish, treasure, and junk";
            case "fishing_treasure": return "Fishing treasure - Enchanted books, bows, name tags";
            default: return "Custom loot table: " + alias;
        }
    }
}
