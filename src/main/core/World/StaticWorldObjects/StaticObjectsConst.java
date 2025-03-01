package core.World.StaticWorldObjects;

import core.EventHandling.Logging.Config;
import core.Global;
import core.World.StaticWorldObjects.Structures.Structures;
import core.g2d.Atlas;

import java.util.concurrent.ConcurrentHashMap;

public class StaticObjectsConst implements Cloneable {
    private static final ConcurrentHashMap<Byte, StaticObjectsConst> constants = new ConcurrentHashMap<>();
    public short[][] optionalTiles;
    public float maxHp, density, resistance;
    public int lightTransmission;
    public boolean hasMotherBlock;
    // original file name - filename, object name - name at file
    public String originalFileName, objectName;
    public Atlas.Region texture;
    public Runnable onInteraction;
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
            throw new RuntimeException(e);
        }
    }

    private StaticObjectsConst(boolean hasMotherBlock, float maxHp, float density, float resistance, int lightTransmission, Atlas.Region texture, String objectName, String originalFileName, short[][] optionalTiles, Types type) {
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

    public static void setConst(StaticObjectsConst staticConst, byte id) {
        constants.put(id, staticConst);
    }

    public static void setConst(String name, byte id, short[][] optionalTiles) {
        if (constants.get(id) == null) {
            StaticObjectsConst staticConst = createConst("World/ItemsCharacteristics/" + name + ".properties", id);
            staticConst.optionalTiles = optionalTiles;
            staticConst.originalFileName = name;

            constants.put(id, staticConst);

            Structures.bindStructure(name, id);
        }
    }

    public static StaticObjectsConst createConst(String path, byte id) {
        if (!constants.contains(id)) {
            var props = Config.getProperties(path);
            boolean hasMotherBlock = Boolean.parseBoolean(props.getOrDefault("HasMotherBlock", "false"));
            float density = Float.parseFloat(props.getOrDefault("Density", "1"));
            float resistance = Float.parseFloat(props.getOrDefault("Resistance", "100"));
            int lightTransmission = Integer.parseInt(props.getOrDefault("LightTransmission", "100"));
            int maxHp = Integer.parseInt(props.getOrDefault("MaxHp", "100"));
            Atlas.Region texture = Global.atlas.byPath(props.get("Path"));
            String enumType = props.getOrDefault("Type", Types.SOLID.name());
            String objectName = props.getOrDefault("Name", "notFound");

            return new StaticObjectsConst(hasMotherBlock, maxHp, density, resistance, lightTransmission,
                    texture, objectName, objectName, null, Types.valueOf(enumType.toUpperCase()));
        }
        return constants.get(id);
    }

    public static void setConst(String name, byte id) {
        setConst(name, id, null);
    }

    public static void setDestroyed() {
        constants.put((byte) 0, new StaticObjectsConst(false, 0, 0, 0, 100, null, "Destroyed", null, null, Types.GAS));
    }

    public static StaticObjectsConst getConst(byte id) {
        return constants.get(id);
    }

    public static boolean checkIsHere(byte id) {
        return constants.get(id) != null;
    }
}
