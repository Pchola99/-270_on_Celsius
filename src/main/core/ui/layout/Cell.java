package core.ui.layout;

import core.ui.Element;

import java.awt.*;
import java.util.function.Consumer;

public final class Cell<E extends Element> {
    static final float unset = Float.NEGATIVE_INFINITY;

    static final int EXPAND_X  = 1 << 5;
    static final int EXPAND_Y  = 1 << 6;
    static final int FILL_X    = 1 << 7;
    static final int FILL_Y    = 1 << 8;
    static final int UNIFORM_X = 1 << 9;
    static final int UNIFORM_Y = 1 << 10;
    static final int END_ROW   = 1 << 11;

    static int flag(int val, int flag, boolean st) {
        if (st) {
            return val | flag;
        } else {
            return val & ~flag;
        }
    }

    Table table;

    int columnSize = 1;
    int column, row;
    int cellAboveIndex;

    int align;
    int flags;
    float fillX, fillY;
    int expandX, expandY;

    float minWidth = unset, maxWidth = unset;
    float minHeight = unset, maxHeight = unset;

    float padTop, padLeft, padBottom, padRight;

    float elementX, elementY;
    float elementWidth, elementHeight;

    E element;

    void setEndRow(boolean st) {
        flags = flag(flags, END_ROW, st);
    }

    public Cell<E> with(Consumer<E> c) {
        c.accept(element);
        return this;
    }

    public Cell<E> self(Consumer<Cell<E>> c) {
        c.accept(this);
        return this;
    }

    public float prefWidth() {
        return element.getPrefWidth();
    }

    public float prefHeight() {
        return element.getPrefHeight();
    }

    public float maxWidth() {
        return maxWidth == unset ? element.getMaxWidth() : maxWidth;
    }

    public float maxHeight() {
        return maxHeight == unset ? element.getMaxHeight() : maxHeight;
    }

    public float minWidth() {
        return minWidth == unset ? element.getMinWidth() : minWidth;
    }

    public float minHeight() {
        return minHeight == unset ? element.getMinHeight() : minHeight;
    }

    public Cell<E> size(float size) {
        minWidth = minHeight = maxWidth = maxHeight = scl(size);
        return this;
    }

    public Cell<E> size(float width, float height) {
        minWidth = maxWidth = scl(width);
        minHeight = maxHeight = scl(height);
        return this;
    }

    public Cell<E> width(float width) {
        minWidth = maxWidth = scl(width);
        return this;
    }

    public Cell<E> height(float height) {
        minHeight = maxHeight = scl(height);
        return this;
    }

    public Cell<E> minSize(float size) {
        minWidth = minHeight = scl(size);
        return this;
    }

    public Cell<E> minSize(float width, float height) {
        minWidth = scl(width);
        minHeight = scl(height);
        return this;
    }

    public Cell<E> minWidth(float minWidth) {
        this.minWidth = scl(minWidth);
        return this;
    }

    public Cell<E> minHeight(float minHeight) {
        this.minHeight = scl(minHeight);
        return this;
    }

    public Cell<E> maxSize(float size) {
        maxWidth = maxHeight = scl(size);
        return this;
    }

    public Cell<E> maxSize(float width, float height) {
        maxWidth = scl(width);
        maxHeight = scl(height);
        return this;
    }

    public Cell<E> maxWidth(float maxWidth) {
        this.maxWidth = scl(maxWidth);
        return this;
    }

    public Cell<E> maxHeight(float maxHeight) {
        this.maxHeight = scl(maxHeight);
        return this;
    }

    public Cell<E> pad(float pad) {
        padTop = padLeft = padBottom = padRight = scl(pad);
        return this;
    }

    public Cell<E> pad(float top, float left, float bottom, float right) {
        padTop = scl(top);
        padLeft = scl(left);
        padBottom = scl(bottom);
        padRight = scl(right);
        return this;
    }

    public Cell<E> padTop(float padTop) {
        this.padTop = scl(padTop);
        return this;
    }

    public Cell<E> padLeft(float padLeft) {
        this.padLeft = scl(padLeft);
        return this;
    }

    public Cell<E> padBottom(float padBottom) {
        this.padBottom = scl(padBottom);
        return this;
    }

    public Cell<E> padRight(float padRight) {
        this.padRight = scl(padRight);
        return this;
    }

    public Cell<E> fill() {
        fillX = 1;
        fillY = 1;
        return this;
    }

    public Cell<E> fillX() {
        fillX = 1;
        return this;
    }

    public Cell<E> fillY() {
        fillY = 1;
        return this;
    }

    public Cell<E> fill(float x, float y) {
        fillX = x;
        fillY = y;
        return this;
    }

    public Cell<E> fill(boolean x, boolean y) {
        fillX = x ? 1 : 0;
        fillY = y ? 1 : 0;
        return this;
    }

    public Cell<E> fill(boolean fill) {
        fillX = fill ? 1 : 0;
        fillY = fill ? 1 : 0;
        return this;
    }

    public Cell<E> align(int align) {
        this.align = align;
        return this;
    }

    public Cell<E> center() {
        align = Align.center;
        return this;
    }

    public Cell<E> top() {
        align = (align | Align.top) & ~Align.bottom;
        return this;
    }

    public Cell<E> left() {
        align = (align | Align.left) & ~Align.right;
        return this;
    }

    public Cell<E> bottom() {
        align = (align | Align.bottom) & ~Align.top;
        return this;
    }

    public Cell<E> right() {
        align = (align | Align.right) & ~Align.left;
        return this;
    }

    public Cell<E> grow() {
        expandX = 1;
        expandY = 1;
        fillX = 1;
        fillY = 1;
        return this;
    }

    public Cell<E> growX() {
        expandX = 1;
        fillX = 1;
        return this;
    }

    public Cell<E> growY() {
        expandY = 1;
        fillY = 1;
        return this;
    }

    public Cell<E> expand() {
        expandX = 1;
        expandY = 1;
        return this;
    }

    public Cell<E> expandX() {
        expandX = 1;
        return this;
    }

    public Cell<E> expandY() {
        expandY = 1;
        return this;
    }

    public Cell<E> expand(int x, int y) {
        expandX = x;
        expandY = y;
        return this;
    }

    public Cell<E> expand(boolean x, boolean y) {
        expandX = x ? 1 : 0;
        expandY = y ? 1 : 0;
        return this;
    }

    public Cell<E> colspan(int colspan) {
        this.columnSize = colspan;
        return this;
    }

    public Cell<E> uniform() {
        flags |= UNIFORM_X | UNIFORM_Y;
        return this;
    }

    public Cell<E> uniformX() {
        flags &= ~UNIFORM_X;
        return this;
    }

    public Cell<E> uniformY() {
        flags &= ~UNIFORM_Y;
        return this;
    }

    public Cell<E> uniform(boolean x, boolean y) {
        flags = flag(flags, UNIFORM_X, x);
        flags = flag(flags, UNIFORM_Y, y);
        return this;
    }

    public E as() {
        return element;
    }

    public void row() {
        table.row();
    }

    float scl(float value){
        return value;
    }
}
