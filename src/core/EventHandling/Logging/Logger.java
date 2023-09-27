package core.EventHandling.Logging;

import com.sun.management.OperatingSystemMXBean;
import core.AnonymousStatistics;
import core.Window;
import core.World.Weather.Sun;
import java.awt.*;
import java.io.*;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Random;
import static core.EventHandling.Logging.Config.getFromConfig;
import static core.Window.*;
import static org.lwjgl.glfw.GLFW.*;

public class Logger extends PrintStream {
    private static final long sessionId = (long) (new Random().nextDouble() * Long.MAX_VALUE);
    public static boolean cleanup = false;
    private static byte[] lastErrBuf;
    public Logger() {
        super(System.err);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        if (!Arrays.equals(buf, lastErrBuf)) {
            lastErrBuf = buf;
            log("Some error: " + new String(buf, off, len));
        }
    }

    public static void log(String message) {
        if (getFromConfig("Debug").equals("true")) {
            System.out.println(message);

            if (!cleanup) {
                try {
                    cleanup = true;
                    PrintWriter printWriter = new PrintWriter(new FileWriter(defPath + "\\log.txt"));

                    printWriter.print("");
                    printWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                PrintWriter printWriter = new PrintWriter(new FileWriter(defPath + "\\log.txt", true));

                printWriter.println(message);
                printWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void logExit(int status, String reason, boolean exitOnProgram) {
        String exit;

        if (reason != null) {
            log("\nExit reason: " + reason);
        } else {
            reason = "none";
        }

        switch (status) {
            case 0 -> exit = " (normal)";
            case 1 -> exit = " (critical error)";
            case 1863 -> exit = " (sudden closure)";
            case 6553 -> exit = " (glfw error)";
            default -> exit = " (unknown state)";
        }

        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            log("\nName GC: " + gcBean.getName() + "\nFull collection cycles GC: " + gcBean.getCollectionCount() + "\nFull time GC (ms): " + gcBean.getCollectionTime() + "\nRAM use: " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
            break;
        }

        AnonymousStatistics.sendStateMessage("Session '" + sessionId + "' exit, time: '" + LocalDateTime.now() + "', reason: '" + reason + "', status: " + status + exit);
        log("\nProgram exit at: " + LocalDateTime.now() + "\nExit code: " + status + exit + "\nGame time: " + Sun.currentTime +  "\n-------- Log ended --------");

        if (exitOnProgram) {
            glfwDestroyWindow(glfwWindow);
            System.exit(status);
        }
    }

    public static void logExit(int status) {
        logExit(status, null, true);
    }

    public static void logStart() {
        System.setErr(new Logger());
        Json.detectLanguage();

        String computerInfo = String.format(" | Разрешение экрана: %d x %d | Процессор: %s | Количество потоков: %s | Количество ОЗУ: %d MB", Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height, System.getenv("PROCESSOR_IDENTIFIER"), Runtime.getRuntime().availableProcessors(), ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getTotalMemorySize() / (1024 * 1024));
        String system = System.getProperty("os.name").toLowerCase();
        String message =
                !system.contains("windows 10") ? "Warning: " + System.getProperty("os.name") + " not supported!\n" : "" +
                "\nGLFW version: " + glfwGetVersionString() + "\nGame version: " + Window.version +
                "\nStart time: " + LocalDateTime.now() + "\n\nPreload textures: " + getFromConfig("PreloadTextures") +
                "\nVertical sync: " + Config.getFromConfig("VerticalSync") + " (" + verticalSync + ")" +
                "\nCurrent language: " + getFromConfig("Language") + "\nAvailable languages: " +
                Json.getAllLanguages().replace(" ", ", ") + "\n";

        AnonymousStatistics.sendStateMessageThread("Session '" + sessionId + "' started, time: '" + LocalDateTime.now() + "', system info: " + system + computerInfo);
        log(message);
    }
}
