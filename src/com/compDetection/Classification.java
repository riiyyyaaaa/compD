package com.compDetection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.compDetection.TexGLCM.checkVal;
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

    private static Classification cl = new Classification();

    public static void main(String[] args) throws IOException {
        //iB.first();
    }

    /**
     * 与えられた領域に連続したブロックが何個あるかを数える。
     * @param blocks
     * @param clNum
     * @param direction 0: vertical, 1: horizon
     */
    public static int[][] calPos(List<List<Integer>> blocks, int clNum, int direction) {
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

    /**
     * 配列内で0以外での最小値、最大値を返却。
     * @param array
     * @return
     */
    public int[] checkSL (int[] array) {
        int[] result = new int[2];
        int numS=0;
        int varS = 10;
        int numL=0;
        int varL = 0;

        for(int i=0; i<numOfBlock; i++) {
            // 0以外での最小値
            if(varS >= array[i] && array[i] != 0) {
                varS = array[i];
                numS = i;
            }
            if(varL <= array[i]) {
                varL = array[i];
                numL = i;
            }
        }
        result[0] = numS;
        result[1] = numL;

//        // もし最小値、最大値の場所が複数ある時はそれを入れる
//        for(int i=0; i<numOfBlock; i++) {
//            int countS = 0;
//            if(array[i] == varS) {
//                result[0][countS] = i;
//            }
//            countS++;
//        }

        return result;
    }


    public static int detectComp (List<List<Integer>> blocks, int clNum, int backNum) {
        int comp = 0;   // 日の丸: 0, 一点透視: 1, 二点透視: 2, アオリ: 3, 俯瞰: 4, 水平: 5
        int diff = 0;
        int st = 0;
        int shMo = 0; //あとで設定する 最小値の位置

        boolean flag = false;


        int[][] verLon = calPos(blocks, clNum, 0);
        int[][] horLon = calPos(blocks, clNum, 1);

        for(int i=0; i<verLon.length; i++) {

            for(int j=0; j<numOfBlock; j++) {
                if(verLon[i][j] != 0) {
                    st = verLon[i][j];
                    flag = true;
                }
                if(flag == true && verLon[i][j] != 0 && j<=shMo) {
                    // 一点の時、diffが　<0　になる
                    diff += verLon[i][j] - st;
                }
            }
        }

        return comp;
    }

    /**
     * 指定した領域がアオリ、俯瞰、どちらでもないかを決める
     * @param blocks
     * @param reCl
     * @param clNum
     * @return
     */
    public static int checkEyeLevel (List<List<Integer>> blocks, int reCl, int clNum) {
        int result = 2; // 0: アオリ, 1: 俯瞰, 2: 無し
        int top = 8;
        int bottom = 0;

        for(int i=0; i<numOfBlock; i++) {
            if(blocks.get(i).contains(reCl)) {
                if(top > i) {
                    top = i;
                } else if (bottom < i) {
                    bottom = i;
                }
            }
        }
        int middle = top+bottom/2;

        // 領域の高さが3ブロック以下ならば俯瞰でもアオリでもない
        if(bottom - top > 3) {

            System.out.println("top: " + top + ", bottom: " + bottom);

            int[][] pos = calPos(blocks, clNum, 1);
            int[] sl = cl.checkSL(pos[reCl]);

            // 最長の位置, 複数あるかもだからlist
            List<Integer> lPos = new ArrayList<>();
            double posAve = 0;
            for (int i = 0; i < numOfBlock; i++) {
                if (sl[1] == pos[reCl][i]) {
                    lPos.add(i);
                    posAve += i;
                }
            }
            posAve /= lPos.size();

            double aori = Math.abs(posAve-bottom);
            double hukan = Math.abs(posAve-top);
            double nashi = Math.abs(posAve-middle);
            if(aori<hukan && aori<nashi) {
                result = 0;
            } else if(hukan<aori && hukan<nashi) {
                result = 1;
            }

            return result;
        }





        //cl.checkSL()

        return result;
    }



}
