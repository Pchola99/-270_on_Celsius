package core.EventHandling;

import core.EventHandling.Logging.Config;
import core.Global;
import core.UI;
import core.Window;
import core.graphic.Layer;
import core.ui.Dialog;
import core.World.Creatures.Physics;
import core.World.Creatures.Player.Player;
import core.math.Point2i;
import core.ui.Element;
import core.ui.TextArea;
import org.lwjgl.glfw.GLFWCharCallback;

import java.util.function.Supplier;

import static core.EventHandling.Logging.Logger.log;
import static core.Global.*;
import static core.Utils.Commandline.updateLine;
import static core.Utils.NativeResources.addResource;
import static core.Window.*;
import static core.ui.Styles.DEBUG_TEXT;
import static org.lwjgl.glfw.GLFW.*;

public class EventHandler {
    private static boolean keyLogging = false;
    public static final StringBuilder keyLoggingText = new StringBuilder(256);
    public static int debugLevel = Integer.parseInt(Config.getFromConfig("Debug"));

    private static final class DebugBox extends TextArea {
        private final Supplier<String> format;

        private DebugBox(Supplier<String> format) {
            super(debugDialog, DEBUG_TEXT);
            this.format = format;
        }

        @Override
        public void preUpdate() {
            setText(format.get());
        }

        @Override
        public void draw() {
            batch.draw(Layer.DEBUG, super::draw);
        }
    }
    private static final Dialog debugDialog = new Dialog();

    public static void setKeyLoggingText(String text) {
        keyLoggingText.setLength(0);
        keyLoggingText.append(text);
    }

    private static void initCallbacks() {
        log("Thread: Event handling started");

        debugDialog.marginLeft(5);
        debugDialog.top().left();
        debugDialog.maximize();
        debugDialog.show();

        glfwSetCharCallback(glfwWindow, addResource(new GLFWCharCallback() {
            @Override
            public void invoke(long window, int codepoint) {
                if (keyLogging) {
                    keyLoggingText.appendCodePoint(codepoint);
                }
            }
        }));
    }

    public static void startKeyLogging() {
        keyLogging = true;
    }

    public static void endKeyLogging() {
        resetKeyLogginText();
        keyLogging = false;
    }

    public static boolean isMousePressed(Element element) {
        return getRectangleClick(element.x(), element.y(), element.x() + element.width(), element.y() + element.height());
    }

    public static boolean getRectangleClick(float x, float y, float x1, float y1) {
        Point2i mousePos = input.mousePos();

        return mousePos.x >= x && mousePos.x <= x1 &&
                mousePos.y >= y && mousePos.y <= y1 &&
                input.justClicked(GLFW_MOUSE_BUTTON_LEFT);
    }

    private static void updateHotkeys() {
        if (start) {
            if (input.justPressed(GLFW_KEY_ESCAPE)) {
                UI.pause().toggle();
            }

            if (!windowFocused && Config.getFromConfig("Autopause").equals("true")) {
                UI.pause().show();
                Physics.stopPhysics();
            }
        }

        if (input.justPressed(GLFW_KEY_F12)) {
            Window.toggleFullscreen();
        }

        if (input.justPressed(GLFW_KEY_SPACE)) {
            scene.debug();
        }

        if ((input.justPressed(GLFW_KEY_BACKSPACE) || input.repeated(GLFW_KEY_BACKSPACE)) && isKeylogging()) {
            int length = keyLoggingText.length();

            if (length > 0) {
                keyLoggingText.deleteCharAt(length - 1);
            }
        }
    }

    public static void setDebugValue(Supplier<String> format) {
        if (debugLevel > 0) {
            var elem = new DebugBox(format);

            debugDialog.addCell(elem).fillX()
                    .row();
        }
    }

    public static void init() {
        initCallbacks();

        setDebugValue(() -> "[Render] fps: " + Global.app.getFps());
    }

    public static void update() {
        Player.updatePlayerGUILogic();
        updateHotkeys();
        updateLine();
    }

    public static boolean isKeylogging() {
        return keyLogging;
    }

    public static void resetKeyLogginText() {
        keyLoggingText.setLength(0);
    }
}
