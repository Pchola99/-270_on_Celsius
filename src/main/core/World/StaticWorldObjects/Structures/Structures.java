package core.World.StaticWorldObjects.Structures;

import core.EventHandling.Logging.Config;
import core.EventHandling.Logging.Logger;
import core.Global;
import core.World.StaticWorldObjects.StaticObjectsConst;
import core.World.StaticWorldObjects.StaticWorldObjects;
import core.World.Textures.TextureDrawing;
import core.g2d.Atlas;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import static core.EventHandling.Logging.Logger.printException;
import static core.Global.assets;
import static core.World.StaticWorldObjects.StaticWorldObjects.getType;

public class Structures implements Serializable {
    private static final HashMap<String, Structures> structures = new HashMap<>();
    public final String[][] blocks;
    public final int lowestSolidBlock;

    private Structures(int lowestSolidBlock, String[][] blocks) {
        this.lowestSolidBlock = lowestSolidBlock;
        this.blocks = blocks;
    }

    public static Structures getStructure(String name) {
        Structures struct = structures.get(name);

        if (struct == null) {
            struct = read(assets.assetsDir("\\World\\Saves\\Structures\\" + name + ".ser"));
            if (struct != null) {
                structures.put(name, struct);
            }
        }
        return struct;
    }
    public static void clearStructuresMap() {
        structures.clear();
    }

    public static short[][] bindStructures(String[][] blocks) {
        short[][] bindedBlocks = new short[blocks.length][blocks[0].length];

        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                bindedBlocks[x][y] = StaticWorldObjects.createStatic(blocks[x][y]);
            }
        }
        return bindedBlocks;
    }

    private static Structures read(String path) {
        if (new File(path).exists()) {
            try (var fis = assets.resourceStream(path);
                 InflaterInputStream iis = new InflaterInputStream(fis);
                 ObjectInputStream ois = new ObjectInputStream(iis)) {

                return (Structures) ois.readObject();
            } catch (Exception e) {
                printException("Error when load structure, path: " + path, e);
            }
        }
        return null;
    }

    private static void write(Structures data, String structureName) {
        Logger.log("Start saving structure: " + structureName);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(data);
            oos.close();
            byte[] bytes = baos.toByteArray();

            ByteArrayOutputStream compressed = new ByteArrayOutputStream();
            DeflaterOutputStream dos = new DeflaterOutputStream(compressed);
            dos.write(bytes);
            dos.close();
            byte[] compressedBytes = compressed.toByteArray();

            FileOutputStream fos = new FileOutputStream(assets.assetsDir("World/Saves/Structures/structure" + structureName + ".ser"));
            fos.write(compressedBytes);
            fos.close();
        } catch (Exception e) {
            printException("Error when serialization (saving) structure: " + structureName, e);
        }
        Logger.log("End saving structure: " + structureName);
    }

    public static void createStructure(String name, short[][] blocks) {
        if (blocks.length > 0) {
            int lowestSolidBlock = -1;
            String[][] names = new String[blocks.length][blocks[0].length];

            for (int x = 0; x < blocks.length; x++) {
                for (int y = 0; y < blocks[x].length; y++) {
                    names[x][y] = StaticWorldObjects.getFileName(blocks[x][y]);

                    if (lowestSolidBlock == -1 && y == 0 && getType(blocks[x][y]) == StaticObjectsConst.Types.SOLID) {
                        lowestSolidBlock = x;
                    }
                }
            }
            write(new Structures(lowestSolidBlock, names), name);
        } else {
            Logger.printException("Error when creating structure '" + name + "' because blocks[][] length is zero", new Exception());
        }
    }

    public static void bindStructure(String name, byte id) {
        String path = assets.assetsDir("World/ItemsCharacteristics/" + name + ".properties");

        byte maxHp;
        short[][] blocks;

        // if its simple structure (without .ser file)
        if (new File(path).exists()) {
            Properties prop = Config.getProperties(path);
            Atlas.Region texture = Global.atlas.byPath((String) prop.get("Path"));
            blocks = new short[texture.width() / TextureDrawing.blockSize][texture.height() / TextureDrawing.blockSize];

            if (blocks.length > 1 || blocks[0].length > 1) {
                maxHp = Byte.parseByte((String) prop.getOrDefault("MaxHp", "100"));

                StaticObjectsConst rootConst = StaticObjectsConst.getConst(id);
                StaticObjectsConst tailConst = rootConst.clone();

                tailConst.optionalTiles = null;
                tailConst.texture = null;
                tailConst.hasMotherBlock = true;

                for (int x = 0; x < blocks.length; x++) {
                    for (int y = 0; y < blocks[0].length; y++) {
                        byte tailId = StaticWorldObjects.generateId(name + "" + x + "" + y);
                        blocks[x][y] = (short) (((maxHp & 0xFF) << 8) | (tailId & 0xFF));
                        StaticObjectsConst.setConst(tailConst, tailId);
                    }
                }
                rootConst.optionalTiles = blocks;
                StaticObjectsConst.setConst(rootConst, id);
            }
        } else {
            Structures str = getStructure(name);
            if (str != null) {
                blocks = bindStructures(str.blocks);
                StaticObjectsConst.setConst(name, id, blocks);
            }
        }
    }
}
