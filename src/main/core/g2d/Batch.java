package core.g2d;

import core.Global;
import core.Utils.Disposable;
import core.Utils.SimpleColor;
import core.math.Mat3;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static core.g2d.VertexAttribute.*;
import static org.lwjgl.opengl.GL46.*;

public class Batch implements Disposable {
    private static final int VERTEX_PER_SPRITE   = 4;
    private static final int VERTEX_PER_TRIANGLE = 6;

    private static final VertexFormat VERTEX_FORMAT = VertexFormat.of(List.of(
            create(2, Type.FLOAT, Interp.NORMAL),
            create(4, Type.UNSIGNED_BYTE, Interp.COLOR),
            create(2, Type.FLOAT, Interp.NORMAL)
    ));

    protected Blending blending = Blending.NORMAL;
    protected SimpleColor color = SimpleColor.WHITE;
    protected float colorBits = toBits(SimpleColor.WHITE);

    private static float toBits(SimpleColor color) {
        return Float.intBitsToFloat(color.getValueABGR() & 0xfeffffff);
    }

    protected final Mat3 matrix = new Mat3();

    protected float xScale = 1f, yScale = 1f;
    protected Texture currentTexture;

    protected FloatBuffer vertices;
    protected Mesh mesh;
    protected Shader shader;
    protected int vertexCount;

    private boolean disposed;

    // Состояние по умолчанию / кеш
    private SimpleColor prevColor;
    private float prevColorBits;
    private Blending prevBlending;
    private float prevXScale = 1f, prevYScale = 1f;

    // region Изменение параметров

    public final void blending(Blending blending) {
        flush(); // Иначе будут артефакты и неконсистентность
        this.prevBlending = this.blending;
        this.blending = blending;
    }

    public final void scale(float scale) {
        scale(scale, scale);
    }

    public final void scale(float xScale, float yScale) {
        this.prevXScale = this.xScale;
        this.prevYScale = this.yScale;

        this.xScale = xScale;
        this.yScale = yScale;
    }

    public final void color(SimpleColor color) {
        this.prevColorBits = this.colorBits;
        this.prevColor = this.color;
        this.colorBits = toBits(color);
        this.color = color;
    }

    // endregion
    // region Восстановление состояния/параметров

    public final void resetScale() {
        xScale = prevXScale;
        yScale = prevYScale;
    }

    public final void resetBlending() {
        blending = prevBlending;
    }

    public final void resetColor() {
        color = prevColor;
        colorBits = prevColorBits;
    }

    // endregion

    public Batch(int bufferSize) {
        try {
            shader = Shader.load(Global.assets.assetsDir("Shaders/default"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int vertexCount = bufferSize * VERTEX_PER_SPRITE;
        vertices = BufferUtils.createFloatBuffer(vertexCount);
        mesh = new Mesh(GL_STATIC_DRAW);

        mesh.bindVao();
        mesh.bindVbo();

        VERTEX_FORMAT.enableAttributes();

        int indexesCount = bufferSize * VERTEX_PER_TRIANGLE;
        IntBuffer indexes = BufferUtils.createIntBuffer(indexesCount);

        int j = 0;
        for (int i = 0; i < bufferSize; i++) {
            indexes.put(j);
            indexes.put(j + 1);
            indexes.put(j + 2);

            indexes.put(j + 2);
            indexes.put(j + 3);
            indexes.put(j);

            j += 4;
        }

        indexes.flip();
        mesh.updateIndexes(indexes);
        mesh.useIndexes(true);
    }

    public final void matrix(Mat3 matrix) {
        flush();

        this.matrix.set(matrix);
    }

    protected final void prepareTexture(Texture tex) {
        if (currentTexture != tex || vertices.remaining() < VERTEX_PER_SPRITE) {
            flush();
            currentTexture = tex;
        }
    }

    public void flush() {
        if (vertexCount == 0) {
            return;
        }

        blending.apply();

        shader.use();
        shader.setUniform("u_texture", currentTexture);
        shader.setUniformTransforming("u_proj", matrix);

        vertices.flip();
        mesh.draw(GL_TRIANGLES, vertices, vertexCount * VERTEX_PER_TRIANGLE);

        vertices.clear();
        vertexCount = 0;
    }

    // Отрисовка

    public final void draw(Drawable drawable) {
        draw(drawable, 0, 0);
    }

    public final void draw(Drawable drawable, float x, float y) {
        draw(drawable, x, y, drawable.width() * xScale, drawable.height() * yScale);
    }

    public final void draw(Drawable drawable, float x, float y, float width, float height) {
        drawTexture(drawable, x, y, width, height);
    }

    protected void drawTexture(Drawable drawable, float x, float y, float w, float h) {
        prepareTexture(textureOf(drawable));

        rect(x, y,
                x + w, y + h,
                drawable.u(), drawable.v(),
                drawable.u2(), drawable.v2(),
                colorBits);
    }

    protected final Texture textureOf(Drawable drawable) {
        return switch (drawable) {
            case Font.Glyph gl -> gl.font().getTexture();
            case Atlas.Region region -> region.atlas().getTexture();
            case Texture tex -> tex;
        };
    }

    protected final void vertex(float x, float y,
                                float u, float v,
                                float color) {
        vertices.put(x);
        vertices.put(y);
        vertices.put(color);
        vertices.put(u);
        vertices.put(v);

        vertexCount++;
    }

    protected final void rect(float x1, float y1,
                              float x2, float y2,
                              float u1, float v1,
                              float u2, float v2,
                              float c) {

        vertex(x1, y1, u1, v2, c);
        vertex(x1, y2, u1, v1, c);
        vertex(x2, y2, u2, v1, c);
        vertex(x2, y1, u2, v2, c);
    }

    @Override
    public final boolean isDisposed() {
        return disposed;
    }

    @Override
    public final void close() {
        if (disposed) return;
        mesh.close();
        shader.close();
        MemoryUtil.memFree(vertices);
        disposed = true;
    }
}
