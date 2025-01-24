package core.ui;

import core.EventHandling.EventHandler;
import core.Global;
import core.g2d.Drawable;

public class ImageButton extends ImageElement {
    public boolean isClickable = true, isClicked;
    public Runnable clickAction;

    public ImageButton(Group parent, Drawable image) {
        super(parent, image);
    }

    public ImageButton onClick(Runnable clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    public ImageButton setClickable(boolean isClickable) {
        this.isClickable = isClickable;
        return this;
    }

    @Override
    public void update() {
        if (!isClickable) {
            return;
        }
        boolean press = EventHandler.isMousePressed(this);
        isClicked = press;
        if (press && clickAction != null) {
            clickAction.run();
        }
    }
}
