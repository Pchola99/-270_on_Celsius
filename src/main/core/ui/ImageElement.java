package core.ui;

import core.Global;
import core.g2d.Drawable;

public class ImageElement extends BaseElement<ImageElement> {
    public Drawable image;

    public ImageElement(Group parent, Drawable image) {
        super(parent);
        this.image = image;
    }

    public ImageElement setImage(Drawable image) {
        this.image = image;
        return this;
    }

    @Override
    public float getMinWidth() {
        return image.width();
    }

    @Override
    public float getMinHeight() {
        return image.height();
    }

    @Override
    public void draw() {
        Global.batch.draw(image, x, y);
    }
}
