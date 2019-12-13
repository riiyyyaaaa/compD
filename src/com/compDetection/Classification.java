package com.compDetection;

import org.omg.CORBA.INTERNAL;

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
        // TODO 日の丸構図；hor,verの中央が最大になる
        // TODO 二点透視,一点透視: 単純にブロック数を数えて最長のとこを把握、その最長の位置で分類

        return comp;
    }

    /**
     * 指定した領域がアオリ、俯瞰、どちらでもないかを決める
     * @param blocks
     * @param reCl 背景以外で現在参照したいクラスタ番号
     * @param clNum
     * @return
     */
    public static int checkEyeLevel (List<List<Integer>> blocks, int clNum) {
        int result = 2; // 0: アオリ, 1: 俯瞰, 2: 無し
        int top = 8;
        int bottom = 0;

        for(int i=0; i<numOfBlock; i++) {
            if(blocks.get(i).contains(clNum)) {
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

//            int[][] pos = calPos(blocks, clNum, 1);
//            int[] sl = cl.checkSL(pos[reCl]);
            int[] array = countBlock(blocks, clNum, 1);
            int[] slArray = getSL(array);

            // 最長の位置, 複数あるかもだからlist
            List<Integer> lPos = new ArrayList<>();
            double posAve = 0;
            for (int i = 0; i < numOfBlock; i++) {
                if (slArray[1] == array[i]) {
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
        }

        return result;
    }

    /**
     * 水平かどうか。0: 無し, 1: 水平
     * @param blocks
     * @param reCl
     * @return
     */
    public static int checkHorizon(List<List<Integer>> blocks, int reCl) {
        int result = 0;
        int[] horizonNum = countBlock(blocks, reCl, 1);
        boolean flag = false;
        boolean firstVar = false;

        for(int i=0; i<numOfBlock; i++) {
            if((horizonNum[i] >= numOfBlock-2) && !firstVar) {
                flag = true;
                firstVar = true;
            }
            if(flag) {
                if(!(horizonNum[i] >= numOfBlock-2)) {
                    flag = false;
                }
            }
        }

        if(flag) {
            result = 1;
        } else {
            result = 0;
        }

        return result;
    }

    /**
     * 日の丸構図かどうかを判断
     * @param blocks
     * @param reCl
     * @return
     */
    public static int checkCenter(List<List<Integer>> blocks, int reCl) {
        // 縦の最長と横の最長が同じくらいであれば日の丸構図
        int result = 0;

        int[] horLen  = new int[numOfBlock];

        for (int i = 0; i < numOfBlock; i++) {
            boolean fl = false;
            for (int j = 0; j < numOfBlock; j++) {
                if (blocks.get(j).get(i) == reCl) {
                    fl = true;
                }
            }
            if (fl) {
                horLen[i] = 1;
            } else {
                horLen[i] = 0;
            }
        }

        int first1 = 0; // 最初に値が出た位置(横方向の)
        int last1 = 0;  // 最後に値が出値値(横方向)
        boolean fl1 = false;
        for (int i = 0; i < numOfBlock; i++) {
            if (!fl1 && horLen[i] == 1) {
                first1 = i;
                fl1 = true;
            } else if (fl1 && horLen[i] == 1) {
                last1 = i;
            }
        }

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

        double percentage;
        if((last1-first1) >=  (bottom-top)) {
            percentage = (last1-first1) / (bottom-top);
        } else {
            percentage = (bottom-top) / (last1-first1);
        }

        if(percentage<2.0) {
            result = 1;
        }

        return result;
    }

    /**
     *　一点透視、二点透視かどうかを判断
     * @param blocks
     * @param reCl: 現在みている領域の番号
     * @return
     */
    public static int checkPers(List<List<Integer>> blocks, int reCl) {
        int result = 0; // 0: 一点透視, 1: 二点透視, 2: 無し

        int left = 8;
        int right = 0;
        int[] horLen  = new int[numOfBlock];    // i列にreClが含まれていれば1, そうでなければ0

        int[] block = countBlock(blocks, reCl, 0);
        int[] sl = getSL(block);

        // 最大と最小の差が2未満であれば2にする
        if(!(Math.abs(sl[0]-sl[1])<2)) {

            for (int i = 0; i < numOfBlock; i++) {
                boolean fl = false;
                for (int j = 0; j < numOfBlock; j++) {
                    if (blocks.get(j).get(i) == reCl) {
                        fl = true;
                    }
                }
                if (fl) {
                    horLen[i] = 1;
                } else {
                    horLen[i] = 0;
                }
            }

            int first1 = 0; // 最初に値が出た位置(横方向の)
            int last1 = 0;  // 最後に値が出値値(横方向)
            boolean fl1 = false;
            for (int i = 0; i < numOfBlock; i++) {
                if (!fl1 && horLen[i] == 1) {
                    first1 = i;
                    fl1 = true;
                } else if (fl1 && horLen[i] == 1) {
                    last1 = i;
                }
            }
            double middle = (double) (last1 - first1) / 2 + (double) first1;


            int firstNum = block[first1];   // 最初に出現する値

            List<Integer> kugiri = new ArrayList<>();
            boolean cont = true;
            int count = first1;

            System.out.println("block中身");
            for (int i = first1; i < last1+1; i++) {
                System.out.println(block[i]);
                if (block[i] == sl[0] || block[i] == sl[1]) {
                    kugiri.add(i);
                }
            }

            System.out.println("sl中身\n" + sl[0] + ", " + sl[1]);
            System.out.println("first: " + first1);
            System.out.println("last: " + last1);

            // 最初の値が最大と最小どちらに近いか
            int diffS = Math.abs(kugiri.get(0) - sl[0]);
            int diffL = Math.abs(kugiri.get(0) - sl[1]);


            //最大値が中央に近い、または最小値が恥：二点透視
            //最小値が中央に近い、またわ最大値が恥：一点透視、
            List<Integer> sPos = getPos(sl[0], block);
            List<Integer> lPos = getPos(sl[1], block);
            boolean flag2 = false;
            if(diffS<diffL && lPos.get(0) != 7) {
                //左端が最小値に近い時,最大値の向こうに値が存在すれば二点透視
                for(int i=lPos.get(0)+1; i<numOfBlock; i++) {
                    if(block[i] != 0) {
                        flag2 = true;
                    }
                }
            }
            if(flag2) {
                result = 1;
            } else {
                result = 0;
            }


//        int[][] pos = calPos(blocks, clNum, 0);
//        int[] sl = cl.checkSL(pos[reCl]);
//
//
//        boolean flag = true;
//
//        int[] horLen = new int[numOfBlock];

            // 連続と連続の間を埋める
//        for(int i=0; i<numOfBlock; i++) {
//            int tempLen = 0;
//            int firstNum = 0;
//            boolean cont = false;
//            for(int j=0; j<numOfBlock; j++) {
//                if(blocks.get(j).get(i) == reCl && flag == true) {
//                    tempLen++;
//                    flag = false;
//                    firstNum = j;
//                } else if(blocks.get(j).get(i) == reCl && blocks.get(j-1).get(i) == reCl && !cont && !flag) {
//                    tempLen++;
//                    cont = true;
//                } else if(blocks.get(j).get(i) == reCl && blocks.get(j-1).get(i) == reCl && cont && !flag) {
//                    tempLen = j-firstNum;
//                }
//            }
//            horLen[i] = tempLen;
//        }

//        int result = 0;
//
//        int[] slLen = getSL(horLen);
//        int recLen = 0;
//        int dec = 0;
//        int inc = 0;
//
//        int rate = 0;
//        int kugiri = 0;
//
//        recLen = horLen[0];
//        for(int i=1; i<numOfBlock; i++) {
//            rate += recLen-horLen[i];
//            if(horLen[i] == slLen[0] || horLen[i] == slLen[1]) {
//
//                kugiri = i;
//            }
//
//        }

        }
        return result;

    }

    /**
     * 与えられた配列内に指定した値の位置、インデックス番号を返却する。
     * @param var
     * @param array
     * @return
     */
    public static List<Integer> getPos(int var, int[] array) {
        List<Integer> pos = new ArrayList<>();
        for(int i=0; i<array.length; i++) {
            if(array[i] == var) {
                pos.add(i);
            }
        }
        return pos;
    }

    /**
     * 最小の値と、最大の値を返す。位置ではない。
     * @param array
     * @return
     */
    public static int[] getSL(int[] array) {
        int s = 8;
        int l = 0;
        int[] result = new int[2];

        for(int i=0; i<numOfBlock; i++) {
            if(array[i]<s) {
                s = array[i];
            }
            if(array[i]>l) {
                l = array[i];
            }
        }
        result[0] = s;
        result[1] = l;

        return result;
    }

    /**
     * 縦, 横にブロックが何個あるかを数える。
     * @param blocks
     * @param reCl: 数えたいクラスタ番号
     * @param direction: 方向、縦か横
     * @return
     */
    public static int[] countBlock(List<List<Integer>> blocks, int reCl, int direction) {
        int[] result = new int[numOfBlock];

        for(int i=0; i<numOfBlock; i++) {
            int count = 0;
            for(int j=0; j<numOfBlock; j++) {
                if(direction == 0) {
                    if (blocks.get(j).get(i) == reCl) {
                        count++;
                    }
                } else {
                    if (blocks.get(i).get(j) == reCl) {
                        count++;
                    }
                }
            }
            result[i] = count;
        }

        return result;
    }


}
