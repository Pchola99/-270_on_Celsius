package core.World.StaticWorldObjects;

import core.World.Creatures.Player.Inventory.Items.Items;
import core.World.StaticWorldObjects.Structures.Factories;
import core.g2d.Atlas;

public class FactoryType extends StaticObjectsConst {
    public float needEnergy;
    public int maxProductionProgress;
    public short maxStoredObjects;
    public String sound;
    public BreakingType breakingType;
    public Items[] outputObjects, inputObjects, fuel;

    public FactoryType(boolean hasMotherBlock, float maxHp, float density, float resistance,
                       int lightTransmission, Atlas.Region texture, String objectName,
                       String originalFileName, StaticObjectsConst[][] optionalTiles, Types type,

                       int productionSpeed, int needEnergy, short maxStoredObjects, String sound,
                       BreakingType breakingType,
                       Items[] outputObjects, Items[] inputObjects, Items[] fuel) {
        super(hasMotherBlock, maxHp, density, resistance, lightTransmission, texture, objectName, originalFileName, optionalTiles, type);
        this.maxProductionProgress = productionSpeed;
        this.needEnergy = needEnergy;
        this.maxStoredObjects = maxStoredObjects;
        this.sound = sound;
        this.outputObjects = outputObjects;
        this.inputObjects = inputObjects;
        this.fuel = fuel;
        this.breakingType = breakingType;
    }

    // TODO Как насчёт MalfunctionType?
    public enum BreakingType {
        WEAK_SLOW, // slow working
        WEAK_OVERCONSUMPTION, // high consumption
        AVERAGE_STOP, // stop working
        AVERAGE_MISWORKING, // misworking
        CRITICAL // full stop working, need rebuild
    }
}
