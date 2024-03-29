package core.World.Creatures;

import core.EventHandling.Logging.Logger;
import core.World.HitboxMap;
import core.World.Textures.TextureDrawing;
import core.World.Textures.TextureLoader;
import core.World.WorldGenerator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

//dynamic objects, can have any coordinates within the world and be moved at any time
public class DynamicWorldObjects implements Serializable {
    private static final HashMap<String, Byte> ids = new HashMap<>();
    private final byte id;
    private short currentFrame, motionVectorX, motionVectorY;
    private long lastFrameTime = System.currentTimeMillis();
    private float x, y, currentHp;

    private DynamicWorldObjects(byte id, float x, float y, float maxHp) {
        this.id = id;
        this.currentFrame = 0;
        this.x = x;
        this.y = y;
        this.currentHp = maxHp;
        this.motionVectorX = 0;
        this.motionVectorY = 0;
    }

    public static DynamicWorldObjects createDynamic(String name, float x) {
        byte id = generateId(name);
        DynamicObjectsConst.bindDynamic(name, id);
        ArrayList<Integer> topmostBlocks = new ArrayList<>(4);

        for (int xSize = 0; xSize < (int) Math.ceil(TextureLoader.getSize(DynamicObjectsConst.getConst(id).path).width() / TextureDrawing.blockSize) + 1; xSize++) {
            topmostBlocks.add(WorldGenerator.findTopmostSolidBlock((int) ((x / TextureDrawing.blockSize) + xSize), 5));
        }

        return new DynamicWorldObjects(generateId(name), x, (Collections.max(topmostBlocks) + 1) * TextureDrawing.blockSize, DynamicObjectsConst.getConst(id).maxHp);
    }

    public static DynamicWorldObjects createDynamic(String name, float x, float y) {
        byte id = generateId(name);
        DynamicObjectsConst.bindDynamic(name, id);
        return new DynamicWorldObjects(generateId(name), x, y, DynamicObjectsConst.getConst(id).maxHp);
    }

    private static byte generateId(String name) {
        if (name == null) {
            return 0;
        }
        byte id = ids.getOrDefault(name, (byte) 0);
        if (id != 0) {
            return id;
        } else {
            for (byte i = -127; i < 127; i++) {
                if (i != 0 && !ids.containsValue(i)) {
                    ids.put(name, i);
                    return i;
                }
                if (i == 126) {
                    Logger.log("Number of id's dynamic objects exceeded, errors will occur");
                }
            }
        }
        return 0;
    }

    public void jump(float impulse) {
        TextureLoader.Size size = TextureLoader.getSize(DynamicObjectsConst.getConst(id).path);

        if (HitboxMap.checkIntersStaticD(x, y, size.width(), size.height()) && motionVectorY == 0) {
            motionVectorY += (impulse * 1000);
        }
    }

    public float getX() {
        return x;
    }

    public void incrementX(float increment) {
        this.x += increment;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void incrementY(float increment) {
        this.y += increment;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getMaxHp() {
        return DynamicObjectsConst.getConst(id).maxHp;
    }

    public void setCurrentHp(float hp) {
        this.currentHp = hp;
    }

    public float getCurrentHP() {
        return currentHp;
    }

    public void incrementCurrentHP(float increment) {
        this.currentHp += increment;
    }

    public void incrementCurrentFrame() {
        DynamicObjectsConst dynamicConst = DynamicObjectsConst.getConst(id);

        if (dynamicConst.animSpeed != 0 && dynamicConst.framesCount != 0 && System.currentTimeMillis() - lastFrameTime >= dynamicConst.animSpeed) {
            if (currentFrame >= dynamicConst.framesCount) {
                currentFrame = 0;
                lastFrameTime = System.currentTimeMillis();
                return;
            }
            lastFrameTime = System.currentTimeMillis();
            currentFrame++;
        }
    }

    public void setCurrentFrame(short currentFrame) {
        this.currentFrame = currentFrame;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public int getFramesCount() {
        return DynamicObjectsConst.getConst(id).framesCount;
    }

    public int getAnimationSpeed() {
        return DynamicObjectsConst.getConst(id).animSpeed;
    }

    public void setAnimationSpeed(int speed) {
        DynamicObjectsConst.getConst(id).animSpeed = speed;
    }

    public float getWeight() {
        return DynamicObjectsConst.getConst(id).weight;
    }

    public String getPath() {
        return DynamicObjectsConst.getConst(id).path;
    }

    public boolean getIsFlying() {
        return DynamicObjectsConst.getConst(id).isFlying;
    }

    public float getMotionVectorX() {
        return motionVectorX / 1000f;
    }

    public void incrementMotionVectorX(float vectorX) {
        this.motionVectorX += Math.clamp(vectorX * 1000, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    public void setMotionVectorX(float vectorX) {
        this.motionVectorX = (short) Math.clamp(vectorX * 1000, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    public float getMotionVectorY() {
        return motionVectorY / 1000f;
    }

    public void incrementMotionVectorY(float vectorY) {
        this.motionVectorY += Math.clamp(vectorY * 1000, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    public void setMotionVectorY(float vectorY) {
        this.motionVectorY = (short) Math.clamp(vectorY * 1000, Short.MIN_VALUE, Short.MAX_VALUE);
    }
}
