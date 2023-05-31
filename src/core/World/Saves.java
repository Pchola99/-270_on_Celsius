package core.World;

import core.World.Textures.DynamicWorldObjects;
import core.World.Textures.StaticWorldObjects;

import java.io.*;
import java.util.HashMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import static core.EventHandling.Logging.Logger.log;
import static core.Window.defPath;

public class Saves {

    public static void createWorldSave() {
        try {
            HashMap<String, Object> objects = new HashMap<>();
            objects.put("StaticWorldObjects", WorldGenerator.StaticObjects);
            objects.put("DynamicWorldObjects", WorldGenerator.DynamicObjects);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(objects);
            oos.close();
            byte[] bytes = baos.toByteArray();

            ByteArrayOutputStream compressed = new ByteArrayOutputStream();
            DeflaterOutputStream dos = new DeflaterOutputStream(compressed);
            dos.write(bytes);
            dos.close();
            byte[] compressedBytes = compressed.toByteArray();

            FileOutputStream fos = new FileOutputStream(defPath + "\\src\\assets\\World\\Saves\\Save" + (int) (Math.random() * 100000) + ".ser");
            fos.write(compressedBytes);
            fos.close();
        } catch (Exception e) {
            log("Error at serialization world: " + e);
        }
    }

    public static void loadWorldSave(String path) {
        HashMap<String, Object> data = new HashMap<>();
        try {
            FileInputStream fis = new FileInputStream(path);
            InflaterInputStream iis = new InflaterInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(iis);

            data = (HashMap<String, Object>) ois.readObject();

            ois.close();
            iis.close();
            fis.close();
        } catch (Exception e) {
            log("Error at load world save: " + e + ". Path: " + path);
        }

        WorldGenerator.StaticObjects = (StaticWorldObjects[][]) data.get("StaticWorldObjects");
        WorldGenerator.DynamicObjects = (DynamicWorldObjects[]) data.get("DynamicWorldObjects");
    }
}