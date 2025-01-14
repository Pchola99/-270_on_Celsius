package core.graphic;

import java.util.ArrayList;

public final class RectanglePacker {
    private final ArrayList<RectanglePacker.Region> regions;

    public int w, h;

    public RectanglePacker(int w, int h) {
        this(0, w, h);
    }

    public RectanglePacker(int regionCount, int w, int h) {
        this.regions = new ArrayList<>(regionCount);
        resize(w, h);
    }

    private int rectFits(int idx, int w, int h) {
        Region rg = regions.get(idx);
        if (rg.x + w > this.w) {
            return -1;
        }

        int y = rg.y;
        int spaceLeft = w;
        int i = idx;
        while (spaceLeft > 0) {
            if (i == regions.size()) {
                return -1;
            }

            Region th = regions.get(i);
            y = Math.max(y, th.y);
            if (y + h > this.h) {
                return -1;
            }
            spaceLeft -= th.w;
            i++;
        }
        return y;
    }

    private void addRegion(int idx, int x, int y, int w, int h) {
        regions.add(idx, new Region(x, y + h, w));
        for (int i = idx + 1; i < regions.size(); ) {
            Region prev = regions.get(i - 1);
            Region curr = regions.get(i);

            if (curr.x < prev.x + prev.w) {
                int shrink = prev.x - curr.x + prev.w;
                curr.x += shrink;
                curr.w -= shrink;
                if (curr.w <= 0) {
                    regions.remove(i);
                    i--;
                } else {
                    break;
                }
            } else {
                break;
            }
            i++;
        }

        int i = 0;
        while (i + 1 < regions.size()) {
            Region next = regions.get(i + 1);
            Region curr = regions.get(i);

            if (curr.y == next.y) {
                curr.w += next.w;
                regions.remove(i + 1);
                i--;
            }
            i++;
        }
    }

    public void resize(int w, int h) {
        regions.add(new Region(this.w, 0, w - this.w));
        this.w = w;
        this.h = h;
    }

    private final Position pos = new Position();

    public Position pack(int w, int h, int padding) {
        var res = pack(w + 2*padding, h + 2*padding);
        if (!res.isInvalid()) {
            res.x += padding;
            res.y += padding;
        }
        return res;
    }

    public Position pack(int w, int h) {
        int bestIDX = -1, bestX = -1, bestY = -1;
        int bestH = this.h, bestW = this.w;

        for (int i = 0; i < regions.size(); i++) {
            int y = rectFits(i, w, h);

            if (y != -1) {
                Region rg = regions.get(i);
                if (y + h < bestH && rg.w < bestW) {
                    bestIDX = i;
                    bestW = rg.w;
                    bestH = y + h;
                    bestX = rg.x;
                    bestY = y;
                }
            }
        }

        if (bestIDX != -1) {
            addRegion(bestIDX, bestX, bestY, w, h);
            pos.set(bestX, bestY);
        } else {
            pos.set(-1, -1);
        }
        return pos;
    }

    private static class Region {
        public int x, y, w;

        Region(int x, int y, int w) {
            this.x = x;
            this.y = y;
            this.w = w;
        }
    }

    public static class Position {
        public int x, y;

        public void set(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public boolean isInvalid() {
            return x < 0 || y < 0;
        }
    }
}
