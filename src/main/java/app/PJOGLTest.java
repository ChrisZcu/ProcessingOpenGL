package app;

import com.jogamp.opengl.util.GLBuffers;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL3;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.MapBox;
import de.fhpotsdam.unfolding.utils.MapUtils;
import model.TrafficMovement;
import model.Trajectory;
import org.lwjgl.Sys;
import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PJOGL;
import util.IOHandle;

public class PJOGLTest extends PApplet {
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

    public void settings() {
        size(1000, 800, P2D);
        PJOGL.profile = 3;
    }

    long bufferDone = 0;

    public void setup() {

        //map
        String WHITE_MAP_PATH = "https://api.mapbox.com/styles/v1/pacemaker-yc/ck4gqnid305z61cp1dtvmqh5y/tiles/512/{z}/{x}/{y}@2x?access_token=pk.eyJ1IjoicGFjZW1ha2VyLXljIiwiYSI6ImNrNGdxazl1aTBsNDAzZW41MDhldmQyancifQ.WPAckWszPCEHWlyNmJfY0A";


        map = new UnfoldingMap(this, new MapBox.CustomMapBoxProvider(WHITE_MAP_PATH));
        map.setZoomRange(1, 20);
        map.zoomAndPanTo(ZOOM_LEVEL, PRESENT);
        map.setBackgroundColor(255);
        MapUtils.createDefaultEventDispatcher(this, map);

        tm = new TrafficMovement();
        String filePath = "data/Porto5w.txt";
//        String filePath = "data/porto_full.txt";
        long bufferInitTime = System.currentTimeMillis();
        tm.initMetaData(filePath, -1);
        tm.movementInit(trajFull, map);
        vertexData = tm.getFloatBuffer();
        bufferDone = System.currentTimeMillis();
        System.out.println("buffer init time: " + (bufferDone - bufferInitTime));

        gl3 = ((PJOGL) beginPGL()).gl.getGL3();
        endPGL();//?

        // initializeProgram
        shaderProgram = gl3.glCreateProgram();

        fragShader = gl3.glCreateShader(GL3.GL_FRAGMENT_SHADER);
        gl3.glShaderSource(fragShader, 1,
                new String[]{//决定颜色
                        "#ifdef GL_ES\n" +
                                "precision mediump float;\n" +
                                "precision mediump int;\n" +
                                "#endif\n" +
                                "\n" +
                                "varying vec4 vertColor;\n" +
                                "\n" +
                                "void main() {\n" +
                                "  gl_FragColor = vertColor;\n" +
                                "}"
                }, null);
        gl3.glCompileShader(fragShader);

        vertShader = gl3.glCreateShader(GL3.GL_VERTEX_SHADER);
        gl3.glShaderSource(vertShader, 1,
                new String[]{//序列化
                        "#version 330 \n"
                                + "layout (location = 0) in vec4 position;"
                                + "layout (location = 1) in vec4 color;"
                                + "smooth out vec4 theColor;"
                                + "void main(){"
                                + "gl_Position = position;"
                                + "theColor = color;"
                                + "}"
                }, null);
        gl3.glCompileShader(vertShader);


//        int[] compiled = new int[1];

        // Check compile status fragShader
//        gl3.glGetShaderiv(fragShader, GL3.GL_COMPILE_STATUS, compiled, 0);
//        // Check compile status vertShader
//        gl3.glGetShaderiv(vertShader, GL3.GL_COMPILE_STATUS, compiled, 0);

        // attach and link
        gl3.glAttachShader(shaderProgram, vertShader);
        gl3.glAttachShader(shaderProgram, fragShader);
        gl3.glLinkProgram(shaderProgram);

        // program compiled we can free the object
        gl3.glDeleteShader(vertShader);
        gl3.glDeleteShader(fragShader);

// set up vertex Data to display

        // initializeVertexBuffer

    }

    boolean png = false;

    @Override
    public void keyPressed() {
        if (key == 'q') {
            png = true;
            loop();
        }
    }


    public void draw() {
//        map.draw();
        if (checkLevel != map.getZoomLevel() || !checkCenter.equals(map.getCenter())) {
            totalLoad = false;
            checkLevel = map.getZoomLevel();
            checkCenter = map.getCenter();
        }
        if (!totalLoad) {
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
            System.out.println("map done!");
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

            gl3.glDrawArrays(GL3.GL_LINES, 0, vertexData.length / 2);
            gl3.glDisableVertexAttribArray(0);
            gl3.glDisableVertexAttribArray(1);
            System.out.println("time used: " + (System.currentTimeMillis() - t0));
            System.out.println("since buffer done: " + (System.currentTimeMillis() - bufferDone));
            if (png) {
                saveFrame("data/test.png");
            }
            noLoop();
        }
    }

    TrafficMovement tm;

    public static void main(String[] args) {
        PApplet.main(new String[]{PJOGLTest.class.getName()});

    }
}