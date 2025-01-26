package core.World.Creatures.Player.Inventory;

import core.EventHandling.EventHandler;
import core.EventHandling.Logging.Config;
import core.Global;
import core.World.Creatures.Player.BuildMenu.BuildMenu;
import core.World.Creatures.Player.Inventory.Items.Items;
import core.Utils.SimpleColor;
import core.World.StaticWorldObjects.StaticObjectsConst;
import core.World.StaticWorldObjects.StaticWorldObjects;
import core.World.Textures.TextureDrawing;
import core.World.WorldGenerator;
import core.g2d.Atlas;
import core.math.Point2i;
import core.math.Rectangle;

import java.util.ArrayList;
import java.util.Arrays;

import static core.Global.*;
import static core.World.Textures.TextureDrawing.*;
import static core.World.WorldUtils.getBlockUnderMousePoint;
import static core.World.WorldUtils.getDistanceToMouse;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class Inventory {
    public static boolean inventoryOpen = false, create = false;
    public static Items[][] inventoryObjects = new Items[8][6];
    public static Point2i currentObject, underMouseItem;
    public static Items.Types currentObjectType;

    public static Items getCurrent() {
        Point2i current = currentObject;
        if (currentObject != null) {
            return inventoryObjects[current.x][current.y];
        }

        return null;
    }

    public static void draw() {
        String gridTex = "UI/GUI/inventory/inventory" + (inventoryOpen ? "Open" : "Closed");
        batch.draw(atlas.byPath(gridTex), inventoryOpen ? 1488 : 1866, 756);
        Items item;

        for (int x = inventoryOpen ? 0 : 7; x < inventoryObjects.length; x++) {
            for (int y = 0; y < inventoryObjects[x].length; y++) {
                item = inventoryObjects[x][y];
                if (item != null) {
                    drawInventoryItem(1498 + x * 54, 766 + y * 54f, item.countInCell + 1, item.texture);
                }
            }
        }
    }

    public static void create() {
        create = true;
        BuildMenu.create();
    }

    public static void update() {
        if (create) {
            updateCurrentItem();
        }
    }

    private static Point2i getObjectUnderMouse() {
        Point2i mousePos = input.mousePos();
        int x = mousePos.x;
        int y = mousePos.y;

        // 1488 и 756 - нижний левый угол инвентаря, 54 - размер ячейки
        if (x > 1488 && y > 756) {
            x -= 1488;
            y -= 756;
            return new Point2i(x / 54, y / 54);
        }
        return null;
    }

    public static void drawInventoryItem(float x, float y, int countInCell, Atlas.Region region) {
        drawInventoryItem(x, y, region);
        drawText(x + 31, y - 7, countInCell > 9 ? "9+" : String.valueOf(countInCell), SimpleColor.DIRTY_BRIGHT_BLACK);
    }

    public static void drawInventoryItem(float x, float y, Atlas.Region region) {
        float scale = Items.computeZoom(region);

        batch.pushState(() -> {
            batch.scale(scale);
            batch.draw(region, x + 5, y + 5);
        });
    }

    public static void decrementItem(int x, int y) {
        if (inventoryObjects[x][y] != null) {
            inventoryObjects[x][y].countInCell--;

            if (inventoryObjects[x][y].countInCell < 0) {
                inventoryObjects[x][y] = null;
                currentObject = null;
            }
        }
    }

    private static void updateCurrentItem() {
        updateUnderMouse();
        updateDropItem();

        if (EventHandler.getRectangleClick(1875, 1035, 1920, 1080)) {
            inventoryOpen = !inventoryOpen;
        }

        Point2i current = currentObject;
        if (current != null) {

            Point2i mousePos = input.mousePos();
            if (underMouseItem != null) {
                Items focusedItems = inventoryObjects[underMouseItem.x][underMouseItem.y];
                float scale = Items.computeZoom(focusedItems.texture);

                batch.pushState(() -> {
                    batch.scale(scale);
                    batch.draw(focusedItems.texture, mousePos.x - 15, mousePos.y - 15);
                });
            }
            if ((inventoryOpen || current.x > 6)) {
                batch.draw(atlas.byPath("UI/GUI/inventory/inventoryCurrent.png"), 1488 + current.x * 54, 756 + current.y * 54f);
            }
        }
    }

    public static void updateStaticBlocksPreview() {
        Point2i current = currentObject;

        if (current != null) {
            var placeable = inventoryObjects[current.x][current.y].placeable;
            int blockX = getBlockUnderMousePoint().x;
            int blockY = getBlockUnderMousePoint().y;

            if (placeable != null && underMouseItem == null && !Rectangle.contains(1488, 756, 500, 500, input.mousePos())) {
                boolean isDeclined = getDistanceToMouse() < 8 && WorldGenerator.checkPlaceRules(blockX, blockY, placeable);
                // TextureDrawing.addToBlocksQueue(blockX, blockY, placeable, isDeclined);

                if (Config.getFromConfig("BuildGrid").equalsIgnoreCase("true")) {
                    var color = SimpleColor.fromRGBA(230, 230, 230, 150);
                    batch.draw(atlas.byPath("World/buildGrid.png"), color, WorldGenerator.findX(blockX, blockY) - 243f, WorldGenerator.findY(blockX, blockY) - 244f);
                }
            }
        }
    }

    private static void updateUnderMouse() {
        Point2i underMouse = getObjectUnderMouse();

        if (underMouse != null && EventHandler.getRectangleClick(1488, 756, 1919, 1079) && underMouseItem == null) {
            boolean hasUnderMouseItem = inventoryObjects[underMouse.x][underMouse.y] != null;

            if (currentObject != underMouse && hasUnderMouseItem) {
                currentObject = underMouse;
                currentObjectType = inventoryObjects[underMouse.x][underMouse.y].type;

                if (input.justClicked(GLFW_MOUSE_BUTTON_LEFT)) {
                    underMouseItem = underMouse;
                }
            } else if (!hasUnderMouseItem) {
                currentObject = null;
                currentObjectType = null;
            }
        }
    }

    private static void moveItems(Point2i from, Point2i to) {
        Items buff = inventoryObjects[from.x][from.y];
        inventoryObjects[from.x][from.y] = inventoryObjects[to.x][to.y];
        inventoryObjects[to.x][to.y] = buff;
    }

    private static void updateDropItem() {
        if (!input.clicked(GLFW_MOUSE_BUTTON_LEFT) && underMouseItem != null) {
            // hasItemsMouse - inventory cell under the mouse when the mouse button is released, underMouseItem - item selected for movement or drop
            Point2i hasItemsMouse = getObjectUnderMouse();

            if (hasItemsMouse != null) {
                moveItems(hasItemsMouse, underMouseItem);
                currentObject = hasItemsMouse;
            } else {
                Point2i mousePos = Global.input.mouseBlockPos();
                var block = world.get(mousePos.x, mousePos.y);
                if (block != null) {
                    block.onItemDropped(inventoryObjects[underMouseItem.x][underMouseItem.y]);
                }
            }
            underMouseItem = null;
        }
    }

    private static Point2i findFreeCell() {
        for (int x = 0; x < inventoryObjects.length; x++) {
            for (int y = 0; y < inventoryObjects[x].length; y++) {
                if (x == 7 && y == 5) {
                    continue;
                }
                if (inventoryObjects[x][y] == null) {
                    return new Point2i(x, y);
                }
            }
        }
        return null;
    }

    public static int findCountID(int id) {
        return Arrays.stream(inventoryObjects).flatMapToInt(row -> Arrays.stream(row).filter(obj -> obj != null && obj.id == id).mapToInt(obj -> 1)).sum() + 1;
    }

    public static Point2i findItemByID(int id) {
        for (int x = 0; x < inventoryObjects.length; x++) {
            for (int y = 0; y < inventoryObjects[x].length; y++) {
                if (inventoryObjects[x][y] != null && inventoryObjects[x][y].id == id) {
                    return new Point2i(x, y);
                }
            }
        }
        return findFreeCell();
    }

    public static void createElement(Items item) {
        int id = item.name.hashCode();

        if (findCountID(id) > 1) {
            Point2i cell = findItemByID(id);
            inventoryObjects[cell.x][cell.y].countInCell++;
            return;
        }

        Point2i cell = findFreeCell();
        if (cell != null) {
            inventoryObjects[cell.x][cell.y] = item;
        }
    }

    public static void createElementTool(String name) {
        int id = name.hashCode();

        if (findCountID(id) > 1) {
            Point2i cell = findItemByID(id);
            inventoryObjects[cell.x][cell.y].countInCell++;
            return;
        }

        Point2i cell = findFreeCell();
        if (cell != null) {
            inventoryObjects[cell.x][cell.y] = Items.createItem(name);
        }
    }

    public static void createElementPlaceable(StaticObjectsConst object) {
        byte id = StaticWorldObjects.getId(object);

        if (findCountID(id) > 1) {
            Point2i cell = findItemByID(id);
            inventoryObjects[cell.x][cell.y].countInCell++;
            return;
        }

        Point2i cell = findFreeCell();
        if (cell != null) {
            inventoryObjects[cell.x][cell.y] = Items.createItem(object);
        }
    }

    public static void createElementDetail(String name) {
        int id = name.hashCode();

        if (findCountID(id) > 1) {
            Point2i cell = findItemByID(id);
            inventoryObjects[cell.x][cell.y].countInCell++;
            return;
        }

        Point2i cell = findFreeCell();
        if (cell != null) {
            inventoryObjects[cell.x][cell.y] = Items.createItem(name);
        }
    }

    public static void createElementWeapon(String name) {
        int id = name.hashCode();

        if (findCountID(id) > 1) {
            Point2i cell = findItemByID(id);
            inventoryObjects[cell.x][cell.y].countInCell++;
            return;
        }

        Point2i cell = findFreeCell();
        if (cell != null) {
            inventoryObjects[cell.x][cell.y] = Items.createItem(name);
        }
    }
}
