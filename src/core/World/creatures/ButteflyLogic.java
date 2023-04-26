package core.World.creatures;

import static core.World.WorldGenerator.*;
import static core.World.WorldGenerator.DynamicObjects;
import static core.World.creatures.CreaturesGenerate.getEntityName;

public class ButteflyLogic {
    private static long deltaTime = System.currentTimeMillis();
    private static int thisFrame = 1;

    public static void update() {
        if (System.currentTimeMillis() - deltaTime > 100) {
            deltaTime = System.currentTimeMillis();

            for (int x = 0; x < DynamicObjects.length; x++) {
                if (DynamicObjects[x] != null && !DynamicObjects[x].isPlayer && getEntityName(DynamicObjects[x].path).equals("butterfly") && DynamicObjects[x].animSpeed != 0) {
                    int randX = (int) (Math.random() * 60) - 25;
                    int randY = (int) (Math.random() * 50) - 19;

                    if (randX + DynamicObjects[x].x < SizeX * 16 && randY + DynamicObjects[x].y < SizeY * 16 && randX + DynamicObjects[x].x > 0 && randY + DynamicObjects[x].y > 0 && DynamicObjects[x].currentFrame != thisFrame) {
                        DynamicObjects[x].x += randX;
                        DynamicObjects[x].y += randY;
                        thisFrame = DynamicObjects[x].currentFrame;
                    }
                }
            }
        }
    }
}