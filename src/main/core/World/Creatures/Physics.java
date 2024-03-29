package core.World.Creatures;

import core.EventHandling.Logging.Config;
import core.EventHandling.Logging.Logger;
import core.UI.GUI.Menu.Pause;
import core.UI.GUI.Menu.Settings;
import core.World.Creatures.Player.Inventory.Inventory;
import core.World.Creatures.Player.Inventory.Items.Weapons.Weapons;
import core.World.HitboxMap;
import core.World.Saves;
import core.World.StaticWorldObjects.StaticObjectsConst;
import core.World.StaticWorldObjects.StaticWorldObjects;
import core.World.Textures.TextureDrawing;
import core.World.Textures.TextureLoader;
import core.World.WorldGenerator;
import java.awt.*;
import java.util.Iterator;
import static core.Window.*;
import static core.World.StaticWorldObjects.StaticWorldObjects.*;
import static core.World.StaticWorldObjects.Structures.Factories.updateFactoriesOutput;
import static core.World.Creatures.Player.Player.*;
import static core.World.HitboxMap.*;
import static core.World.WorldGenerator.*;
import static org.lwjgl.glfw.GLFW.*;

//version 1.5
public class Physics {
    //default 400
    public static int physicsSpeed = 400, updates = 0, worldSaveDelay = Integer.parseInt(Config.getFromConfig("AutosaveWorldFrequency"));
    private static boolean stop = false;
    public static int lastSpeed = physicsSpeed;

    public static void restart() {
        physicsSpeed = 400;
        updates = 0;
        stop = false;
        initPhysics();

        Logger.log("Thread: Physics restarted");
    }

    public static void initPhysics() {
        new Thread(() -> {
            Logger.log("Thread: Physics started");
            Inventory.create();

            long lastUpdate = System.nanoTime();

            while (!glfwWindowShouldClose(glfwWindow)) {
                if (System.nanoTime() - lastUpdate >= 1.0 / physicsSpeed * 1000000000) {
                    updatePhys();
                    updateWorldInteractions();

                    updates++;
                    lastUpdate = System.nanoTime();
                }
                if ((Settings.createdSettings || Pause.created) && !stop) {
                    lastSpeed = physicsSpeed;
                    physicsSpeed = 1;
                    stop = true;
                } else if (!Settings.createdSettings && !Pause.created && stop) {
                    physicsSpeed = lastSpeed;
                    stop = false;
                }
            }
        }).start();
    }

    private static void updateVerticalSpeed(DynamicWorldObjects dynamicObject, TextureLoader.Size size) {
        dynamicObject.setMotionVectorY(dynamicObject.getMotionVectorY() * (1 - (getTotalResistanceInside(dynamicObject) / 100f)));

        boolean intersD = checkIntersStaticD(dynamicObject.getX(), dynamicObject.getY() + dynamicObject.getMotionVectorY(), size.width(), size.height());
        boolean intersU = checkIntersStaticU(dynamicObject.getX(), dynamicObject.getY() + dynamicObject.getMotionVectorY(), size.width(), size.height());

        if (!intersD) {
            dynamicObject.incrementMotionVectorY(-dynamicObject.getWeight());
        }
        if ((dynamicObject.getMotionVectorY() < 0 && intersD) || (dynamicObject.getMotionVectorY() > 0 && intersU)) {
            decrementHp(dynamicObject);
            dynamicObject.setMotionVectorY(0);
        }

        dynamicObject.incrementY(dynamicObject.getMotionVectorY());
    }

    private static void updateHorizontalSpeed(DynamicWorldObjects dynamicObject, TextureLoader.Size size) {
        dynamicObject.setMotionVectorX(dynamicObject.getMotionVectorX() * (1 - (getTotalResistanceInside(dynamicObject) / 100f)));

        boolean intersR = checkIntersStaticR(dynamicObject.getX() + dynamicObject.getMotionVectorX() * 60, dynamicObject.getY(), size.width(), size.height());
        boolean intersL = checkIntersStaticL(dynamicObject.getX() + dynamicObject.getMotionVectorX(), dynamicObject.getY(), size.height());

        if (!intersR && dynamicObject.getMotionVectorX() > 0) {
            if (dynamicObject.getMotionVectorX() - dynamicObject.getWeight() > 0) {
                dynamicObject.incrementMotionVectorX(-dynamicObject.getWeight());
            } else {
                dynamicObject.setMotionVectorX(0);
            }
        } else if (intersR) {
            decrementHp(dynamicObject);
            dynamicObject.setMotionVectorX(0);
        }

        if (!intersL && dynamicObject.getMotionVectorX() < 0) {
            dynamicObject.incrementMotionVectorX(dynamicObject.getWeight());
        } else if (intersL) {
            decrementHp(dynamicObject);
            dynamicObject.setMotionVectorX(0);
        }

        dynamicObject.incrementX(dynamicObject.getMotionVectorX());
    }

