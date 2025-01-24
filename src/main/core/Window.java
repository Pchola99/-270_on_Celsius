package core;

import core.EventHandling.EventHandler;
import core.EventHandling.Logging.Config;
import core.EventHandling.Logging.Logger;
import core.Utils.NativeResources;
import core.World.Textures.TextureDrawing;
import core.assets.AssetsManager;
import core.assets.TextureLoader;
import core.g2d.Atlas;
import core.g2d.Camera2;
import core.g2d.Font;
import core.g2d.SortingBatch;
import core.graphic.Layer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL46;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;

import static core.EventHandling.Logging.Logger.log;
import static core.Global.*;
import static core.Utils.NativeResources.addResource;
import static core.assets.TextureLoader.BufferedImageEncoder;
import static core.assets.TextureLoader.readImage;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46.*;

public class Window extends Application {
    public static final String versionStamp = "0.0.6", version = "alpha " + versionStamp + " (non stable)";
    public static int defaultWidth = 1920, defaultHeight = 1080, verticalSync = Config.getFromConfig("VerticalSync").equals("true") ? 1 : 0;
    public static boolean start = false, windowFocused = true;
    public static long glfwWindow;
    public static Font defaultFont;

    private static int w, h, f;
    private static boolean fullscreen;

    public static void toggleFullscreen() {
        int w, h;
        int targetFps;
        long monitor;
        if (fullscreen) {
            w = Window.w;
            h = Window.h;
            targetFps = Window.f;
            monitor = MemoryUtil.NULL;

            fullscreen = false;
        } else {
            fullscreen = true;

            Window.w = input.getWidth();
            Window.h = input.getHeight();

            monitor = glfwGetPrimaryMonitor();
            var mode = glfwGetVideoMode(monitor);
            w = mode.width();
            h = mode.height();
            Window.f = targetFps = mode.refreshRate();
        }
        glfwSetWindowMonitor(glfwWindow, monitor, 0, 0, w, h, targetFps);
    }


    public void run() {
        init();
        draw();
    }

    public void init() {
        Logger.logStart();

        glfwSetErrorCallback(addResource(new GLFWErrorCallback() {
            @Override
            public void invoke(int error, long description) {
                Logger.logExit(error, "Error at glfw: '" + error + "', with description: '" + GLFWErrorCallback.getDescription(description) + "'", false);
                if (error != GLFW_FORMAT_UNAVAILABLE) {
                    System.exit(error);
                }
            }
        }));

        switch (System.getenv("XDG_SESSION_TYPE")) {
            // case "wayland" -> {
            //     glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_WAYLAND);
            //     glfwInitHint(GLFW_WAYLAND_LIBDECOR, GLFW_WAYLAND_DISABLE_LIBDECOR);
            // }
            case null, default -> {}
        }
        glfwInit();
        glfwDefaultWindowHints();

        glfwWindow = glfwCreateWindow(defaultWidth, defaultHeight, "-270 on Celsius", /*glfwGetPrimaryMonitor()*/MemoryUtil.NULL, MemoryUtil.NULL);

        glfwMakeContextCurrent(glfwWindow);

        TextureLoader.ImageData cursorImage = readImage(BufferedImageEncoder(assets.assetsDir("World/Other/cursorDefault.png")));
        GLFWImage glfwImg = GLFWImage.create().set(cursorImage.width(), cursorImage.height(), cursorImage.data());
        addResource(glfwImg);
        glfwSetCursor(glfwWindow, glfwCreateCursor(glfwImg, 0, 0));

        //vsync
        glfwSwapInterval(verticalSync);
        //display settings
        glfwShowWindow(glfwWindow);
        //connects library tools
        GL.createCapabilities();

        // Великий инструмент.
        // glEnable(GL_DEBUG_OUTPUT);
        // GLUtil.setupDebugMessageCallback();

        scene = new Scene(defaultWidth, defaultHeight);
        input = new InputHandler(defaultWidth, defaultHeight);
        input.init();
        input.addListener(scene);

        try {
            TextureLoader.preLoadResources();
        } catch (IOException e) {
            Logger.printException("Error when pre-loading resources", e);
        }

        glfwSetWindowFocusCallback(glfwWindow, addResource(new GLFWWindowFocusCallback() {
            @Override
            public void invoke(long window, boolean focused) {
                windowFocused = focused;
            }
        }));

        try {
            atlas = Atlas.load(assets.assetsDir("sprites"));
        } catch (IOException e) {
            Logger.printException("Error when loading texture atlas", e);
        }

        EventHandler.init();

        camera = new Camera2();
        camera.setToOrthographic(defaultWidth, defaultHeight);
        batch = new SortingBatch(4 * 1024 * 1024, 1024 * 8, 1024 * 8);
        batch.matrix(camera.projection);

        UI.mainMenu().show();

        addListener(new AutoSaveListener());

        log("Init status: true\n");
    }

    public void draw() {
        log("Thread: Main thread started drawing");

        glClearColor(206f / 255f, 246f / 255f, 1.0f, 1.0f);

        while (!glfwWindowShouldClose(glfwWindow)) {
            updateTime();

            input.update();
            for (ApplicationListener listener : listeners) {
                try {
                    listener.update();
                } catch (Exception e) {
                    Logger.printException("Failed to update ApplicationListener: " + listener, e);
                }
            }

            EventHandler.update();

            scheduler.executeAll();

            TextureDrawing.updateVideo();
            batch.z(Layer.STATIC_OBJECTS);

            if (start) {
                TextureDrawing.updateStaticObj();
                batch.z(Layer.DYNAMIC_OBJECTS);
                TextureDrawing.updateDynamicObj();
            } else {
                batch.draw(assets.getTextureByPath(assets.assetsDir("World/Other/background.png")));
            }

            TextureDrawing.updateGUI();
            batch.flush();

            glfwSwapBuffers(glfwWindow);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }

        glfwTerminate();
        NativeResources.terminateResources();

        batch.close();
        Logger.logExit(0, "Main thread ending drawing", false);
    }
}
