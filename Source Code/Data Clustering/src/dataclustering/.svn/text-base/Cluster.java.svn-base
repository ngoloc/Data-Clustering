/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataclustering;

import java.util.Vector;

/**
 *
 * @author Thuan Loc
 */
public class Cluster {

    Vector<Point3D> V;
    Point3D Centroid;
    double SSE;

    Cluster(Point3D Centroid) {
        this.V = new Vector<Point3D>();
        this.Centroid = Centroid;
    }

    Cluster(Vector<Point3D> V) {
        this.V = V;
        double x = 0;
        double y = 0;
        double z = 0;
        for (int i = 0; i < V.size(); i++) {
            x += V.elementAt(i).x;
            y += V.elementAt(i).y;
            z += V.elementAt(i).z;
        }
        x /= V.size();
        y /= V.size();
        z /= V.size();
        Centroid = new Point3D(x, y, z);
    }
}
