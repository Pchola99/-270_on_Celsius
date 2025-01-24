package core.ui;

import core.Utils.Sized;
import core.input.InputListener;
import core.math.Point2i;

public interface Element extends InputListener {
    String id();

    // null если это корневой элемент интерфейса, т.е. специальная затычка
    // @Nullable
    Group parent();

    float x();

    float y();

    float width();

    float height();

    // Если выставлен в true, то draw() метод не будет вызван
    boolean visible();

    // Выставляет режим элементу, при котором его (width, height) копируется с родительского элемента.
    // Причём setSize() и другие методы перестанут реагировать на смену размера
    // В реализации FLAG_W_CHANGED, FLAG_H_CHANGED будет проверять изменения в соответствии с родительским элементом
    void fillParent();
    void minimize(boolean x, boolean y);

    void draw();

    default void preUpdate() {

    }
    void update();
    Element setId(String id);

    Element setX(float x);
    Element setY(float y);
    Element setWidth(float width);
    Element setHeight(float height);

    Element setPosition(float x, float y);
    Element setSize(Sized sized);
    Element setSize(float width, float height);
    Element set(float x, float y, float width, float height);

    Element setVisible(boolean visible);
    Element toggleVisibility();

    Element hit(float x, float y);

    default Element hit(Point2i point) {
        return hit(point.x, point.y);
    }

    default float getPrefWidth()  { return 0; }
    default float getPrefHeight() { return 0; }

    default float getMinWidth() { return 0; }
    default float getMaxWidth() { return 0; }

    default float getMinHeight() { return 0; }
    default float getMaxHeight() { return 0; }
}
