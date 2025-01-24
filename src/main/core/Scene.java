package core;

import core.EventHandling.Logging.Logger;
import core.g2d.Camera2;
import core.graphic.Layer;
import core.input.InputListener;
import core.math.Vector2f;
import core.ui.Element;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import static core.Global.*;

public class Scene implements InputListener {
    private final Camera2 orthoView = new Camera2();

    public Scene(int width, int height) {
        orthoView.setToOrthographic(width, height);
    }

    private final CopyOnWriteArrayList<Element> elements = new CopyOnWriteArrayList<>();

    public void add(Element element) {
        if (contains(element)) {
            return;
        }
        elements.add(element);
    }

    public void remove(Element element) {
        elements.remove(element);
    }

    public void clear() {
        elements.clear();
    }

    // Не вызывать ниоткуда!
    public void update() {
        // TODO что-то придумать с ConcurrentModificationException
        for (Element element : elements) {
            try {
                element.update();
            } catch (Exception e) {
                Logger.printException("Failed to update '" + element + "'", e);
            }
        }
    }

    public void draw() {
        batch.z(Layer.GUI);
        batch.matrix(orthoView.projection);

        for (Element element : elements) {
            if (!element.visible()) {
                continue;
            }
            try {
                element.draw();
            } catch (Exception e) {
                Logger.printException("Failed to draw '" + element + "'", e);
            }
        }
    }

    public boolean contains(Element element) {
        return elements.contains(element);
    }

    @Override
    public void onResize(int width, int height) {
        orthoView.setToOrthographic(width, height);

        for (Element element : elements) {
            element.onResize(width, height);
        }
    }

    public void debug() {
        for (Element element : elements) {
            System.out.println(element);
        }
    }

    public Vector2f toScreenCoordinates(Vector2f vec) {
        return orthoView.project(vec);
    }

    public Vector2f toSceneCoordinates(Vector2f vec) {
        return orthoView.unproject(vec);
    }
}
