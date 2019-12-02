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

    private int numOfBlock = Integer.valueOf(propertyUtil.getProperty("numOfBlock"));
    private int imageSize = Integer.valueOf(PropertyUtil.getProperty("imageSize"));

    private static IntegrateBlock iB = new IntegrateBlock();

    public static void main(String[] args) throws IOException {
        iB.first();
    }

    public List<List<Integer>> calSLPos(List<List<Integer>> blocks, int clNum) {
        List<List<Integer>> result = new ArrayList<>();
        int[][] verLon = new int[clNum][numOfBlock];
        int[][] horLon = new int[clNum][numOfBlock];
        //int[][] sh = new int[clNum][numOfBlock];
        Arrays.fill(verLon, 0);
        //Arrays.fill(sh, numOfBlock+1);

        for(int i=0; i<numOfBlock; i++) {
            for(int j=0; j<numOfBlock; j++) {
                for(int k=0; k<clNum; k++) {
                    if(k == blocks.get(j).get(i) && k>0 && blocks.get(j).get(i) == blocks.get(j-1).get(i)) {
                        verLon[k][i]++;
                    }
                    if(k == blocks.get(i).get(j) && k>0 && blocks.get(i).get(j) == blocks.get(i).get(j-1)) {
                        horLon[k][i]++;
                    }
                }
            }
        }
        Arrays.sort(verLon);
    return  result;
    }

}
