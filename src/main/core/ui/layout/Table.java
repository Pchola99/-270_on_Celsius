package core.ui.layout;

import core.Global;
import core.g2d.Drawable;
import core.ui.*;

import java.util.ArrayList;
import java.util.function.Consumer;

import static core.ui.layout.Cell.*;

public class Table extends BaseGroup<Table> {
    private final ArrayList<Cell<?>> cells = new ArrayList<>();

    protected Drawable background;

    private int columns, rows;
    private boolean implicitEndRow;

    private float tableMinWidth, tableMinHeight;
    private float tablePrefWidth, tablePrefHeight;

    float marginTop = unset, marginLeft = unset, marginBottom = unset, marginRight = unset;

    int align = Align.center;

    protected Table(Group parent) {
        super(parent);
        invalidateSize(); // Первая постройка таблицы
    }

    public void setBackground(Drawable background) {
        this.background = background;
    }

    public <E extends Element> Cell<E> addCell(E element) {
        var cell = new Cell<E>();
        cell.table = this;
        cell.element = element;

        // The row was ended for layout, not by the user, so revert it.
        if (implicitEndRow) {
            implicitEndRow = false;
            rows--;
            cells.getLast().setEndRow(false);
        }

        var cells = this.cells;
        int cellCount = cells.size();
        if (cellCount > 0) {
            // Set cell column and row.
            var lastCell = cells.getLast();
            if ((lastCell.flags & END_ROW) == 0) {
                cell.column = lastCell.column + lastCell.columnSize;
                cell.row = lastCell.row;
            } else {
                cell.column = 0;
                cell.row = lastCell.row + 1;
            }
            // Set the index of the cell above.
            if (cell.row > 0) {
                outer:
                for (int i = cellCount - 1; i >= 0; i--) {
                    var other = cells.get(i);
                    for (int column = other.column, k = column + other.columnSize; column < k; column++) {
                        if (column == cell.column) {
                            cell.cellAboveIndex = i;
                            break outer;
                        }
                    }
                }
            }
        } else {
            cell.column = 0;
            cell.row = 0;
        }
        cells.add(cell);
        add(element);

        return cell;
    }

    @Override
    public void removeAll() {
        super.removeAll();
        cells.clear();
        rows = columns = 0;
        implicitEndRow = false;
        invalidateSize();
    }

    @Override
    public void preUpdate() {
        boolean any = false;
        if ((flags & FLAG_X_MINIMIZE) != 0) {
            any = true;
            setWidth(getPrefWidth());
        }
        if ((flags & FLAG_Y_MINIMIZE) != 0) {
            any = true;
            setHeight(getPrefHeight());
        }
        if (any) {
            computeSizeIfNeeded();
        }
    }

    @Override
    public void updateThis() {
        float h = height;
        buildLayout(0, 0, width, h);
        for (var c : cells) {
            float actorHeight = c.elementHeight;
            float actorY = h - c.elementY - actorHeight;
            c.elementY = actorY;
            c.element.set(x + c.elementX, y + actorY, c.elementWidth, actorHeight);
        }
    }

