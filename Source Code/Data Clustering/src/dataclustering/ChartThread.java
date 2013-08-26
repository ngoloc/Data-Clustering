/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataclustering;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import org.math.plot.Plot3DPanel;

class MyComparator implements Comparator<Cluster> {

    public int compare(Cluster o1, Cluster o2) {
        if (o1.SSE < o2.SSE) {
            return 1;
        }
        if (o1.SSE > o2.SSE) {
            return -1;
        }
        return 0;
    }
}

/**
 *
 * @author Thuan Loc
 */
public class ChartThread extends Thread {

    private Vector<Point3D> VP;
    private int K;
    private int MaxLoops;
    private Plot3DPanel ClusteringPanelChart;
    private JTable jTable;
    private JLabel jLabel4;
    private JButton jButton2;
    private int KMeansType;
    private JTabbedPane jTabbedPane;
    private int DistanceType;

    ChartThread(int KMeansType, Vector<Point3D> VP, int K, int MaxLoops, Plot3DPanel ClusteringPanelChart, JTable jTable, JLabel jLabel4, JButton jButton2, JTabbedPane jTabbedPane, int DistanceType) {
        this.VP = new Vector<Point3D>();
        for (int i = 0; i < VP.size(); i++) {
            this.VP.add(new Point3D(VP.elementAt(i).x, VP.elementAt(i).y, VP.elementAt(i).z));
        }
        this.K = K;
        this.MaxLoops = MaxLoops;
        this.ClusteringPanelChart = ClusteringPanelChart;
        this.jTable = jTable;
        this.jLabel4 = jLabel4;
        this.jButton2 = jButton2;
        this.KMeansType = KMeansType;
        this.jTabbedPane = jTabbedPane;
        this.DistanceType = DistanceType;
    }

    private void createChart(Vector<Cluster> ClusterVector) {
        try {
            this.ClusteringPanelChart.removeAllPlots();
            Thread.sleep(Math.max(1000, VP.size() / 50));
            for (int i = 0; i < ClusterVector.size(); i++) {
                double[] x = new double[ClusterVector.elementAt(i).V.size()];
                double[] y = new double[ClusterVector.elementAt(i).V.size()];
                double[] z = new double[ClusterVector.elementAt(i).V.size()];

                for (int j = 0; j < ClusterVector.elementAt(i).V.size(); j++) {
                    x[j] = ClusterVector.elementAt(i).V.elementAt(j).x;
                    y[j] = ClusterVector.elementAt(i).V.elementAt(j).y;
                    z[j] = ClusterVector.elementAt(i).V.elementAt(j).z;
                }

                this.ClusteringPanelChart.addScatterPlot("Cluster " + Integer.toString(i), x, y, z);
                jTable.setValueAt(i + 1, i, 0);
                jTable.setValueAt(ClusterVector.elementAt(i).V.size(), i, 1);
                BigDecimal bd = new BigDecimal(ClusterVector.elementAt(i).Centroid.x);
                jTable.setValueAt(bd.setScale(5, BigDecimal.ROUND_HALF_UP).toString(), i, 2);
                bd = new BigDecimal(ClusterVector.elementAt(i).Centroid.y);
                jTable.setValueAt(bd.setScale(5, BigDecimal.ROUND_HALF_UP).toString(), i, 3);
                bd = new BigDecimal(ClusterVector.elementAt(i).Centroid.z);
                jTable.setValueAt(bd.setScale(5, BigDecimal.ROUND_HALF_UP).toString(), i, 4);

                Thread.sleep(Math.max(1000, VP.size() / 50));
            }
        } catch (Exception e) {
        }
    }

    private Vector<Cluster> K_Means_Clustering(Cluster MainCluster, int K, int MaxLoops, int[] Seeds, boolean showChart) {
        Vector<Cluster> ClusterVector = new Vector<Cluster>();
        for (int i = 0; i < K; i++) {
            ClusterVector.add(new Cluster(MainCluster.V.elementAt(Seeds[i])));
        }

        int loops = 0;
        while (loops < Math.min(10, MaxLoops)) {
            for (int i = 0; i < K; i++) {
                ClusterVector.elementAt(i).V.removeAllElements();
            }

            for (int i = 0; i < MainCluster.V.size(); i++) {
                Point3D p = MainCluster.V.elementAt(i);
                double MinDist = CONST.INF;
                int ChosenCluster = 0;
                for (int j = 0; j < K; j++) {
                    if (MinDist > CONST.Distance(p, ClusterVector.get(j).Centroid, this.DistanceType)) {
                        MinDist = CONST.Distance(p, ClusterVector.get(j).Centroid, this.DistanceType);
                        ChosenCluster = j;
                    }
                }

                ClusterVector.get(ChosenCluster).V.add(p);
            }
            boolean Terminate = true;
            for (int i = 0; i < K; i++) {
                if (!CONST.Equal(ClusterVector.get(i).Centroid, CONST.getCentroid(ClusterVector.get(i).V), this.DistanceType)) {
                    Terminate = false;
                }
                ClusterVector.get(i).Centroid = CONST.getCentroid(ClusterVector.get(i).V);
                ClusterVector.get(i).SSE = CONST.calSSE(ClusterVector.get(i), this.DistanceType);
            }
            if (Terminate) {
                break;
            }
            loops++;
            if (CONST.STOP) {
                this.jLabel4.setVisible(false);
                break;
            }
            if (showChart) {
                createChart(ClusterVector);
            }
        }

        return ClusterVector;
    }

