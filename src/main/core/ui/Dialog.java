package core.ui;

import core.ui.layout.Table;

import static core.Global.*;

// Никакой это не диалог. Просто базовая реализация BaseGroup.
// Просто список для дочерних элементов без какой-то особой отрисовки по умолчанию
public class Dialog extends Table {
    private boolean maximized;

    public Dialog() {
        this(null);
    }

    protected Dialog(Group parent) {
        super(parent);
    }

    public void maximize() {
        setSize(input.getWidth(), input.getHeight());
        maximized = true;
    }

    public void show() {
        scene.add(this);
    }

    @Override
    public void onResize(int width, int height) {
        if (maximized) {
            setSize(width, height);
        }
        super.onResize(width, height);
    }

    public void hide() {
        scene.remove(this);
    }

    public void toggle() {
        if (scene.contains(this)) {
            hide();
        } else {
            show();
        }
    }

    public boolean isShown() { // Если элемент показан это ещё не значит, что он отрисуется или будет взаимодействовать
        return scene.contains(this);
    }
}
