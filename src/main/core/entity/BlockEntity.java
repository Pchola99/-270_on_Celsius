package core.entity;

import core.World.Creatures.Player.Inventory.Items.Items;
import core.World.StaticWorldObjects.StaticObjectsConst;
import core.World.Textures.TextureDrawing;

public interface BlockEntity {

    int x();
    int y();

    default float worldX() { return x() * TextureDrawing.blockSize; }
    default float worldY() { return y() * TextureDrawing.blockSize; }

    float hp();

    boolean damage(float d);

    StaticObjectsConst type();

    void onItemDropped(Items item);

    void update();
    void draw();
}
