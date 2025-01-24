package core.g2d;

import core.Utils.Sized;

public sealed interface Drawable
        extends Sized
        permits Atlas.Region, Font.Glyph, Texture {

    int width();
    int height();

    float u();
    float v();
    float u2();
    float v2();

    default float getLeftWidth() { return 0; }

    default float getTopHeight() { return 0; }

    default float getBottomHeight() { return 0; }

    default float getRightWidth() { return 0; }

    default float getMinWidth() { return width(); }

    default float getMinHeight() { return height(); }
}
