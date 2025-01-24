package core.ui;

import core.Utils.SimpleColor;
import core.g2d.Drawable;
import core.g2d.Font;

public abstract class Style {

    public abstract void load();

    public static abstract class Panel extends Style {
        public float borderWidth;
        public SimpleColor backgroundColor;
    }

    public static abstract class Button extends Style {
        public SimpleColor backgroundColor;
        public float textOffsetX;
        public float textOffsetY; // TODO замастерить типа ResizableValue height / 2.8f
        public Text textStyle;
    }

    public static abstract class TextButton extends Button {
        public float borderWidth;
        public SimpleColor disabledColor;
        public float prefWidth, prefHeight;
    }

    public static abstract class ToggleButton extends Button {
        public float width, height;
        public Drawable checkUp, checkDown;
        public float borderOffset;

        public float maxCheckmarkWidth() {
            return Math.max(checkUp.width(), checkDown.width());
        }

        public float maxCheckmarkHeight() {
            return Math.max(checkUp.height(), checkDown.height());
        }
    }

    public static abstract class Text extends Style {
        public SimpleColor color;
        public Font font;
    }
}
