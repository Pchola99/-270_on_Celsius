package core.World.StaticWorldObjects;

import core.Global;
import core.PlayGameScene;
import core.util.Sized;
import core.World.Weather.Sun;
import core.math.Point2i;

import java.util.HashMap;

import static core.World.Textures.TextureDrawing.blockSize;

public class TemperatureMap {
    private static HashMap<Point2i, Float> individualTemperature = new HashMap<>();
    private static float[] temperature;
    private static final float coreTemp = 4000;
    public static float dayTemperatureDecrement = 0.04f, currentWorldTemperature;

    // todo как то криво работает
    public static void create(PlayGameScene playGameScene) {
        currentWorldTemperature = playGameScene.sun.currentTime / 100;
        temperature = new float[Global.world.sizeY];

        for (int i = 0; i < temperature.length; i++) {
            temperature[i] = Math.min(coreTemp, (coreTemp / (i + 1)) * (Global.world.sizeY / 1000f));
        }
    }

    public static HashMap<String, Object> getTemperatures() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("WorldTemperatureDecrement", dayTemperatureDecrement);
        map.put("WorldCurrentTemperature", currentWorldTemperature);
        map.put("WorldBlocksTemperature", temperature);
        map.put("WorldBlocksIndividualTemps", individualTemperature);

        return map;
    }

    public static void setData(HashMap<String, Object> map) {
        dayTemperatureDecrement = (float) map.get("WorldTemperatureDecrement");
        currentWorldTemperature = (float) map.get("WorldCurrentTemperature");
        temperature = (float[]) map.get("WorldBlocksTemperature");
        individualTemperature = (HashMap<Point2i, Float>) map.get("WorldBlocksIndividualTemps");
    }

    public static float getTemp(int cellX, int cellY) {
        float temp = individualTemperature.getOrDefault(new Point2i(cellX, cellY), Float.MIN_VALUE);

        if (temp != Float.MIN_VALUE) {
            if (temp == temperature[cellY]) {
                individualTemperature.remove(new Point2i(cellX, cellY));
            }
            return temp;
        }
        // thermal conductivity
        int n = 100;
        return Math.clamp(temperature[cellY] + (currentWorldTemperature / (temperature.length / (cellY + 1f))) / (101 - n), -270, coreTemp);
    }

    public static void setTemp(int cellX, int cellY, float temp) {
        if (temperature[cellY] != temp) {
            individualTemperature.put(new Point2i(cellX, cellY), temp);
        }
    }

    public static void update(Sun sun) {
        if (sun.currentTime >= Sun.startDay || (sun.currentTime >= 0 && sun.currentTime <= Sun.endSunset)) {
            currentWorldTemperature += dayTemperatureDecrement / 2.6f;
        } else {
            currentWorldTemperature -= dayTemperatureDecrement;
        }
    }

    public static float getAverageTempAroundDynamic(float xPos, float yPos, Sized size) {
        int count = 0;
        float totalTemp = 0;


        int minX = (int) Math.floor(xPos / blockSize);
        int minY = (int) Math.floor(yPos / blockSize);

        int maxX = (int) Math.floor((xPos + size.width()) / blockSize);
        int maxY = (int) Math.floor((yPos + size.height()) / blockSize);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (Global.world.inBounds(x, y)) {
                    totalTemp += getTemp(x, y);
                    count++;
                }
            }
        }
        return totalTemp / count;
    }
}
