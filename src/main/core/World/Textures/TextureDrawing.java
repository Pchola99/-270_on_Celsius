package core.World.Textures;

import core.Utils.Commandline;
import core.EventHandling.EventHandler;
import core.UI.GUI.Objects.ButtonObject;
import core.UI.GUI.Objects.PanelObject;
import core.UI.GUI.Objects.SliderObject;
import core.UI.GUI.Objects.TextObject;
import core.UI.GUI.Video;
import core.Utils.SimpleColor;
import core.Window;
import core.World.Creatures.DynamicWorldObjects;
import core.World.StaticWorldObjects.Structures.Factories;
import core.World.StaticWorldObjects.StaticWorldObjects;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import static core.EventHandling.EventHandler.getMousePos;
import static core.EventHandling.Logging.Config.getFromConfig;
import static core.UI.GUI.CreateElement.*;
import static core.UI.GUI.Fonts.*;
import static core.UI.GUI.Video.*;
import static core.Window.*;
import static core.World.Creatures.Player.Player.updatePlayerGUI;
import static core.World.StaticWorldObjects.StaticWorldObjects.*;
import static core.World.Textures.TextureLoader.ByteBufferEncoder;
import static core.World.Weather.Sun.updateSun;
import static core.World.WorldGenerator.*;
import static org.lwjgl.opengl.GL13.*;

public class TextureDrawing {
    private static float playerX, playerY;
    //TODO: here need intHashMap..
    public static final HashMap<Integer, Integer> textures = new HashMap<>();

    //for textures (world)
    public static void drawTexture(String path, float x, float y, float zoom, SimpleColor color, boolean isStatic, boolean mirrorVertical) {
        if (color != null && color.getAlpha() == 0) {
            return;
        } else if (color == null) {
            color = SimpleColor.WHITE;
        }

        int textureId = path.hashCode();
        if (textures.get(textureId) == null) {
            bindTexture(path);
        }

        int id = textures.get(textureId);
        int width = TextureLoader.getSizeStatic(path).width();
        int height = TextureLoader.getSizeStatic(path).height();

        glBindTexture(GL_TEXTURE_2D, id);

        glPushMatrix();
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glLoadIdentity();

        glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);

        if (start && !isStatic) {
            glTranslatef(-playerX * zoom + Window.width / 2f - 32, -playerY * zoom + Window.height / 2f - 200, 0);
        }
//            glTranslatef(x * zoom + (width / 2f), y * zoom + (height / 2f), 0.0f);
//            glRotatef(0f, 0.0f, 0.0f, 1.0f);
//            glTranslatef(-(x * zoom + (width / 2f)), -(y * zoom + (height / 2f)), 0.0f);

        glMultMatrixf(new float[]{zoom, 0, 0, 0, 0, zoom, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1});
        glBegin(GL_QUADS);

        if (mirrorVertical) {
            //top right
            glTexCoord2f(1, 1);
            glVertex2f(x, y);
            //top left
            glTexCoord2f(0, 1);
            glVertex2f(x + width, y);
            //bottom left
            glTexCoord2f(0, 0);
            glVertex2f(x + width, y + height);
            //bottom right
            glTexCoord2f(1, 0);
            glVertex2f(x, y + height);
        } else {
            //top left
            glTexCoord2f(0, 1);
            glVertex2f(x, y);
            //top right
            glTexCoord2f(1, 1);
            glVertex2f(x + width, y);
            //bottom right
            glTexCoord2f(1, 0);
            glVertex2f(x + width, y + height);
            //bottom left
            glTexCoord2f(0, 0);
            glVertex2f(x, y + height);
        }

        glColor4f(1f, 1f, 1f, 1f);
        glEnd();

