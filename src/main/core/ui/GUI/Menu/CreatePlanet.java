package core.ui.GUI.Menu;

import core.UI;
import core.Utils.SimpleColor;
import core.World.WorldGenerator;
import core.ui.*;

import static core.EventHandling.Logging.Json.getName;
import static core.Global.atlas;

public class CreatePlanet extends Dialog {
    private final ImageElement planet;
    private final GenerationParameters parameters = new GenerationParameters();
    private final Dialog basicParameters, generationParameters;

    public CreatePlanet() {
        addPanel(Styles.DEFAULT_PANEL, 20, 20, 1880, 200);
        addPanel(Styles.DEFAULT_PANEL, 20, 240, 1400, 820);
        addPanel(Styles.DEFAULT_PANEL, 1440, 240, 460, 820);
        // Панель с вкладками
        var upperPanel = addPanel(Styles.SIMPLE_PANEL, 40, 955, 1360, 85);
        addImage(1460, 620, atlas.byPath("World/WorldGenerator/skyBackgroundPlanet.png"));
        planet = addImage(1510, 670, atlas.byPath("World/WorldGenerator/planetMini.png"));

        addButton(Styles.SIMPLE_TEXT_BUTTON, b -> {
            hide();
            UI.mainMenu().show();
        })
        .set(40, 975, 240, 65)
        .setName(getName("Return"));

        upperPanel.oneOf(
            // Поскольку сделать что-то с ресивером нельзя, то приходится страдать и тут указывать `upperPanel.`
            upperPanel.addButton(Styles.SIMPLE_TEXT_BUTTON, this::basicBtn)
                    .set(640, 975, 240, 65)
                    .setName(getName("Basic")),
            upperPanel.addButton(Styles.SIMPLE_TEXT_BUTTON, this::generationBtn)
                    .set(900, 975, 240, 65)
                    .setName(getName("Generation")),
            addButton(Styles.SIMPLE_TEXT_BUTTON, () -> {})
                    .set(1160, 975, 240, 65)
                    .setName(getName("Physics"))
        );
        addButton(Styles.SIMPLE_TEXT_BUTTON, () -> WorldGenerator.generateWorld(parameters))
                .set(1460, 260, 420, 65)
                .setName(getName("GenerateWorld"))
                .setOneShot(true);
        addSlider(2500, (x, max) -> {
            String pic;
            if (x >= max / 1.5f) {
                pic = "planetBig.png";
            } else if (x >= max / 3f) {
                pic = "planetAverage.png";
            } else {
                pic = "planetMini.png";
            }
            planet.setImage(atlas.byPath("World/WorldGenerator/" + pic));
            parameters.size = x + 20;
        })
                .set(1460, 340, 420, 20)
                .setSliderColor(Styles.DEFAULT_PANEL_COLOR)
                .setDotColor(SimpleColor.DEFAULT_ORANGE);
        basicParameters = add(new Dialog() {{
            visible = true;
            addToggleButton(Styles.DEFAULT_TOGGLE_BUTTON, () -> parameters.creatures = !parameters.creatures)
                    .setPosition(70, 890)
                    .setName(getName("GenerateCreatures"));
            addToggleButton(Styles.DEFAULT_TOGGLE_BUTTON, () -> parameters.randomSpawn = !parameters.randomSpawn)
                    .setPosition(70, 820)
                    .setName(getName("RandomSpawn"));
        }});
        generationParameters = add(new Dialog() {{
            visible = false;
            addToggleButton(Styles.DEFAULT_TOGGLE_BUTTON, () -> parameters.simple = !parameters.simple)
                    .setPosition(70, 890)
                    .setName(getName("GenerateSimpleWorld"));
        }});
    }

    private void basicBtn(Button b) {
        generationParameters.setVisible(false);
        basicParameters.setVisible(true);
    }

    private void generationBtn(Button b) {
        basicParameters.setVisible(false);
        generationParameters.setVisible(true);
    }

    public static class GenerationParameters {
        public boolean randomSpawn;
        public boolean creatures;
        public boolean simple;
        public int size = 20; // TODO помню про планы убрать ограничение генерации на размеры
    }
}
