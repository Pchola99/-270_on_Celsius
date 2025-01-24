package core.ui;

import core.Utils.SimpleColor;
import core.g2d.Font;

import java.util.Arrays;

public final class GlyphCache {
    private static final GlyphData[] EMPTY = new GlyphData[0];

    private GlyphData[] glyphs = EMPTY;
    private float width, height;

    public void setText(Font font,
                        CharSequence text, int begin, int length,
                        SimpleColor color,
                        float x, float y) {
        resize(length);
        var cache = glyphs;
        float gx = x;
        float gheight = 0;
        for (int i = 0; i < length; i++) {
            char c = text.charAt(begin + i);
            var gl = font.getGlyph(c);
            var data = cache[i];

            data.x = gx;
            data.y = y;
            data.colorRgba = color.rgba;
            data.glyph = gl;

            gx += gl.width();
            gheight = Math.max(gheight, gl.height());
        }
        width  = gx - x;
        height = gheight;
    }

    public void recomputePosition(float x, float y) {
        float gx = x;
        float gheight = 0;
        for (GlyphData data : glyphs) {
            data.x = gx;
            data.y = y;
            gx += data.glyph.width();
            gheight = Math.max(gheight, data.glyph.height());
        }
        width  = gx - x;
        height = gheight;
    }

    public GlyphData[] getGlyphs() {
        return glyphs;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    private void resize(int len) {
        GlyphData[] gl = glyphs;
        if (gl == null) {
            gl = len == 0 ? EMPTY : new GlyphData[len];
        } else {
            gl = Arrays.copyOf(gl, len);
        }
        glyphs = gl;
        for (int i = 0; i < gl.length; i++) {
            var data = gl[i];
            if (data == null) {
                gl[i] = new GlyphData();
            } else {
                data.reset();
            }
        }
    }

    public static final class GlyphData {
        public float x, y;
        public int colorRgba;
        public Font.Glyph glyph;

        void reset() {
            x = y = 0;
            colorRgba = 0;
            glyph = null;
        }
    }
}
