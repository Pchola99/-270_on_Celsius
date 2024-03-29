package core.Utils;

import core.EventHandling.Logging.Config;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import static core.EventHandling.Logging.Logger.printException;

public class AnonymousStatistics extends Thread {
    public static final boolean sendStatic = Boolean.parseBoolean(Config.getFromConfig("SendAnonymousStatistics"));

    public static void sendStateMessageThread(String message) {
        if (sendStatic) {
            new Thread(() -> {
                try {
                    URL url = new URL("/");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setDoOutput(true);

                    String jsonMessage = String.format("{\"content\":\"%s\"}", message);
                    OutputStream os = con.getOutputStream();
                    os.write(jsonMessage.getBytes());
                    os.flush();
                    os.close();
                    con.getResponseCode();

                } catch (Exception e) {
                    printException("Error when push anonymous state", e);
                }
            }).start();
        }
    }

    public static void sendStateMessage(String message) {
        if (sendStatic) {
            try {
                URL url = new URL("/");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                String jsonMessage = String.format("{\"content\":\"%s\"}", message);
                OutputStream os = con.getOutputStream();
                os.write(jsonMessage.getBytes());
                os.flush();
                os.close();
                con.getResponseCode();

            } catch (Exception e) {
                printException("Error when push anonymous state", e);
            }
        }
    }
}