    private void Bisecting_K_Means_Clustering(Cluster MainCluster, int K, int MaxLoops) {
        MainCluster.SSE = CONST.calSSE(MainCluster, this.DistanceType);
        Comparator<Cluster> cmp = new MyComparator();
        PriorityQueue<Cluster> Q = new PriorityQueue<Cluster>(K, cmp);
        Q.add(MainCluster);

        while (Q.size() < K) {
            Vector<Cluster> V = new Vector<Cluster>();
            Iterator<Cluster> I = Q.iterator();
            while (I.hasNext()) {
                V.add(I.next());
            }
            createChart(V);
            Cluster MaxCluster = Q.poll();
            double MinTotalSSE = CONST.INF;
            Cluster C1 = null;
            Cluster C2 = null;

            for (int i = 0; i < MaxCluster.V.size() - 1; i = i * 10 + 1) {
                for (int j = i + 1; j < MaxCluster.V.size(); j *= 10) {
                    int[] Seeds = new int[2];
                    Seeds[0] = i;
                    Seeds[1] = j;
                    Vector<Cluster> BiClusters = K_Means_Clustering(MaxCluster, 2, Math.min(MaxLoops, 5), Seeds, false);

                    if (MinTotalSSE > BiClusters.elementAt(0).SSE + BiClusters.elementAt(1).SSE) {
                        C1 = BiClusters.elementAt(0);
                        C2 = BiClusters.elementAt(1);
                        MinTotalSSE = BiClusters.elementAt(0).SSE + BiClusters.elementAt(1).SSE;
                    }
                }
            }

            Q.add(C1);
            Q.add(C2);
        }

        Vector<Cluster> V = new Vector<Cluster>();
        Iterator<Cluster> I = Q.iterator();
        while (I.hasNext()) {
            V.add(I.next());
        }
        createChart(V);
    }

    private void EM_K_Means_Clustering(Cluster MainCluster, int K) {
        int R = MainCluster.V.size();
        Point3D[] Means = new Point3D[K];
        double[][] PxBelongsToC = new double[R][K];
        Vector<Cluster> ClusterVector;
        MainCluster.SSE = CONST.calSSE(MainCluster, CONST.EUCLIDEAN);
        double Deviation = MainCluster.SSE;
        Deviation /= MainCluster.V.size();
        Deviation = Math.sqrt(Deviation);

        for (int i = 0; i < K; i++) {
            Means[i] = new Point3D(MainCluster.V.elementAt(i));
        }

        ClusterVector = new Vector<Cluster>();
        for (int i = 0; i < K; i++) {
            ClusterVector.add(new Cluster(new Point3D(Means[i])));
        }

        //Expectation Step
        for (int k = 0; k < R; k++) {
            double SumOfPxBelongsToC = 0;
            for (int i = 0; i < K; i++) {
                SumOfPxBelongsToC += CONST.NormalDistribution(MainCluster.V.elementAt(k), Means[i], Deviation);
            }

            for (int i = 0; i < K; i++) {
                PxBelongsToC[k][i] = CONST.NormalDistribution(MainCluster.V.elementAt(k), Means[i], Deviation) / SumOfPxBelongsToC;
            }
        }

        //Maximization Step
        for (int i = 0; i < K; i++) {
            Point3D SumOfMeanPx = new Point3D(0, 0, 0);
            double SumOfPx = 0;
            for (int k = 0; k < R; k++) {
                SumOfMeanPx.x += PxBelongsToC[k][i] * MainCluster.V.elementAt(k).x;
                SumOfMeanPx.y += PxBelongsToC[k][i] * MainCluster.V.elementAt(k).y;
                SumOfMeanPx.z += PxBelongsToC[k][i] * MainCluster.V.elementAt(k).z;
                SumOfPx += PxBelongsToC[k][i];
            }

            Means[i].x = SumOfMeanPx.x / SumOfPx;
            Means[i].y = SumOfMeanPx.y / SumOfPx;
            Means[i].z = SumOfMeanPx.z / SumOfPx;

        }

        ClusterVector = new Vector<Cluster>();
        for (int i = 0; i < K; i++) {
            ClusterVector.add(new Cluster(new Point3D(Means[i])));
        }

        for (int i = 0; i < R; i++) {
            double Min = CONST.INF;
            int pos = 0;
            for (int k = 0; k < K; k++) {
                if (Min > CONST.Distance(Means[k], MainCluster.V.elementAt(i), CONST.EUCLIDEAN)) {
                    Min = CONST.Distance(Means[k], MainCluster.V.elementAt(i), CONST.EUCLIDEAN);
                    pos = k;
                }
            }
            ClusterVector.elementAt(pos).V.add(MainCluster.V.elementAt(i));
        }
        createChart(ClusterVector);
    }