        glDisable(GL_TEXTURE_2D);
        glPopMatrix();

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public static void drawMultiTexture(String pathMain, String pathSecond, float x, float y, float zoom, SimpleColor color, boolean isStatic, boolean mirrorVertical) {
        if (color != null && color.getAlpha() == 0) {
            return;
        } else if (color == null) {
            color = SimpleColor.WHITE;
        }

        int textureId = (pathMain + pathSecond).hashCode();
        if (textures.get(textureId) == null) {
            bindTexture(TextureLoader.uniteTextures(pathMain, pathSecond), textureId, TextureLoader.getSize(pathMain).width(), TextureLoader.getSize(pathMain).height());
        }

        int id = textures.get(textureId);
        int width = TextureLoader.getSizeStatic(pathMain).width();
        int height = TextureLoader.getSizeStatic(pathMain).height();

        glBindTexture(GL_TEXTURE_2D, id);

        glPushMatrix();
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glLoadIdentity();

        glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);

        if (start && !isStatic) {
            glTranslatef(-playerX * zoom + Window.width / 2f - 32, -playerY * zoom + Window.height / 2f - 200, 0);
        }

        glMultMatrixf(new float[]{zoom, 0, 0, 0, 0, zoom, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1});
        glBegin(GL_QUADS);

        if (mirrorVertical) {
            //top right
            glTexCoord2f(1, 1);
            glVertex2f(x, y);
            //top left
            glTexCoord2f(0, 1);
            glVertex2f(x + width, y);
            //bottom left
            glTexCoord2f(0, 0);
            glVertex2f(x + width, y + height);
            //bottom right
            glTexCoord2f(1, 0);
            glVertex2f(x, y + height);
        } else {
            //top left
            glTexCoord2f(0, 1);
            glVertex2f(x, y);
            //top right
            glTexCoord2f(1, 1);
            glVertex2f(x + width, y);
            //bottom right
            glTexCoord2f(1, 0);
            glVertex2f(x + width, y + height);
            //bottom left
            glTexCoord2f(0, 0);
            glVertex2f(x, y + height);
        }

        glColor4f(1f, 1f, 1f, 1f);
        glEnd();

        glDisable(GL_TEXTURE_2D);
        glPopMatrix();

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public static void drawTexture(String path, float x, float y, float zoom, boolean isStatic) {
        drawTexture(path, x, y, zoom, SimpleColor.WHITE, isStatic, false);
    }

    //for video, text, etc
    public static void drawTexture(float x, float y, int width, int height, String name, ByteBuffer buffer, SimpleColor color, float zoom) {
        if (name != null && textures.get(name.hashCode()) != null) {
            glBindTexture(GL_TEXTURE_2D, textures.get(name.hashCode()));
        } else {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        }

        glPushMatrix();
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glLoadIdentity();

        glMultMatrixf(new float[]{zoom, 0, 0, 0, 0, zoom, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1});

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        glBegin(GL_QUADS);
        glTexCoord2f(0, 1);
        glVertex2f(x, y);

        glTexCoord2f(1, 1);
        glVertex2f(x + width, y);

        glTexCoord2f(1, 0);
        glVertex2f(x + width, y + height);

        glTexCoord2f(0, 0);
        glVertex2f(x, y + height);

        glColor4f(1f, 1f, 1f, 1f);
        glEnd();

        glDisable(GL_TEXTURE_2D);
        glPopMatrix();

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public static void drawText(int x, int y, String text, SimpleColor color) {
        int startX = x;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            if (ch == ' ') {
                x += getCharDimension('A').width;
                continue;
            } else if (ch == '\\' && text.charAt(i + 1) == 'n') {
                y -= 30;
                i++;
                x = startX;
                continue;
            }
            TextureDrawing.drawTexture(x, y, getCharDimension(ch).width, getCharDimension(ch).height, String.valueOf(ch), getCharBuffer(ch), color, 1);
            x += getCharDimension(ch).width;
        }
    }

    public static void drawText(int x, int y, String text) {
        drawText(x, y, text, new SimpleColor(210, 210, 210, 255));
    }

