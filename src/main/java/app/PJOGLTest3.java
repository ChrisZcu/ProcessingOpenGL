package app;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.MapBox;
import de.fhpotsdam.unfolding.utils.MapUtils;
import model.TrafficMovement;
import model.Trajectory;
import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PJOGL;
import util.IOHandle;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

/**
 * Test the resize algorithm
 */
public class PJOGLTest3 extends PApplet {
    public static final String DATA_PATH
            = "C:\\LocalDocument\\LocalCode\\DBGroup\\DemoSystem\\data\\GPS\\porto_full.txt";
    public static final int LIMIT = 5_0000;

    private Trajectory[] trajFull;
    GL3 gl3;
    private UnfoldingMap map;
    private static final Location PORTO_CENTER = new Location(41.14, -8.639);//维度经度
    private static final Location PRESENT = PORTO_CENTER;
    private static final int ZOOM_LEVEL = 11;

    //map control
    private int checkLevel = -1;
    private Location checkCenter = new Location(0, 0);
    private boolean totalLoad = false;
    private PImage mapImage = null;

    int[] vboHandles;
    int shaderProgram, vertShader, fragShader;
    int vertexBufferObject;

    IntBuffer vao = GLBuffers.newDirectIntBuffer(1);
    float[] vertexData = {};

    @Override
    public void settings() {
        size(1000, 800, P2D);
        PJOGL.profile = 3;
    }

    long bufferDone = 0;

    @Override
    public void setup() {
        //map
        String WHITE_MAP_PATH = "https://api.mapbox.com/styles/v1/pacemaker-yc/ck4gqnid305z61cp1dtvmqh5y/tiles/512/{z}/{x}/{y}@2x?access_token=pk.eyJ1IjoicGFjZW1ha2VyLXljIiwiYSI6ImNrNGdxazl1aTBsNDAzZW41MDhldmQyancifQ.WPAckWszPCEHWlyNmJfY0A";


        map = new UnfoldingMap(this, new MapBox.CustomMapBoxProvider(WHITE_MAP_PATH));
        map.setZoomRange(1, 20);
        map.zoomAndPanTo(ZOOM_LEVEL, PRESENT);
        map.setBackgroundColor(255);
        MapUtils.createDefaultEventDispatcher(this, map);

        tm = new TrafficMovement();

        long t0 = System.currentTimeMillis();
        List<String> lineList = IOHandle.readAllLines(DATA_PATH, LIMIT);
        long t1 = System.currentTimeMillis();
        trajFull = tm.lineStrToTraj(lineList);
        long t2 = System.currentTimeMillis();
        tm.movementInit(trajFull, map);
        long t3 = System.currentTimeMillis();
        vertexData = tm.getFloatBuffer();

        bufferDone = System.currentTimeMillis();

        System.out.println("\ndisk -> mem : " + (t1 - t0));
        System.out.println("str -> gps point : " + (t2 - t1));
        System.out.println("gps point -> screen point : " + (t3 - t2));
        System.out.println("screen point -> buffer : " + (bufferDone - t3));

        gl3 = ((PJOGL) beginPGL()).gl.getGL3();
        endPGL();//?
    }

    private void shaderInit() {
        // initializeProgram

        shaderProgram = gl3.glCreateProgram();

        fragShader = gl3.glCreateShader(GL3.GL_FRAGMENT_SHADER);
        gl3.glShaderSource(fragShader, 1,
                new String[]{
                        "#ifdef GL_ES\n" +
                                "precision mediump float;\n" +
                                "precision mediump int;\n" +
                                "#endif\n" +
                                "\n" +
                                "varying vec4 vertColor;\n" +
                                "\n" +
                                "void main() {\n" +
                                "  gl_FragColor = vec4(1.0,0.0,0.0,1.0);\n" +
                                "}"
                }, null);
        gl3.glCompileShader(fragShader);

        vertShader = gl3.glCreateShader(GL3.GL_VERTEX_SHADER);
        gl3.glShaderSource(vertShader, 1,
                new String[]{
                        "#version 330 \n"
                                + "layout (location = 0) in vec4 position;"
                                + "layout (location = 1) in vec4 color;"
                                + "smooth out vec4 theColor;"
                                + "void main() {"
                                + "mat4 m4 = mat4(1,0,0,0,  0,1,0,0,  0,0,1,0,  -0.4,0,0,1);"
                                + "gl_Position.x = position.x / 500.0 - 1;"
                                + "gl_Position.y = -1 * position.y / 400.0 + 1;"
                                + "theColor = color;"
                                + "}"
                }, null);
        gl3.glCompileShader(vertShader);
        gl3.glGetAttribLocation(vertShader, "");


        // attach and link
        gl3.glAttachShader(shaderProgram, vertShader);
        gl3.glAttachShader(shaderProgram, fragShader);
        gl3.glLinkProgram(shaderProgram);

        // program compiled we can free the object
        gl3.glDeleteShader(vertShader);
        gl3.glDeleteShader(fragShader);

    }

    boolean png = false;

    @Override
    public void keyPressed() {
        if (key == 'q') {
            png = true;
            loop();
        }
    }


    @Override
    public void draw() {
//        map.draw();
        if (checkLevel != map.getZoomLevel() || !checkCenter.equals(map.getCenter())) {
            totalLoad = false;
            checkLevel = map.getZoomLevel();
            checkCenter = map.getCenter();
        }
        if (totalLoad) {
            if (!map.allTilesLoaded()) {
                if (mapImage == null) {
                    mapImage = map.mapDisplay.getInnerPG().get();
                }
                image(mapImage, 0, 0);
            } else {
                totalLoad = true;
            }
            map.draw();
        } else {
//            map.draw();
            shaderInit();
            long t0 = System.currentTimeMillis();
            FloatBuffer vertexDataBuffer = GLBuffers.newDirectFloatBuffer(vertexData);


            vboHandles = new int[1];
            gl3.glGenBuffers(1, vboHandles, 0);

            gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboHandles[0]);
            gl3.glBufferData(GL3.GL_ARRAY_BUFFER, vertexDataBuffer.capacity() * 4, vertexDataBuffer, GL3.GL_STATIC_DRAW);
            gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
            vertexDataBuffer = null;

            gl3.glGenVertexArrays(1, vao);
            gl3.glBindVertexArray(vao.get(0));

            gl3.glUseProgram(shaderProgram);

            gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboHandles[0]);
            gl3.glEnableVertexAttribArray(0);
            gl3.glEnableVertexAttribArray(1);
            gl3.glVertexAttribPointer(0, 2, GL3.GL_FLOAT, false, 0, 0);
            gl3.glVertexAttribPointer(1, 2, GL3.GL_FLOAT, false, 0, 0);

            long t1 = System.currentTimeMillis();

            gl3.glDrawArrays(GL3.GL_LINES, 0, vertexData.length / 2);
            gl3.glDisableVertexAttribArray(0);
            gl3.glDisableVertexAttribArray(1);

            long t2 = System.currentTimeMillis();

            System.out.println("mem buf -> gpu mem : " + (t1 - t0));
            System.out.println("rendering : " + (t2 - t1));
            System.out.println("\nsince buffer done: " + (System.currentTimeMillis() - bufferDone));
            if (png) {
                saveFrame("data/test.png");
            }

            exit();
            noLoop();
        }
    }

    TrafficMovement tm;

    public static void main(String[] args) {
        PApplet.main(new String[]{PJOGLTest3.class.getName()});

    }
}