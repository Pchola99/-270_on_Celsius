package core.World;

import core.World.Creatures.Player.Player;
import core.World.StaticWorldObjects.StaticBlocksEvents;
import core.World.StaticWorldObjects.StaticObjectsConst;
import core.World.Textures.ShadowMap;
import core.entity.BaseBlockEntity;
import core.entity.BlockEntity;
import core.entity.TileEntity;
import core.math.MathUtil;
import core.math.Point2i;

import java.util.ArrayList;

import static core.Window.start;
import static core.World.StaticWorldObjects.StaticWorldObjects.*;

public final class World {
    public final int sizeX, sizeY;
    public final BlockEntity[] tiles;

    private final ArrayList<StaticBlocksEvents> listeners = new ArrayList<>();

    public int dayCount;

    public World(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.tiles = new BaseBlockEntity[sizeX * sizeY];
    }

    public void registerListener(StaticBlocksEvents listener) {
        listeners.add(listener);
    }

    public void set(int x, int y, StaticObjectsConst object, boolean followingRules) {
        // Global.app.ensureMainThread();
        // assert object != -1;
        if (!inBounds(x, y)) {
            return;
        }

        if (object != null && object.optionalTiles != null) {
            StaticObjectsConst[][] tiles = object.optionalTiles;

            for (int blockX = 0; blockX < tiles.length; blockX++) {
                for (int blockY = 0; blockY < tiles[0].length; blockY++) {
                    if (tiles[blockX][blockY] != null && getType(tiles[blockX][blockY]) != StaticObjectsConst.Types.GAS) {
                        setImpl(x + blockX, y + blockY, tiles[blockX][blockY], followingRules);
                    }
                }
            }
            setImpl(x, y, object, followingRules);
        } else {
            setImpl(x, y, object, followingRules);
        }

        if (start) {
            var block = get(x, y);
            if (block != null) {
                for (StaticBlocksEvents listener : listeners) {
                    listener.placeStatic(block);
                }
            }
        }
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < sizeX && y >= 0 && y < sizeY;
    }

    public BlockEntity get(int x, int y) {
        // Global.app.ensureMainThread();

        if (x < 0 || x >= sizeX || y < 0 || y >= sizeY) {
            throw new IllegalStateException();
        }
        return tiles[x + sizeX * y];
    }

    public void destroy(int x, int y) {
        // Global.app.ensureMainThread();
        if (!inBounds(x, y)) {
            return;
        }

        var block = get(x, y);
        if (block != null) {
            if (block.type().hasMotherBlock || block.type().optionalTiles != null) {
                Point2i root = Player.findRoot(x, y);

                if (root != null) {
                    deleteTiles(get(root.x, root.y).type(), root.x, root.y);
                }
            } else {
                set(x, y, null, false);
                ShadowMap.update();
            }

            if (start) {
                for (StaticBlocksEvents listener : listeners) {
                    listener.destroyStatic(block);
                }
            }
        }
    }

    // region Детали реализации

    private void deleteTiles(StaticObjectsConst id, int cellX, int cellY) {
        for (int blockX = 0; blockX < id.optionalTiles.length; blockX++) {
            for (int blockY = 0; blockY < id.optionalTiles[0].length; blockY++) {
                tiles[(cellX + blockX) + sizeX * (cellY + blockY)] = null;
            }
        }
        ShadowMap.update();
    }

    private void setImpl(int x, int y, StaticObjectsConst object, boolean followingRules) {
        if (!followingRules || checkPlaceRules(x, y, object)) {
            var block = new TileEntity(x, y);
            block.setTile(object);
            tiles[x + sizeX * y] = block;
        }
    }

    private boolean checkPlaceRules(int x, int y, StaticObjectsConst root) {
        var entity1 = get(x, y);
        if (entity1 != null) {
            return false;
        }
        if (root.optionalTiles != null) {
            var tiles = root.optionalTiles;

            for (int xBlock = 0; xBlock < tiles.length; xBlock++) {
                if (!inBounds(x + xBlock, y - 1)) {
                    return false;
                }
                if (getResistance(get(x + xBlock, y - 1)) < 100) {
                    return false;
                }
                for (int yBlock = 0; yBlock < tiles[0].length; yBlock++) {
                    if (!inBounds(x + xBlock, y)) {
                        return false;
                    }
                    if (get(x + xBlock, y) != null) {
                        return false;
                    }
                }
            }
        } else {
            for (Point2i d : MathUtil.CROSS_OFFSETS) {
                if (!inBounds(x + d.x, y + d.y)) {
                    continue;
                }
                if (getResistance(get(x + d.x, y + d.y)) >= 100) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    // endregion
}
