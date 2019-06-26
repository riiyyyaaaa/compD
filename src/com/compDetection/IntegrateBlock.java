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


//    /**
//     * ブロックの番号と列のインデックス番号が対応
//     * 行には自身と同じクラス分類されたブロックの番号のところに1が立っている
//     * 自分と同じでなければ0
//     */
//    public IntegrateBlock() {
//        for(int i=0; i<numOfBlock*numOfBlock; i++) {
//            List<Integer> cl = new ArrayList<>(numOfBlock*numOfBlock);
//            Collections.addAll(cl, 0);
//            cl.add(i, 1);
//
//            group.add(cl);
//        }
//        System.out.println(group);
//    }

    public static void main(String[] arg) throws IOException {
        String cd = new File(".").getAbsoluteFile().getParent();
        File dir = new File(cd + "\\src\\input\\");

        File[] list = dir.listFiles();
        for(int i=0; i<1; i++) {
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
    public List<List<Double>> calDistanceMat(double[][][] featureMat) {
        // 使用する特徴の番号をcompDetection.propertiesから持ってくる
        int[] fNum = new int[featureNumStr.length];
        for(int i=0; i<featureNumStr.length; i++) {
            fNum[i] = Integer.valueOf(featureNumStr[i]);
        }

        int fNumLen = featureMat.length;

        List<List<Double>> data = convFeatData2CalData(featureMat, fNum);
        List<List<Double>> disMat = new ArrayList<>();

        List<Double> testData1 = Arrays.asList(5.0, 4.0, 0.0, 1.0, 2.0, 8.0, 10.0, 1.0);
        List<Double> testData2 = Arrays.asList(1.0, 2.0, 5.0, 4.0, 9.0, 10.0, 8.0, 3.0);

        for(int i=0; i<1; i++) {
            List<Double> horizon = new ArrayList<>();
            for(int j=0; j<2; j++) {
                if(i<j) {
                    System.out.println("< i:" + i + ", j:" + j);

                    List<List<Double>> material = new ArrayList<>();
//                    material.add(data.get(i));
//                    material.add(data.get(j));
                    material.add(testData1);
                    material.add(testData2);
                    System.out.println(calAve(material));
                    horizon.add(j, calDis(material, calAve(material)));
                    System.out.println(calDis(material, calAve(material)));

                    System.out.println("si: " + calAve(material).size());
                } else if(i>j) {
                    System.out.println("> i:" + i + ", j:" + j);
                    // TODO 合ってるか要チェック
                    horizon.add(j, disMat.get(j).get(i));
                } else {
                    horizon.add(j, 0.0);
                }
            }

            disMat.add(horizon);
            System.out.println(horizon);
        }

        System.out.println("column: " + disMat.size() + ", line: " + disMat.get(0).size() + ", data size: " + disMat.get(0).size());

        return disMat;
    }

/**
 *
 * クラスごとの中心点に近いものにクラス分けしていくと
 * 二つのクラスに入っているときに両方に提案できる
 */


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
        int numOfData = data.size();
        int dimension = data.get(0).size();
        List<Double> ave = new ArrayList<>();

        System.out.println("line: " + dimension);
        for(int i=0; i<dimension; i++) {
            double sum = 0;
            for(int j=0; j<numOfData; j++) {
                sum += data.get(j).get(i);
                //ave.add(i, sum);
            }
            double valueAve = sum/numOfData;
            ave.add(i, valueAve);

        }
        System.out.println("line:: " + ave.size());

        return ave;
    }

    /**
     * Calculate Euclidean distance N dimension.
     * 使ってないやつ
     * @param data
     * @param dataAve
     * @return
     */
    public double calDistance(double[] data, double[] dataAve) {
        double dis = 0;

        for(int i=0; i<data.length; i++) {
            dis += (data[i] - dataAve[i]) * (data[i] - dataAve[i]);
        }
        dis = Math.sqrt(dis);

        return dis;
    }

    /**
     * Calculate Sum of Euclidean distance Average of a class
     * @param data
     * @param dataAve
     * @return
     */
     public double calDis(List<List<Double>> data, List<Double> dataAve) {
        double disAll = 0;

        for(int i=0; i<data.size(); i++) {
            double dis = 0;
            for(int j=0; j<data.get(0).size(); j++) {
                dis += (data.get(i).get(j) - dataAve.get(j)) * (data.get(i).get(j) - dataAve.get(j));
            }
            disAll += Math.sqrt(dis);
        }

        return disAll;
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