    private void K_Medoids_Clustering(Cluster MainCluster, int K) {
        int N = MainCluster.V.size();

        int[] M = new int[K];//Array Of Medoids
        boolean[] IsMedoids = new boolean[N];
        Arrays.fill(IsMedoids, false);
        for (int i = 0; i < K; i++) {
            M[i] = i;
            IsMedoids[i] = true;
        }

        int[] B = new int[N];//which medoid point i belongs to
        int[] NB = new int[N];
        double SE = CONST.SumOfError(M, B, IsMedoids, K, MainCluster.V, this.DistanceType);
        int loops = 0;
        while (loops < this.MaxLoops) {
            boolean found = false;
            int O = 0;
            int P = 0;
            for (int i = 0; i < K; i++) {
                for (int j = 0; j < N; j++) {
                    if (!IsMedoids[j]&&NB[j]==i) {
                        IsMedoids[j] = true;
                        IsMedoids[M[i]] = false;
                        double tmp = CONST.SumOfError(M, B, IsMedoids, K, MainCluster.V, this.DistanceType);
                        if (SE > tmp) {
                            SE = tmp;
                            O = i;
                            P = j;
                            NB = Arrays.copyOf(B, N);
                            found = true;
                        }
                        IsMedoids[j] = false;
                        IsMedoids[M[i]] = true;
                    }
                }
            }

            if (found) {
                IsMedoids[M[O]] = false;
                M[O] = P;
                IsMedoids[P] = true;

                Vector<Cluster> ClusterVector = new Vector<Cluster>();
                for (int i = 0; i < K; i++) {
                    ClusterVector.add(new Cluster(new Point3D(0, 0, 0)));
                }

                for (int i = 0; i < K; i++) {
                    ClusterVector.elementAt(i).V.add(MainCluster.V.elementAt(M[i]));
                }

                for (int i = 0; i < N; i++) {
                    if (!IsMedoids[i]) {
                        ClusterVector.elementAt(NB[i]).V.add(MainCluster.V.elementAt(i));
                    }
                }
                for(int i=0;i<K;i++)
                    ClusterVector.elementAt(i).Centroid = CONST.getCentroid(ClusterVector.elementAt(i).V);
                
                createChart(ClusterVector);
            } else {
                this.jLabel4.setVisible(false);
                break;
            }
            if (CONST.STOP) {
                this.jLabel4.setVisible(false);
                break;
            }
            loops++;
        }
    }

    private void createKMeansChart() {
        Cluster C = new Cluster(VP);
        jTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[K][5],
                new String[]{
                    "Cluster No", "NOf Objects", "Centroid's X", "Centroid's Y", "Centroid's Z"
                }) {

            Class[] types = new Class[]{
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        });

        switch (KMeansType) {
            case CONST.BASIC_KMEANS:
                int[] Seeds = new int[K];
                for (int i = 0; i < K; i++) {
                    Seeds[i] = i;
                }
                K_Means_Clustering(C, K, MaxLoops, Seeds, true);
                break;
            case CONST.BISECTING_KMEANS:
                this.jButton2.setEnabled(false);
                Bisecting_K_Means_Clustering(C, K, MaxLoops);
                break;
            case CONST.EM_KMEANS:
                this.jButton2.setEnabled(false);
                EM_K_Means_Clustering(C, K);
                break;
            case CONST.K_MEDOIDS:
                K_Medoids_Clustering(C, K);
                break;
        }
    }

    @Override
    public void run() {
        createKMeansChart();
        this.jButton2.setEnabled(false);
        this.jTabbedPane.setEnabledAt(0, true);
    }
}
