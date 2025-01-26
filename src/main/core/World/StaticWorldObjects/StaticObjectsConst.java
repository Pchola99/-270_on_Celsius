package core.World.StaticWorldObjects;

import core.EventHandling.Logging.Config;
import core.EventHandling.Logging.Logger;
import core.Global;
import core.World.Creatures.Player.Inventory.Items.Items;
import core.World.StaticWorldObjects.Structures.Factories;
import core.g2d.Atlas;

import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static core.Global.assets;
import static core.World.StaticWorldObjects.Structures.Factories.transformItems;

public class StaticObjectsConst implements Cloneable {
    public StaticObjectsConst[][] optionalTiles;
    public float maxHp, density, resistance;
    public int lightTransmission;
    public boolean hasMotherBlock;
    // original file name - filename, object name - name at file
    public String originalFileName, objectName;
    public Atlas.Region texture;
    public Types type;

    public enum Types {
        GAS,
        LIQUID,
        SOLID,
        PLASMA
    }

    @Override
    public StaticObjectsConst clone() {
        try {
            return (StaticObjectsConst) super.clone();
        } catch (CloneNotSupportedException e) {
            Logger.printException("Error when cloning static objects const", e);
        }
        return null;
    }

    StaticObjectsConst(boolean hasMotherBlock, float maxHp, float density, float resistance,
                       int lightTransmission, Atlas.Region texture, String objectName,
                       String originalFileName, StaticObjectsConst[][] optionalTiles, Types type) {
        this.hasMotherBlock = hasMotherBlock;
        this.maxHp = maxHp;
        this.density = density;
        this.texture = texture;
        this.objectName = objectName;
        this.originalFileName = originalFileName;
        this.type = type;
        this.lightTransmission = lightTransmission;
        this.optionalTiles = optionalTiles;
        this.resistance = resistance;
    }

    public static StaticObjectsConst load(String path) {
        Properties props = Config.getProperties(path);
        boolean hasMotherBlock = Boolean.parseBoolean((String) props.getOrDefault("HasMotherBlock", "false"));
        float density = Float.parseFloat((String) props.getOrDefault("Density", "1"));
        float resistance = Float.parseFloat((String) props.getOrDefault("Resistance", "100"));
        int lightTransmission = Integer.parseInt((String) props.getOrDefault("LightTransmission", "100"));
        int maxHp = Integer.parseInt((String) props.getOrDefault("MaxHp", "100"));
        Atlas.Region texture = Global.atlas.byPath((String) props.get("Path"));
        String enumType = (String) props.getOrDefault("Type", Types.SOLID.name());
        String objectName = (String) props.getOrDefault("Name", "notFound");

        if (path.toLowerCase(Locale.ROOT).contains("factories")) {
            int productionSpeed = Integer.parseInt((String) props.get("ProductionSpeed"));
            int needEnergy = Integer.parseInt((String) props.get("NeedEnergy"));
            short maxStoredObjects = Short.parseShort((String) props.get("MaxStoredObjects"));
            String sound = (String) props.get("Sound");
            String breakingType = (String) props.get("BreakingType");
            Items[] outputObjects = transformItems((String) props.get("OutputObjects"));
            Items[] inputObjects = transformItems((String) props.get("InputObjects"));
            Items[] fuel = transformItems((String) props.get("Fuel"));

            return new FactoryType(
                    hasMotherBlock, maxHp, density, resistance, lightTransmission,
                    texture, objectName, objectName, null, Types.valueOf(enumType.toUpperCase()),
                    productionSpeed, needEnergy, maxStoredObjects,
                    sound, breakingType != null ? FactoryType.BreakingType.valueOf(breakingType.toUpperCase(Locale.ROOT)) : null,
                    outputObjects, inputObjects, fuel
            );
        }

        return new StaticObjectsConst(
                hasMotherBlock, maxHp, density, resistance, lightTransmission,
                texture, objectName, objectName, null, Types.valueOf(enumType.toUpperCase())
        );
    }
}
