package core.ui.menu;

import core.EventHandling.Logging.Logger;
import core.UI;
import core.Utils.SimpleColor;
import core.ui.Dialog;
import core.ui.Styles;

import java.awt.*;
import java.net.URI;

import static core.EventHandling.Logging.Json.getName;
import static core.EventHandling.Logging.Logger.printException;
import static core.Global.atlas;
import static core.Window.start;

public class MainMenu extends Dialog {
    public MainMenu() {
        maximize();
        top();

        panel(Styles.SIMPLE_PANEL).height(115).growX().with(panel -> {
            panel.margin(30);
            panel.button(Styles.TEXT_BUTTON, this::playButton)
                    .padRight(262)
                    .with(b -> {
                        b.setName(getName("Play"));
                    });
            panel.button(Styles.TEXT_BUTTON, this::settingsBtn)
                    .padRight(34)
                    .with(b -> {
                        b.setName(getName("Settings")).setColor(SimpleColor.DIRTY_WHITE);
                    });
            panel.button(Styles.TEXT_BUTTON, this::exitBtn)
                    .with(b -> {
                        b.setName(getName("Exit"))
                                .setColor(SimpleColor.DIRTY_WHITE);
                    });
            panel.imageButton(atlas.byPath("UI/discordIcon"), this::discordBtn)
                    .expandX()
                    .right();
        });
    }

    private void discordBtn() {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI("https://discord.gg/gUS9X6exAQ"));
        } catch (Exception e) {
            printException("Error when open discord server", e);
        }
    }

    private void exitBtn() {
        Logger.logExit(0);
    }

    private void settingsBtn() {
        UI.settings().show();
        if (!start) {
            hide();
        } else {
            UI.pause().hide();
        }
    }

    private void playButton() {
        hide();
        UI.createPlanet().show();
    }
}