    private static void decrementHp(DynamicWorldObjects object) {
        float vectorX = object.getMotionVectorX();
        float vectorY = object.getMotionVectorY();

        if (vectorX > minVectorIntersDamage || vectorX < -minVectorIntersDamage || vectorY > minVectorIntersDamage || vectorY < -minVectorIntersDamage) {
            Point[] staticObjectPoint = HitboxMap.checkIntersOutside(object.getX() + vectorX * 2, object.getY() + vectorY, TextureLoader.getSize(object.getPath()).width(), TextureLoader.getSize(object.getPath()).height() + 4);

            if (staticObjectPoint != null) {
                float damage = 0;
                for (Point point : staticObjectPoint) {
                    short staticObject = WorldGenerator.getObject(point.x, point.y);
                    float currentDamage = ((((StaticWorldObjects.getResistance(staticObject) / 100) * StaticWorldObjects.getDensity(staticObject)) + (object.getWeight() + (Math.max(Math.abs(vectorY), Math.abs(vectorX)) - minVectorIntersDamage)) * intersDamageMultiplier)) / staticObjectPoint.length;
                    damage += currentDamage;
                    WorldGenerator.setObject(point.x, point.y, StaticWorldObjects.decrementHp(staticObject, (int) (currentDamage + (getResistance(staticObject) / 100) * StaticWorldObjects.getDensity(staticObject)) / staticObjectPoint.length));
                }
                object.incrementCurrentHP(-damage);

                //TODO: rewrite
                if (object.getPath().toLowerCase().contains("player")) {
                    lastDamage = (int) damage;
                    lastDamageTime = System.currentTimeMillis();
                }
                if (object.getCurrentHP() <= 0 && !object.getPath().toLowerCase().contains("player")) {
                    DynamicObjects.remove(object);
                }
            }
        }
    }

    private static void updatePhys() {
        Iterator<DynamicWorldObjects> dynamicIterator = DynamicObjects.iterator();
        updatePlayerMove();
        updatePlayerJump();
        updateWorldSave();

        while (dynamicIterator.hasNext()) {
            DynamicWorldObjects dynamicObject = dynamicIterator.next();

            if (dynamicObject != null) {
                TextureLoader.Size size = TextureLoader.getSize(dynamicObject.getPath());
                updateHorizontalSpeed(dynamicObject, size);

                if (!dynamicObject.getIsFlying()) {
                    updateVerticalSpeed(dynamicObject, size);
                }
            }
        }
    }

    public static float getTotalResistanceInside(DynamicWorldObjects dynamicObject) {
        int tarX = (int) (dynamicObject.getX() / TextureDrawing.blockSize);
        int tarY = (int) (dynamicObject.getY() / TextureDrawing.blockSize);
        int tarYSize = (int) Math.ceil(TextureLoader.getSize(dynamicObject.getPath()).height() / TextureDrawing.blockSize);
        int tarXSize = (int) Math.ceil(TextureLoader.getSize(dynamicObject.getPath()).width() / TextureDrawing.blockSize);
        float totalResistance = 0;

        for (int xPos = 0; xPos < tarXSize; xPos++) {
            for (int yPos = 0; yPos < tarYSize; yPos++) {
                if (tarX + tarXSize > SizeX || tarY + tarYSize > SizeY || getObject(tarX + xPos, tarY + yPos) == -1 || getObject(tarX + tarXSize, tarY + tarYSize) == -1) {
                    continue;
                }
                if (getResistance(getObject(tarX + xPos, tarY + yPos)) < 100 && getType(getObject(tarX + xPos, tarY + yPos)) == StaticObjectsConst.Types.SOLID) {
                    totalResistance += getResistance(getObject(tarX + xPos, tarY + yPos));
                }
            }
        }
        return totalResistance > 90 ? 90 : totalResistance;
    }

    private static void updateWorldSave() {
        if (System.currentTimeMillis() - Saves.lastSaved >= worldSaveDelay) {
            Saves.createWorldBackup();
        }
    }

    private static void updateWorldInteractions() {
        updateInventoryInteraction();
        Weapons.updateAmmo();
        updateFactoriesOutput();
    }
}