    public void buildLayout(float layoutX, float layoutY, float layoutWidth, float layoutHeight) {
        var cells = this.cells;
        var columnData = this.columnData;
        var rowData = this.rowData;

        float padLeft = getMarginLeft();
        float hpadding = padLeft + getMarginRight();
        float padTop = getMarginTop();
        float vpadding = padTop + getMarginBottom();

        float totalExpandWidth = 0, totalExpandHeight = 0;
        for (var ccol : columnData) {
            totalExpandWidth += ccol.expandWidth;
        }
        for (var crow : rowData) {
            totalExpandHeight += crow.expandHeight;
        }

        // Size columns and rows between min and pref size using (preferred - min) size to weight distribution of extra space.
        float totalGrowWidth = tablePrefWidth - tableMinWidth;
        if (totalGrowWidth != 0) {
            float extraWidth = Math.min(totalGrowWidth, Math.max(0, layoutWidth - tableMinWidth));
            for (var ccol : columnData) {
                float growWidth = ccol.columnPrefWidth - ccol.columnMinWidth;
                float growRatio = growWidth / totalGrowWidth;
                ccol.columnWeightedWidth = ccol.columnMinWidth + extraWidth * growRatio;
            }
        } else {
            for (var ccol : columnData) {
                ccol.columnWeightedWidth = ccol.columnMinWidth;
            }
        }

        float totalGrowHeight = tablePrefHeight - tableMinHeight;
        if (totalGrowHeight != 0) {
            float extraHeight = Math.min(totalGrowHeight, Math.max(0, layoutHeight - tableMinHeight));
            for (var crow : rowData) {
                float growHeight = crow.rowPrefHeight - crow.rowMinHeight;
                float growRatio = growHeight / totalGrowHeight;
                crow.rowWeightedHeight = crow.rowMinHeight + extraHeight * growRatio;
            }
        } else {
            for (var crow : rowData) {
                crow.rowWeightedHeight = crow.rowMinHeight;
            }
        }

        // Determine actor and cell sizes (before expand or fill).
        for (var c : cells) {
            int column = c.column, row = c.row;

            float spannedWeightedWidth = 0;
            int colspan = c.columnSize;
            for (int j = column, k = j + colspan; j < k; j++) {
                spannedWeightedWidth += columnData[j].columnWeightedWidth;
            }
            var crow = rowData[row];
            float weightedHeight = crow.rowWeightedHeight;

            float prefWidth = c.prefWidth();
            float prefHeight = c.prefHeight();
            float minWidth = c.minWidth();
            float minHeight = c.minHeight();
            float maxWidth = c.maxWidth();
            float maxHeight = c.maxHeight();
            if (prefWidth < minWidth) {
                prefWidth = minWidth;
            }
            if (prefHeight < minHeight) {
                prefHeight = minHeight;
            }
            if (maxWidth > 0 && prefWidth > maxWidth) {
                prefWidth = maxWidth;
            }
            if (maxHeight > 0 && prefHeight > maxHeight) {
                prefHeight = maxHeight;
            }

            c.elementWidth = Math.min(spannedWeightedWidth - c.padLeft - c.padRight, prefWidth);
            c.elementHeight = Math.min(weightedHeight - c.padTop - c.padBottom, prefHeight);

            if (colspan == 1) {
                var ccol = columnData[column];
                ccol.columnWidth = Math.max(ccol.columnWidth, spannedWeightedWidth);
            }
            crow.rowHeight = Math.max(crow.rowHeight, weightedHeight);
        }

        // Distribute remaining space to any expanding columns/rows.
        if (totalExpandWidth > 0) {
            float extra = layoutWidth - hpadding;
            for (ColumnData columnDatum : columnData) {
                extra -= columnDatum.columnWidth;
            }
            float used = 0;
            int lastIndex = 0;
            for (int i = 0; i < columnData.length; i++) {
                var ccol = columnData[i];
                if (ccol.expandWidth == 0) {
                    continue;
                }
                float amount = extra * ccol.expandWidth / totalExpandWidth;
                ccol.columnWidth += amount;
                used += amount;
                lastIndex = i;
            }
            columnData[lastIndex].columnWidth += extra - used;
        }
        if (totalExpandHeight > 0) {
            float extra = layoutHeight - vpadding;
            for (RowData rowDatum : rowData) {
                extra -= rowDatum.rowHeight;
            }
            float used = 0;
            int lastIndex = 0;
            for (int i = 0; i < rowData.length; i++) {
                var crow = rowData[i];
                if (crow.expandHeight == 0) {
                    continue;
                }
                float amount = extra * crow.expandHeight / totalExpandHeight;
                crow.rowHeight += amount;
                used += amount;
                lastIndex = i;
            }
            rowData[lastIndex].rowHeight += extra - used;
        }

        // Distribute any additional width added by colspanned cells to the columns spanned.
        for (var c : cells) {
            int colspan = c.columnSize;
            if (colspan == 1) {
                continue;
            }

            float extraWidth = 0;
            for (int column = c.column, k = column + colspan; column < k; column++) {
                var ccol = columnData[column];
                extraWidth += ccol.columnWeightedWidth - ccol.columnWidth;
            }
            extraWidth -= Math.max(0, c.padLeft + c.padRight);

            extraWidth /= colspan;
            if (extraWidth > 0) {
                for (int column = c.column, k = column + colspan; column < k; column++) {
                    columnData[column].columnWidth += extraWidth;
                }
            }
        }

        // Determine table size.
        float tableWidth = hpadding, tableHeight = vpadding;
        for (ColumnData columnDatum : columnData) {
            tableWidth += columnDatum.columnWidth;
        }
        for (RowData rowDatum : rowData) {
            tableHeight += rowDatum.rowHeight;
        }

        // Position table within the container.
        int align = this.align;
        float x = layoutX + padLeft;
        if ((align & Align.right) != 0) {
            x += layoutWidth - tableWidth;
        } else if ((align & Align.left) == 0) { // Center
            x += (layoutWidth - tableWidth) / 2;
        }

        float y = layoutY + padTop;
        if ((align & Align.bottom) != 0) {
            y += layoutHeight - tableHeight;
        } else if ((align & Align.top) == 0) { // Center
            y += (layoutHeight - tableHeight) / 2;
        }

        // Position actors within cells.
        float currentX = x, currentY = y;
        for (var c : cells) {
            float spannedCellWidth = 0;
            for (int column = c.column, k = column + c.columnSize; column < k; column++) {
                spannedCellWidth += columnData[column].columnWidth;
            }
            spannedCellWidth -= c.padLeft + c.padRight;

            currentX += c.padLeft;

            float fillX = c.fillX, fillY = c.fillY;
            if (fillX > 0) {
                c.elementWidth = Math.max(spannedCellWidth * fillX, c.minWidth());
                float maxWidth = c.maxWidth();
                if (maxWidth > 0) {
                    c.elementWidth = Math.min(c.elementWidth, maxWidth);
                }
            }
            var crowHeight = rowData[c.row].rowHeight;
            if (fillY > 0) {
                c.elementHeight = Math.max(crowHeight * fillY - c.padTop - c.padBottom, c.minHeight());
                float maxHeight = c.maxHeight();
                if (maxHeight > 0) {
                    c.elementHeight = Math.min(c.elementHeight, maxHeight);
                }
            }

            align = c.align;
            if ((align & Align.left) != 0) {
                c.elementX = currentX;
            } else if ((align & Align.right) != 0) {
                c.elementX = currentX + spannedCellWidth - c.elementWidth;
            } else {
                c.elementX = currentX + (spannedCellWidth - c.elementWidth) / 2;
            }

            if ((align & Align.top) != 0) {
                c.elementY = currentY + c.padTop;
            } else if ((align & Align.bottom) != 0) {
                c.elementY = currentY + crowHeight - c.elementHeight - c.padBottom;
            } else {
                c.elementY = currentY + (crowHeight - c.elementHeight + c.padTop - c.padBottom) / 2;
            }

            if ((c.flags & END_ROW) != 0) {
                currentX = x;
                currentY += crowHeight;
            } else {
                currentX += spannedCellWidth + c.padRight;
            }
        }
    }

