package com.compDetection;

import org.omg.CORBA.INTERNAL;

import java.io.IOException;
import java.util.*;

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

//        System.out.println("Vertical");
//        for(int i=0; i<result.length; i++) {
//            for(int j=0; j<result[i].length; j++) {
//                System.out.print(result[i][j]);
//            }
//            System.out.println();
//        }

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

        List<List<Double>> data = iB.convData2AveTex(featureMat, fNum);

        List<List<Double>> materials = new ArrayList<>();

        //System.out.println("clusterList: " + clusterList);
        for (Integer cluster : clusterList) {
            //List<List<Double>> material = iB.makeDataForAve(cluster, data);
            //materials.addAll(material);
            List<Double> material = data.get(cluster);
            materials.add(material);
        }

        List<Double> result = iB.calAve(materials);

//        System.out.println("Cluster");
//        System.out.println(clusterList);
//        System.out.println("平均");
//        System.out.println(result);

        return result;
    }


    /**
     * クラスタごとの平均から背景を決める
     * TODO 特徴量の特性から決める、要検討
     * @return
     */
    public static int judgeBack(List<List<Double>> aveList) {
        int num = 0;
        int[] flag = new int[aveList.size()];
        //double temp = 10000;
        double temp;
        Map<Integer, Integer> convNum = new HashMap();
        int[] fNumArray = new int[featureNumStr.length];

        for(int i=0; i<featureNumStr.length; i++) {
            fNumArray[i] = Integer.valueOf(featureNumStr[i]);
        }
        for(int i=0; i<fNumArray.length; i++) {
            convNum.put(i, fNumArray[i]);
        }

        for(int i=0; i<aveList.get(0).size(); i++) {
            temp = aveList.get(0).get(i);
            for(int j=1; j<aveList.size(); j++) {
                if((convNum.get(i/4) == 0 && aveList.get(j).get(i) > temp)) {
                    temp = aveList.get(j).get(i);
                    num = j;
                } else if((convNum.get(i/4) == 1 && aveList.get(j).get(i) < temp)) {
                    temp = aveList.get(j).get(i);
                    num = j;
                } else if( (convNum.get(i/4) == 2 && aveList.get(j).get(i) > temp)) {
                    temp = aveList.get(j).get(i);
                    num = j;
                } else if((convNum.get(i/4) == 3 && aveList.get(j).get(i) > temp)) {
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

    // TODO 最大値から左右の消失点があるようにする
    // TODO 消失点がない側で最大値からの落ちていく速度が、収束するさきが400px内に収まるなら二点透視、それ以外が一点透視

    /**
     * 指定した領域がアオリ、俯瞰、どちらでもないかを決める
     * @param blocks
     * @param clNum
     * @return
     */
    public static int checkEyeLevel (List<List<Integer>> blocks, int clNum) {
        int result = 2; // 0: アオリ, 1: 俯瞰, 2: 無し
        int top = 8;
        int bottom = 0;

//        System.out.println("clNum: " + clNum);
//        System.out.println("block　中身");
//        for(int i=0; i<numOfBlock; i++) {
//            for(int j=0; j<numOfBlock; j++) {
//                System.out.print(" " + blocks.get(i).get(j));
//            }
//            System.out.println();
//        }
//        System.out.println();

        for(int i=0; i<numOfBlock; i++) {
            if(blocks.get(i).contains(clNum)) {
                if(top > i) {
                    top = i;
                } else if (bottom < i){
                    bottom = i;
                }
            }
        }
        double middle = top+bottom/2;

        System.out.println("top!: " + top + ", bottom!: " + bottom);

//            int[][] pos = calPos(blocks, clNum, 1);
//            int[] sl = cl.checkSL(pos[reCl]);
        int[] array = countBlock(blocks, clNum, 1);
        int[] slArray = getSL(array);

//        System.out.println("count Block 結果");
//        for(int i=0; i<numOfBlock; i++) {
//            System.out.print(" " +array[i]);
//        }
//        System.out.println();

        // 最長の位置, 複数あるかもだからlist
        List<Integer> lPos = new ArrayList<>();
        double posAve = 0;
//        System.out.println("max length: " + slArray[1]);
        for (int i = 0; i < numOfBlock; i++) {
            if (slArray[1] == array[i]) {
                lPos.add(i);
                //System.out.println(lPos.get(i));
                posAve += i;
            }
        }
        posAve /= lPos.size();

//        System.out.println("top: " + top);
//        System.out.println("middle: " + middle);
//        System.out.println("bottom: " + bottom);
//        System.out.println("average: " + posAve);
        double aori = Math.abs(posAve-bottom);
        double hukan = Math.abs(posAve-top);
        double nashi = Math.abs(posAve-middle);

        if(aori<hukan && aori<nashi) {
            result = 0;
            //System.out.println("アオリ");
        } else if(hukan<aori && hukan<nashi) {
            result = 1;
            //System.out.println("俯瞰");
        } else {
            //System.out.println("無し");
        }

        return result;
    }

    /**
     * 水平かどうか。0: 無し, 1: 水平
     * @param blocks
     * @param clNum
     * @return
     */
    public static int checkHorizon(List<List<Integer>> blocks, int clNum) {
        int result = 0;
        int[] horizonNum = countBlock(blocks, clNum, 1);
        int top = 7;
        int bottom = 0;

        for(int i=0; i<numOfBlock; i++) {
            if(blocks.get(i).contains(clNum)) {
                if(top >= i) {
                    top = i;
                } else if (bottom <= i) {
                    bottom = i;
                }
            }
        }
//        System.out.println("horizon top: " + top);
//        System.out.println("horizon bottom: " + bottom);

        boolean once = false;
        int firstHor = 0;
        int contCount = 0;
        int allCount = 0;
        boolean start = false;
        List<Integer> countList = new ArrayList<>();

        // 境界と同じ長さが連続する時
        for(int i=bottom; i<= top; i++) {
            if(i == top && horizonNum[i] >= numOfBlock-1) {
                start = true;
                allCount++;
            }

            if(start) {
                if(horizonNum[i] >= numOfBlock-1) {
                    contCount++;
                } else {
                    start = false;
                }
            } else if(i != top && horizonNum[i] >= numOfBlock-1) {
                start = true;
                contCount = 0;
                allCount++;
            }

            if(contCount >= 3) {
                System.out.println("allCOunt" + allCount);
                result = 1;
            }
//            if(horizonNum[i] >= numOfBlock-1 && i != 0) {
//                if(horizonNum[i-1] >= numOfBlock) {
//                    contCount ++;
//                } else {
//                    if(contCount > finalCount ) finalCount = contCount;
//                    contCount = 0;
//                }
//            }
//            if(finalCount > numOfBlock/2-1){
//                result = 1;
//            }
        }
        if(allCount == 1) {
            result = 1;
        }

        return result;
    }

    public static int checkHinomaru(double[][][] featureMat, List<Integer> backCluster) {
        int result = 1;
        int[] fNum = {1};   //慣性だけを使って判別する
        List<List<Double>> data = iB.convFeatData2CalData(featureMat, fNum);
        List<List<Double>> materials = new ArrayList<>();

        for(Integer cluster : backCluster) {
            List<Double> material =  data.get(cluster);
            materials.add(material);
        }
        List<Double> kanseiList = iB.calAve(materials);
        for(Double kansei : kanseiList) {
            if(kansei > 10.0) {
                result = 0;
            }
        }

        return result;
    }
//    /**
//     * 背景のテクスチャ特徴から日の丸構図かどうかを判別する。
//     * @param aveTexList
//     * @return
//     */
//    public static int checkHinomaru( List<List<Double>> aveTexList) {
//        int result = 0;
//        int[] fNumArray = new int[featureNumStr.length];
//        Map<Integer, Integer> convNum = new HashMap();
//
//        for(int i=0; i<featureNumStr.length; i++) {
//            fNumArray[i] = Integer.valueOf(featureNumStr[i]);
//        }
//        for(int i=0; i<fNumArray.length; i++) {
//            convNum.put(i, fNumArray[i]);
//        }
//        System.out.println("conv" + convNum);
//        System.out.println("aveTex" + aveTexList);
//
//        // 特徴量に慣性を用いる時10未満であれば一点透視
//        for(int i=0; i<aveTexList.size(); i++) {
//            for(int j=0; j<aveTexList.get(i).size(); j++) {
//                if(convNum.get(i) == 1) {
//                    if(aveTexList.get(i).get(j) > 10.0 ) result = 1;
//                }
//            }
//        }
//
//
//        return result;
//    }

    /**
     * 日の丸構図かどうかを判断
     * @param blocks
     * @param reCl
     * @return
     */
    public static int checkCenter(List<List<Integer>> blocks, int reCl) {
        // 縦の最長と横の最長が同じくらいであれば日の丸構図
        // ブロック数が集中しいているところの上下、左右の中心座標？
        // TODO 背景となる領域のテクスチャ特徴が日の丸構図以外の値と明らかに違うかどうか、違えばそれで判定する

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

        int top = 7;
        int bottom = 0;

        for(int i=0; i<numOfBlock; i++) {
            if(blocks.get(i).contains(reCl)) {
                if(top >= i) {
                    top = i;
                } else if (bottom <= i) {
                    bottom = i;
                }
            }
        }
        double middleHor = (first1+last1)/2.0 +  first1;
        double middleVer = (top+bottom)/2.0 + top;

        if(middleHor>=3.0 && middleHor<=5.0 && middleVer>=3.0 && middleVer<=5.0) {
            result = 1;
        }

        double percentage;
//        System.out.println("Check Center");
        //System.out.println("last-first: " + (last1-first1));
//        System.out.println("first1: " + first1);
        //System.out.println("bottom-top: " + (bottom-top));
//        System.out.println("bottom: "  + bottom);
//        if((last1-first1) >=  (bottom-top)) {
//            percentage = (last1-first1) / (bottom-top);
//        } else {
//            percentage = (bottom-top) / (last1-first1);
//        }
//
//        if(percentage<2.0) {
//            result = 1;
//        }

        return result;
    }

    /**
     *　一点透視、二点透視かどうかを判断
     * @param blocks
     * @param reCl: 現在みている領域の番号
     * @return
     */
    public static int checkPers(List<List<Integer>> blocks, int reCl) {
        int result = 2; // 0: 一点透視, 1: 二点透視, 2: 無し

        int left = 8;
        int right = 0;
        int[] horLen  = new int[numOfBlock];    // i列にreClが含まれていれば1, そうでなければ0

        int[] block = countBlock(blocks, reCl, 0);
        int[] sl = getSL(block);

        double percentage = (double)sl[1]/(double)sl[0];
        //if(percentage>1.5) {
        // 最大と最小の差が2未満であれば2にする
        //if(!(Math.abs(sl[0]-sl[1])<2)) {

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

            List<Integer> sPos = getPos(sl[0], block);
            List<Integer> lPos = getPos(sl[1], block);

            // lPosの外側の一座標を集める
            List<Integer> smallTlPos = new ArrayList<>();
            List<Integer> bigTlPos = new ArrayList<>();
            boolean needCheckSmall = false;
            boolean needCheckBig = false;

            for(int i=0; i<numOfBlock; i++) {
                if(i<lPos.get(0) && block[i] != 0) {
                    smallTlPos.add(i);
                    // 画面内に収まっていなければ
                    if(i == 0 && block[i] > 1) {
                        needCheckSmall = true;
                    }
                } else if(i>lPos.get(lPos.size()-1) && block[i] != 0) {
                    bigTlPos.add(i);
                    // 画面内に収まっていなければ
                    if(i == numOfBlock-1 && block[i] > 1) {
                        needCheckBig = true;
                    }
                }
            }

            if(needCheckSmall && needCheckBig) {
                result = 1;
            } else {
                if(!needCheckSmall && smallTlPos.size()>0) {
                    int changeVarSmall = block[lPos.get(0)]-block[lPos.get(0)-1];
                    for(int i=1; i<smallTlPos.size(); i++) {
                        changeVarSmall += (block[lPos.get(0)-i]-block[lPos.get(0)-i-1]);
                    }
                    if(changeVarSmall != 0) {
                        int vanishingPSmall = smallTlPos.size() / changeVarSmall * block[lPos.get(0)];
                        // TODO 判定の値については要検討
                        if (vanishingPSmall < numOfBlock) needCheckSmall = true;
                    }
                }

                if(!needCheckBig && bigTlPos.size()>0) {
                    int changeVarBig = block[lPos.get(0)]-block[lPos.get(0)+1];
                    for(int i=1; i<bigTlPos.size()-1; i++) {
                        changeVarBig += (block[lPos.get(0)+i]-block[lPos.get(0)+i+1]);
                    }
                    if(changeVarBig != 0) {
                        int vanishingPBig = smallTlPos.size() / changeVarBig * block[lPos.get(0)];
                        // TODO 判定の値については要検討
                        if (vanishingPBig < numOfBlock) needCheckSmall = true;
                    }
                }
                if(needCheckSmall && needCheckBig) {
                    result = 1;
                } else if(needCheckSmall || needCheckBig) {
                    result = 0;
                }
            }


//            //左端が最小値に近い時,最大値の向こうに値が存在すれば二点透視
//            if(lPos.get(0)<numOfBlock) {
//                for (int i = lPos.get(0) + 1; i < numOfBlock; i++) {
//                    if (block[i] != 0 && block[i]<block[lPos.get(0)]) {
//                        flag2 = true;
//                        //System.out.println("true");
//                    }
//                }
//            }
//
//            if(lPos.get(0)!=0) {
//                for (int i = lPos.get(0) - 1; i>0; i--) {
//                    if (block[i] != 0 && block[i]<block[lPos.get(0)]) {
//                        flag2 = true;
//                        //System.out.println("true");
//                    }
//                }
//            }



//            if(diffLM <diffLS && diffLM<diffLL) {
//                //左端が最小値に近い時,最大値の向こうに値が存在すれば二点透視
////                for(int i=lPos.get(0)+1; i<numOfBlock; i++) {
////                    if(block[i] != 0) {
////                        flag2 = true;
////                    }
////                }
//                flag2 = true;
//            } else if(diffSS < diffSM || diffSL <diffSM) {
//                flag1 = true;
//            }
//            if(diffSS < diffSM || diffSL <diffSM) {
//                flag1 = true;
//            }
//
//
//            if(flag2) {
//                result = 1;
//            } else if(flag1) {
//                result = 0;
//            }
        //}
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
