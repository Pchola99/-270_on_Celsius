package core.World.Textures;

import core.EventHandling.EventHandler;
import core.EventHandling.Logging.Config;
import core.ui.BaseButton;
import core.ui.GUI.Video;
import core.Utils.SimpleColor;
import core.Utils.Sized;
import core.Window;
import core.World.Creatures.DynamicWorldObjects;
import core.World.Creatures.Player.Inventory.Inventory;
import core.World.StaticWorldObjects.StaticWAnimations;
import core.World.StaticWorldObjects.StaticWorldObjects;
import core.World.StaticWorldObjects.Structures.Factories;
import core.World.StaticWorldObjects.TemperatureMap;
import core.World.WorldUtils;
import core.g2d.Fill;
import core.g2d.Font;
import core.graphic.Layer;
import core.math.Point2i;
import core.math.Rectangle;
import core.math.Vector2f;
import core.ui.Styles;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.*;

import static core.EventHandling.Logging.Config.getFromConfig;
import static core.Global.*;
import static core.ui.GUI.Video.byteBuffer;
import static core.ui.GUI.Video.video;
import static core.World.Creatures.Player.Player.*;
import static core.World.StaticWorldObjects.StaticWorldObjects.*;
import static core.World.Weather.Sun.updateSun;
import static core.World.WorldGenerator.*;

public class TextureDrawing {
    private static final ArrayDeque<blockQueue> blocksQueue = new ArrayDeque<>();
    private static final ArrayDeque<Vector2f> smoothCameraX = new ArrayDeque<>(), smoothCameraY = new ArrayDeque<>();
    private static final int multiplySmoothCameraX = Integer.parseInt(Config.getFromConfig("SmoothingCameraHorizontal")), multiplySmoothCameraY = Integer.parseInt(Config.getFromConfig("SmoothingCameraVertical"));
    public static final int blockSize = 48;
    public static float playerX = 0, playerY = 0;

    public static Rectangle viewport = new Rectangle();

    public record blockQueue(int cellX, int cellY, short obj, boolean breakable) {}

    // todo Сломано сознательно, чуть позже доделаю
    @Deprecated(forRemoval = true)
    public static void drawTexture(float x, float y, int w, int h, float zoom, boolean isStatic, int id, ByteBuffer buffer, SimpleColor color) {
    }

