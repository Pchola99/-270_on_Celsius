package core.ui.menu;

import core.Constants;
import core.UI;
import core.Utils.SimpleColor;
import core.World.WorldGenerator;
import core.ui.*;
import core.ui.Button;
import core.ui.Dialog;
import core.ui.layout.Table;

import java.util.function.Consumer;

import static core.EventHandling.Logging.Json.getName;
import static core.Global.atlas;

public class CreatePlanet extends Dialog {
    private final GenerationParameters parameters = new GenerationParameters();

    private ImageElement planet;

    record Layout(String name, Consumer<Table> modifier) {}

    final Layout[] menus = {
            // basic
            new Layout("Basic", t -> {
                t.toggleButton(Styles.DEFAULT_TOGGLE_BUTTON, () -> parameters.randomSpawn = !parameters.randomSpawn)
                        .padTop(50)
                        .with(b -> b.setName(getName("RandomSpawn")))
                        .row();
                t.toggleButton(Styles.DEFAULT_TOGGLE_BUTTON, () -> parameters.creatures = !parameters.creatures)
                        .padTop(50)
                        .with(b -> b.setName(getName("GenerateCreatures")));
            }),
            // generation
            new Layout("Generation", t -> {
                t.toggleButton(Styles.DEFAULT_TOGGLE_BUTTON, () -> parameters.simple = !parameters.simple)
                        .padTop(50)
                        .with(b -> b.setName(getName("GenerateSimpleWorld")))
                        .row();
            }),
            // physics
            new Layout("Physics", t -> {})
    };
    Layout currentMenu;

    public CreatePlanet() {
        maximize();
        margin(20);
        bottom();

        // Панель с настройками
        panel(Styles.DEFAULT_PANEL)
                .grow()
                .with(panel -> {
                    panel.top().left();

                    // Панель с вкладками
                    var buttons = panel.panel(Styles.SIMPLE_PANEL)
                            .height(85)
                            .growX()
                            .as();
                    buttons.left();

                    panel.row();
                    var settings = panel.table(t -> {
                                t.left().top();
                            })
                            .grow()
                            .as();

                    buttons.button(Styles.SIMPLE_TEXT_BUTTON, b -> {
                                hide();
                                UI.mainMenu().show();
                            })
                            .padRight(360)
                            .with(b -> {
                                b.setName(getName("Return"));
                            });

                    var menuButtons = new Button[menus.length];
                    for (int i = 0; i < menuButtons.length; i++) {
                        var menu = menus[i];
                        var button = buttons.button(Styles.SIMPLE_TEXT_BUTTON, () -> setLayout(settings, menu))
                                .padRight(20)
                                .with(b -> b.setName(getName(menu.name)))
                                .as();
                        menuButtons[i] = button;
                    }
                    buttons.oneOf(menuButtons);
                    setLayout(settings, menus[0]);
                });
        // Панель с изображением планеты
        panel(Styles.DEFAULT_PANEL).padLeft(20)
                .growY()
                .with(panel -> {
                    panel.minimize(true, false);

                    panel.table(t -> {
                        t.margin(50);
                        t.setBackground(atlas.byPath("World/WorldGenerator/skyBackgroundPlanet"));
                        t.image(atlas.byPath("World/WorldGenerator/planetMini"))
                                .with(c -> planet = c);
                    });
                    panel.row();

                    panel.slider(Constants.World.MIN_WORLD_SIZE, Constants.World.MAX_WORLD_SIZE, (size, max) -> {
                                String pic;
                                if (size >= max / 1.5f) {
                                    pic = "planetBig";
                                } else if (size >= max / 3f) {
                                    pic = "planetAverage";
                                } else {
                                    pic = "planetMini";
                                }
                                planet.setImage(atlas.byPath("World/WorldGenerator/" + pic));
                                parameters.size = size;
                            })
                            .padLeft(panel.getMarginLeft())
                            .padRight(panel.getMarginRight())
                            .padTop(70)
                            .with(s -> {
                                s.setSliderColor(Styles.DEFAULT_PANEL_COLOR)
                                        .setDotColor(SimpleColor.DEFAULT_ORANGE);
                            })
                            .row();

                    panel.button(Styles.SIMPLE_TEXT_BUTTON, () -> WorldGenerator.generateWorld(parameters))
                            .padBottom(15)
                            .padTop(70)
                            .with(block -> {
                                block.setName(getName("GenerateWorld")).setOneShot(true);
                            })
                            .row();
                });
        row();
        // Консоль
        panel(Styles.DEFAULT_PANEL)
                .padTop(20).height(200)
                .growX()
                .colspan(2);
    }

    private void setLayout(Table table, Layout requested) {
        if (currentMenu == requested) {
            return;
        }

        table.removeAll();
        table.setId(requested.name);
        for (Layout m : menus) {
            if (m == requested) {
                m.modifier.accept(table);
            }
        }
        currentMenu = requested;
    }

    public static class GenerationParameters {
        public boolean randomSpawn;
        public boolean creatures;
        public boolean simple;
        public int size = Constants.World.MIN_WORLD_SIZE;
    }
}