    static class ColumnData {
        float columnMinWidth;
        float columnWidth;
        float columnPrefWidth;
        float expandWidth;
        float columnWeightedWidth;
    }

    static class RowData {
        float rowMinHeight;
        float rowPrefHeight;
        float rowHeight;
        float expandHeight;
        float rowWeightedHeight;
    }

    ColumnData[] columnData;
    RowData[] rowData;

    private void computeSize() {
        if (!cells.isEmpty() && (cells.getLast().flags & END_ROW) == 0) {
            endRow();
            implicitEndRow = true;
        }

        int columns = this.columns, rows = this.rows;
        var columnData = new ColumnData[columns];
        var rowData = new RowData[rows];
        for (int j = 0; j < columnData.length; j++) columnData[j] = new ColumnData();
        for (int j = 0; j < rowData.length; j++) rowData[j] = new RowData();

        this.columnData = columnData;
        this.rowData = rowData;

        for (var c : cells) {
            int column = c.column, row = c.row, columnSize = c.columnSize;

            var crow = rowData[row];
            var ccol = columnData[column];

            if (c.expandY != 0 && crow.expandHeight == 0) {
                crow.expandHeight = c.expandY;
            }
            if (columnSize == 1 && c.expandX != 0 && ccol.expandWidth == 0) {
                ccol.expandWidth = c.expandX;
            }

            float minWidth = c.minWidth();
            float minHeight = c.minHeight();
            float maxWidth = c.maxWidth();
            float maxHeight = c.maxHeight();
            float prefWidth = c.prefWidth();
            float prefHeight = c.prefHeight();
            if (prefWidth < minWidth) {
                prefWidth = minWidth;
            }
            if (prefHeight < minHeight) {
                prefHeight = minHeight;
            }
            if (maxWidth > 0 && prefWidth > maxWidth) {
                prefWidth = maxWidth;
            }
            if (maxHeight > 0 && prefHeight > maxHeight) {
                prefHeight = maxHeight;
            }

            if (columnSize == 1) {
                float hpadding = c.padLeft + c.padRight;
                ccol.columnPrefWidth = Math.max(ccol.columnPrefWidth, prefWidth + hpadding);
                ccol.columnMinWidth = Math.max(ccol.columnMinWidth, minWidth + hpadding);
            }
            float vpadding = c.padTop + c.padBottom;
            crow.rowPrefHeight = Math.max(crow.rowPrefHeight, prefHeight + vpadding);
            crow.rowMinHeight = Math.max(crow.rowMinHeight, minHeight + vpadding);
        }


        float uniformMinWidth = 0, uniformMinHeight = 0;
        float uniformPrefWidth = 0, uniformPrefHeight = 0;
        for (var c : cells) {
            int column = c.column;

            // Colspan with expand will expand all spanned columns if none of the spanned columns have expand.
            int expandX = c.expandX;
            outer:
            if (expandX != 0) {
                int k = column + c.columnSize;
                for (int j = column; j < k; j++) {
                    if (columnData[j].expandWidth != 0) {
                        break outer;
                    }
                }
                for (int j = column; j < k; j++) {
                    columnData[j].expandWidth = expandX;
                }
            }

            // Collect uniform sizes.
            if ((c.flags & UNIFORM_X) != 0 && c.columnSize == 1) {
                float hpadding = c.padLeft + c.padRight;
                var ccol = columnData[column];
                uniformMinWidth = Math.max(uniformMinWidth, ccol.columnMinWidth - hpadding);
                uniformPrefWidth = Math.max(uniformPrefWidth, ccol.columnPrefWidth - hpadding);
            }
            if ((c.flags & UNIFORM_Y) != 0) {
                float vpadding = c.padTop + c.padBottom;
                var crow = rowData[c.row];
                uniformMinHeight = Math.max(uniformMinHeight, crow.rowMinHeight - vpadding);
                uniformPrefHeight = Math.max(uniformPrefHeight, crow.rowPrefHeight - vpadding);
            }
        }

        // Distribute any additional min and pref width added by colspanned cells to the columns spanned.
        for (var c : cells) {
            int colspan = c.columnSize;
            if (colspan == 1) {
                continue;
            }
            int column = c.column;

            float minWidth = c.minWidth();
            float prefWidth = c.prefWidth();
            float maxWidth = c.maxWidth();
            if (prefWidth < minWidth) {
                prefWidth = minWidth;
            }
            if (maxWidth > 0 && prefWidth > maxWidth) {
                prefWidth = maxWidth;
            }

            float spannedMinWidth = -(c.padLeft + c.padRight), spannedPrefWidth = spannedMinWidth;
            float totalExpandWidth = 0;
            for (int j = column, k = j + colspan; j < k; j++) {
                var ccol = columnData[j];
                spannedMinWidth += ccol.columnMinWidth;
                spannedPrefWidth += ccol.columnPrefWidth;
                totalExpandWidth += ccol.expandWidth; // Distribute extra space using expand, if any columns have expand.
            }

            float extraMinWidth = Math.max(0, minWidth - spannedMinWidth);
            float extraPrefWidth = Math.max(0, prefWidth - spannedPrefWidth);
            for (int j = column, k = j + colspan; j < k; j++) {
                var ccol = columnData[j];
                float ratio = totalExpandWidth == 0
                        ? 1f / colspan
                        : ccol.expandWidth / totalExpandWidth;
                ccol.columnMinWidth += extraMinWidth * ratio;
                ccol.columnPrefWidth += extraPrefWidth * ratio;
            }
        }

        // Determine table min and pref size.
        tableMinWidth = 0;
        tableMinHeight = 0;
        tablePrefWidth = 0;
        tablePrefHeight = 0;
        for (int i = 0; i < columns; i++) {
            var ccol = columnData[i];
            tableMinWidth += ccol.columnMinWidth;
            tablePrefWidth += ccol.columnPrefWidth;
        }
        for (int i = 0; i < rows; i++) {
            var crow = rowData[i];
            tableMinHeight += crow.rowMinHeight;
            tablePrefHeight += Math.max(crow.rowMinHeight, crow.rowPrefHeight);
        }
        float hpadding = getMarginLeft() + getMarginRight();
        float vpadding = getMarginTop() + getMarginBottom();
        tableMinWidth = tableMinWidth + hpadding;
        tableMinHeight = tableMinHeight + vpadding;
        tablePrefWidth = Math.max(tablePrefWidth + hpadding, tableMinWidth);
        tablePrefHeight = Math.max(tablePrefHeight + vpadding, tableMinHeight);
    }