    public static void drawText(float x, float y, String text, SimpleColor color) {
        float startX = x;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            if (ch == ' ') {
                x += Window.defaultFont.getGlyph('A').width();
                continue;
            } else if (ch == '\\' && i + 1 < text.length() && text.charAt(i + 1) == 'n') {
                y -= 30;
                i++;
                x = startX;
                continue;
            }
            Font.Glyph glyph = Window.defaultFont.getGlyph(ch);
            batch.draw(glyph, color, x, y);
            x += glyph.width();
        }
    }

    public static void drawText(float x, float y, String text) {
        drawText(x, y, text, SimpleColor.TEXT_COLOR);
    }

    public static void drawPrompt(BaseButton<?> button) {
        if (getFromConfig("ShowPrompts").equals("true")) {
            if (EventHandler.isMousePressed(button) && System.currentTimeMillis() - input.getLastMouseMoveTimestamp() >= 1000 && button.prompt != null) {
                drawRectangleText(input.mousePos().x, input.mousePos().y, 0, button.prompt, false, Styles.DEFAULT_PANEL_COLOR);
            }
        }
    }

    public static void drawPointArray(Point2i[] points) {
        Fill.lineWidth(4f);

        var color = SimpleColor.fromRGBA(0, 0, 0, 1);
        float d = blockSize + 8;

        for (int i = 0; i < points.length - 1; i++) {
            Fill.line(points[i].x * d, points[i].y * d, points[i + 1].x * d, points[i + 1].y * d, color);
        }
        Fill.resetLineWidth();
    }

    public static void drawRectangleText(int x, int y, int maxWidth, String text, boolean staticTransfer, SimpleColor panColor) {
        maxWidth = (maxWidth > 0 ? maxWidth : 1920 - x);
        y = staticTransfer ? y + getTextSize(text).width / maxWidth * blockSize : y;

        StringBuilder modifiedText = new StringBuilder();
        int currentWidth = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == ' ') {
                currentWidth += Window.defaultFont.getGlyph('A').width();
            } else {
                currentWidth += Window.defaultFont.getGlyph(c).width();
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

        Fill.rect(x + 30, y - height / 2f, width, height, panColor);
        drawText(x + 36, y + height - 32 - height / 2f, text);
    }

    public static Dimension getTextSize(String text) {
        String longestLine = "";
        int width = 12;
        int linesCount = 0;

        // find '\\n'
        for (String line : text.split("\\\\n")) {
            linesCount++;
            if (line.length() >= longestLine.replaceAll("\\s+", "").length()) {
                longestLine = line;
            }
        }

        for (int i = 0; i < longestLine.length(); i++) {
            char c = longestLine.charAt(i);

            if (c == ' ') {
                width += Window.defaultFont.getGlyph('A').width();
                continue;
            }
            width += Window.defaultFont.getGlyph(c).width();
        }
        return new Dimension(width, linesCount * 28 + 16);
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
                        // TODO сломано специально
                        // drawTexture(video.x, video.y, video.width, video.height, 1, true, name.hashCode(), byteBuffer.get(name), SimpleColor.WHITE);
                        buff = byteBuffer.get(name);
                    }
                }
            }
        }
    }

    private static void updatePlayerPos() {
        DynamicWorldObjects player = DynamicObjects.getFirst();

        playerX = player.getX();
        playerY = player.getY();

        if (multiplySmoothCameraX > 0) {
            smoothCameraX.add(new Vector2f(playerX, player.getMotionVectorX()));

            // todo Math.max(Window.pfps - 75, 0) / 7 не воспринимать всерьез - это костыль просто для того, чтоб плавная камера работала на любом фпс, потом переделаю
            if (smoothCameraX.size() > multiplySmoothCameraX + Math.max(app.getFpsMeasurement() - 75, 0) / 7) {
                playerX = smoothCameraX.getFirst().x - (smoothCameraX.getFirst().y * multiplySmoothCameraX);
                smoothCameraX.removeFirst();
            }
        }

        if (multiplySmoothCameraY > 0) {
            smoothCameraY.add(new Vector2f(playerY, player.getMotionVectorY()));

            // по тех. причинам тут пока без интерполяции
            if (smoothCameraY.size() > multiplySmoothCameraY + Math.max(app.getFpsMeasurement() - 75, 0) / 7) {
                playerY = smoothCameraY.getFirst().x;
                smoothCameraY.removeFirst();
            }
        }

        camera.position.set(playerX + 32, playerY + 200);
        camera.update();

        batch.matrix(camera.projection);
    }

    public static void updateStaticObj() {
        Factories.update();

        batch.pushState(() -> {
            batch.z(Layer.BACKGROUND);
            updateSun();
        });

        // always before drawing the blocks!!!
        updatePlayerPos();

        for (int x = (int) (playerX / blockSize) - 20; x < playerX / blockSize + 21; x++) {
            for (int y = (int) (playerY / blockSize) - 8; y < playerY / blockSize + blockSize; y++) {
                if (x < 0 || y < 0 || x > SizeX || y > SizeY) {
                    continue;
                }
                drawBlock(x, y);
            }
        }

        Iterator<blockQueue> iterator = blocksQueue.iterator();
        while (iterator.hasNext()) {
            blockQueue q = iterator.next();

            drawQueuedBlock(q.cellX, q.cellY, q.obj, q.breakable);
            iterator.remove();
        }

        // todo превью не хочет рисоваться откуда должно, поэтому висит тут, может потом что то красивое придумаю
        Inventory.updateStaticBlocksPreview();
        updateBlocksInteraction();
    }

    private static void drawQueuedBlock(int x, int y, short obj, boolean breakable) {
        if (obj == -1 || StaticWorldObjects.getTexture(obj) == null) {
            return;
        }

        byte hp = getHp(obj);
        int xBlock = findX(x, y);
        int yBlock = findY(x, y);

        if (isOnCamera(xBlock, yBlock, getTexture(obj))) {
            SimpleColor color = ShadowMap.getColor(x, y);
            int a = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
            SimpleColor blockColor = breakable ? SimpleColor.fromRGBA(Math.max(0, a - 150), Math.max(0, a - 150), a, 255) : SimpleColor.fromRGBA(a, Math.max(0, a - 150), Math.max(0, a - 150), 255);

            StaticWAnimations.AnimData currentFrame = StaticWAnimations.getCurrentFrame(obj, new Point2i(x, y));
            if (currentFrame != null) {
                drawTexture(xBlock, yBlock, currentFrame.width(), currentFrame.height(), 1, false, currentFrame.currentFrame() + StaticWorldObjects.getId(obj), currentFrame.currentFrameImage(), blockColor);
                return;
            }

            batch.draw(getTexture(obj), blockColor, xBlock, yBlock);

            float maxHp = getMaxHp(obj);
            if (hp > maxHp / 1.5f) {
                // ???
            } else if (hp < maxHp / 3) {
                batch.draw(atlas.byPath("World/Blocks/damaged1.png"), xBlock, yBlock);
            } else {
                batch.draw(atlas.byPath("World/Blocks/damaged0.png"), xBlock, yBlock);
            }
        }
    }

    private static void drawBlock(int x, int y) {
        short obj = getObject(x, y);

        if (obj == -1 || StaticWorldObjects.getId(obj) == 0 || getTexture(obj) == null) {
            return;
        }
        byte hp = getHp(obj);
        if (hp <= 0) {
            destroyObject(x, y);
            return;
        }

        int xBlock = findX(x, y);
        int yBlock = findY(x, y);

        if (isOnCamera(xBlock, yBlock, getTexture(obj))) {
            SimpleColor color = ShadowMap.getColor(x, y);
            int upperLimit = 100;
            int lowestLimit = -20;
            int maxColor = 65;
            float temp = TemperatureMap.getTemp(x, y);

            int a;
            if (temp > upperLimit) {
                a = (int) Math.min(maxColor, Math.abs((temp - upperLimit) / 3));
                color = SimpleColor.fromRGBA(color.getRed(), color.getGreen() - (a / 2), color.getBlue() - a, color.getAlpha());

            } else if (temp < lowestLimit) {
                a = (int) Math.min(maxColor, Math.abs((temp + lowestLimit) / 3));
                color = SimpleColor.fromRGBA(color.getRed() - a, color.getGreen() - (a / 2), color.getBlue(), color.getAlpha());
            }

            StaticWAnimations.AnimData currentFrame = StaticWAnimations.getCurrentFrame(obj, new Point2i(x, y));
            if (currentFrame != null) {
                drawTexture(xBlock, yBlock, currentFrame.width(), currentFrame.height(), 1, false, currentFrame.currentFrame() + StaticWorldObjects.getId(obj), currentFrame.currentFrameImage(), color);
                return;
            }

            batch.draw(getTexture(obj), color, xBlock, yBlock);

            float maxHp = getMaxHp(obj);
            if (hp > maxHp / 1.5f) {
                // ???
            } else if (hp < maxHp / 3) {
                batch.draw(atlas.byPath("World/Blocks/damaged1.png"), xBlock, yBlock);
            } else {
                batch.draw(atlas.byPath("World/Blocks/damaged0.png"), xBlock, yBlock);
            }
        }
    }

    public static void addToBlocksQueue(int cellX, int cellY, short obj, boolean breakable) {
        blocksQueue.add(new TextureDrawing.blockQueue(cellX, cellY, obj, breakable));
    }

    private static void updateBlocksInteraction() {
        char interactionChar = 'E';
        Point2i mousePos = WorldUtils.getBlockUnderMousePoint();
        Point2i root = findRoot(mousePos.x, mousePos.y);
        boolean interactionButtonPressed = input.pressed(interactionChar);

        if (root != null) {
            Runnable interaction = StaticWorldObjects.getOnInteraction(getObject(root.x, root.y));

            if (currentInteraction != null && currentInteraction.isAlive() && interactionButtonPressed) {
                currentInteraction.interrupt();
                return;
            }
            if (interaction != null) {
                int iconY = (root.y * blockSize) + blockSize;
                int iconX = (root.x * blockSize) + blockSize;
                batch.draw(atlas.byPath("UI/GUI/interactionIcon.png"), iconX, iconY);
                batch.draw(Window.defaultFont.getGlyph(interactionChar),
                        (root.x * blockSize + 16) + blockSize,
                        (root.y * blockSize + 12) + blockSize);

                if (interactionButtonPressed) {
                    currentInteraction = new Thread(interaction);
                    currentInteraction.start();
                }
            }
        }
    }

    public static boolean isOnCamera(float x, float y, Sized texture) {
        camera.getBounds(viewport);

        return viewport.contains(x, y, texture.width(), texture.height());
    }

    public static void updateDynamicObj() {
        for (DynamicWorldObjects dynamicObject : DynamicObjects) {
            if (dynamicObject != null) {
                dynamicObject.incrementCurrentFrame();

                if (isOnCamera(dynamicObject.getX(), dynamicObject.getY(), dynamicObject.getTexture())) {
                    if (dynamicObject.getFramesCount() == 0) {
                        var shadow = ShadowMap.getColorDynamic(dynamicObject);
                        batch.draw(dynamicObject.getTexture(), shadow, dynamicObject.getX(), dynamicObject.getY());
                    } else {
                        // todo дописать
                        // drawTexture(dynamicObject.getPath() + dynamicObject.getCurrentFrame() + ".png", dynamicObject.getX(), dynamicObject.getY(), ShadowMap.getColorDynamic(), false, false);
                    }
                }
            }
        }

        updateTemperatureEffect();
    }

    public static void updateGUI() {
        scene.update();
        scene.draw();
        updatePlayerGUI();
    }
}