    public static void drawRectangleBorder(int x, int y, int width, int height, int thickness, SimpleColor color) {
        glPushMatrix();
        glLineWidth(thickness);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        drawRectangle(x, y, width, thickness, color); //Upper border
        drawRectangle(x + width - thickness, y + thickness, thickness, height - thickness * 2, color); //Right border
        drawRectangle(x, y + height - thickness, width, thickness, color); //Down border
        drawRectangle(x, y + thickness, thickness, height - thickness * 2, color); //Left border

        glEnd();
        glPopMatrix();
    }

    public static void drawRectangle(int x, int y, int width, int height, SimpleColor color) {
        glPushMatrix();
        glBegin(GL_QUADS);

        glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        glVertex2f(x, y);
        glVertex2f(x + width, y);
        glVertex2f(x + width, y + height);
        glVertex2f(x, y + height);
        glColor3f(1, 1, 1);

        glEnd();
        glPopMatrix();
    }

    public static void drawRoundedRectangle(int x, int y, int width, int height, SimpleColor color) {
        int radius = height / 2;
        int SEGMENTS = 16;
        float ANGLE_INCREMENT = (float) (2.0 * Math.PI / SEGMENTS);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBegin(GL_TRIANGLE_FAN);

        glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        glVertex2f(x + radius, y + radius);

        float theta = (float) (Math.PI / 4.0);
        for (int i = 0; i <= SEGMENTS; i++) {
            float dx = radius * (float) Math.cos(theta);
            float dy = radius * (float) Math.sin(theta);
            glVertex2f(x + radius + dx, y + radius - dy);
            theta += ANGLE_INCREMENT;
        }
        glEnd();

        glBegin(GL_QUADS);
        glVertex2f(x + radius, y);
        glVertex2f(x + width - radius, y);
        glVertex2f(x + width - radius, y + height);
        glVertex2f(x + radius, y + height);
        glEnd();

        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(x + width - radius, y + radius);

        theta = (float) (Math.PI / 4.0);
        for (int i = 0; i <= SEGMENTS; i++) {
            float dx = radius * (float) Math.sin(theta);
            float dy = radius * (float) Math.cos(theta);
            glVertex2f(x + width - radius + dx, y + radius + dy);
            theta += ANGLE_INCREMENT;
        }
        glEnd();

        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(x + width - radius, y + height - radius);

        theta = (float) (Math.PI / 4.0);
        for (int i = 0; i <= SEGMENTS; i++) {
            float dx = radius * (float) Math.sin(theta);
            float dy = radius * (float) Math.cos(theta);
            glVertex2f(x + width - radius + dx, y + height - radius - dy);
            theta += ANGLE_INCREMENT;
        }
        glEnd();

        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(x + radius, y + height - radius);

        theta = (float) (Math.PI / 4.0);
        for (int i = 0; i <= SEGMENTS; i++) {
            float dx = radius * (float) Math.cos(theta);
            float dy = radius * (float) Math.sin(theta);
            glVertex2f(x + radius - dx, y + height - radius - dy);
            theta += ANGLE_INCREMENT;
        }

        glColor4f(1, 1, 1, 1);
        glEnd();
    }

    public static void drawCircle(int x, int y, float radius, SimpleColor color) {
        int samples = 64;
        glPushMatrix();
        glBegin(GL_TRIANGLE_FAN);

        glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        glVertex2f(x, y);

        for (int i = 0; i <= samples; i++) {
            float angle = (float) (i * 2 * Math.PI / samples);
            float dx = (float) (radius * Math.cos(angle));
            float dy = (float) (radius * Math.sin(angle));
            glVertex2f(x + dx, y + dy);
        }
        glColor4f(1f, 1f, 1f, 1f);

        glEnd();
        glPopMatrix();
    }

    public static void drawPrompt(ButtonObject button) {
        if (getFromConfig("ShowPrompts").equals("true") && new Rectangle(button.x, button.y, button.width, button.height).contains(getMousePos()) && System.currentTimeMillis() - EventHandler.lastMouseMovedTime >= 1000 && button.prompt != null) {
            drawRectangleText(EventHandler.getMousePos().x, EventHandler.getMousePos().y, 0, button.prompt, false, new SimpleColor(40, 40, 40, 240));
        }
    }