    public Table row() {
        if (!cells.isEmpty()) {
            if (!implicitEndRow) {
                endRow();
            }
            invalidateSize();
        }
        implicitEndRow = false;
        return this;
    }

    private void invalidateSize() {
        flags |= (FLAG_X_CHANGED | FLAG_Y_CHANGED | FLAG_W_CHANGED | FLAG_H_CHANGED);
    }

    private void endRow() {
        int rowColumns = 0;
        for (var c : cells.reversed()) {
            if ((c.flags & END_ROW) != 0) {
                break;
            }
            rowColumns += c.columnSize;
        }
        columns = Math.max(columns, rowColumns);
        rows++;
        cells.getLast().setEndRow(true);
    }

    @Override
    protected void drawThis() {
        var b = background;
        if (b != null) {
            Global.batch.draw(b, x, y);
        }
    }

    @Override
    protected void resize() {
        computeSizeIfNeeded();
    }

    private void computeSizeIfNeeded() {
        if ((flags & (FLAG_X_CHANGED | FLAG_Y_CHANGED | FLAG_W_CHANGED | FLAG_H_CHANGED)) != 0) {
            computeSize();
        }
    }

    // region Element overrides

    @Override
    public float getMinWidth() {
        computeSize();
        return tableMinWidth;
    }

