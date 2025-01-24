package core.ui;

import core.g2d.Fill;

import static core.Global.input;
import static core.World.Textures.TextureDrawing.drawPrompt;
import static core.World.Textures.TextureDrawing.drawText;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;

public class Button extends BaseButton<Button> {
    protected final Style.TextButton style;

    public Button(Group panel, Style.TextButton style) {
        super(panel, style.textStyle);
        this.style = style;
    }

    @Override
    protected void resize() {
        if ((flags & (FLAG_X_CHANGED | FLAG_Y_CHANGED)) != 0) {
            name.setPosition(x + style.textOffsetX, y + style.textOffsetY);
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
        boolean press = hit(input.mousePos()) == this && input.justClicked(GLFW_MOUSE_BUTTON_1);
        isClicked = press;
        if (press && clickAction != null) {
            clickAction.accept(this);
        }

        if (isClicked && oneShot) {
            isClickable = false;
        }
    }

    @Override
    public void draw() {
        var backgroundColor = color;
        if (backgroundColor == null) backgroundColor = style.backgroundColor;

        float borderWidth = style.borderWidth;
        if (borderWidth == 0) {
            Fill.rect(x, y, width, height, backgroundColor);
        } else {
            Fill.rectangleBorder(x, y, width, height, borderWidth, backgroundColor);
        }

        var disabledColor = style.disabledColor;
        if (disabledColor != null) {
            if (isClicked) {
                Fill.rect(x, y, width, height, disabledColor);
            }
        }
        if (name.visible()) {
            name.draw();
        }
        drawPrompt(this);
    }

    @Override
    public float getPrefWidth() {
        return Math.max(style.prefWidth, name.width);
    }

    @Override
    public float getPrefHeight() {
        return Math.max(style.prefHeight, name.height);
    }
}
