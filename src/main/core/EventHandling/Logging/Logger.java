package core.EventHandling.Logging;

import com.sun.management.OperatingSystemMXBean;
import core.Window;
import core.World.Weather.Sun;
import java.awt.Toolkit;
import java.io.*;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static core.EventHandling.Logging.Config.getFromConfig;
import static core.Global.assets;
import static core.Window.*;
import static org.lwjgl.glfw.GLFW.*;

public class Logger extends PrintStream {
    private static final long sessionId = ThreadLocalRandom.current().nextLong();
    private static boolean cleanup = false, ended = false;
    private static byte[] lastErrBuf;
    private static final Set<String> testedSystems = Set.of("6.6.8-arch", "windows10");

    public Logger() {
        super(System.err);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        if (!Arrays.equals(buf, lastErrBuf)) {
            lastErrBuf = buf;
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();

            printStackTrace(Arrays.copyOfRange(elements, Integer.parseInt(Config.getFromConfig("TrimSystemErrStackTraceElements")), elements.length), "none", "none", new String(buf, off, len, StandardCharsets.UTF_8), "System.err");
        }
    }

    public static void printException(String message, Throwable exception) {
        printStackTrace(exception.getStackTrace(), exception.getMessage(), String.valueOf(exception.getCause()), message, "Error");
    }

    public static void printStackTrace(StackTraceElement[] stackTrace, String exceptionMessage, String exceptionCause, String message, String whatDetected) {
        StringBuilder stackTraceMessage = new StringBuilder();

        if (Integer.parseInt(getFromConfig("Debug")) == 2) {
            stackTraceMessage.append("\n-------- ").append(whatDetected).append(" detected -------- \nMessage: '").append(message);
            stackTraceMessage.append("', exception message: '").append(exceptionMessage);
            stackTraceMessage.append("', cause: '").append(exceptionCause);
            stackTraceMessage.append("', total stack trace length: '").append(stackTrace.length);
            stackTraceMessage.append("', stack trace: ");

            for (int i = 0; i < stackTrace.length && i < Integer.parseInt(Config.getFromConfig("MaxStackTraceLength")); i++) {
                stackTraceMessage.append("\n\nClass name: ").append(stackTrace[i].getClassName());
                stackTraceMessage.append("\nMethod name: ").append(stackTrace[i].getMethodName());
                stackTraceMessage.append("\nLine: ").append(stackTrace[i].getLineNumber());
                stackTraceMessage.append("\nIs native: ").append(stackTrace[i].isNativeMethod());
            }
            stackTraceMessage.append("\n-------- ").append(whatDetected).append(" end --------\n");

        } else if (Integer.parseInt(getFromConfig("Debug")) == 1) {
            stackTraceMessage.append("\n").append(whatDetected).append(" message: '").append(message);
            stackTraceMessage.append("', exception message: '").append(exceptionMessage).append("' ");
        }

        log(stackTraceMessage.toString());
    }

    public static void log(String message) {
        if (Integer.parseInt(getFromConfig("Debug")) > 0) {
            System.out.println(message);

            if (!cleanup) {
                try {
                    try (PrintWriter printWriter = new PrintWriter(new FileWriter(assets.pathTo("log.txt"), StandardCharsets.UTF_8))) {
                        printWriter.print("");
                        cleanup = true;
                    }
                } catch (IOException e) {
                    printException("Error when cleanup log", e);
                }
            }

            try {
                try (PrintWriter printWriter = new PrintWriter(new FileWriter(assets.pathTo("log.txt"), StandardCharsets.UTF_8, true))) {
                    printWriter.println(message);
                }
            } catch (IOException e) {
                printException("Error when print to log", e);
            }
        }
    }

    public static void logExit(int status, String reason, boolean exitOnProgram) {
        if (!ended) {
            ended = true;

            StringBuilder exitMessage = getExitMessage(status, reason);
            log(exitMessage.toString());

            if (exitOnProgram) {
                glfwSetWindowShouldClose(glfwWindow, true);
            }
        }
    }

    private static StringBuilder getExitMessage(int status, String reason) {
        StringBuilder exitMessage = new StringBuilder();

        String exitStatus = switch (status) {
            case 0 -> " (normal)";
            case 1 -> " (critical error)";
            case 1863 -> " (sudden closure)";
            case 6553 -> " (glfw error)";
            default -> " (unknown state)";
        };

        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            exitMessage.append("\nName GC: ").append(gcBean.getName());
            exitMessage.append("\nFull collection cycles GC: ").append(gcBean.getCollectionCount());
            exitMessage.append("\nFull time GC (ms): ").append(gcBean.getCollectionTime());
            exitMessage.append("\nRAM use: ").append(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
            break;
        }
        exitMessage.append("\nProgram exit at: ").append(LocalDateTime.now());
        exitMessage.append("\nExit code: ").append(status).append(exitStatus).append(", reason: ").append(reason);
        exitMessage.append("\nGame time: ").append(Sun.currentTime);
        exitMessage.append("\n-------- Log ended --------");

        return exitMessage;
    }

    public static void logExit(int status) {
        logExit(status, null, true);
    }

    public static void logStart() {
        if (getFromConfig("InterceptionErrors").equals("true")) {
            System.setErr(new Logger());
        }
        Json.detectLanguage();

        StringBuilder message = getStartMessage();
        StringBuilder computerInfo = getComputerInfo();

        log(message.toString() + computerInfo);
    }

    private static StringBuilder getComputerInfo() {
        StringBuilder computerInfo = new StringBuilder();

        computerInfo.append("\nComputer info: ");
        computerInfo.append("\nScreen resolution: ").append(Toolkit.getDefaultToolkit().getScreenSize().width).append(" x ").append(Toolkit.getDefaultToolkit().getScreenSize().height);
        computerInfo.append("\nCPU: ").append(System.getenv("PROCESSOR_IDENTIFIER"));
        computerInfo.append("\nCPU threads count: " ).append(Runtime.getRuntime().availableProcessors());
        computerInfo.append("\nRAM: ").append(ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getTotalMemorySize() / (1024 * 1024)).append("\n");

        return computerInfo;
    }

    private static StringBuilder getStartMessage() {
        StringBuilder message = new StringBuilder();

        String os = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");

        String identifier = os.toLowerCase(Locale.ROOT).replaceAll(" ", "");
        String identifierVersion = osVersion.toLowerCase(Locale.ROOT).replaceAll(" ", "");

        if (!testedSystems.contains(identifierVersion) && !testedSystems.contains(identifier)) {
            message.append("Warning: '").append(os).append("' with version '").append(osVersion).append("' not tested!\n");
        }
        message.append("\nGLFW version: ").append(glfwGetVersionString());
        message.append("\nGame version: " + Window.version);
        message.append("\nStart time: ").append(LocalDateTime.now());
        message.append("\n\nPreload resources: ").append(getFromConfig("PreloadResources"));
        message.append("\nVertical sync: ").append(Config.getFromConfig("VerticalSync")).append(" (").append(verticalSync).append(")");
        message.append("\nCurrent language: ").append(getFromConfig("Language"));
        message.append("\nAvailable languages: ").append(Json.getAllLanguages().replace(" ", ", ")).append("\n");

        return message;
    }
}
