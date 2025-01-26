package core.entity;

import core.Utils.SimpleColor;
import core.World.Creatures.Player.Inventory.Items.Items;
import core.World.StaticWorldObjects.StaticObjectsConst;
import core.World.StaticWorldObjects.TemperatureMap;
import core.World.Textures.ShadowMap;

import static core.Global.*;

public abstract class BaseBlockEntity<B extends StaticObjectsConst> implements BlockEntity {
    protected static final SimpleColor tmpColor = new SimpleColor();

    protected B type;
    protected int x, y;
    protected float hp;

    public BaseBlockEntity(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setTile(B tile) {
        this.type = tile;
        this.hp = tile.maxHp;
    }

    @Override
    public final int x() {
        return x;
    }

    @Override
    public final int y() {
        return y;
    }

    @Override
    public final float hp() {
        return hp;
    }

    @Override
    public final boolean damage(float d) {
        hp -= d;
        if (hp <= 0) {
            onDestroy();
            world.destroy(x, y);
            return true;
        }
        return false;
    }

    protected void onDestroy() {
    }

    @Override
    public final B type() {
        return type;
    }

    @Override
    public void update() {

    }

    @Override
    public void onItemDropped(Items item) {

    }

    @Override
    public void draw() {
        ShadowMap.getColorTo(x, y, tmpColor);
        SimpleColor color = tmpColor;
        int upperLimit = 100;
        int lowestLimit = -20;
        int maxColor = 65;
        float temp = TemperatureMap.getTemp(x, y);

        int a;
        if (temp > upperLimit) {
            a = (int) Math.min(maxColor, Math.abs((temp - upperLimit) / 3));
            color.setRGBA(color.getRed(), color.getGreen() - (a / 2), color.getBlue() - a, color.getAlpha());

        } else if (temp < lowestLimit) {
            a = (int) Math.min(maxColor, Math.abs((temp + lowestLimit) / 3));
            color.setRGBA(color.getRed() - a, color.getGreen() - (a / 2), color.getBlue(), color.getAlpha());
        }

        float wx = worldX(), wy = worldY();
        batch.draw(type.texture, color, wx, wy);

        float maxHp = type.maxHp;
        if (hp > maxHp / 1.5f) {
            // ???
        } else if (hp < maxHp / 3) {
            batch.draw(atlas.byPath("World/Blocks/damaged1.png"), wx, wy);
        } else {
            batch.draw(atlas.byPath("World/Blocks/damaged0.png"), wx, wy);
        }
    }
}
