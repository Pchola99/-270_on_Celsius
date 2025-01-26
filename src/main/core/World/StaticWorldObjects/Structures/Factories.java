package core.World.StaticWorldObjects.Structures;

import core.EventHandling.Logging.Config;
import core.Global;
import core.Utils.ArrayUtils;
import core.Utils.SimpleColor;
import core.Window;
import core.World.Creatures.Player.Inventory.Inventory;
import core.World.Creatures.Player.Inventory.Items.Items;
import core.World.StaticWorldObjects.FactoryType;
import core.World.WorldUtils;
import core.entity.BaseBlockEntity;
import core.g2d.Atlas;
import core.g2d.Fill;
import core.math.Point2i;
import core.math.Rectangle;
import core.ui.Sounds.Sound;

import static core.Global.*;
import static core.Utils.ArrayUtils.findEqualsObjects;
import static core.World.Creatures.Player.Player.playerSize;
import static core.World.Textures.TextureDrawing.blockSize;
import static core.World.Textures.TextureDrawing.drawText;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class Factories extends BaseBlockEntity<FactoryType> {
    public float needEnergy, currentEnergy;
    public int currentProductionProgress, maxProductionProgress;
    public Items[] outputObjects;
    public Items[] outputStoredObjects, inputStoredObjects, storedFuel;
    private static long lastMouseClickTime;

    @Override
    public void onItemDropped(Items item) {
        Point2i current = Inventory.currentObject;
        int cell = ArrayUtils.findFreeCell(this.inputStoredObjects);

        if (cell != -1 && current != null) {
            for (Items inputObject : type.inputObjects) {
                if (inputObject.id == item.id) {
                    this.inputStoredObjects[cell] = Inventory.getCurrent();
                    Inventory.decrementItem(current.x, current.y);

                    return;
                }
            }
        }

        cell = ArrayUtils.findFreeCell(this.storedFuel);

        if (cell != -1 && current != null) {
            for (Items items : type.fuel) {
                if (items.id == item.id) {
                    this.storedFuel[cell] = Inventory.getCurrent();
                    Inventory.decrementItem(current.x, current.y);

                    return;
                }
            }
        }
    }

    private Factories(int x, int y, FactoryType type) {
        super(x, y);
        setTile(type);

        this.maxProductionProgress = type.maxProductionProgress;
        this.needEnergy = type.needEnergy;
        this.outputStoredObjects = new Items[type.maxStoredObjects];
        this.inputStoredObjects = new Items[type.inputObjects.length];
        this.storedFuel = new Items[type.fuel == null ? 0 : type.fuel.length];
        this.outputObjects = type.outputObjects.clone();
    }

    public static Items[] transformItems(String items) {
        if (items == null) {
            return null;
        }

        String[] inItems = items.split(",");
        Items[] outItems = new Items[inItems.length];

        for (int i = 0; i < inItems.length; i++) {
            outItems[i] = Items.createItem(inItems[i]);
        }
        return outItems;
    }

    public void breakFactory(FactoryType.BreakingType breakingType) {
        switch (breakingType) {
            case WEAK_SLOW -> maxProductionProgress *= (int) (Math.random() * 4 + 1);
            case AVERAGE_STOP -> maxProductionProgress = Integer.MAX_VALUE;
            case AVERAGE_MISWORKING -> System.arraycopy(outputObjects, 0, outputObjects, 0, outputObjects.length - 1);
            case WEAK_OVERCONSUMPTION -> needEnergy *= (float) (Math.random() * 4 + 1);
        }
    }

    public void removeBreakEffect(FactoryType.BreakingType breakingType) {
        switch (breakingType) {
            case WEAK_SLOW, AVERAGE_STOP ->
                    maxProductionProgress = type.maxProductionProgress;
            case AVERAGE_MISWORKING ->
                    outputObjects = type.outputObjects.clone();
            case WEAK_OVERCONSUMPTION ->
                    needEnergy = type.needEnergy;
        }
    }

    @Override
    public void draw() {
        super.draw();
        if (System.currentTimeMillis() - input.getLastMouseMoveTimestamp() <= 1000) {
            return;
        }
        var mouse = input.mouseWorldPos();
        float xMouse = mouse.x;
        float yMouse = mouse.y;
        if (Rectangle.contains(worldX(), worldY(), type.texture.width(), type.texture.height(), xMouse, yMouse)) {
            boolean input = this.inputStoredObjects != null;
            boolean output = this.outputStoredObjects != null;
            SimpleColor color = SimpleColor.fromRGBA(0, 0, 0, 170);

            if (input && ArrayUtils.findFreeCell(this.inputStoredObjects) != 0) {
                int w = ArrayUtils.findDistinctObjects(this.inputStoredObjects) * 54 + playerSize;

                Fill.rect(xMouse, yMouse, w, 64, color);
                drawObjects(xMouse, yMouse, this.inputStoredObjects, atlas.byPath("UI/GUI/buildMenu/factoryIn.png"));
            }
            if (output && ArrayUtils.findFreeCell(this.outputStoredObjects) != 0) {
                xMouse += ArrayUtils.findFreeCell(this.inputStoredObjects) != 0 ? 78 : 0;
                int w = ArrayUtils.findDistinctObjects(this.outputStoredObjects) * 54 + playerSize;

                Fill.rect(xMouse, yMouse, w, 64, color);
                drawObjects(xMouse, yMouse, this.outputStoredObjects, atlas.byPath("UI/GUI/buildMenu/factoryOut.png"));
            }
        }
    }

    // todo вопрос на засыпку - почему оно в этом классе?
    public static void drawObjects(float x, float y, Items[] items, Atlas.Region iconRegion) {
        if (items != null && ArrayUtils.findFreeCell(items) != 0) {
            batch.draw(iconRegion, x, y + 16);

            for (int i = 0; i < ArrayUtils.findDistinctObjects(items); i++) {
                Items item = items[i];
                if (item != null) {
                    float scale = Items.computeZoom(item.texture);
                    int countInCell = findEqualsObjects(items, item);

                    drawText(x + i * 54 + playerSize + 31, y + 3, countInCell > 9 ? "9+" : String.valueOf(countInCell), SimpleColor.DIRTY_BRIGHT_BLACK);

                    int finalI = i;
                    batch.pushState(() -> {
                        batch.scale(scale);
                        batch.draw(item.texture, x + finalI * 54 + playerSize + 5, y + 15);
                    });
                }
            }
        }
    }

    //todo сделать нормально
    private static boolean mouseDoubleClick() {
        if (Global.input.justClicked(GLFW_MOUSE_BUTTON_LEFT)) {
            lastMouseClickTime = System.currentTimeMillis();
        }

        return System.currentTimeMillis() - lastMouseClickTime <= 60 && !Global.input.clicked(GLFW_MOUSE_BUTTON_LEFT);
    }

    @Override
    public void update() {
        var mouse = input.mouseWorldPos();
        float xMouse = mouse.x;
        float yMouse = mouse.y;
        if (Rectangle.contains(worldX(), worldY(), type.texture.width(), type.texture.height(), xMouse, yMouse)) {
            if (mouseDoubleClick() && this.outputStoredObjects[0] != null) {
                for (int i = 0; i < this.outputStoredObjects.length; i++) {
                    if (this.outputStoredObjects[i] == null) {
                        break;
                    }
                    Inventory.createElement(this.outputStoredObjects[i]);
                    this.outputStoredObjects[i] = null;
                }
            }
            if (type.fuel == null && type.breakingType != FactoryType.BreakingType.CRITICAL && this.currentEnergy >= this.needEnergy) {
                float iconY = worldY() + blockSize;
                float iconX = worldX() + blockSize;

                // todo починить отрисовку из других потоков
                batch.draw(atlas.byPath("UI/GUI/interactionIcon.png"), iconX, iconY);
                batch.draw(Window.defaultFont.getGlyph('E'),
                        worldX() + 16 + blockSize,
                        worldY() + 12 + blockSize);

                if (input.pressed(GLFW_KEY_E)) {
                    this.currentProductionProgress++;
                }

                if (findInput() && this.currentProductionProgress >= this.maxProductionProgress) {
                    int outputCell = ArrayUtils.findFreeCell(this.outputObjects);
                    for (int i = 0; i < (outputCell == -1 ? this.outputObjects.length : outputCell); i++) {
                        for (int j = 0; j < this.outputStoredObjects.length; j++) {
                            int cell = ArrayUtils.findFreeCell(this.outputStoredObjects);

                            if (cell != -1) {
                                this.currentProductionProgress = 0;
                                this.outputStoredObjects[cell] = this.outputObjects[i];
                                deleteFirstFound(type.inputObjects, this.inputStoredObjects);
                                Sound.playSound(type.sound, Sound.types.EFFECT, false);
                                break;
                            }
                        }
                    }
                }
                return;
            }
        }

        if (type.fuel != null && type.breakingType != FactoryType.BreakingType.CRITICAL && this.currentEnergy >= this.needEnergy) {
            this.currentProductionProgress++;

            if (findInput() && this.currentProductionProgress >= this.maxProductionProgress) {
                int outputCell = ArrayUtils.findFreeCell(this.outputObjects);

                for (int i = 0; i < (outputCell == -1 ? this.outputObjects.length : outputCell); i++) {
                    for (int j = 0; j < this.outputStoredObjects.length; j++) {
                        int cell = ArrayUtils.findFreeCell(this.outputStoredObjects);

                        if (cell != -1) {
                            this.currentProductionProgress = 0;
                            deleteFirstFound(this.storedFuel, type.fuel);
                            deleteFirstFound(type.inputObjects, this.inputStoredObjects);
                            this.outputStoredObjects[cell] = this.outputObjects[i];
                            Sound.playSound(type.sound, Sound.types.EFFECT, false);
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean findInput() {
        int length = 0;
        for (Items items : type.inputObjects) {
            for (Items inputStoredObject : this.inputStoredObjects) {
                if (items != null && inputStoredObject != null && items.id == inputStoredObject.id) {
                    length++;
                    break;
                }
            }
        }
        return type.inputObjects.length == length;
    }

    private static void deleteFirstFound(Items[] items, Items[] target) {
        for (Items item : items) {
            if (item == null) {
                continue;
            }
            for (int j = 0; j < target.length; j++) {
                if (target[j] != null && item.id == target[j].id) {
                    target[j] = null;
                    break;
                }
            }
        }
    }

    private Factories findFactoryUnderMouse() {
        Point2i blockUnderMouse = WorldUtils.getBlockUnderMousePoint();
        if (blockUnderMouse.x == x && blockUnderMouse.y == y) {
            return this;
        }
        return null;
    }
}
