package core.World.StaticWorldObjects;

import core.entity.BlockEntity;

public interface StaticBlocksEvents {
    void placeStatic(BlockEntity block);
    void destroyStatic(BlockEntity block);
}
