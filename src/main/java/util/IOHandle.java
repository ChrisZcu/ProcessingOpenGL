package util;

import de.fhpotsdam.unfolding.geo.Location;
import model.Trajectory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * IO util class
 */
public class IOHandle {

    public static Trajectory[] loadRowData(String filePath, int limit) {
        List<Trajectory> res = new ArrayList<>();
        LineIterator it = null;
        int cnt = 0;

        System.out.print("Read raw data from " + filePath + " ...");

        try {
            it = FileUtils.lineIterator(new File(filePath), "UTF-8");

            long loadTime = -System.currentTimeMillis();

            while (it.hasNext() && (limit == -1 || cnt < limit)) {
                String line = it.nextLine();
                String[] item = line.split(";");
                String[] data = item[item.length - 1].split(",");

                Trajectory traj = new Trajectory(cnt);
                if (item.length == 2) {
                    // when data contain "score;"
                    traj.setScore(Integer.parseInt(item[0]));
                }
                ArrayList<Location> locations = new ArrayList<>();
                for (int i = 0; i < data.length - 2; i = i + 2) {      // FIXME
                    // the longitude and latitude are reversed
                    locations.add(new Location(Float.parseFloat(data[i + 1]),
                            Float.parseFloat(data[i])));
                }
                traj.setLocations(locations.toArray(new Location[0]));
                res.add(traj);
                ++cnt;
            }

            loadTime += System.currentTimeMillis();

            System.out.println("\b\b\bfinished.");
            System.out.println(">>> data -> mem raw : " + loadTime + " ms");

        } catch (IOException | NoSuchElementException e) {
            System.out.println("\b\b\bfailed. \nProblem line: " + cnt);
            e.printStackTrace();
        } finally {
            if (it != null) {
                LineIterator.closeQuietly(it);
            }
        }

        return res.toArray(new Trajectory[0]);
    }

}
