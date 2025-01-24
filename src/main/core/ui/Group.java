package core.ui;

import core.g2d.Drawable;

import java.util.List;
import java.util.function.Consumer;

public interface Group extends Element {
    List<Element> children();

    <E extends Element> E add(E element);

    void remove(Element element);

    void removeAll();

    // region ковариантное переопределение
    @Override
    Group set(float x, float y, float width, float height);

    @Override
    Group setSize(float width, float height);

    @Override
    Group setX(float x);

    @Override
    Group setY(float y);

    @Override
    Group setPosition(float x, float y);

    @Override
    Group setVisible(boolean state);

    // endregion
    // region Дополнительные методы
    default Panel addPanel(Style.Panel style, float x, float y, float width, float height) {
        Panel element = new Panel(this, style);
        element.set(x, y, width, height);
        return add(element);
    }

    default Button addButton(Style.TextButton style, Consumer<Button> onClick) {
        return add(new Button(this, style))
                .onClick(onClick);
    }

    default Button addButton(Style.TextButton style, Runnable onClick) {
        return add(new Button(this, style))
                .onClick(onClick);
    }

    default ToggleButton addToggleButton(Style.ToggleButton style, Runnable onClick) {
        return add(new ToggleButton(this, style))
                .onClick(onClick);
    }

    default Slider addSlider(int min, int max, Slider.MoveListener onMove) {
        return add(new Slider(this))
                .setBounds(min, max)
                .onMove(onMove);
    }

    default ImageElement addImage(float x, float y, Drawable image) {
        return add(new ImageElement(this, image))
                .setPosition(x, y);
    }
    // endregion
}
