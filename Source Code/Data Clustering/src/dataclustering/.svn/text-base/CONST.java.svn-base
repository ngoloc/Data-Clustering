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
public class CONST {

    static final double INF = (double) 2000000000 * (double) 2000000000;
    static final int EUCLIDEAN = 1;
    static final int MANHATTAN = 2;
    static final int BASIC_KMEANS = 1;
    static final int EM_KMEANS = 2;
    static final int BISECTING_KMEANS = 3;
    static final int K_MEDOIDS = 4;
    static boolean STOP;

    static double Distance(Point3D p1, Point3D p2, int DistanceType) {
        switch (DistanceType) {
            case EUCLIDEAN:
                return Math.sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y) + (p2.z - p1.z) * (p2.z - p1.z));
            default:
                return Math.abs(p2.x - p1.x) + Math.abs(p2.y - p1.y) + Math.abs(p2.z - p1.z);
        }
    }

    static double calSSE(Cluster C, int DistanceType) {
        double sum = 0;
        for (int i = 0; i < C.V.size(); i++) {
            sum += Distance(C.V.elementAt(i), C.Centroid, CONST.EUCLIDEAN) * Distance(C.V.elementAt(i), C.Centroid, DistanceType);
        }
        return sum;
    }

    static Point3D getCentroid(Vector<Point3D> V) {
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
        return new Point3D(x, y, z);
    }

    static boolean Equal(Point3D p1, Point3D p2, int DistanceType) {
        switch (DistanceType) {
            case EUCLIDEAN:
                if (Distance(p1, p2, EUCLIDEAN) < 0.001) {
                    return true;
                }
                return false;
            default://MANHATTAN
                if (Distance(p1, p2, MANHATTAN) < 0.005) {
                    return true;
                }
                return false;
        }
    }

    static double SumOfError(int[] M,int[] B,boolean[] IsMedoids,int K,Vector<Point3D> V,int DistanceType)
    {
        double SOE = 0;
        for(int i=0;i<V.size();i++)
            if(!IsMedoids[i])
            {
                double MinDis = INF;
                for(int j=0;j<K;j++)
                {
                    if(MinDis>Distance(V.elementAt(i),V.elementAt(M[j]),DistanceType))
                    {
                        MinDis=Distance(V.elementAt(i),V.elementAt(M[j]),DistanceType);
                        B[i] = j;
                    }
                }
                SOE += MinDis;
            }
        return SOE;
    }

    static double NormalDistribution(Point3D p, Point3D mean, double Deviation) {
        double result = Math.exp(-(Distance(p, mean, EUCLIDEAN) * Distance(p, mean, EUCLIDEAN)) / (2 * Deviation * Deviation));
        return result;
    }
}