    @Override
    public float getMinHeight() {
        computeSize();
        return tableMinHeight;
    }

    @Override
    public float getPrefWidth() {
        computeSize();
        float width = tablePrefWidth;
        if (background != null) return Math.max(width, background.getMinWidth());
        return width;
    }

    @Override
    public float getPrefHeight() {
        computeSize();
        float height = tablePrefHeight;
        if (background != null) return Math.max(height, background.getMinHeight());
        return height;
    }

    // endregion

    public float getMarginTop() {
        return marginTop != unset ? marginTop : background == null ? 0f : background.getTopHeight();
    }

    public float getMarginLeft() {
        return marginLeft != unset ? marginLeft : background == null ? 0f : background.getLeftWidth();
    }

    public float getMarginBottom() {
        return marginBottom != unset ? marginBottom : background == null ? 0f : background.getBottomHeight();
    }

    public float getMarginRight() {
        return marginRight != unset ? marginRight : background == null ? 0f : background.getRightWidth();
    }

    // region Modifiers

    public Table margin(float pad) {
        margin(pad, pad, pad, pad);
        return this;
    }

    public Table margin(float top, float left, float bottom, float right) {
        marginTop = (top);
        marginLeft = (left);
        marginBottom = (bottom);
        marginRight = (right);
        invalidateSize();
        return this;
    }

    public Table marginTop(float padTop) {
        this.marginTop = (padTop);
        invalidateSize();
        return this;
    }

    public Table marginLeft(float padLeft) {
        this.marginLeft = (padLeft);
        invalidateSize();
        return this;
    }

    public Table marginBottom(float padBottom) {
        this.marginBottom = (padBottom);
        invalidateSize();
        return this;
    }

    public Table marginRight(float padRight) {
        this.marginRight = (padRight);
        invalidateSize();
        return this;
    }

    public Table align(int align) {
        this.align = align;
        return this;
    }

    public Table center() {
        align = Align.center;
        return this;
    }

    public Table top() {
        align |= Align.top;
        align &= ~Align.bottom;
        return this;
    }

    public Table left() {
        align |= Align.left;
        align &= ~Align.right;
        return this;
    }

    public Table bottom() {
        align |= Align.bottom;
        align &= ~Align.top;
        return this;
    }

    public Table right() {
        align |= Align.right;
        align &= ~Align.left;
        return this;
    }

    // endregion

    // region Overloads

    public Cell<Panel> panel(Style.Panel style) {
        return addCell(new Panel(this, style));
    }

    public Cell<Table> table(Consumer<Table> consumer) {
        Table t = new Table(this);
        consumer.accept(t);
        return addCell(t);
    }

    public Cell<Table> table() {
        return addCell(new Table(this));
    }

    public Cell<ImageButton> imageButton(Drawable image, Runnable action) {
        return addCell(new ImageButton(this, image)
                .onClick(action));
    }

    public Cell<Button> button(Style.TextButton style, Consumer<Button> onClick) {
        Button b = new Button(this, style);
        b.onClick(onClick);
        return addCell(b);
    }

    public Cell<ToggleButton> toggleButton(Style.ToggleButton style, Runnable onClick) {
        return addCell(new ToggleButton(this, style)
                .onClick(onClick));
    }

    public Cell<Button> button(Style.TextButton style, Runnable onClick) {
        Button b = new Button(this, style);
        b.onClick(onClick);
        return addCell(b);
    }

    public Cell<ImageElement> image(Drawable drawable) {
        return addCell(new ImageElement(this, drawable));
    }

    public Cell<Slider> slider(int min, int max, Slider.MoveListener onMove) {
        var s = new Slider(this);
        s.setBounds(min, max);
        s.onMove(onMove);
        return addCell(s);
    }

    public Cell<TextArea> label(Style.Text style, String text) {
        return addCell(new TextArea(this, style)
                .setText(text));
    }

    // endregion
}
