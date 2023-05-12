package core.GUI;

import core.Logging.config;
import core.Logging.logger;
import core.World.Textures.TextureLoader;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;


public class Fonts {
    private static int fontSize = Integer.parseInt(config.jetFromConfig("FontSize"));
    private static Font font;
    public static ConcurrentHashMap<Character, ByteBuffer> chars = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Character, Dimension> letterSize = new ConcurrentHashMap<>();

    public static void generateFont(String pathTTF) {
        Font font = null;
        try {
            font = Font.createFont(Font.PLAIN, new File(pathTTF));
            //default 12
            font = font.deriveFont(Font.PLAIN, (float) (fontSize * Toolkit.getDefaultToolkit().getScreenResolution() / 72.0));
            Fonts.font = font;
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
        FontRenderContext fontRenderContext = new FontRenderContext(null, true, true);

        String allChars = "";
        for (char c = 0; c < Character.MAX_VALUE; c++) {
            if (font.canDisplay(c) && font.createGlyphVector(fontRenderContext, Character.toString(c)).getGlyphCode(0) != 0) {
                allChars += c;
            }
        }

        for (char c : allChars.toCharArray()) {
            if (font.canDisplay(c)) {
                String str = Character.toString(c);
                LineMetrics lm = font.getLineMetrics(str, fontRenderContext);
                if (font.getStringBounds(str, fontRenderContext).getWidth() == 0 || lm.getHeight() == 0) {
                    continue;
                }
                int charWidth = (int) font.getStringBounds(str, fontRenderContext).getWidth();
                int charHeight = (int) lm.getHeight();

                BufferedImage charImage = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics graphics = charImage.getGraphics();

                graphics.setColor(Color.WHITE);
                graphics.setFont(font);
                graphics.drawString(str, 0, charHeight - (int) lm.getDescent());

                graphics.dispose();
                letterSize.put(c, new Dimension(charWidth, charHeight));
                chars.put(c, TextureLoader.ByteBufferEncoder(charImage));
            } else {
                logger.log("charter '" + c + "' cannot displayed, file: " + pathTTF);
            }
        }
    }

    public static Dimension getTextCenter(String text) {
        FontRenderContext frc = new FontRenderContext(null, true, true);
        Rectangle2D bounds = font.getStringBounds(text, frc);

        int textWidth = (int) bounds.getWidth();
        int textHeight = (int) bounds.getHeight();

        return new Dimension(textWidth / 2, textHeight / 2);
    }
}
