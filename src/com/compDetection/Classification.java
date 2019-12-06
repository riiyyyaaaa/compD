package com.compDetection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.compDetection.TexGLCM.propertyUtil;

/**
 * 領域の広がりによって分類する
 * makeBlockArray()から
 */
public class Classification {

    private static int numOfBlock = Integer.valueOf(propertyUtil.getProperty("numOfBlock"));
    private static int imageSize = Integer.valueOf(PropertyUtil.getProperty("imageSize"));
    private static String[] featureNumStr = propertyUtil.getProperty("featureNum").split(",");

    private static IntegrateBlock iB = new IntegrateBlock();

    public static void main(String[] args) throws IOException {
        //iB.first();
    }

    /**
     * 縦と横それぞれに連続したブロックが何個あるかを数える。
     * @param blocks
     * @param clNum
     * @param direction 0: vertical, 1: horizon
     */
    public static int[][] calSLPos(List<List<Integer>> blocks, int clNum, int direction) {
        int[][] result = new int[clNum][numOfBlock];
        //int[][] horLon = new int[clNum][numOfBlock];

        for(int i=0; i<numOfBlock; i++) {
            List<Integer> ver = new ArrayList<>();
            List<Integer> hor = new ArrayList<>();
            for(int j=0; j<numOfBlock; j++) {

                for(int k=0; k<clNum; k++) {
                    if(k == blocks.get(j).get(i) && j>0 && blocks.get(j).get(i) == blocks.get(j-1).get(i) && direction == 0) {
                        result[k][i]++;
                    }
                    if(k == blocks.get(i).get(j) && j>0 && blocks.get(i).get(j) == blocks.get(i).get(j-1) && direction == 1) {
                        result[k][i]++;
                    }
                }
            }

        }

        System.out.println("Vertical");
        for(int i=0; i<result.length; i++) {
            for(int j=0; j<result[i].length; j++) {
                System.out.print(result[i][j]);
            }
            System.out.println();
        }

//        System.out.println("Horizon");
//        for(int i=0; i<horLon.length; i++) {
//            for(int j=0; j<horLon[i].length; j++) {
//                System.out.print(horLon[i][j]);
//            }
//            System.out.println();
//        }
//        System.out.println();

        return  result;
    }


    /**
     * 与えられたクラスの特徴量の平均を返す。平均の値は次元数ある。
     * @param featureMat
     * @param clusterList
     * @return
     */
    public static List<Double> getTexAve(double[][][] featureMat, List<Integer> clusterList) {
        int[] fNum = new int[featureNumStr.length];
        for(int i=0; i<featureNumStr.length; i++) {
            fNum[i] = Integer.valueOf(featureNumStr[i]);
        }

        List<List<Double>> data = iB.convFeatData2CalData(featureMat, fNum);

        List<List<Double>> materials = new ArrayList<>();
       // material = makeDataForAve();

        for (Integer cluster : clusterList) {
            List<List<Double>> material = iB.makeDataForAve(cluster, data);
            materials.addAll(material);
        }

        List<Double> result = iB.calAve(materials);
        System.out.println("Cluster");
        System.out.println(clusterList);
        System.out.println("平均");
        System.out.println(result);

        return result;
    }

    /**
     * クラスタごとの平均から背景を決める
     * @return
     */
    public static int judgeBack(List<List<Double>> aveList) {
        int num = 0;
        int[] flag = new int[aveList.size()];
        double temp = 10000;

        for(int i=0; i<aveList.get(0).size(); i++) {
            for(int j=0; j<aveList.size(); j++) {
                if((j == 0) || (aveList.get(j).get(i) < temp)) {
                    temp = aveList.get(j).get(i);
                    num = j;
                }
            }
            flag[num]++;
        }

        int val = 0;
        int resultNum = 0;
        for(int i=0; i<flag.length; i++) {
            if(flag[i] > val) {
                val = flag[i];
                resultNum = i;
            }
        }

        return resultNum;
    }


    public static int detectComp (List<List<Integer>> blocks, int clNum) {
        int comp = 0;   // 日の丸: 0, 一点透視: 1, 二点透視: 2, アオリ: 3, 俯瞰: 4, 水平: 5
        int sh = 0;
        int lo = 0;

        calSLPos(blocks, clNum, 0);

        return comp;
    }



}
