package com.compDetection;

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
    static PropertyUtil propertyUtil;
    static IntegrateBlock iB = new IntegrateBlock();

    String[] featureNumStr = propertyUtil.getProperty("featureNum").split(",");
    int numOfBlock = Integer.valueOf(propertyUtil.getProperty("numOfBlock"));
    List<List<Integer>> group;


    /**
     * ブロックの番号と列のインデックス番号が対応
     * 行には自身と同じクラス分類されたブロックの番号のところに1が立っている
     * 自分と同じでなければ0
     */
    public IntegrateBlock() {
        for(int i=0; i<numOfBlock*numOfBlock; i++) {
            List<Integer> cl = new ArrayList<>(numOfBlock*numOfBlock);
            Collections.addAll(cl, 0);
            cl.add(i, 1);

            group.add(cl);
        }
        System.out.println(group);
    }

    public static void main(String[] arg) throws IOException {
        String cd = new File(".").getAbsoluteFile().getParent();
        File dir = new File(cd + "\\src\\input\\");

        File[] list = dir.listFiles();
        for(int i=0; i<list.length; i++) {
            System.out.println(list[i]);
            int[][][][] mat_test = TexGLCM.calGLCM(list[i]);

            iB.calDistanceMat(TexGLCM.calFeature(mat_test));


        }

    }

    /**
     * 初期の距離行列を計算
     * @param featureMat
     * @return
     */
    public List<List<List<Double>>> calDistanceMat(double[][][] featureMat) {
        // 使用する特徴の番号をcompDetection.propertiesから持ってくる
        int[] fNum = new int[featureNumStr.length];
        for(int i=0; i<featureNumStr.length; i++) {
            fNum[i] = Integer.valueOf(featureNumStr[i]);
        }

        int fNumLen = featureMat.length;

        List<List<Double>> data = convFeatData2CalData(featureMat, fNum);
        List<List<List<Double>>> disMat = new ArrayList<>();

        for(int i=0; i<numOfBlock+numOfBlock; i++) {
            List<List<Double>> horizon = new ArrayList<>();
            for(int j=0; j<numOfBlock*numOfBlock; j++) {
                if(i<j) {
                    List<List<Double>> material = new ArrayList<>();
                    material.add(data.get(i));
                    material.add(data.get(j));
                    horizon.add(j, calAve(material));
                } else {
                    // TODO 合ってるか要チェック
                    horizon.add(j, disMat.get(j).get(i));
                }
            }
            disMat.add(horizon);
        }



        return disMat;
    }


    /**
     * Add value of 1 to group(List)
     * Show progress of Clustering
     * @param c1
     * @param c2
     * @return
     */
    public List<List<Integer>> refIntegration(int c1, int c2) {
        this.group.get(c1).add(c2, 1);
        this.group.get(c2).add(c1, 1);

        return group;
    }

    /**
     * Convert featureMat to calculatingMat
     * four rad including features integrate to one line
     * @param featureData
     * @param featureNum
     * @return
     */
    public List<List<Double>> convFeatData2CalData(double[][][] featureData, int[] featureNum) {
        int fLen = featureNum.length;
        int dataNum = numOfBlock*numOfBlock;
        List<List<Double>> calDatas = new ArrayList<>();

        for(int num = 0; num<dataNum; num++) {
            List<Double> calData = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < fLen; j++) {
                    calData.add(featureData[num][i][featureNum[j]]);
                }
            }
            calDatas.add(calData);
        }
        return calDatas;
    }


    /**
     * Calculate center of average in N dimension.
     * これを使うこと!!!!!!
     * @param data
     * @return ave
     */
    public List<Double> calAve(List<List<Double>> data) { ;
        int column = data.size();
        int line = data.get(0).size();
        List<Double> ave = new ArrayList<>(line);
        Collections.addAll(ave, 0.0);

        for(int i=0; i<line; i++) {
            for(int j=0; j<column; j++) {
                ave.add(i,ave.get(i) + data.get(j).get(i));
            }
            ave.add(i, ave.get(i)/column);
        }

        return ave;
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
