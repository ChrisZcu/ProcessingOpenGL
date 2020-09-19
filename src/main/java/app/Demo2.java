package app;

import cn.siat.vcc.util.MathUtil;
import cn.siat.vcc.util.math.Vec2;
import cn.siat.vcc.util.math.Vec4;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.MapBox;
import de.fhpotsdam.unfolding.utils.MapUtils;
import model.TrafficMovement;
import model.Trajectory;
import org.lwjgl.opengl.GL11;
import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;
import processing.opengl.VertexBuffer;
import util.IOHandle;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Demo2 extends PApplet {
    private Trajectory[] trajFull;
    private UnfoldingMap map;
    private static final Location PORTO_CENTER = new Location(41.14, -8.639);//维度经度
    private static final Location PRESENT = PORTO_CENTER;
    private static final int ZOOM_LEVEL = 11;

    //map control
    private int checkLevel = -1;
    private Location checkCenter = new Location(0, 0);
    private boolean totalLoad = false;
    private PImage mapImage = null;

    boolean dataLoadDone = false;
    TrafficMovement tm = new TrafficMovement();
    int shaderProgram;
    int vertShader;
    int fragShader;

    @Override
    public void settings() {
        size(1000, 800, P2D);
    }

    @Override
    public void setup() {

        String WHITE_MAP_PATH = "https://api.mapbox.com/styles/v1/pacemaker-yc/ck4gqnid305z61cp1dtvmqh5y/tiles/512/{z}/{x}/{y}@2x?access_token=pk.eyJ1IjoicGFjZW1ha2VyLXljIiwiYSI6ImNrNGdxazl1aTBsNDAzZW41MDhldmQyancifQ.WPAckWszPCEHWlyNmJfY0A";


        map = new UnfoldingMap(this, new MapBox.CustomMapBoxProvider(WHITE_MAP_PATH));
        map.setZoomRange(1, 20);
        map.zoomAndPanTo(ZOOM_LEVEL, PRESENT);
        map.setBackgroundColor(255);


        gl3 = ((PJOGL) beginPGL()).gl.getGL3();
        endPGL();

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
                                "  gl_FragColor = vertColor;\n" +
                                "}"
                }, null);
        gl3.glCompileShader(fragShader);

        vertShader = gl3.glCreateShader(GL3.GL_VERTEX_SHADER);
        gl3.glShaderSource(vertShader, 1, new String[]{"uniform mat4 transform;\n" +
                "uniform vec4 viewport;\n" +
                "\n" +
                "attribute vec4 position;\n" +
                "attribute vec4 color;\n" +
                "attribute vec4 direction;\n" +
                "\n" +
                "varying vec4 vertColor;\n" +
                "\n" +
                "vec3 clipToWindow(vec4 clip, vec4 viewport) {\n" +
                "  vec3 dclip = clip.xyz / clip.w;\n" +
                "  vec2 xypos = (dclip.xy + vec2(1.0, 1.0)) * 0.5 * viewport.zw;\n" +
                "  return vec3(xypos, dclip.z * 0.5 + 0.5);\n" +
                "}\n" +
                "\n" +
                "void main() {\n" +
                "  vec4 clip0 = transform * position;\n" +
                "  vec4 clip1 = clip0 + transform * vec4(direction.xyz, 0);\n" +
                "  float thickness = direction.w;\n" +
                "\n" +
                "  vec3 win0 = clipToWindow(clip0, viewport);\n" +
                "  vec3 win1 = clipToWindow(clip1, viewport);\n" +
                "  vec2 tangent = win1.xy - win0.xy;\n" +
                "\n" +
                "  vec2 normal = normalize(vec2(-tangent.y, tangent.x));\n" +
                "  vec2 offset = normal * thickness;\n" +
                "  gl_Position.xy = clip0.xy + offset.xy;\n" +
                "  gl_Position.zw = clip0.zw;\n" +
                "  vertColor = color;\n" +
                "}"}, null);
        gl3.glCompileShader(vertShader);

        int[] compiled = new int[1];

        gl3.glGetShaderiv(fragShader, GL3.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] != 0) {
            println("Horray! fragment shader compiled");
        } else {
            int[] logLength = new int[1];
            gl3.glGetShaderiv(fragShader, GL3.GL_INFO_LOG_LENGTH, logLength, 0);
            byte[] log = new byte[logLength[0]];
            gl3.glGetShaderInfoLog(fragShader, logLength[0], (int[]) null, 0, log, 0);
            println("Error compiling the fragment shader: " + new String(log));
            exit();
        }

        gl3.glGetShaderiv(vertShader, GL3.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] != 0) {
            System.out.println("Horray! vertex shader compiled");
        } else {
            int[] logLength = new int[1];
            gl3.glGetShaderiv(vertShader, GL3.GL_INFO_LOG_LENGTH, logLength, 0);
            byte[] log = new byte[logLength[0]];
            gl3.glGetShaderInfoLog(vertShader, logLength[0], (int[]) null, 0, log, 0);
            println("Error compiling the vertex shader: " + new String(log));
            exit();
        }

        gl3.glAttachShader(shaderProgram, vertShader);
        gl3.glAttachShader(shaderProgram, fragShader);
        gl3.glLinkProgram(shaderProgram);

        // program compiled we can free the object
        gl3.glDeleteShader(vertShader);
        gl3.glDeleteShader(fragShader);

        loadData();
        tm.movementInit(trajFull, map);

        init();

    }


    @Override
    public void draw() {
        gl3.glClearColor(255f, 255f, 255f, 1f);
        gl3.glClear(GL3.GL_COLOR_BUFFER_BIT);

        gl3.glUseProgram(shaderProgram);

        gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboHandles[0]);
        gl3.glEnableVertexAttribArray(0);
        gl3.glEnableVertexAttribArray(1);

        gl3.glVertexAttribPointer(0, 2, GL3.GL_FLOAT, false, 0, 0);
        gl3.glVertexAttribPointer(1, 2, GL3.GL_FLOAT, false, 0, 48);

        gl3.glDrawArrays(GL3.GL_LINES, 0, 3);

        gl3.glDisableVertexAttribArray(0);
        gl3.glDisableVertexAttribArray(1);

    }

    int[] vboHandles;
    GL3 gl3;
    IntBuffer vao = GLBuffers.newDirectIntBuffer(1);

    private void init() {
        FloatBuffer vertexDataBuffer = GLBuffers.newDirectFloatBuffer(tm.getFloatBuffer());
        vboHandles = new int[1];
        gl3.glGenBuffers(1, vboHandles, 0);
        gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboHandles[0]);
        gl3.glBufferData(GL3.GL_ARRAY_BUFFER, vertexDataBuffer.capacity() * 4, vertexDataBuffer, GL3.GL_STATIC_DRAW);
        gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
        vertexDataBuffer = null;

        gl3.glGenVertexArrays(1, vao);
        gl3.glBindVertexArray(vao.get(0));
    }

    private void loadData() {
        System.out.println("loading...");
        String filePath = "data/Porto5w.txt";
//        String filePath = "data/porto_full.txt";
        trajFull = IOHandle.loadRowData(filePath, -1);
        dataLoadDone = true;
        System.out.println("loadDone, total number: " + trajFull.length);
    }


    public static void main(String[] args) {
        PApplet.main(new String[]{Demo2.class.getName()});
    }
}
