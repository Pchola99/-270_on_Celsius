package core.World.Textures;

import core.util.ArrayUtils;
import core.World.Creatures.Player.Inventory.Items.Items;
import core.World.Creatures.Player.Inventory.Items.Weapons.Ammo.Bullets;
import core.g2d.Atlas;
import core.util.Color;
import core.Window;
import core.World.Creatures.DynamicWorldObjects;
import core.World.StaticWorldObjects.StaticWorldObjects;
import core.World.StaticWorldObjects.Structures.Factories;
import core.World.StaticWorldObjects.TemperatureMap;
import core.World.WorldUtils;
import core.g2d.Fill;
import core.g2d.Font;
import core.math.Point2i;
import core.math.Rectangle;
import core.ui.Styles;

import java.util.*;

import static core.Global.*;
import static core.util.ArrayUtils.findEqualsObjects;
import static core.World.Creatures.Player.Player.*;
import static core.World.StaticWorldObjects.StaticWorldObjects.*;
import static core.World.WorldGenerator.*;

public class TextureDrawing {
    public static final int blockSize = 48;

    private static final ArrayDeque<BlockPreview> blocksQueue = new ArrayDeque<>();

    private static final Rectangle viewport = new Rectangle();

    public static void drawObjects(float x, float y, Items[] items, Atlas.Region iconRegion) {
        if (items != null && ArrayUtils.findFreeCell(items) != 0) {
            batch.draw(iconRegion, x, y + 16);

            for (int i = 0; i < ArrayUtils.findDistinctObjects(items); i++) {
                Items item = items[i];
                if (item != null) {
                    float scale = Items.computeZoom(item.texture);
                    int countInCell = findEqualsObjects(items, item);

                    drawText((x + (i * 54)) + playerSize + 31, y + 3, countInCell > 9 ? "9+" : String.valueOf(countInCell), Styles.DIRTY_BRIGHT_BLACK);

                    int finalI = i;
                    batch.pushState(() -> {
                        batch.scale(scale);
                        batch.draw(item.texture, ((x + (finalI * 54)) + playerSize + 5), (y + 15));
                    });
                }
            }
        }
    }

    public record BlockPreview(int x, int y, short blockId, boolean breakable) {}

    public static void drawText(float x, float y, String text, Color color) {
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
        drawText(x, y, text, Styles.TEXT_COLOR);
    }

    public static void drawPointArray(Point2i[] points) {
        Fill.lineWidth(4f);

        var color = Color.fromRgba8888(0, 0, 0, 1);
        float d = blockSize + 8;

        for (int i = 0; i < points.length - 1; i++) {
            Fill.line(points[i].x * d, points[i].y * d, points[i + 1].x * d, points[i + 1].y * d, color);
        }
        Fill.resetLineWidth();
    }

