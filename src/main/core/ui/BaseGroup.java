package core.ui;

import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class BaseGroup<G extends BaseElement<G> & Group> extends BaseElement<G> implements Group {
    // TODO специальный лист с защитой от ConcurrentModificationException
    protected CopyOnWriteArrayList<Element> children;

    protected BaseGroup(Group parent) {
        super(parent);
    }

    private CopyOnWriteArrayList<Element> initChildren() {
        if (children == null) {
            children = new CopyOnWriteArrayList<>();
        }
        return children;
    }

    @Override
    public List<Element> children() {
        return children != null ? children : List.of();
    }

    @Override
    public <E extends Element> E add(E element) {
        initChildren().add(element);
        return element;
    }

    @Override
    public void remove(Element element) {
        if (children != null) {
            children.remove(element);
        }
    }

    @Override
    public void removeAll() {
        if (children != null) {
            children.clear();
        }
    }

    protected void drawThis() {}

    @Override
    public void draw() {
        drawThis();
        if (children != null) {
            for (Element child : children) {
                if (child.visible()) {
                    child.draw();
                }
            }
        }
    }

    @Override
    public void preUpdate() {
        if (children != null) {
            for (Element child : children) {
                child.preUpdate();
            }
        }
    }

    @Override
    public void update() {
        preUpdate();
        super.update();
        if (children != null) {
            for (Element child : children) {
                child.update();
            }
        }
    }

    @Override
    public Element hit(float hx, float hy) {
        if (children != null) {
            for (Element child : children) {
                var hit = child.hit(hx, hy);
                if (hit != null) {
                    return hit;
                }
            }
        }
        return super.hit(hx, hy);
    }

    @Override
    public void onResize(int width, int height) {
        super.onResize(width, height);
        if (children != null) {
            for (Element child : children) {
                child.onResize(width, height);
            }
        }
    }

    @Override
    protected String toStringImpl(int indent) {
        StringJoiner ch = new StringJoiner("\n", "\n", "")
                .setEmptyValue("");
        var children = children();
        for (int i = 0; i < children.size(); i++) {
            Element el = children.get(i);
            String tab = " ".repeat(indent);
            String str = el instanceof BaseElement<?> d ? d.toStringImpl(indent + 1) : el.toString();
            ch.add(tab + "[" + i + "] " + str);
        }
        return super.toStringImpl(indent) + ch;
    }
}
