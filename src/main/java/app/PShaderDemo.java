package app;

import cn.siat.vcc.util.MathUtil;
import cn.siat.vcc.util.math.Vec2;
import cn.siat.vcc.util.math.Vec4;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.MapBox;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import model.TrafficMovement;
import model.Trajectory;
import org.lwjgl.BufferUtils;
import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.*;
import util.IOHandle;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class PShaderDemo extends PApplet {
    private Trajectory[] trajFull;
    private UnfoldingMap map;
    private static final Location PORTO_CENTER = new Location(41.14, -8.639);//维度经度
    private static final Location PRESENT = PORTO_CENTER;
    private static final int ZOOM_LEVEL = 12;

    //map control
    private int checkLevel = -1;
    private Location checkCenter = new Location(0, 0);
    private boolean totalLoad = false;
    private PImage mapImage = null;

    boolean dataLoadDone = false;
    VertexBuffer vf;
    PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;
    TrafficMovement tm = new TrafficMovement();

    @Override
    public void settings() {
        size(1000, 800, P2D);
    }

    PShader testShader;

    @Override
    public void setup() {
//        testShader = loadShader("data/shaders/line_frag.glsl", "data/shaders/line_vert.glsl");
        String WHITE_MAP_PATH = "https://api.mapbox.com/styles/v1/pacemaker-yc/ck4gqnid305z61cp1dtvmqh5y/tiles/512/{z}/{x}/{y}@2x?access_token=pk.eyJ1IjoicGFjZW1ha2VyLXljIiwiYSI6ImNrNGdxazl1aTBsNDAzZW41MDhldmQyancifQ.WPAckWszPCEHWlyNmJfY0A";

        (new Thread(this::loadData)).start();

        map = new UnfoldingMap(this, new MapBox.CustomMapBoxProvider(WHITE_MAP_PATH));
        map.setZoomRange(1, 20);
        map.zoomAndPanTo(ZOOM_LEVEL, PRESENT);
        map.setBackgroundColor(255);
        MapUtils.createDefaultEventDispatcher(this, map);

    }

    private FloatBuffer buffer;
    private boolean bufferInit = true;

    @Override
    public void draw() {
//        shader(testShader, LINES);

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
            if (dataLoadDone) {
                if (bufferInit) {
                    bufferInit = false;
                    tm.movementInit(trajFull, map);
                    init();
                }
                //颜色混合
                gl.glEnable(PGL.BLEND);
                gl.glBlendFunc(PGL.SRC_ALPHA, PGL.ONE_MINUS_DST_COLOR);

                gl.glDrawArrays(PGL.LINES, 0, buffer.capacity() / 2);

                gl.glDisable(PGL.BLEND);
//                noLoop();

                /*
                long t1 = System.currentTimeMillis();
                noFill();
                stroke(255, 0, 0);
                strokeWeight(1);
                long mapTime = 0l;
//                for (int j = 0; j < 20; j++) {
//                    int bound = j == 19 ? trajFull.length : (j + 1) * 100000;
//                    beginShape(LINES);
//                    for (int k = j * 100000; k < bound; k++) {
//                        Trajectory traj = trajFull[k];
////                        printMessage(k);
//                        //TODO buffer
//                        for (int i = 0; i < traj.locations.length - 1; i++) {
//                            Location loc1 = traj.locations[i];
//                            Location loc2 = traj.locations[i + 1];
//                            long mapT0 = System.currentTimeMillis();
//                            ScreenPosition pos1 = map.getScreenPosition(loc1);
//                            ScreenPosition pos2 = map.getScreenPosition(loc2);
//                            mapTime += System.currentTimeMillis() - mapT0;
//                            vertex(pos1.x, pos1.y);
//                            vertex(pos2.x, pos2.y);
//                        }
//                    }
//                    endShape();
//
//                }
//                beginShape(LINES);
//                for (Trajectory traj : trajFull) {
////                        printMessage(k);
//                    //TODO buffer
//                    for (int i = 0; i < traj.locations.length - 1; i++) {
//                        Location loc1 = traj.locations[i];
//                        Location loc2 = traj.locations[i + 1];
//                        long mapT0 = System.currentTimeMillis();
//                        ScreenPosition pos1 = map.getScreenPosition(loc1);
//                        ScreenPosition pos2 = map.getScreenPosition(loc2);
//                        mapTime += System.currentTimeMillis() - mapT0;
//                        vertex(pos1.x, pos1.y);
//                        vertex(pos2.x, pos2.y);
//                    }
//                }
//                long bufferTime = System.currentTimeMillis();
//                endShape();
                for (int i = 0; i < trajFull.length; i++) {
                    Trajectory traj = trajFull[i];
//                    beginContour();
                    beginShape();

                    for (Location loc : traj.locations) {
                        long mapT0 = System.currentTimeMillis();
                        ScreenPosition pos = map.getScreenPosition(loc);
                        mapTime += System.currentTimeMillis() - mapT0;

                        vertex(pos.x, pos.y);
                    }
//                    endContour();

                    long bufferTime = System.currentTimeMillis();
                    endShape();
                }
                long t2 = System.currentTimeMillis();

//                System.out.println("buffer test time: " + (System.currentTimeMillis() - bufferTime) + "ms");
                System.out.println("time map: " + mapTime + "ms");
                System.out.println("time render: " + (t2 - t1) + "ms");
                noLoop();
                */
            }
        }
        endShape();
    }

    PSurfaceJOGL pj;
    PJOGL pjogl;
    GL4 gl;
    private IntBuffer bufferI;

    private void init() {
        Vec4 bound = new Vec4(-8.77824f, 39.712276f, -7.454304f, 41.73824f);

//        if (surface instanceof PSurfaceJOGL) {
//            System.out.println(1);
//            pj = (PSurfaceJOGL) surface;
//        }
        pjogl = (PJOGL)beginPGL();
        int line_count = 0;
        for (Vec2[] path : tm.getMovements())
            line_count += (path.length - 1);

        // 声明buffer
        buffer = FloatBuffer.allocate(line_count * 2 * 2);
        for (Vec2[] path : tm.getMovements()) {
            for (int i = 0; i < path.length - 1; i++) {
                Vec2 p0 = normalize(bound, path[i]), p1 = normalize(bound, path[i + 1]);

//                Vec2 p0 = path[i], p1 = path[i + 1];
//                if (p0.x > 1 || p0.x < -1 || p0.y > 1 || p0.y < -1) {
//                    System.out.println("!!!!!");
//                }
                buffer.put(p0.x).put(p0.y);
                buffer.put(p1.x).put(p1.y);
            }
        }
        buffer.flip();

        System.out.println("buffer done.");

        System.out.println((buffer.remaining() << 2));
        System.out.println(buffer.capacity());
        System.out.println(buffer.capacity() * Float.SIZE / 2);

        bufferI = IntBuffer.allocate(1);
        gl = pjogl.gl.getGL4();
        gl.glGenBuffers(1, bufferI);

        int index = bufferI.get(0);
        gl.glBindBuffer(PGL.ARRAY_BUFFER, index);
        gl.glBufferData(PGL.ARRAY_BUFFER, buffer.capacity() * Buffers.SIZEOF_FLOAT, buffer, PGL.STATIC_DRAW);

        gl.glEnableVertexAttribArray(index);
        gl.glVertexAttribPointer(index, 2, PGL.FLOAT, false, 0, 0);

    }

    private void loadData() {
        System.out.println("loading...");
        String filePath = "data/Porto5w.txt";
//        String filePath = "data/porto_full.txt";
        trajFull = IOHandle.loadRowData(filePath, -1);
        dataLoadDone = true;
        System.out.println("loadDone, total number: " + trajFull.length);
    }

    private Vec2 normalize(Vec4 bound, Vec2 xy) {
        return new Vec2(MathUtil.map(xy.x, bound.x, bound.z, -1f, 1f), MathUtil.map(xy.y, bound.y, bound.w, -1f, 1f));
    }

    public void printMessage(int index) {
        new Thread() {
            @Override
            public void run() {
                System.out.println(index);
            }
        }.start();
    }

    public static void main(String[] args) {
        PApplet.main(new String[]{PShaderDemo.class.getName()});

    }
}
