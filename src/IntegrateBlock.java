import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * TexGLCMを基に似たテクスチャのブロックを統合する
 * ウォード法を用いてクラスタリング(仮)
 */
public class IntegrateBlock {

    public static void main(String[] arg) throws IOException {
        String cd = new File(".").getAbsoluteFile().getParent();
        File dir = new File(cd + "\\src\\input\\");

        File[] list = dir.listFiles();git
        for(int i=0; i<list.length; i++) {
            System.out.println(list[i]);
            int[][][][] mat_test = TexGLCM.calGLCM(list[i]);
            TexGLCM.calFeature(mat_test);
        }

    }

    /**
     * 初期の距離行列を計算
     * @param featureMat
     * @return
     */
    public double[][] calDistancehMat(double[][][] featureMat) {
        double[][] disMat = {};


        return disMat;
    }

    /**
     * Calculate Euclidean distance N dimension.
     * @param data1
     * @param data2
     * @return
     */
    public double calDistance(double[] data1, double[] data2) {
        double dis = 0;

        for(int i=0; i<data1.length; i++) {
            dis += (data1[i] - data2[i]) * (data1[i] - data2[i]);
        }
        dis = Math.sqrt(dis);

        return dis;
    }

    /**
     * Calculate RMS distance N dimension.
     * @param cluster1
     * @param cluster2
     * @return
     */
    public double calDistWard(double[] cluster1, double[] cluster2) {
        double dis = 0;
        int dimension = cluster1.length;
        double ave = 0;

        for(int i=0; i<dimension; i++) {
            ave = (cluster1[i] + cluster2[i])/dimension;
            dis += (cluster1[i]-ave)*(cluster1[i]-ave) + (cluster2[i]-ave)*(cluster2[i]-ave);
        }

        return dis;
    }

}
