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

    private static IntegrateBlock iB = new IntegrateBlock();

    public static void main(String[] args) throws IOException {
        //iB.first();
    }

    /**
     * 縦と横それぞれに連続したブロックが何個あるかを数える。
     * @param blocks
     * @param clNum
     */
    public static void calSLPos(List<List<Integer>> blocks, int clNum) {
        List<List<Integer>> result = new ArrayList<>();
        int[][] verLon = new int[clNum][numOfBlock];
        int[][] horLon = new int[clNum][numOfBlock];
        //Arrays.fill(verLon, 0);

        for(int i=0; i<numOfBlock; i++) {
            for(int j=0; j<numOfBlock; j++) {
                for(int k=0; k<clNum; k++) {
                    if(k == blocks.get(j).get(i) && j>0 && blocks.get(j).get(i) == blocks.get(j-1).get(i)) {
                        verLon[k][i]++;
                    }
                    if(k == blocks.get(i).get(j) && j>0 && blocks.get(i).get(j) == blocks.get(i).get(j-1)) {
                        horLon[k][i]++;
                    }
                }
            }
        }

        System.out.println("Vertical");
        for(int i=0; i<verLon.length; i++) {
            for(int j=0; j<verLon[i].length; j++) {
                System.out.print(verLon[i][j]);
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

        //return  result;
    }

    public static void getTexAve() {

    }

}
