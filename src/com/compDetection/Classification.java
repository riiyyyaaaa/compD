package com.compDetection;

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

    public static void main(String[] args) {

    }

    public List<List<Integer>> calVerticalgPos(List<List<Integer>> blocks, int clNum) {
        List<List<Integer>> result = new ArrayList<>();
        int[][] lon = new int[clNum][numOfBlock];
        //int[][] sh = new int[clNum][numOfBlock];
        Arrays.fill(lon, 0);
        //Arrays.fill(sh, numOfBlock+1);

        for(int i=0; i<numOfBlock; i++) {
            for(int j=0; j<numOfBlock; j++) {
                for(int k=0; k<clNum; k++) {
                    if(k == blocks.get(j).get(i) && k>0 && blocks.get(j).get(i) == blocks.get(j-1).get(i)) {
                        lon[k][i]++;
                    }
                }
            }
        }
    return  result;
    }

}
