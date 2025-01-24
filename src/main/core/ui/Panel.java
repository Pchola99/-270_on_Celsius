package core.ui;

import core.Global;
import core.Utils.SimpleColor;
import core.g2d.Fill;
import core.ui.layout.Table;

public class Panel extends Table {
    public final Style.Panel style;

    public SimpleColor color = Styles.DEFAULT_PANEL_COLOR;

    public Panel(Group parent, Style.Panel style) {
        super(parent);
        this.style = style;
        margin(style.borderWidth);
    }

    public Panel setColor(SimpleColor color) {
        this.color = color;
        return this;
    }

    @Override
    protected void drawThis() {

        var b = background;
        if (b != null) {
            Global.batch.draw(b, x, y);
        } else {
            var backgroundColor = color;
            if (backgroundColor == null) backgroundColor = style.backgroundColor;

            Fill.rect(x, y, width, height, backgroundColor);

            float borderWidth = style.borderWidth;
            if (borderWidth != 0) {
                Fill.rectangleBorder(x, y, width, height, borderWidth, backgroundColor);
            }
        }
    }

    // Метод для настройки вкладок. То есть это некоторый список действий (у нас за это кнопки отвечают),
    // в котором при на выполнении одного действия, нужно потом выполнить другое, чтобы разблокировать вновь это действие
    // Мне это нужно для красивого оформления, которое даёт setClickable(false)
    public void oneOf(Button... buttons) {
        for (Button button : buttons) {
            var oldAction = button.clickAction;
            button.onClick(b -> {
                b.setClickable(false);
                for (Button other : buttons) {
                    if (other != b) {
                        other.setClickable(true);
                    }
                }
                oldAction.accept(b);
            });
        }
    }
}
