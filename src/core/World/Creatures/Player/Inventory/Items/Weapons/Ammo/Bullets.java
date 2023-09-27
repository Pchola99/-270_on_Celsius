package core.World.Creatures.Player.Inventory.Items.Weapons.Ammo;

import core.EventHandling.EventHandler;
import core.UI.Sounds.Sound;
import core.World.Creatures.Player.Inventory.Inventory;
import core.World.Creatures.Player.Inventory.Items.Items;
import core.World.Creatures.Player.Inventory.Items.Weapons.Weapons;
import core.World.HitboxMap;
import core.World.Textures.DynamicWorldObjects;
import core.World.Textures.StaticWorldObjects.StaticWorldObjects;
import java.awt.*;
import static core.EventHandling.EventHandler.getMousePos;
import static core.Window.defPath;
import static core.World.Textures.StaticWorldObjects.StaticWorldObjects.destroyObject;
import static core.World.Textures.TextureDrawing.drawTexture;
import static core.World.WorldGenerator.*;

public class Bullets {
    public float bulletSpeed, damage, angle, x, y;
    public static Bullets[] bullets = new Bullets[2000];
    private static int lastCreatedCell = 0, bulletCount = 0;

    private Bullets(float x, float y, float bulletSpeed, float damage, float angle) {
        this.x = x;
        this.y = y;
        this.bulletSpeed = bulletSpeed;
        this.damage = damage;
        this.angle = angle;
    }

    public static void createBullet(float x, float y, float bulletSpeed, float damage, float angle) {
        if (lastCreatedCell + 1 < bullets.length && bullets[lastCreatedCell + 1] == null) {
            bullets[lastCreatedCell] = new Bullets(x, y, bulletSpeed, damage, angle);
            lastCreatedCell++;
            bulletCount++;
            return;
        }
        for (int i = 0; i < bullets.length; i++) {
            if (bullets[i] == null) {
                bullets[i] = new Bullets(x, y, bulletSpeed, damage, angle);
                lastCreatedCell = i;
                bulletCount++;
                return;
            }
        }
    }

    public static void updateBullets() {
        if (Inventory.currentObjectType == Items.Types.WEAPON) {
            Weapons weapon = Inventory.inventoryObjects[Inventory.currentObject.x][Inventory.currentObject.y].weapon;

            if (EventHandler.getMousePress() && System.currentTimeMillis() - weapon.lastShootTime >= weapon.fireRate) {
                weapon.lastShootTime = System.currentTimeMillis();
                Bullets.createBullet(DynamicObjects.get(0).x, DynamicObjects.get(0).y, weapon.ammoSpeed, weapon.damage, Math.abs((float) Math.toDegrees(Math.atan2(getMousePos().y - 540, getMousePos().x - 960)) - 180));
                Sound.SoundPlay(weapon.sound, Sound.types.EFFECT, false);
            }
        }

        if (bulletCount > 0) {
            for (int i = 0; i < bullets.length; i++) {
                Bullets bullet = bullets[i];

                if (bullet != null) {
                    float deltaX = (float) (bullet.bulletSpeed * Math.cos(Math.toRadians(bullet.angle)));
                    float deltaY = (float) (bullet.bulletSpeed * Math.sin(Math.toRadians(bullet.angle)));
                    float x = bullet.x - deltaX;
                    float y = bullet.y + deltaY;

                    bullet.x -= deltaX;
                    bullet.y += deltaY;
                    bullet.damage -= 0.01f;

                    Point staticObjectPoint = HitboxMap.checkIntersInside(x, y, 8, 8);
                    short staticObject = getObject(staticObjectPoint.x, staticObjectPoint.y);
                    DynamicWorldObjects dynamicObject = HitboxMap.checkIntersectionsDynamic(x, y, 8, 8);

                    if (staticObject > 0) {
                        float hp = StaticWorldObjects.getHp(staticObject);
                        setObject(staticObjectPoint.x, staticObjectPoint.y, StaticWorldObjects.decrementHp(staticObject, (int) bullet.damage));
                        bullets[i].damage -= hp;

                        if (getObject(staticObjectPoint.x, staticObjectPoint.y) <= 0) {
                            destroyObject(staticObjectPoint.x, staticObjectPoint.y);
                        }
                    } else if (dynamicObject != null) {
                        float hp = dynamicObject.currentHp;
                        dynamicObject.currentHp -= bullet.damage;
                        bullet.damage -= hp;

                        if (dynamicObject.currentHp <= 0) {
                            DynamicObjects.remove(dynamicObject);
                        }
                    }
                    if (bullet.damage <= 0 || bullet.x < 0 || bullet.y < 0 || bullet.x / 16 > SizeX || bullet.y / 16 > SizeY) {
                        bullets[i] = null;
                        bulletCount--;
                    }
                }
            }
        }
    }

    public static void drawBullets() {
        for (Bullets bullet : bullets) {
            //TODO: дописать пути нормальные
            if (bullet != null && !(bullet.x > DynamicObjects.get(0).x + 350 || bullet.x < DynamicObjects.get(0).x - 350)) {
                drawTexture(defPath + "\\src\\assets\\World\\Items\\someBullet.png", bullet.x, bullet.y, 3, false);
            }
        }
    }
}
