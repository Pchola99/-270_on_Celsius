package core.ui;

import core.Utils.SimpleColor;
import core.g2d.Drawable;
import core.g2d.Fill;

import static core.Global.*;
import static core.World.Textures.TextureDrawing.drawPrompt;
import static core.World.Textures.TextureDrawing.drawText;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;

public class ToggleButton extends BaseButton<ToggleButton> {
    protected final Style.ToggleButton style;

    public ToggleButton(Group panel, Style.ToggleButton style) {
        super(panel, style.textStyle);
        this.style = style;
        setSize(style.width, style.height);
    }

    @Override
    protected void resize() {
        if ((flags & (FLAG_X_CHANGED | FLAG_Y_CHANGED)) != 0) {
            float checkmarkOffsetX = style.borderOffset*2 + style.maxCheckmarkWidth();
            name.setPosition(x + checkmarkOffsetX + style.textOffsetX, y + style.textOffsetY);
        }
    }

    @Override
    public void updateThis() {
        name.update();
        if (!visible()) {
            return;
        }
        if (!isClickable) {
            return;
        }
        if (hit(input.mousePos()) == this && input.justClicked(GLFW_MOUSE_BUTTON_1)) {
            isClicked = !isClicked;
            if (clickAction != null) {
                clickAction.accept(this);
            }
        }
        if (isClicked && oneShot) {
            isClickable = false;
        }
    }

    @Override
    public float getPrefWidth() {
        return style.borderOffset*2 + style.maxCheckmarkWidth() + name.width;
    }

    @Override
    public float getPrefHeight() {
        return Math.max(style.borderOffset*2 + style.maxCheckmarkHeight(), name.height);
    }

    @Override
    public void draw() {
        float offset = style.borderOffset;
        SimpleColor c = color;
        if (c == null) c = style.backgroundColor;

        Fill.rectangleBorder(x, y, width, height, offset, c);

        Drawable tex = isClicked ? style.checkUp : style.checkDown;
        batch.draw(tex, x + offset, y + offset);
        if (name.visible()) {
            name.draw();
        }

        drawPrompt(this);
    }
}