    public static void drawRectangleText(int x, int y, int maxWidth, String text, boolean staticTransfer, Color panColor, Font font) {
        maxWidth = (maxWidth > 0 ? maxWidth : 1920 - x);
        y = staticTransfer ? y + getTextSize(text).x / maxWidth * blockSize : y;

        StringBuilder modifiedText = new StringBuilder();
        int currentWidth = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == ' ') {
                currentWidth += font.getGlyph('A').width();
            } else {
                currentWidth += font.getGlyph(c).width();
            }
            if (currentWidth > maxWidth) {
                modifiedText.append("\\n");
                currentWidth = 0;
            }
            modifiedText.append(c);
        }
        text = modifiedText.toString();

        var textSize = getTextSize(text);
        int width = textSize.x;
        int height = textSize.y;

        Fill.rect(x + 30, y - height / 2f, width, height, panColor);
        drawText(x + 36, y + height - 32 - height / 2f, text);
    }

    private static final Point2i textSize = new Point2i();

    public static Point2i getTextSize(String text) {
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
        return textSize.set(width, linesCount * 28 + 16);
    }

    public static void drawStatic() {
        camera.getBoundsTo(viewport);
        int minX = (int) Math.floor(viewport.x / blockSize);
        int maxX = (int) Math.floor((viewport.x + viewport.width) / blockSize);
        int minY = (int) Math.floor(viewport.y / blockSize);
        int maxY = (int) Math.floor((viewport.y + viewport.height) / blockSize);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (!world.inBounds(x, y)) {
                    continue;
                }
                short obj = world.get(x, y);
                if (obj == -1 || obj == 0) {
                    continue;
                }
                if (getTexture(obj) == null) {
                    continue;
                }
                // TODO всё же тут нужно проверять объекты на поле зрения
                drawBlock(x, y, obj);
            }
        }

        Factories.draw();

        for (BlockPreview q : blocksQueue) {
            drawQueuedBlock(q.x, q.y, q.blockId, q.breakable);
        }
        blocksQueue.clear();
    }

    private static final Color tmp = new Color();

    private static void drawQueuedBlock(int x, int y, short blockId, boolean breakable) {
        if (blockId == -1 || StaticWorldObjects.getTexture(blockId) == null) {
            return;
        }

        byte hp = getHp(blockId);
        float wx = x * blockSize;
        float wy = y * blockSize;

        if (isOnCamera(wx, wy, getTexture(blockId))) {
            Color color = ShadowMap.getColorTo(x, y, tmp);
            int a = (color.r() + color.g() + color.b()) / 3;
            if (breakable) {
                color.set(Math.max(0, a - 150), Math.max(0, a - 150), a, 255);
            } else {
                color.set(a, Math.max(0, a - 150), Math.max(0, a - 150), 255);
            }

            batch.draw(getTexture(blockId), color, wx, wy);

            float maxHp = getMaxHp(blockId);
            if (hp > maxHp / 1.5f) {
                // ???
            } else if (hp < maxHp / 3) {
                batch.draw(atlas.byPath("World/Blocks/damaged1.png"), wx, wy);
            } else {
                batch.draw(atlas.byPath("World/Blocks/damaged0.png"), wx, wy);
            }
        }
    }

    private static void drawBlock(int x, int y, short obj) {
        byte hp = getHp(obj);

        int xBlock = findX(x, y);
        int yBlock = findY(x, y);

        Color color = ShadowMap.getColorTo(x, y, tmp);
        int upperLimit = 100;
        int lowestLimit = -20;
        int maxColor = 65;
        float temp = TemperatureMap.getTemp(x, y);

        int a;
        if (temp > upperLimit) {
            a = (int) Math.min(maxColor, Math.abs((temp - upperLimit) / 3));
            color.set(color.r(), color.g() - (a / 2), color.b() - a, color.a());
        } else if (temp < lowestLimit) {
            a = (int) Math.min(maxColor, Math.abs((temp + lowestLimit) / 3));
            color.set(color.r() - a, color.g() - (a / 2), color.b(), color.a());
        }

        batch.draw(getTexture(obj), color, xBlock, yBlock);

        float maxHp = getMaxHp(obj);
        if (hp > maxHp / 1.5f) {
            // ???
        } else if (hp < maxHp / 3) {
            batch.draw(atlas.byPath("World/Blocks/damaged1"), xBlock, yBlock);
        } else {
            batch.draw(atlas.byPath("World/Blocks/damaged0"), xBlock, yBlock);
        }
    }

    public static void addToBlocksQueue(int blockX, int blockY, short obj, boolean breakable) {
        blocksQueue.add(new BlockPreview(blockX, blockY, obj, breakable));
    }

    public static void updateBlocksInteraction() {
        char interactionChar = 'E';
        Point2i mousePos = WorldUtils.getBlockUnderMousePoint();
        Point2i root = findRoot(mousePos.x, mousePos.y);
        boolean interactionButtonPressed = input.pressed(interactionChar);

        if (root != null) {
            Runnable interaction = StaticWorldObjects.getOnInteraction(world.get(root.x, root.y));

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

    public static boolean isOnCamera(float x, float y, Atlas.Region texture) {
        camera.getBoundsTo(viewport);

        return viewport.contains(x, y, texture.width(), texture.height());
    }

    public static void drawDynamic() {
        for (DynamicWorldObjects dynamicObject : DynamicObjects) {
            if (dynamicObject != null) {
                dynamicObject.incrementCurrentFrame();

                if (isOnCamera(dynamicObject.getX(), dynamicObject.getY(), dynamicObject.getTexture())) {
                    if (dynamicObject.getFramesCount() == 0) {
                        var shadow = ShadowMap.getColorDynamic(dynamicObject);
                        batch.draw(dynamicObject.getTexture()/*, shadow*/, dynamicObject.getX(), dynamicObject.getY());
                    } else {
                        // todo дописать
                        // drawTexture(dynamicObject.getPath() + dynamicObject.getCurrentFrame() + ".png", dynamicObject.getX(), dynamicObject.getY(), ShadowMap.getColorDynamic(), false, false);
                    }
                }
            }
        }

        Bullets.drawBullets();
    }
}
