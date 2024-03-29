package core.World.Creatures.Player.Inventory.Items;

import core.EventHandling.Logging.Config;
import core.Window;
import core.World.Creatures.Player.Inventory.Items.Weapons.Weapons;
import core.World.StaticWorldObjects.StaticObjectsConst;
import core.World.StaticWorldObjects.StaticWorldObjects;
import java.io.Serializable;
import java.util.Properties;
import static core.World.Textures.TextureLoader.getSize;

public class Items implements Serializable {
    public Items[] requiredForBuild;
    public Weapons weapon;
    public short placeable;
    public Tools tool;
    public Details detail;
    public int id, countInCell;
    public float zoom;
    public String path, description, name, filename;
    public Types type;

    private record DefaultValues(String name, String path, String description, Items[] requiredForBuild) {}

    public enum Types {
        TOOL,
        WEAPON,
        PLACEABLE,
        DETAIL
    }

    private Items(Weapons weapon, short placeable, Tools tool, Details detail, int id, float zoom, String path, String description, String name, String filename, Items[] requiredForBuild, Types type) {
        this.name = name;
        this.weapon = weapon;
        this.placeable = placeable;
        this.tool = tool;
        this.detail = detail;
        this.id = id;
        this.zoom = zoom;
        this.path = path;
        this.description = description;
        this.filename = filename;
        this.requiredForBuild = requiredForBuild;
        this.type = type;
    }

    private static DefaultValues getDefault(Properties properties) {
        String path = Window.assetsDir((String) properties.getOrDefault("Path", "\\World\\textureNotFound.png"));
        String description = (String) properties.getOrDefault("Description", null);
        String name = (String) properties.getOrDefault("Name", null);
        String requiredForBuild = (String) properties.getOrDefault("RequiredForBuild", null);

        if (requiredForBuild != null) {
            String[] required = requiredForBuild.split(",");
            Items[] output = new Items[required.length];

            for (int i = 0; i < required.length; i++) {
                //todo хосподе
                if (required[i].contains("Blocks")) {
                    output[i] = createPlaceable(StaticWorldObjects.createStatic(required[i]));
                } else if (required[i].contains("Weapons")) {
                    output[i] = createWeapon(required[i]);
                } else if (required[i].contains("Details")) {
                    output[i] = createDetail(required[i]);
                } else if (required[i].contains("Tools")) {
                    output[i] = createTool(required[i]);
                }
            }
            return new DefaultValues(name, path, description, output);
        }

        return new DefaultValues(name, path, description, null);
    }

    public static Items createWeapon(String fileName) {
        Properties weapon = Config.getProperties(Window.assetsDir("\\World\\ItemsCharacteristics\\BuildMenu\\Weapons\\" + fileName + ".properties"));
        DefaultValues defaultValues = getDefault(weapon);

        int id = fileName.hashCode();
        float zoom = findZoom(defaultValues.path);
        float fireRate = Float.parseFloat((String) weapon.getOrDefault("FireRate", "100"));
        float damage = Float.parseFloat((String) weapon.getOrDefault("Damage", "100"));
        float ammoSpeed = Float.parseFloat((String) weapon.getOrDefault("AmmoSpeed", "100"));
        float reloadTime = Float.parseFloat((String) weapon.getOrDefault("ReloadTime", "100"));
        float bulletSpread = Float.parseFloat((String) weapon.getOrDefault("BulletSpread", "0"));
        int magazineSize = Integer.parseInt((String) weapon.getOrDefault("MagazineSize", "10"));
        String sound = Window.assetsDir((String) weapon.getOrDefault("Sound", null));
        String bulletPath = Window.assetsDir((String) weapon.getOrDefault("BulletPath", "World/Items/someBullet.png"));
        Weapons.Types type = Weapons.Types.valueOf((String) weapon.getOrDefault("Type", "BULLET"));

        return new Items(new Weapons(fireRate, damage, ammoSpeed, reloadTime, bulletSpread, magazineSize, sound, bulletPath, type), (short) 0, null, null, id, zoom, defaultValues.path(), defaultValues.description, defaultValues.name, fileName, defaultValues.requiredForBuild, Types.WEAPON);
    }

    public static Items createTool(String fileName) {
        Properties tool = Config.getProperties(Window.assetsDir("\\World\\ItemsCharacteristics\\BuildMenu\\Tools\\" + fileName + ".properties"));
        DefaultValues defaultValues = getDefault(tool);

        int id = fileName.hashCode();
        float zoom = findZoom(defaultValues.path);
        float maxHp = Float.parseFloat((String) tool.getOrDefault("MaxHp", "100"));
        float damage = Float.parseFloat((String) tool.getOrDefault("Damage", "30"));
        float secBetweenHits = Float.parseFloat((String) tool.getOrDefault("SecBetweenHits", "100"));
        float maxInteractionRange = Float.parseFloat((String) tool.getOrDefault("MaxInteractionRange", "8"));

        return new Items(null, (short) 0, new Tools(maxHp, damage, secBetweenHits, maxInteractionRange), null, id, zoom, defaultValues.path(), defaultValues.description, defaultValues.name, fileName, defaultValues.requiredForBuild, Types.TOOL);
    }

    public static Items createPlaceable(short placeable) {
        StaticObjectsConst placeableProp = StaticObjectsConst.getConst(StaticWorldObjects.getId(placeable));
        int id = StaticWorldObjects.getId(placeable);
        float zoom = findZoom(StaticWorldObjects.getPath(placeable));

        return new Items(null, placeable, null, null, id, zoom, placeableProp.path, "", placeableProp.objectName, StaticWorldObjects.getFileName(placeable), null, Types.PLACEABLE);
    }

    public static Items createDetail(String fileName) {
        Properties detail = Config.getProperties(Window.assetsDir("\\World\\ItemsCharacteristics\\BuildMenu\\Details\\" + fileName + ".properties"));
        DefaultValues defaultValues = getDefault(detail);
        int id = fileName.hashCode();
        float zoom = findZoom(defaultValues.path);

        return new Items(null, (short) 0, null, new Details(""), id, zoom, defaultValues.path(), defaultValues.description, defaultValues.name, fileName, defaultValues.requiredForBuild, Types.DETAIL);
    }

    public static float findZoom(String path) {
        int width = getSize(path).width();
        int height = getSize(path).height();

        return 32f / (Math.max(width, height));
    }
}
