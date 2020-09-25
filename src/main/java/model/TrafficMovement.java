package model;

import cn.siat.vcc.util.MathUtil;
import cn.siat.vcc.util.math.Vec2;
import cn.siat.vcc.util.math.Vec4;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.ScreenPosition;

import java.util.List;

public class TrafficMovement {
    private Vec2[][] movements;
    private int totalSize = 0;

    public Vec2[][] getMovements() {
        return movements;
    }

    /**
     * Translate traj data list from the raw string lines
     */
    public Trajectory[] lineStrToTraj(List<String> lineStrList) {
        Trajectory[] ret = new Trajectory[lineStrList.size()];
        int cnt = 0;
        for (String s : lineStrList) {
            String[] item = s.split(";");
            String[] data = item[1].split(",");

            Trajectory traj = new Trajectory(cnt);
//            traj.setScore(Integer.parseInt(item[0]));

            Location[] locations = new Location[data.length / 2 - 1];
            for (int i = 0, j = 0; i < data.length - 2; i = i + 2, j++) {
                // the longitude and latitude are reversed
                locations[j] = new Location(Float.parseFloat(data[i + 1]),
                        Float.parseFloat(data[i]));
            }
            traj.setLocations(locations);
            ret[cnt] = traj;
            cnt ++;
        }
        return ret;
    }

    /**
     * Translate location info into movements (i.e. screen position).
     * This process need map.
     */
    public void movementInit(Trajectory[] trajList, UnfoldingMap map) {
        movements = new Vec2[trajList.length][];
        int idx = 0;
        for (Trajectory traj : trajList) {
            Location[] locations = traj.getLocations();
            Vec2[] newMov = new Vec2[locations.length];
            int posIdx = 0;
            for (Location loc : locations) {
                ScreenPosition scp = map.getScreenPosition(loc);
                newMov[posIdx] = new Vec2(scp.x, scp.y);
                posIdx++;
                totalSize++;
            }
            movements[idx] = newMov;
            idx++;
        }
        System.out.println("movement init done, total size: " + movements.length);
    }

    public float[] getFloatBuffer() {
        int line_count = 0;
        for (Vec2[] path : movements) {
            line_count += (path.length - 1);
        }

        System.out.println("totalSize : " + totalSize);
        System.out.println("line_count : " + line_count);
        System.out.println("line_count * 4" + (line_count * 2 * 2));
        float[] vertexData = new float[line_count * 2 * 2];
        int j = 0;
        for (Vec2[] path : movements) {
            for (int i = 0; i < path.length - 2; i++) {
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

//    private List<Vec2[]> metaData = new ArrayList<>();

//    /** @deprecated */
//    public void initMetaData(String filePath, int limit) {
//        LineIterator it = null;
//        int cnt = 0;
//
//        try {
//            it = FileUtils.lineIterator(new File(filePath), "UTF-8");
//
//            while (it.hasNext() && (limit == -1 || cnt < limit)) {
//                String line = it.nextLine();
//                String[] item = line.split(";");
//                String[] data = item[item.length - 1].split(",");
//                Vec2[] tmpVec2 = new Vec2[data.length / 2 - 1];
//                int j = 0;
//                for (int i = 0; i < data.length - 2; i = i + 2) {      // FIXME
//                    // the longitude and latitude are reversed
//                    tmpVec2[j++] = new Vec2(Float.parseFloat(data[i + 1]), Float.parseFloat(data[i]));
//                }
//                ++cnt;
//                metaData.add(tmpVec2);
//            }
//            System.out.println("\b\b\bfinished.");
//
//        } catch (IOException | NoSuchElementException e) {
//            System.out.println("\b\b\bfailed. \nProblem line: " + cnt);
//            e.printStackTrace();
//        } finally {
//            if (it != null) {
//                LineIterator.closeQuietly(it);
//            }
//        }
//    }

//    /**
//     * Init metaData from line string line (in memory)
//     */
//    public void initMetaData(List<String> lineStrList) {
//        throw new NotImplementedException();
//    }
//
//    /**
//     * Init metaData from traj list (in memory)
//     */
//    public void initMetaData(Trajectory[] trajList) {
//
//    }
}
