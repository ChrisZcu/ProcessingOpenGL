package model;

import cn.siat.vcc.util.MathUtil;
import cn.siat.vcc.util.math.Vec2;
import cn.siat.vcc.util.math.Vec3;
import cn.siat.vcc.util.math.Vec4;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import processing.core.PVector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class TrafficMovement {
    private List<Vec2[]> movements;

    public List<Vec2[]> getMovements() {
        return movements;
    }

    int totalSizel = 0;

    public void movementInit(Trajectory[] trajFull, UnfoldingMap map) {
        movements = new ArrayList<>();
        for (Vec2[] vec2List : metaData) {
            ArrayList<Vec2> new_mov = new ArrayList<>();
            for (Vec2 loc : vec2List) {
                ScreenPosition scp = map.getScreenPosition(new Location(loc.x, loc.y));
                float x = scp.x;
                float y = scp.y;
                if (x > 1000 || x < 0 || y < 0 || y > 800)
                    continue;
//                Vec2 tmp = new Vec2(getNormalizeX(x, 1000), getNormalizeY(y, 800));
//                new_mov.add(tmp);
                new_mov.add(new Vec2(x, y));
                totalSizel++;
            }
            Vec2[] new_mov2 = new Vec2[new_mov.size()];
            int k = 0;
            for (Vec2 vec : new_mov) {
                new_mov2[k++] = vec;
            }
            movements.add(new_mov2);
        }
        metaData.clear();
        System.out.println("movement init done, total size: " + movements.size());
    }

    public float[] getFloatBuffer() {
        int line_count = 0;
        for (Vec2[] path : movements)
            line_count += (path.length - 1);

        System.out.println(totalSizel);
        System.out.println(line_count);
        System.out.println(line_count * 2 * 2);
        float[] vertexData = new float[line_count * 2 * 2];
        int j = 0;
        for (Vec2[] path : movements) {
            for (int i = 0; i < path.length - 2; i++) {
//                Vec2 p0 = normalize(bound, path[i]), p1 = normalize(bound, path[i + 1]);
                Vec2 p0 = path[i];
                Vec2 p1 = path[i + 1];
                vertexData[j++] = p0.x;
                vertexData[j++] = p0.y;
                vertexData[j++] = p1.x;
                vertexData[j++] = p1.y;
            }
        }
        return vertexData;
    }

    Vec4 bound = new Vec4(-8.77824f, 39.712276f, -7.454304f, 41.73824f);

    private Vec2 normalize(Vec4 bound, Vec2 xy) {
        return new Vec2(MathUtil.map(xy.x, bound.x, bound.z, -1f, 1f), MathUtil.map(xy.y, bound.y, bound.w, -1f, 1f));
    }

    private float getNormalizeX(float x, int width) {
        return (float) (-1.0 + 2.0 * (double) x / width);
    }

    private float getNormalizeY(float y, int height) {
        return (float) (1.0 - 2.0 * (double) y / height);
    }

    private List<Vec2[]> metaData = new ArrayList<>();

    public void initMetaData(String filePath, int limit) {
        LineIterator it = null;
        int cnt = 0;

        try {
            it = FileUtils.lineIterator(new File(filePath), "UTF-8");

            while (it.hasNext() && (limit == -1 || cnt < limit)) {
                String line = it.nextLine();
                String[] item = line.split(";");
                String[] data = item[item.length - 1].split(",");
                Vec2[] tmpVec2 = new Vec2[data.length / 2 - 1];
                int j = 0;
                for (int i = 0; i < data.length - 2; i = i + 2) {      // FIXME
                    // the longitude and latitude are reversed
                    tmpVec2[j++] = new Vec2(Float.parseFloat(data[i + 1]), Float.parseFloat(data[i]));
                }
                ++cnt;
                metaData.add(tmpVec2);
            }
            System.out.println("\b\b\bfinished.");

        } catch (IOException | NoSuchElementException e) {
            System.out.println("\b\b\bfailed. \nProblem line: " + cnt);
            e.printStackTrace();
        } finally {
            if (it != null) {
                LineIterator.closeQuietly(it);
            }
        }
    }
}