    public static void drawRectangleText(int x, int y, int maxWidth, String text, boolean staticTransfer, SimpleColor panColor) {
        maxWidth = (maxWidth > 0 ? maxWidth : 1920 - x);
        y = staticTransfer ? y + getTextSize(text).width / maxWidth * 16 : y;

        StringBuilder modifiedText = new StringBuilder();
        int currentWidth = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == ' ') {
                currentWidth += getCharDimension('A').width;
            } else {
                currentWidth += getCharDimension(c).width;
            }
            if (currentWidth > maxWidth) {
                modifiedText.append("\\n");
                currentWidth = 0;
            }
            modifiedText.append(c);
        }
        text = modifiedText.toString();

        Dimension textSize = getTextSize(text);
        int width = textSize.width;
        int height = textSize.height;

        drawRectangle(x + 30, y - height / 2, width, height, panColor);
        drawText(x + 36, y + height - 32 - height / 2, text);
    }

    public static Dimension getTextSize(String text) {
        String longestLine = "";
        int width = 12;

        for (String line : text.split("\\\\n")) {
            if (line.length() >= longestLine.replaceAll("\\s+", "").length()) {
                longestLine = line;
            }
        }

        for (int i = 0; i < longestLine.length(); i++) {
            char c = longestLine.charAt(i);

            if (c == ' ') {
                width += getCharDimension('A').width;
                continue;
            }
            width += getCharDimension(c).width;
        }
        return new Dimension(width, (text.split("\\\\n").length) * 28 + 16);
    }

    public static void updateVideo() {
        if (!video.isEmpty()) {

            ByteBuffer buff = null;
            for (Map.Entry<String, Video> entry : video.entrySet()) {
                String name = entry.getKey();
                Video video = entry.getValue();

                if (video != null && video.isPlaying) {
                    if (video.frame == video.totalFrames) {
                        video.frame = 1;
                    }
                    if (byteBuffer.get(name) != null && !byteBuffer.get(name).equals(buff)) {
                        drawTexture(video.x, video.y, video.width, video.height, null, byteBuffer.get(name), SimpleColor.WHITE, 1);
                        buff = byteBuffer.get(name);
                    }
                }
            }
        }
    }

    public static void updatePlayerPos() {
        playerX = DynamicObjects.get(0).x;
        playerY = DynamicObjects.get(0).y;
    }

    public static void updateStaticObj() {
        updateSun();
        updatePlayerPos();

        for (int x = (int) (playerX / 16) - 20; x < playerX / 16 + 21; x++) {
            for (int y = (int) (playerY / 16) - 8; y < playerY / 16 + 16; y++) {
                if (x < 0 || y < 0 || x > SizeX || y > SizeY) {
                    continue;
                }
                short obj = getObject(x, y);

                if (StaticWorldObjects.getId(obj) == 0 || getPath(obj) == null) {
                    continue;
                }
                if (getHp(obj) <= 0) {
                    destroyObject(x, y);
                    continue;
                }

                float xBlock = findX(x, y);
                float yBlock = findY(x, y);

                if (isOnCamera(xBlock, yBlock, 16, 16)) {
                    SimpleColor color = ShadowMap.getColor(x, y);
                    if (currentWorldTemperature <= 0) {
                        int r = ShadowMap.checkColor((int) (color.getRed() + Math.max(currentWorldTemperature / 2, -75)));
                        int g = ShadowMap.checkColor((int) (color.getGreen() + Math.max(currentWorldTemperature / 2, -75)));

                        color = new SimpleColor(r, g, color.getBlue(), color.getAlpha());
                    }

                    if (getHp(obj) > getMaxHp(obj) / 1.5f) {
                        drawTexture(getPath(obj), xBlock, yBlock, 3f, color, false, false);

                    } else if (getHp(obj) < getMaxHp(obj) / 3) {
                        drawMultiTexture(getPath(obj), assetsDir("World/Blocks/damaged1.png"), xBlock, yBlock, 3f, color, false, false);

                    } else {
                        drawMultiTexture(getPath(obj), assetsDir("World/Blocks/damaged0.png"), xBlock, yBlock, 3f, color, false, false);
                    }
                }
            }
        }
        Factories.update();
    }

    public static boolean isOnCamera(float x, float y, float xSize, float ySize) {
        float left = DynamicObjects.get(0).x - (1920 / 5.5f) - (32 + xSize);
        float right = DynamicObjects.get(0).x + (1920 / 5.5f) + (32 - xSize);
        float bottom = DynamicObjects.get(0).y - (1080 / 16f) - (32 + ySize); //lower dividet number - higher drawing
        float top = DynamicObjects.get(0).y + (1080 / 4.5f) + (32 - ySize);

        return !(x + 16 < left) && !(x > right) && !(y + 16 < bottom) && !(y > top);
    }

    public static void updateDynamicObj() {
        for (int x = 0; x < DynamicObjects.size(); x++) {
            DynamicWorldObjects dynamicObject = DynamicObjects.get(x);

            if (dynamicObject != null && !dynamicObject.notForDrawing) {
                float left = DynamicObjects.get(0).x - (1920 / 5.5f) - (48);
                float right = DynamicObjects.get(0).x + (1920 / 5.5f) + (48);
                float bottom = DynamicObjects.get(0).y - (1080 / 16f) - (48); //lower dividet number - higher drawing
                float top = DynamicObjects.get(0).y + (1080 / 5f) + (48);

                float xBlock = dynamicObject.x;
                float yBlock = dynamicObject.y;

                if (dynamicObject.animSpeed != 0) {
                    if (dynamicObject.currentFrame != dynamicObject.framesCount && System.currentTimeMillis() - dynamicObject.lastFrameTime >= dynamicObject.animSpeed * 1000) {
                        dynamicObject.currentFrame++;
                        dynamicObject.lastFrameTime = System.currentTimeMillis();
                    } else if (dynamicObject.currentFrame == dynamicObject.framesCount && System.currentTimeMillis() - dynamicObject.lastFrameTime >= dynamicObject.animSpeed * 1000) {
                        dynamicObject.currentFrame = 0;
                        dynamicObject.lastFrameTime = System.currentTimeMillis();

                        if (dynamicObject.oneoffAnimation) {
                            dynamicObject.animSpeed = 0;
                        }
                    }
                }
                if (!(xBlock + 16 < left) && !(xBlock > right) && !(yBlock + 16 < bottom) && !(yBlock > top)) {
                    if (dynamicObject.framesCount == 0) {
                        drawTexture(dynamicObject.path, dynamicObject.x, dynamicObject.y, 3, ShadowMap.getColorDynamic(x), false, dynamicObject.mirrored);
                    } else {
                        drawTexture(dynamicObject.path + dynamicObject.currentFrame + ".png", dynamicObject.x, dynamicObject.y, 3, ShadowMap.getColorDynamic(x), false, dynamicObject.mirrored);
                    }
                }
            }
        }
    }

    public static void updateGUI() {
        updatePlayerGUI();
        updatePanels();
        updateSwapButtons();
        updateButtons();
        updateSliders();
        updateTexts();
    }

    private static void updatePanels() {
        for (PanelObject panel : panels.values()) {
            if (!panel.visible) {
                continue;
            }

            if (panel.options != null) {
                List<Integer> layers = panels.values().stream().map(p -> p.layer).distinct().sorted().toList();

                for (int layer : layers) {
                    for (PanelObject panelObj : panels.values()) {
                        if (panelObj.options != null && panelObj.layer == layer && panelObj.visible) {
                            drawTexture(panelObj.options, panelObj.x, panelObj.y, 1, true);
                        }
                    }
                }
                continue;
            }

            if (!panel.simple) {
                drawRectangle(panel.x, panel.y, panel.width, panel.height, panel.color);
                drawRectangleBorder(panel.x, panel.y, panel.width, panel.height, 20, panel.color);
            } else {
                drawRectangle(panel.x, panel.y, panel.width, panel.height, panel.color);
            }
        }
    }

    private static void updateSwapButtons() {
        for (Map.Entry<String, ButtonObject> entry : buttons.entrySet()) {
            ButtonObject button = entry.getValue();
            if (!button.visible || !button.swapButton) {
                continue;
            }

            if (button.simple && button.isClicked) {
                drawRectangle(button.x, button.y, button.width, button.height, button.color);
                drawTexture(assetsDir("UI/GUI/checkMarkTrue.png"), button.x + button.width / 1.3f, button.y + button.height / 3f, 1, true);
                drawText((int) (button.x * 1.1f), button.y + button.height / 3, button.name);
            } else if (button.simple) {
                drawRectangle(button.x, button.y, button.width, button.height, button.color);
                drawText((int) (button.x * 1.1f), button.y + button.height / 3, button.name);
            } else {
                //if swap and not simple
                if (button.isClicked) {
                    drawRectangleBorder(button.x - 6, button.y - 6, button.width, button.height, 6, button.color);
                    drawTexture(assetsDir("UI/GUI/checkMarkTrue.png"), button.x, button.y, 1, true);
                    drawText(button.width + button.x + 24, button.y, button.name);
                } else {
                    drawRectangleBorder(button.x - 6, button.y - 6, button.width, button.height, 6, button.color);
                    drawTexture(assetsDir("UI/GUI/checkMarkFalse.png"), button.x, button.y, 1, true);
                    drawText(button.width + button.x + 24, button.y, button.name);
                }
            }
            drawPrompt(button);
        }
    }

    private static void updateButtons() {
        for (Map.Entry<String, ButtonObject> entry : buttons.entrySet()) {
            ButtonObject button = entry.getValue();
            if (!button.visible || button.swapButton) {
                continue;
            }

            if (button.path != null) {
                drawTexture(button.path, button.x, button.y, 1, true);
                continue;
            }
            if (button.simple) {
                drawRectangle(button.x, button.y, button.width, button.height, button.color);
            } else {
                drawRectangleBorder(button.x, button.y, button.width, button.height, 6, button.color);
            }
            if (!button.isClickable) {
                drawRectangle(button.x, button.y, button.width, button.height, new SimpleColor(0, 0, 0, 123));
            }
            drawText(button.x + 20, (int) (button.y + button.height / 2.8f), button.name);
            drawPrompt(button);
        }
    }

    private static void updateSliders() {
        for (SliderObject slider : sliders.values()) {
            if (!slider.visible) {
                continue;
            }
            drawRectangle(slider.x, slider.y, slider.width, slider.height, slider.sliderColor);
            drawCircle(slider.sliderPos, slider.y + slider.height / 2, slider.height / 1.1f, slider.dotColor);
        }
    }

    private static void updateTexts() {
        for (TextObject text : texts.values()) {
            if (!text.visible) {
                continue;
            }
            drawText(text.x, text.y, text.text, text.color);
        }

        if (Commandline.created) {
            drawRectangle(20, 800, 650, 260, new SimpleColor(0, 0, 0, 220));
            drawRectangleText(-10, 810, 630, EventHandler.keyLoggingText, true, SimpleColor.BLACK);
        }
    }


    public static void bindTexture(String path) {
        ByteBuffer buffer = ByteBufferEncoder(path);

        int width = TextureLoader.getSize(path).width();
        int height = TextureLoader.getSize(path).height();
        int id = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        textures.put(path.hashCode(), id);

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public static void bindTexture(ByteBuffer buffer, int id, int width, int height) {
        int texId = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, texId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        textures.put(id, texId);

        glBindTexture(GL_TEXTURE_2D, 0);
    }
}
