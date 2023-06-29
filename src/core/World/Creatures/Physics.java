package core.World.Creatures;

import core.EventHandling.EventHandler;
import core.EventHandling.Logging.Logger;
import core.World.Textures.ShadowMap;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import static core.Window.*;
import static core.World.HitboxMap.*;
import static core.World.WorldGenerator.*;
import static org.lwjgl.glfw.GLFW.*;

public class Physics extends Thread {
    private static boolean isDropping = false;
    private static final int physicsSpeed = 1;
    private static float dropSpeed = 0;
    //min and default 1, max 4

    public void run() {
        Logger.log("Thread: Physics started");

        while (!glfwWindowShouldClose(glfwWindow)) {
            try { Thread.sleep(physicsSpeed); } catch (InterruptedException e) { throw new RuntimeException(e); }
            updateDrop();
            updateMove();
            updateJump();
        }
    }

    public static void setPlayerPos(int x, int y) {
        DynamicObjects[0].x = x == 0 ? DynamicObjects[0].x : x;
        DynamicObjects[0].y = y == 0 ? DynamicObjects[0].y : y;
    }

    public static void updateJump() {
        if (!DynamicObjects[0].isJumping && !isDropping && EventHandler.getKey(GLFW_KEY_SPACE)) {
            DynamicObjects[0].isJumping = true;
            new Thread(() -> {

                float y0 = DynamicObjects[0].y;
                float yMax = y0 + 34; //высота прыжка
                double g = 900 - (physicsSpeed * 200); //гравитация
                double timeToMax = Math.sqrt((2 * (yMax - y0)) / g);
                double totalTime = 2 * timeToMax;
                LocalDateTime startTime = LocalDateTime.now();

                while (true) {
                    LocalDateTime currentTime = LocalDateTime.now();
                    double elapsedTime = Duration.between(startTime, currentTime).toMillis() / 1000.0;

                    if (elapsedTime >= totalTime) {
                        DynamicObjects[0].y = y0;
                        DynamicObjects[0].isJumping = false;
                        break;
                    } else if (elapsedTime >= timeToMax) {
                        DynamicObjects[0].y = (float) (y0 + (yMax - y0) - 0.5 * g * Math.pow(elapsedTime - timeToMax, 2));
                    } else {
                        DynamicObjects[0].y = (float) (y0 + 0.5 * g * Math.pow(elapsedTime, 2));
                    }

                    if ((!checkIntersStaticD(DynamicObjects[0].x, DynamicObjects[0].y, 24) && elapsedTime >= totalTime / 2) || checkIntersStaticU(DynamicObjects[0].x, DynamicObjects[0].y, 24, 24)) {
                        DynamicObjects[0].isJumping = false;
                        break;
                    }
                }
            }).start();
        }
    }

    public static void updateMove() {
        if (EventHandler.getKey(GLFW_KEY_D) || EventHandler.getKey(GLFW_KEY_A)) {
            if (EventHandler.getKey(GLFW_KEY_D) && DynamicObjects[0].x < SizeX * 16 - 24 && !checkIntersStaticR(DynamicObjects[0].x, DynamicObjects[0].y, 24, 24)) {
                DynamicObjects[0].x += 0.1f;
            }
            if (EventHandler.getKey(GLFW_KEY_A) && DynamicObjects[0].x > 0 && !checkIntersStaticL(DynamicObjects[0].x, DynamicObjects[0].y, 24)) {
                DynamicObjects[0].x -= 0.1f;
            }
        }
    }

    private static void updateDrop() {
        if (!checkIntersStaticD(DynamicObjects[0].x, DynamicObjects[0].y, 24)) {
            isDropping = true;
            dropSpeed += 0.001f;
            DynamicObjects[0].y -= dropSpeed;
        } else {
            dropSpeed = 0;
            isDropping = false;
        }
    }
}