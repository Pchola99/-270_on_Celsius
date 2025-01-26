package core.World.StaticWorldObjects;

import core.assets.AssetsManager;
import core.entity.BaseBlockEntity;
import core.entity.BlockEntity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;

import static core.Global.assets;
import static core.World.StaticWorldObjects.StaticObjectsConst.load;

public abstract class StaticWorldObjects implements Serializable {
    private static final HashMap<String, StaticObjectsConst> registry = new HashMap<>();

    public static StaticObjectsConst createStatic(String name) {
        name = AssetsManager.normalizePath(name);
        var objectsConst = registry.get(name);
        if (objectsConst == null) {
            objectsConst = load(assets.assetsDir("World/ItemsCharacteristics/" + name + ".properties"));
            // objectsConst.optionalTiles = optionalTiles;
            objectsConst.originalFileName = name;
            registry.put(name, objectsConst);
            // Structures.bindStructure(name, id);


            ids.put(objectsConst, (byte) idCounter++);
        }
        return objectsConst;
    }

    public static float getDensity(BlockEntity entity) {
        return entity == null ? 0 : entity.type().density;
    }

    public static String getFileName(BlockEntity entity) {
        return entity == null ? null : entity.type().originalFileName;
    }

    public static StaticObjectsConst.Types getType(StaticObjectsConst staticObjectsConst) {
        return staticObjectsConst == null ? StaticObjectsConst.Types.GAS : staticObjectsConst.type;
    }

    public static StaticObjectsConst.Types getType(BlockEntity entity) {
        return entity == null ? StaticObjectsConst.Types.GAS : entity.type().type;
    }

    public static float getResistance(BlockEntity entity) {
        return entity == null ? 0 : entity.type().resistance;
    }

    static final HashMap<StaticObjectsConst, Byte> ids = new HashMap<>();
    static int idCounter = 0;
    public static byte getId(StaticObjectsConst staticObjectsConst) {
        return ids.get(staticObjectsConst);
    }
}
