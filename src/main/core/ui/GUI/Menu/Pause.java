package core.ui.GUI.Menu;

import core.EventHandling.EventHandler;
import core.EventHandling.Logging.Logger;
import core.UI;
import core.Utils.SimpleColor;
import core.World.Saves;
import core.ui.Dialog;
import core.ui.Styles;

import static core.EventHandling.Logging.Json.getName;
import static core.Window.start;

public class Pause extends Dialog {
    public Pause() {
        addPanel(Styles.SIMPLE_PANEL, 0, 0, EventHandler.width, EventHandler.height);
        addButton(Styles.TEXT_BUTTON, this::continueBtn)
                .set(840, 650, 240, 65)
                .setName(getName("Continue"))
                .setColor(SimpleColor.DEFAULT_ORANGE);
        addButton(Styles.TEXT_BUTTON, this::saveButton)
                .set(840, 550, 240, 65)
                .setName(getName("SaveWorld"))
                .setColor(SimpleColor.DEFAULT_ORANGE);
        addButton(Styles.TEXT_BUTTON, this::exitBtn)
                .set(840, 300, 240, 65)
                .setName(getName("Exit"))
                .setColor(SimpleColor.DIRTY_WHITE);
        addButton(Styles.TEXT_BUTTON, this::settingsBtn)
                .set(840, 400, 240, 65)
                .setName(getName("Settings"))
                .setColor(SimpleColor.DIRTY_WHITE);
    }

    private void continueBtn() {
        hide();
        UI.settings().hide();
    }

    private void exitBtn() {
        Logger.logExit(0);
    }

    private void settingsBtn() {
        UI.settings().show();
        if (!start) {
            UI.mainMenu().hide();
        } else {
            hide();
        }
    }

    private void saveButton() {
        Saves.createWorldSave();
    }
}
