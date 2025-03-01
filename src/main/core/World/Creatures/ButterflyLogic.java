package core.World.Creatures;

import core.World.Textures.TextureDrawing;

import java.util.HashMap;

import static core.Global.world;
import static core.World.HitboxMap.*;

public class ButterflyLogic {
    private static final HashMap<DynamicWorldObjects, Long> lastSwapPos = new HashMap<>();
    private static final int swapPosTime = 200;

    // todo переписать
    public static void update(DynamicWorldObjects object) {
        if (object.getTexture().name().contains("butterfly")) {
            if (lastSwapPos.get(object) != null) {
                if (System.currentTimeMillis() - lastSwapPos.get(object) >= swapPosTime) {
                    int randX = (int) (Math.random() * 60) - 30;
                    int randY = (int) (Math.random() * 60) - 30;

                    if (randX + object.getX() < world.sizeX * TextureDrawing.blockSize - 32 && randY + object.getY() < world.sizeY * TextureDrawing.blockSize - 32 && randX + object.getX() > 32 && randY + object.getY() > world.sizeY / 2f * TextureDrawing.blockSize) {
                        object.incrementX((randX < 0 && !checkIntersStaticL(object.getX(), object.getY(), 32)) || (randX > 0 && !checkIntersStaticR(object.getX(), object.getY(), 32, 32)) ? randX : 0);
                        object.incrementY((randY < 0 && !checkIntersStaticD(object.getX(), object.getY(), 32, 32)) || (randY > 0 && !checkIntersStaticU(object.getX(), object.getY(), 32, 32)) ? randY : 0);
                    }
                    lastSwapPos.put(object, System.currentTimeMillis());
                }
            } else {
                lastSwapPos.put(object, System.currentTimeMillis());
            }
        }
    }
}
