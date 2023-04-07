package core;

import core.EventHandling.MouseScrollCallback;
import core.World.MainMenu;
import core.World.Textures.TextureLoader;
import core.World.WorldGenerator;
import core.World.WorldObjects;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import static java.sql.Types.NULL;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT;

public class Window {
    public final int width;
    public final int height;
    private final String title;
    public static long glfwWindow;
    private static Window window;
    private static Physics thread = new Physics();

    public Window() {
        //чекает размеры твоего экрана
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.width = dim.width;
        this.height = dim.height;
        this.title = "-270 on Celsius";
    }

    public static Window get() {
        //если окно не существует (null), то оно создается
        if (Window.window == null) {
            Window.window = new Window();
        }
        return Window.window;
    }

    public void run() {
        init();
        draw();
    }

    public void init() {
        //инициализирует библиотеку
        glfwInit();
        System.out.println("'glfw' has been initialized");
        GLFWErrorCallback.createPrint(System.err).set();
        if (glfwWindow == NULL) {
            //если окна не существует - создаст
            glfwGetCurrentContext();
            glfwWindow = glfwCreateWindow(this.width, this.height, this.title, glfwGetPrimaryMonitor(), NULL);
        } else {
            //при попытке создания окна возникла ошибка - сообщит
            System.err.println("error at create glfwWindow");
        }
        glfwMakeContextCurrent(glfwWindow);
        //vsync
        glfwSwapInterval(1);
        //настройка отображения
        glfwShowWindow(glfwWindow);
        //подключает инструменты библиотеки
        GL.createCapabilities();
        System.out.println(glfwGetVersionString());

        glMatrixMode(GL_PROJECTION);
        glOrtho(0, width, 0, height, 1, -1);
        glMatrixMode(GL_MODELVIEW);

        MainMenu.Create();
        glfwSetScrollCallback(glfwWindow, new MouseScrollCallback());
    }

    public void draw() {
        glEnable(GL_DEBUG_OUTPUT);
        float cameraX = 1f;
        float cameraY = 1f;
        float zoom = 4f;
        boolean start = false;

        WorldGenerator.generateDynamicsObjects();
        WorldGenerator.generateStaticObjects(1000, 20);
        WorldObjects[][] objects = WorldGenerator.StaticObjects;

        glfwSwapBuffers(glfwWindow);
        glClear(GL_COLOR_BUFFER_BIT);

        //пока окно не закрыто
        while (!glfwWindowShouldClose(glfwWindow)) {
            //alt
            if(glfwGetKey(glfwWindow, 342) == 1){
                System.exit(0);
            }

            if (start) {
                glClear(GL_COLOR_BUFFER_BIT);
                for (int x = 0; x < objects.length - 1; x++) {
                    for (int y = 0; y < objects[x].length - 1; y++) {

                        float left = cameraX - width / zoom;
                        float right = cameraX + width / zoom;
                        float top = cameraY - height / zoom;
                        float bottom = cameraY + height / zoom;

                        if (objects[x][y].x < left || objects[x][y].x > right || objects[x][y].y < top || objects[x][y].y > bottom) {
                            objects[x][y].onCamera = false;
                        } else {
                            objects[x][y].onCamera = true;
                        }

                        if (glfwGetKey(glfwWindow, GLFW_KEY_1) == GLFW_PRESS) zoom += 0.000005f;
                        if (glfwGetKey(glfwWindow, GLFW_KEY_2) == GLFW_PRESS) zoom -= 0.000005f;
                        if (glfwGetKey(glfwWindow, GLFW_KEY_D) == GLFW_PRESS) cameraX += 0.0057f;
                        if (glfwGetKey(glfwWindow, GLFW_KEY_A) == GLFW_PRESS) cameraX -= 0.0057f;
                        if (glfwGetKey(glfwWindow, GLFW_KEY_W) == GLFW_PRESS) cameraY += 0.0057f;
                        if (glfwGetKey(glfwWindow, GLFW_KEY_S) == GLFW_PRESS) cameraY -= 0.0057f;

                        if (objects[x][y].onCamera) {
                            glPushMatrix();
                            glEnable(GL_TEXTURE_2D);
                            glEnable(GL_BLEND);
                            glTranslatef(-cameraX, -cameraY, 0);
                            glScalef(zoom, zoom, 0);

                            ByteBuffer buffer = TextureLoader.ByteBufferEncoder(objects[x][y].path);
                            BufferedImage image = TextureLoader.BufferedImageEncoder(objects[x][y].path);

                            // параметры, бинд текстур, и прочее
                            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getHeight(), image.getWidth(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

                            // верхний левый угол
                            glBegin(GL_QUADS);
                            glTexCoord2i(0, 1);
                            glVertex2i((int) objects[x][y].x, (int) objects[x][y].y);
                            // верхний правый угол
                            glTexCoord2i(1, 1);
                            glVertex2i(image.getHeight() + (int) objects[x][y].x, (int) objects[x][y].y);
                            // нижний правый угол
                            glTexCoord2i(1, 0);
                            glVertex2i(image.getHeight() + (int) objects[x][y].x, image.getWidth() + (int) objects[x][y].y);
                            // нижний левый угол
                            glTexCoord2i(0, 0);
                            glVertex2i((int) objects[x][y].x, image.getWidth() + (int) objects[x][y].y);

                            //glVertex2i Задает вершины
                            //glTexCoord2i Задает текущие координаты текстуры

                            glEnd();
                            glDisable(GL_TEXTURE_2D);
                            glPopMatrix();
                        }
                    }
                }
                glfwSwapBuffers(glfwWindow);
            }
            //f1
            else if (glfwGetKey(glfwWindow, 290) == 1) {
                thread.start();
                start = true;
            }
            glfwPollEvents();
        }
    }
}