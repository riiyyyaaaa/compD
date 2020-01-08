package com.compDetection;

import com.sun.scenario.animation.shared.ClipEnvelope;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * TexGLCMを基に似たテクスチャのブロックを統合する
 * ウォード法を用いてクラスタリング
 */
public class IntegrateBlock {
    private static PropertyUtil propertyUtil;
    private static IntegrateBlock iB = new IntegrateBlock();
    private static Block image;
    private static ImageUtility iu;
    private static Classification cl;

    private String[] featureNumStr = propertyUtil.getProperty("featureNum").split(",");
    private int numOfBlock = Integer.valueOf(propertyUtil.getProperty("numOfBlock"));
    private int imageSize = Integer.valueOf(propertyUtil.getProperty("imageSize"));
    private int lengthOfASide = Integer.valueOf(propertyUtil.getProperty("imageSize")); //画像の一片の長さ
    private int bSize = lengthOfASide/numOfBlock;// 1つのブロックの一辺の長さ
    private List<List<Integer>> group = new ArrayList<>();
    private List<List<Double>> process = new ArrayList<>(); // 25回分の統合したクラスと距離
    private int rest = 3;
    private List<List<List<Integer>>> clusterList = new ArrayList<>();  // 後ろからnumOfBlock個のクラスタリング状況

    private List<List<File>> searchList = new ArrayList<>(); // クラスタリング結果をリスト化、検索用
    private File[][] searchArray = new File[6][30];

    double blockNum = 0.0;   // 最終的な分割数
    //List<List<List<Integer>>> blocks = new ArrayList<>();     // numOfBlock*numObBlockの配列を0~(blockNum-1)でラベリングした配列,rest個

    public IntegrateBlock() {
        for(int i=0; i<numOfBlock*numOfBlock; i++) {
            List list = new ArrayList();
            for(int j=0; j<numOfBlock*numOfBlock; j++) {
                list.add(0);
            }
            //Collections.addAll(list, 0);
            group.add(i, list);
            //System.out.println(group);

        }

    }
    public void reset() {
        for(int i=0; i<numOfBlock*numOfBlock; i++) {
            List list = new ArrayList();
            for(int j=0; j<numOfBlock*numOfBlock; j++) {
                list.add(0);
            }
            //Collections.addAll(list, 0);
            group.add(i, list);
            //System.out.println(group);
        }
        process.clear();
        clusterList.clear();
    }


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
    public static void main(String[] args) throws IOException {
        //iB.first_test();
        iB.first();

        // tset
//        String cd = new File(".").getAbsoluteFile().getParent();
//        File dir = new File(cd + "\\src\\input\\example\\");
//        File[] list = dir.listFiles();
//
//        for(int i=0; i<list.length; i++) {
//            BufferedImage read = ImageIO.read(list[i]);
//            read = iu.scaleImage(read, iB.imageSize, iB.imageSize);
//
//            BufferedImage bf = new BufferedImage(iB.lengthOfASide, iB.lengthOfASide, BufferedImage.TYPE_INT_RGB);
//            Graphics gr = bf.createGraphics();
//            gr.drawImage(read, 0,0, null);
//            gr.dispose();
//
//            File file = new File(cd + "\\src\\output\\IntegrateOutput\\example" + i + ".jpg");
//            ImageIO.write(bf, "jpg", file);
//        }

    }

    public void first() throws IOException {
        boolean testMode = false;
        boolean testHukan = false;
        String cd = new File(".").getAbsoluteFile().getParent();
        File dir = new File(cd + "\\src\\input\\");
        if(testMode) dir = new File(cd + "\\src\\input_hinomaru\\");
        if(testHukan) dir = new File(cd + "\\src\\input_hukan\\");

        ArrayList<File> hinoList = new ArrayList<>();
        ArrayList<File> ittenList = new ArrayList<>();
        ArrayList<File> nitenList = new ArrayList<>();
        ArrayList<File> aoriList = new ArrayList<>();
        ArrayList<File> hukanList = new ArrayList<>();
        ArrayList<File> suiheiList = new ArrayList<>();

        File[] list = dir.listFiles();
        for(int i=0; i<list.length; i++) {
            System.out.println("i: " + i);
            System.out.println(list[i]);
            int[][][][] mat_test = TexGLCM.calGLCM(list[i]);

//            for(int j=0; j<mat_test.length; j++) {
//                for(int k=0; k<mat_test[j].length; k++) {
//                    for(int l=0; l<mat_test[j][k].length; l++) {
//                        for(int m=0; m<mat_test[j][k][l].length; m++) {
//                            if(Objects.isNull(mat_test[j][k][l][m])) {
//                                System.out.println("N: " + j + ": " + k +" :"  + l + " :" + m);
//                                System.out.println(mat_test[j][k][l][m]);
//
//                            }
//                        }
//                    }
//                }
//            }
            double[][][] featureMat = TexGLCM.calFeature((mat_test));
//            for(int j=0; j<featureMat.length; j++) {
//                for(int k=0; k<featureMat[j].length; k++) {
//                    for(int l=0; l<featureMat[j][k].length; l++) {
//                        if(Double.isNaN(featureMat[j][k][l])) {
//                            System.out.println("N: " + j + ": " + k +" :"  + l);
//                            System.out.println(featureMat[j][k][l]);
//                        }
//                    }
//                }
//            }
            BufferedImage read = ImageIO.read(list[i]);
            BufferedImage output = new BufferedImage(iB.lengthOfASide*iB.numOfBlock, iB.lengthOfASide+400, BufferedImage.TYPE_INT_RGB);
            BufferedImage resultOutput = new BufferedImage(lengthOfASide*2, lengthOfASide*7/6, BufferedImage.TYPE_INT_RGB);
            BufferedImage simpleOutput = new BufferedImage(lengthOfASide*3/2, lengthOfASide, BufferedImage.TYPE_INT_RGB);
            Graphics gr = output.createGraphics(); // クラスタリング状況を出力
            Graphics outputGr = resultOutput.createGraphics(); // 最終的な結果の出力
            Graphics simpleGr = simpleOutput.createGraphics(); // 簡易的な結果出力


            read = iu.scaleImage(read, iB.imageSize, iB.imageSize);
            iB.calFirstDistanceMat(featureMat);
            gr.drawImage(read, 0, 0, null);
            outputGr.drawImage(read, 0, 0, null);
            simpleGr.drawImage(read, 0, 0, null);
            List<List<List<Integer>>> resultBlock = new ArrayList();


            for(int j=0; j<iB.numOfBlock*iB.numOfBlock-2; j++) {
                //System.out.println("count: " + j);

                iB.calDistanceMatRepeat(featureMat);
                List<List<Integer>> cluster = iB.showIntegration();
                BufferedImage pieceImage = iB.showIntegrationBlock(cluster, read);

//                if(j>iB.numOfBlock*iB.numOfBlock-10) {
//                    List<Double> tex = iB.getAveTex(iB.showIntegration());
//                    for(int k=0; k<tex.size(); k++) {
//                        gr.drawString(String.valueOf(tex.get(k)), ((j + 1) % 6) * iB.lengthOfASide + 150, iB.lengthOfASide * 4 + ((j + 1) / 6) * 80 + 50 + k*5);
//                    }
//                }

                //gr.drawImage(pieceImage, ((j+1)%iB.numOfBlock)*iB.lengthOfASide, ((j+1)/iB.numOfBlock)*iB.lengthOfASide, null);

                // 最下段のみ出力、全部出力の時は外す
                if(j >= iB.numOfBlock*(iB.numOfBlock-1)-1) {
                    gr.drawImage(pieceImage, ((j+2)%iB.numOfBlock)*iB.lengthOfASide, 0, null);

                    List<List<Integer>> blocks = iB.makeBlockArray(cluster);
                    int clNum = numOfBlock*numOfBlock-2-j;

                    resultBlock.add(blocks);
                    //System.out.println(blocks);
                    cl.calPos(blocks, clNum, 0);
                    clusterList.add(cluster);
                }

            }

            gr.setColor(Color.WHITE);
            gr.setFont(new Font("", Font.PLAIN, 40));

            for (int j=0; j<iB.numOfBlock*iB.numOfBlock-1; j++) {
                // 最下段の距離出力、全部出力の時は外す
                if(j >= iB.numOfBlock*(iB.numOfBlock-1)-1) {
                    gr.drawString("" + iB.process.get(j).get(0).intValue() + ", " + iB.process.get(j).get(1).intValue() + ": " + iB.process.get(j).get(2).intValue(), (j%(iB.numOfBlock)+1)*(iB.lengthOfASide) + 100, iB.lengthOfASide + 50);
                }
            }

            int resultClNum = iB.drawRedFrame(gr);
            int resultStage = numOfBlock*numOfBlock-resultClNum;
            BufferedImage pieceImage = iB.showIntegrationBlock(clusterList.get(numOfBlock-1-resultClNum), read);
            outputGr.drawImage(pieceImage, lengthOfASide, 0, null);
            List<List<Double>> aveList = new ArrayList<>();

//            System.out.println("分割数: " + resultClNum);
//            System.out.println("Stage: " + resultStage);
            int x=0;
            for (List<Integer> cluster : clusterList.get(numOfBlock-1-resultClNum)) {
                List<Double> texAve = cl.getTexAve(featureMat, cluster);
                //分割数だけtexAveがある
                aveList.add(texAve);
                //((Graphics2D) gr).drawString("Ave: " + texAve, 50, iB.lengthOfASide + 199*x + 100);
                //((Graphics2D) gr).drawString("cluster " + x + " : s" + cluster, 50, iB.lengthOfASide + 199*x + 100);
                x++;
            }
            //　背景の領域がどれかを出力
            int backNum =  cl.judgeBack(aveList);
            System.out.println("Background: " + backNum);
            gr.drawString("background: " + backNum, 50, iB.lengthOfASide+40);

            int resultNum = numOfBlock-resultClNum;
            gr.drawString("一点、二点", 50, iB.lengthOfASide + 50 * (0 + 1) + 200);
            gr.drawString("アオリ、俯瞰", 400, iB.lengthOfASide + 50 * (0 + 1) + 200);
            gr.drawString("水平", 750, iB.lengthOfASide + 50 * (0 + 1) + 200);
            gr.drawString("日の丸", 1100, iB.lengthOfASide + 50 * (0 + 1) + 200);

            //結果出力
            Map<String, Integer> compResult = new HashMap<>();

            // 構図番号を出力
            //int center = cl.checkCenter(resultBlock.get(resultNum-1), j);
            int center = cl.checkHinomaru(featureMat, clusterList.get(numOfBlock-1-resultClNum).get(backNum));

            if(center == 0) {
                gr.drawString("無し", 1100, iB.lengthOfASide + 50 * (1) + 400);
                compResult.put("日の丸構図", 0);
            } else {
                gr.drawString("日の丸構図", 1100, iB.lengthOfASide + 50 * (1) + 400);
                compResult.put("日の丸構図", 1);
            }
            for(int j=0; j<resultClNum; j++) {
                if (j != backNum) {
                    int pers = cl.checkPers(resultBlock.get(resultNum-1), j, center);

                    if (pers == 0) {
                        gr.drawString(j + "一点透視図法", 50, iB.lengthOfASide + 50 * (j + 1) + 400);
                        compResult.put("一点透視図法", 1);
                    } else if (pers == 1) {
                        gr.drawString(j + "二点透視図法", 50, iB.lengthOfASide + 50 * (j + 1) + 400);
                        compResult.put("二点透視図法", 1);
                    } else {
                        gr.drawString(j + "無し", 50, iB.lengthOfASide + 50 * (j + 1) + 400);
                        compResult.put("一点透視図法", 0);
                        compResult.put("二点透視図法", 0);
                    }

                    int eye = cl.checkEyeLevel(resultBlock.get(resultNum-1), j, center);

                    if (eye == 0) {
                        gr.drawString(j + "アオリ", 400, iB.lengthOfASide + 50 * (j + 1) + 400);
                        compResult.put("アオリ", 1);
                    } else if (eye == 1) {
                        gr.drawString(j + "俯瞰", 400, iB.lengthOfASide + 50 * (j + 1) + 400);
                        compResult.put("俯瞰", 1);
                    } else {
                        gr.drawString(j + "無し", 400, iB.lengthOfASide + 50 * (j + 1) + 400);
                        compResult.put("アオリ", 0);
                        compResult.put("俯瞰", 0);
                    }

                    int horizon = cl.checkSuihei(resultBlock.get(resultNum-1), j);

                    if(horizon == 0) {
                        gr.drawString(j + "無し", 750, iB.lengthOfASide + 50 * (j + 1) + 400);
                        compResult.put("水平", 0);
                    } else {
                        gr.drawString(j + "水平", 750, iB.lengthOfASide + 50 * (j + 1) + 400);
                        compResult.put("水平", 1);
                    }
                }
            }

            gr.setFont(new Font("", Font.PLAIN, 200));
            //outputGr.drawString("Result ", 50, lengthOfASide+50);
            String resultStr = "";

            Map<String, Integer> compMap = new HashMap<String, Integer>() {
                {put("日の丸構図", 0);}
                {put("一点透視図法", 1);}
                {put("二点透視図法", 2);}
                {put("アオリ", 3);}
                {put("俯瞰", 4);}
                {put("水平", 5);}
            };

            simpleGr.setFont(new Font("", Font.PLAIN, 30));
            int pos = 0;
            for (String resultKey : compResult.keySet()) {
                if (compResult.get(resultKey) == 1) {
                    simpleGr.drawString(resultKey, lengthOfASide+10, 40 + pos*45);
                    pos++;
                    resultStr += resultKey + "     ";
                    int compNum = compMap.get(resultKey);
                    if(compNum == 0) {
                        hinoList.add(list[i]);
                    } else if(compNum == 1) {
                        ittenList.add(list[i]);
                    } else if(compNum == 2) {
                        nitenList.add(list[i]);
                    } else if(compNum == 3) {
                        aoriList.add(list[i]);
                    } else if(compNum == 4) {
                        hukanList.add(list[i]);
                    } else {
                        suiheiList.add(list[i]);
                    }
                }
            }
            outputGr.setFont(new Font("", Font.PLAIN, 30));
            outputGr.drawString(resultStr, 50, lengthOfASide+40);

            gr.dispose();
            outputGr.dispose();
            simpleGr.dispose();
            File resultFile = new File(cd + "\\src\\output\\IntegrateOutput\\result" + i + ".jpg");
            File finalResultFile = new File(cd + "\\src\\output\\IntegrateOutput\\finalResult" + i +".jpg");
            if(testMode) {
                resultFile = new File(cd + "\\src\\output\\output_hinomaru\\result" + i + ".jpg");
            }
            File simpleResult = new File(cd + "\\src\\output\\IntegrateOutput\\simpleResult" + i + ".jpg");
            ImageIO.write(output, "jpg", resultFile);
            ImageIO.write(resultOutput, "jpg", finalResultFile);
            ImageIO.write(simpleOutput, "jpg", simpleResult);

            for(int j=0; j<iB.numOfBlock*iB.numOfBlock; j++) {
                System.out.println("group(" + j+ "): " + iB.group.get(j));
            }
            System.out.println();
            for(int j=0; j<iB.numOfBlock*iB.numOfBlock-1; j++) {
                System.out.println("Integ(" + j + ")" + iB.process.get(j));
            }

            iB.reset();

        }

        // 検索結果の出力、 TODO 後で書き直す
        searchList.add(hinoList);
        searchList.add(ittenList);
        searchList.add(nitenList);
        searchList.add(aoriList);
        searchList.add(hukanList);
        searchList.add(suiheiList);

        System.out.println(searchList);
        for(int i=0; i<searchList.size(); i++) {
            if(searchList.get(i).size() != 0) {
                BufferedImage searchOutput = new BufferedImage(lengthOfASide * searchList.get(i).size(), lengthOfASide, BufferedImage.TYPE_INT_RGB);
                Graphics searchGr = searchOutput.createGraphics();
                for (int j = 0; j < searchList.get(i).size(); j++) {
                    BufferedImage read = ImageIO.read(searchList.get(i).get(j));
                    read = iu.scaleImage(read, iB.imageSize, iB.imageSize);
                    searchGr.drawImage(read, lengthOfASide * (j), 0, null);
                }
                searchGr.dispose();

                File searchFile = new File(cd + "\\src\\output\\IntegrateOutput\\comp" + i + ".jpg");
                ImageIO.write(searchOutput, "jpg", searchFile);
            }
        }
    }

    /**
     * 日の丸構図とそれ以外のテクスチャ特徴を比べる。
     * @throws IOException
     */
    public void first_test() throws IOException {
        String cd = new File(".").getAbsoluteFile().getParent();
        File dir_normal = new File(cd + "\\src\\input_normal\\");
        File dir_hinomaru = new File(cd + "\\src\\input_hinomaru\\");

        File[] list_normal = dir_normal.listFiles();
        File[] list_hinomaru = dir_hinomaru.listFiles();

        BufferedImage output_normal = new BufferedImage(lengthOfASide*numOfBlock, lengthOfASide*numOfBlock, BufferedImage.TYPE_INT_RGB);
        BufferedImage output_hinomaru = new BufferedImage(lengthOfASide*numOfBlock, lengthOfASide*numOfBlock, BufferedImage.TYPE_INT_RGB);

        Graphics normalGr = output_normal.createGraphics();
        Graphics hinomalGr = output_hinomaru.createGraphics();

        double[] hinomaruAve = new double[featureNumStr.length*4];
        double[] normalAve = new double[featureNumStr.length*4];

        for(int i=0; i<list_normal.length; i++) {
            int[][][][] mat = TexGLCM.calGLCM(list_normal[i]);
            double[][][] featureMat = TexGLCM.calFeature(mat);
            BufferedImage read = ImageIO.read(list_normal[i]);
            BufferedImage output = new BufferedImage(iB.lengthOfASide*iB.numOfBlock, iB.lengthOfASide*iB.numOfBlock, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = output.createGraphics();

            read = iu.scaleImage(read,iB.imageSize, iB.imageSize);
            iB.calFirstDistanceMat(featureMat);
            graphics.drawImage(read, 0, 0, null);
            List<List<List<Integer>>> resultBlock = new ArrayList();

            for(int j=0; j<numOfBlock*numOfBlock-2; j++) {
                iB.calDistanceMatRepeat(featureMat);
                List<List<Integer>> cluster = iB.showIntegration();

                if(j >= iB.numOfBlock*(iB.numOfBlock-1)-1) {
                    List<List<Integer>> blocks = iB.makeBlockArray(cluster);
                    int clNum = numOfBlock*numOfBlock-2-j;
                    resultBlock.add(blocks);
                    clusterList.add(cluster);
                }
            }
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("", Font.PLAIN, 40));

            int resultClNum = iB.drawRedFrame((graphics));
            List<List<Double>> aveList = new ArrayList<>();

            //System.out.println("texture");
            // クラスタの特徴量、方向ごとの平均
            for (List<Integer> cluster : clusterList.get(numOfBlock-1-resultClNum)) {
                List<Double> texAve = cl.getTexAve(featureMat, cluster);
                aveList.add(texAve);
            }
            //System.out.println(aveList + "\n\n");


            int backNum = cl.judgeBack(aveList);
            normalGr.drawString( i + ": " + aveList.get(backNum), 10, (i+1)*50);

            for(int j=0; j<featureNumStr.length*4; j++) {
                normalAve[j] += aveList.get(backNum).get(j);
            }

            iB.reset();
        }
        for(int j=0; j<featureNumStr.length*4; j++) {
            normalAve[j] /= list_normal.length;
        }
        for(int i=0; i<featureNumStr.length; i++) {
            for(int j=0; j<4; j++) {
                normalGr.drawString(" " + normalAve[i*4+j], 2500+j*200, 100+i*100);
            }
        }

        normalGr.dispose();
        File normalFile = new File(cd + "\\src\\output\\output_hinomaru\\normal.jpg");
        ImageIO.write(output_normal, "jpg", normalFile);

        for(int i=0; i<list_hinomaru.length; i++) {
            int[][][][] mat = TexGLCM.calGLCM(list_hinomaru[i]);
            double[][][] featureMat = TexGLCM.calFeature(mat);
            BufferedImage read = ImageIO.read(list_hinomaru[i]);
            BufferedImage output = new BufferedImage(iB.lengthOfASide*iB.numOfBlock, iB.lengthOfASide*iB.numOfBlock, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = output.createGraphics();

            read = iu.scaleImage(read,iB.imageSize, iB.imageSize);
            iB.calFirstDistanceMat(featureMat);
            graphics.drawImage(read, 0, 0, null);
            List<List<List<Integer>>> resultBlock = new ArrayList();

            for(int j=0; j<numOfBlock*numOfBlock-2; j++) {
                iB.calDistanceMatRepeat(featureMat);
                List<List<Integer>> cluster = iB.showIntegration();

                if(j >= iB.numOfBlock*(iB.numOfBlock-1)-1) {
                    List<List<Integer>> blocks = iB.makeBlockArray(cluster);
                    int clNum = numOfBlock*numOfBlock-2-j;
                    resultBlock.add(blocks);
                    clusterList.add(cluster);
                }
            }
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("", Font.PLAIN, 40));

            int resultClNum = iB.drawRedFrame((graphics));
            List<List<Double>> aveHinomaruList = new ArrayList<>();

            for (List<Integer> cluster : clusterList.get(numOfBlock-1-resultClNum)) {
                List<Double> texAve = cl.getTexAve(featureMat, cluster);
                aveHinomaruList.add(texAve);
            }
            int backNum = cl.judgeBack(aveHinomaruList);
            hinomalGr.drawString(i + ": " + aveHinomaruList.get(backNum), 10, (i+1)*50);

            for(int j=0; j<featureNumStr.length*4; j++) {
                hinomaruAve[j] += aveHinomaruList.get(backNum).get(j);
            }

            iB.reset();
        }
        for(int j=0; j<featureNumStr.length*4; j++) {
            hinomaruAve[j] /= list_hinomaru.length;
        }
        for(int i=0; i<featureNumStr.length; i++) {
            for(int j=0; j<4; j++) {
                hinomalGr.drawString(" " + hinomaruAve[i*4+j], 2500 + j*200, 500+i*100);
            }
        }
        hinomalGr.dispose();
        File hinomaruFile = new File(cd + "\\src\\output\\output_hinomaru\\hinomaru.jpg");
        ImageIO.write(output_hinomaru, "jpg", hinomaruFile);

        for(int i=0; i<normalAve.length; i++) {
            System.out.println("normal Average " + i + ": " + normalAve[i]);

        }
        for(int i=0; i<normalAve.length; i++) {
            System.out.println("hinomaru Average: " + i + ": " + hinomaruAve[i]);
        }

    }


    /**
     * 初期の距離行列を計算
     * @param featureMat
     * @return
     */
    public List<List<Double>> calFirstDistanceMat(double[][][] featureMat) {
        // 使用する特徴の番号をcompDetection.propertiesから持ってくる
        int[] fNum = new int[featureNumStr.length];
        for(int i=0; i<featureNumStr.length; i++) {
            fNum[i] = Integer.valueOf(featureNumStr[i]);
        }

        int fNumLen = featureMat.length;

        List<List<Double>> data = convFeatData2CalData(featureMat, fNum);
        List<List<Double>> disMat = new ArrayList<>();

        // Data for test
        List<Double> testData1 = Arrays.asList(5.0, 4.0, 0.0, 1.0, 2.0, 8.0, 10.0, 1.0);
        List<Double> testData2 = Arrays.asList(1.0, 2.0, 5.0, 4.0, 9.0, 10.0, 8.0, 3.0);
        List<Double> tsstDatg2 = Arrays.asList(3.0, 4.0, 1.0, 2.0, 8.0, 9.0, 8.0, 1.0);
        List<Double> testData3 = Arrays.asList(3.0, 3.0, 0.0, 4.0, 9.0, 8.0, 9.0, 2.0);

        for(int i=0; i<numOfBlock*numOfBlock; i++) {
            List<Double> horizon = new ArrayList<>();
            for(int j=0; j<numOfBlock*numOfBlock; j++) {
                if(i<j) {

                    List<List<Double>> material = new ArrayList<>();
                    material.add(data.get(i));
                    material.add(data.get(j));
//                    material.add(testData1);
//                    material.add(testData2);
                    //System.out.println(calAve(material));
                    double dis = calDis(material, calAve(material));
//                     if (Double.isNaN(dis)) {
//                        System.out.println("!!!!!!!!!!!!!");
//                    }
                    horizon.add(j, dis);
                    //System.out.println(calDis(material, calAve(material)));

                } else if(i>j) {
                    horizon.add(j, disMat.get(j).get(i));
                } else {
                    horizon.add(j, 0.0);
                }
            }

            disMat.add(horizon);
            //System.out.println(horizon);
        }

        //System.out.println("column: " + disMat.size() + ", line: " + disMat.get(0).size() + ", data size: " + disMat.get(0).size());

        //距離行列の表示
//        for(int i=0; i<disMat.size(); i++) {
//            for(int j=0; j<disMat.get(i).size(); j++) {
//                System.out.print(disMat.get(i).get(j) + " ");
//            }
//            System.out.println();
//        }

        calMinClass(disMat);

        return disMat;
    }

    public void calDistanceMatRepeat(double[][][] featureMat) {
        // 使用する特徴の番号をcompDetection.propertiesから持ってくる
        int[] fNum = new int[featureNumStr.length];
        for(int i=0; i<featureNumStr.length; i++) {
            fNum[i] = Integer.valueOf(featureNumStr[i]);
        }

        List<List<Double>> data = convFeatData2CalData(featureMat, fNum);
        List<List<Double>> disMat = new ArrayList<>();

        for(int i=0; i<numOfBlock*numOfBlock; i++) {
            List<Double> horizon = new ArrayList<>();
            for(int j=0; j<numOfBlock*numOfBlock; j++) {
                if(i<j) {
                    double distance = 0.0;
                    //System.out.println("gr(" + i + ")" + this.group.get(i));
                    if (this.group.get(i).get(j) != 1 ) {
                        //System.out.println("i: " + i + ", j: " + j);
                        List<List<Double>> material = new ArrayList<>();
                        List<List<Double>> material1 = new ArrayList<>();
                        List<List<Double>> material2 = new ArrayList<>();

                        //material = makeData4Ave(i, j, data);
                        material1 = makeDataForAve(i, data);
                        material2 = makeDataForAve(j, data);
                        material.addAll(material1);
                        material.addAll(material2);
                        distance = calDis(material, calAve(material)) - calDis(material1, calAve(material1)) - calDis(material2, calAve(material2));
                        //System.out.println("i: "+ i + ", j: " + j);
                        //System.out.println("resultDis: " + distance + ", allDis: " + calDis(material, calAve(material)) + ", 1Dis: " + calDis(material1, calAve(material1)) + ", 2Dis: " + calDis(material2, calAve(material2)));
                        horizon.add(j, Math.abs(distance));
                    } else {
                        horizon.add(j, distance);
                    }
                } else if(i>j) {
                    if(group.get(i).get(j) != 1) {
                        horizon.add(j, disMat.get(j).get(i));
                    } else {
                        horizon.add(j, 0.0);
                    }
                } else {
                    horizon.add(j, 0.0);
                }

            }
            disMat.add(horizon);
        }

        // 距離行列の表示
//        for(int i=0; i<disMat.size(); i++) {
//            for(int j=0; j<disMat.get(i).size(); j++) {
//                System.out.printf("%6.1f ", disMat.get(i).get(j));
//                //System.out.print(disMat.get(i).get(j) + " ");
//            }
//            System.out.println();
//        }
        calMinClass(disMat);

//        System.out.println("!!!!!!!!!!!!!!!!!!!!!");
//        showIntegration();
//        System.out.println("!!!!!!!!!!!!!!!!!!!!!");
    }

    /**
     * Calculate minimum value of distance between classes
     *
     * @param disMat
     * @return
     */
    public List<Integer> calMinClass(List<List<Double>> disMat) {
        int matLen = disMat.size();
        double min = 10000;
        List<Integer> classes = new ArrayList<>();

        for(int i=0; i<matLen; i++) {
            for(int j=i+1; j<matLen; j++) {
                if(min>=disMat.get(i).get(j) && disMat.get(i).get(j) != 0){
                    min = disMat.get(i).get(j);
                    classes = Arrays.asList(i, j);
                }
            }
        }

        //System.out.println(classes);
        //System.out.println(classes + "\n");
        refInteg(classes.get(0), classes.get(1));
        List processData = Arrays.asList((double)classes.get(0), (double)classes.get(1), min);
        //System.out.println(processData);
        this.process.add(processData);

        return classes;
    }


    /**
     * Add value of 1 to group(List)
     * Show progress of Clustering
     * @param c1
     * @param c2
     * @return
     */
    public List<List<Integer>> refIntegration(int c1, int c2) {
        System.out.println("class1: " + c1 + ", class2: " + c2);
        //System.out.println(this.group.get(c1));

        this.group.get(c1).set(c2, 1);
        this.group.get(c2).set(c1, 1);

        System.out.println(this.group.get(c1));
        System.out.println(this.group.get(c2));
        for(int i=0; i<numOfBlock*numOfBlock; i++) {

            if(this.group.get(c1).get(i) == 1 && i != c2) {
                System.out.println("i1: " + i);
                this.group.get(c2).set(i, 1);
                System.out.println("i " + this.group.get(i));
                this.group.get(i).set(c2,1);
                System.out.println("i " + this.group.get(i));
            } else if(this.group.get(c2).get(i) == 1 && i != c1) {
                System.out.println("i2: " + i);
                this.group.get(c1).set(i, 1);
                System.out.println("i " + this.group.get(i));
                this.group.get(i).set(c1, 1);
                System.out.println("i " + this.group.get(i));
            }
        }
        System.out.println();
        System.out.println(this.group.get(c1));
        System.out.println(this.group.get(c2));

        return group;
    }

    public List<List<Integer>> refInteg(int class1, int class2) {
        this.group.get(class1).set(class2, 1);
        this.group.get(class2).set(class1, 1);

        List<Integer> partOfGroup = new ArrayList<>();
        for(int i=0; i<numOfBlock*numOfBlock; i++) {
            if(this.group.get(class1).get(i) == 1 || this.group.get(class2).get(i) == 1) {
                partOfGroup.add(1);
            } else {
                partOfGroup.add(0);
            }
        }

        for(int i=0; i<numOfBlock*numOfBlock; i++) {
            if(partOfGroup.get(i) == 1) {
                for(int j=0; j<numOfBlock*numOfBlock; j++) {
                    if(partOfGroup.get(j) == 1 && i != j) {
                        this.group.get(j).set(i, 1);
                    }
                }
            }
        }

        //統合したグループ2つの表示
//        System.out.println();
//        System.out.println("group(" + class1 + ")" +this.group.get(class1));
//        System.out.println("group(" + class2 + ")" + this.group.get(class2));
//        System.out.println();

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

    public List<List<Double>> convData2AveTex(double[][][] featureData, int[] featureNum) {
        int fLen = featureNum.length;
        int dataNum = numOfBlock*numOfBlock;
        List<List<Double>> calDatas = new ArrayList<>();

        for(int num = 0; num<dataNum; num++) {
            List<Double> calData = new ArrayList<>();
            for (int i = 0; i < fLen; i++) {
                for (int j = 0; j < 4; j++) {
                    calData.add(featureData[num][j][featureNum[i]]);
                }
            }
            calDatas.add(calData);
        }
        return calDatas;
    }


    public List<List<Double>> makeData4Ave(int class1, int class2, List<List<Double>> data) {
        List<List<Double>> material = new ArrayList<>();
        material.add(data.get(class1));
        material.add(data.get(class2));

        for(int i=1; i<numOfBlock*numOfBlock; i++) {
            if(group.get(class1).get(i) == 1 || group.get(class2).get(i) == 1) {
                material.add(data.get(i));
            }
        }

        return material;
    }

    public List<List<Double>> makeDataForAve(int classNum, List<List<Double>> rowData) {
        List<List<Double>> material = new ArrayList<>();
        material.add(rowData.get(classNum));

        // classNum
        for(int i=1; i<numOfBlock*numOfBlock; i++) {
            if(group.get(classNum).get(i) == 1) {
                material.add(rowData.get(i));
            }
        }
        return material;
    }

    /**
     * Calculate center of average in N dimension.
     * これを使うこと!!!!!!
     * @param data
     * @return ave
     */
    public List<Double> calAve(List<List<Double>> data) {
        int numOfData = data.size();
        int dimension = data.get(0).size();
        List<Double> ave = new ArrayList<>();
        for(int i=0; i<dimension; i++) {
            double sum = 0;
            for (int j = 0; j < numOfData; j++) {
                sum += data.get(j).get(i);
            }
            double valueAve = 0.0;
            if(numOfData != 0.0) {
                valueAve = Math.abs(sum) / numOfData;
            }

            ave.add(i, valueAve);

        }
        return ave;
    }

    public List<List<Double>> calTexAve (List<List<Double>> data) {
        int featureLen = featureNumStr.length;
        List<Double> ave = new ArrayList<>();
        List<List<Double>> aveList = new ArrayList<>();

        for(int i=0; i<featureLen*4; i++) {
            double sum = 0;
            for(int j=0; j<data.size(); j++) {
                sum += data.get(j).get(i);
            }
            ave.add(sum/4.0);
            if(i%4==3) {
                aveList.add(ave);
                ave.clear();
            }
        }
        return aveList;
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
//        if(dis > 0) {
//            dis = Math.sqrt(dis);
//        } else {
//            dis = 0;
//        }

        dis = Math.sqrt(Math.abs(dis));
        return dis;
    }

    /**
     * Calculate Sum of Euclidean distance Average of a class
     * @param data
     * @param dataAve
     * @return
     */
     public double calDis(List<List<Double>> data, List<Double> dataAve) {
        double disAll = 0.0;

        for(int i=0; i<data.size(); i++) {
            double dis = 0;
            for(int j=0; j<data.get(0).size(); j++) {
                dis += (data.get(i).get(j) - dataAve.get(j)) * (data.get(i).get(j) - dataAve.get(j));
            }
//            if(dis >= 0.0) {
//                disAll += Math.sqrt(dis);
//            }
            //disAll += Math.sqrt(Math.abs(dis));
            if(dis != 0.0) {
                disAll += Math.sqrt(Math.abs(dis));
            }

        }
//        if(Double.isNaN(disAll)) {
//            System.out.println("!!!!!!!!!!!!");
//        }

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

    /**
     * groupから同じクラスの要素をリストにする。同じ行に同じクラスがまとめられている
     * @return
     */
    public List<List<Integer>> showIntegration() {
        int[] flag = new int[numOfBlock*numOfBlock];
        Arrays.fill(flag, 0);

        List<List<Integer>> cluster = new ArrayList<>();

        for(int i=0; i<numOfBlock*numOfBlock; i++) {
            List<Integer> piece = new ArrayList<>();
            if(flag[i] == 0) {
                piece.add(i);
                flag[i] = 1;
                for(int j=0; j<numOfBlock*numOfBlock; j++) {
                    if(this.group.get(i).get(j) == 1) {
                        piece.add(j);
                        flag[j] = 1;
                    }
                }
                cluster.add(piece);
            }
        }

//        System.out.println();
//        System.out.println("クラスタリング状況");
//        for(int i=0; i<cluster.size(); i++) {
//            System.out.println(cluster.get(i));
//        }
//        System.out.println();

        return(cluster);
    }

//    /**
//     *
//     * @param cluster
//     * @return
//     */
//    public List<Double> getAveTex(List<List<Integer>> cluster) {
//        List<Double> result = new ArrayList<>();
//
//        for(int i=0; i<cluster.size(); i++) {
//            int len = cluster.get(i).size();
//            double ave = 0;
//            for(int j=0; j<len; j++) {
//                ave += cluster.get(i).get(j);
//            }
//            result.add(ave);
//        }
//
//        return result;
//    }



    /**
     * 適当な分類を選び、赤枠で囲う。その時選んだ番号を返却する。
     * @param graphics
     */
    public int drawRedFrame(Graphics graphics) {
        System.out.println("start");
        System.out.println("process num: " + iB.process.size());
        //List maxList = new ArrayList<Double>();
        double maxDiff = 0.0;
        int blNum = iB.numOfBlock*iB.numOfBlock-1;
        List max = Arrays.asList(0.0, 0.0, 0.0);    //リストにn*n-1番を抜いた最大の差分、その番号、n*n番目の差分を入れる
        for (int j = numOfBlock * numOfBlock - (rest+3); j < numOfBlock * numOfBlock-3; j++) {
            //graphics.drawString("" + iB.process.get(j+1).get(0).intValue() + ", " + iB.process.get(j+1).get(1).intValue() + ": " + iB.process.get(j+1).get(2).intValue(), ((j+1)%6)*iB.lengthOfASide + 150, iB.lengthOfASide*4 + ((j+1)/6)*80 + 50);
            double diff = iB.process.get(j+1).get(2)-iB.process.get(j).get(2);
            if (diff >= maxDiff) {
                maxDiff = diff;
                max.set(0, maxDiff);
                max.set(1, (double)(j));
            }
        }
        //graphics.drawString("" + iB.process.get(blNum-1).get(0).intValue() + ", " + iB.process.get(blNum-1).get(1).intValue() + ": " + iB.process.get(blNum-1).get(2).intValue(), ((blNum-1)%6)*iB.lengthOfASide + 150, iB.lengthOfASide*4 + ((blNum-1)/6)*80 + 50);

        double lasDiff = iB.process.get(blNum-1).get(2)-iB.process.get(blNum-2).get(2);
        max.set(2, lasDiff);
        System.out.println(lasDiff);

        System.out.println("max: " + max);

        if ((double)max.get(0) < (double)max.get(2) && (double)max.get(0) < 300.0) {
            max.set(1, (double)numOfBlock*numOfBlock-3.0);
            blockNum = 2.0;
        } else {
            blockNum = (double)(numOfBlock*numOfBlock-1)-(double)max.get(1);
        }

        double fase = (double)max.get(1);
        String faseStr = String.valueOf(fase);
        int faseI = (int)fase-1;
        graphics.setColor(Color.RED);
        BasicStroke bs = new BasicStroke(5);
        ((Graphics2D)graphics).setStroke(bs);
        //graphics.drawRect((faseI%(numOfBlock+1))*iB.lengthOfASide, (faseI/(numOfBlock+1))*iB.lengthOfASide, iB.lengthOfASide, iB.lengthOfASide);
        graphics.drawRect((faseI%(numOfBlock+1))*iB.lengthOfASide, 0, iB.lengthOfASide, iB.lengthOfASide);
        graphics.drawString(faseStr,40,40);
        graphics.setColor(Color.WHITE);

        return numOfBlock*numOfBlock-faseI-2;
    }

    /**
     * numOfBlock*numOfBlockのリストに0~(分割数)の数でナンバリング
     * @param cluster
     * @return
     */
    public List<List<Integer>> makeBlockArray(List<List<Integer>> cluster) {
        List<List<Integer>> blocks = new ArrayList(numOfBlock);

        for(int i=0; i<numOfBlock; i++) {
            List<Integer> block = new ArrayList(numOfBlock);
            for(int j=0; j<numOfBlock; j++) {
                for(int k=0; k<cluster.size(); k++) {
                    if(cluster.get(k).contains(8*i + j)){
                        block.add(k);
                    }
                }
            }
            blocks.add(block);
        }

        for(int i=0; i<numOfBlock; i++) {
            for(int j=0; j<numOfBlock; j++) {
                System.out.print(" " + blocks.get(i).get(j));
            }
            System.out.println();
        }
        System.out.println();

        return blocks;
    }

    /**
     * ブロックの数がnumOfBlock未満のクラスを確認する。
     * 無ければ0番目にnumOfBlock*numOfBlockしか入っていない。
     * ある場合はnumOfBlock*numOfBlock以降、1番目以降が対象のクラス。
     * @param blocks
     * @param clusterNum
     * @return
     */
    public List<Integer> checkDelBlock(List<List<Integer>> blocks, int clusterNum) {
        List<Integer> delCluster = new ArrayList<>();
        // 初期値としてとりあえず与えておく
        delCluster.add(numOfBlock*numOfBlock);
        int[] count = new int[clusterNum];
        Arrays.fill(count, 0);

        for(int i=0; i<numOfBlock; i++) {
            for(int j=0; j<numOfBlock; j++) {
                for(int k=0; k<clusterNum; k++) {
                    if(blocks.get(i).get(j) == k) {
                        count[k] ++;
                    }
                }
            }
        }

        for(int i=0; i<clusterNum; i++) {
            if(count[i] < numOfBlock) {
                delCluster.add(i);
            }
        }

        return delCluster;
    }


    /**
     * Draw a image of process.
     * @param cluster
     * @param input
     * @return
     * @throws IOException
     */
    public BufferedImage showIntegrationBlock(List<List<Integer>> cluster, BufferedImage input) {
        BufferedImage[] imageBlock = image.intoBlock(input);
        BufferedImage outputPaintedBlock = new BufferedImage(lengthOfASide, lengthOfASide, BufferedImage.TYPE_INT_RGB);
        int w = imageBlock[0].getWidth();
        int h = imageBlock[0].getHeight();
        int[][] colorPalette = {{0,204,255},{0,180,180},{0,100,153},{0,204,102},{0,294,51},{0,255,255},{0,255,204},{50,255,153},{0,255,102},
                {0,153,204},{0,133,153},{0,153,180},{102,153,255},{192,153,204},{102,138,153},{102,153,102},{204,255,255},{204,255,204},
                {204,255,153},{204,255,102},{204,255,0},{255,255,153},{255,255,100}, {0,204,255},{0,180,180},{0,100,153},{0,204,102},
                {0,294,51},{0,255,255},{0,255,204},{50,255,153},{0,255,102},
                {0,153,204},{0,133,153},{0,153,180},{102,153,255},{192,153,204},{102,138,153},{102,153,102},{204,255,255},{204,255,204},
                {204,255,153},{204,255,102},{204,255,0},{255,255,153},{255,255,100}, {0,204,255},{0,180,180},{0,100,153},{0,204,102},
                {0,294,51},{0,255,255},{0,255,204},{50,255,153},{0,255,102},
                {0,153,204},{0,133,153},{0,153,180},{102,153,255},{192,153,204},{102,138,153},{102,153,102},{204,255,255},{204,255,204},
                {204,255,153},{204,255,102},{204,255,0},{255,255,153},{255,255,100}, {0,204,255},{0,180,180},{0,100,153},{0,204,102},
                {0,294,51},{0,255,255},{0,255,204},{50,255,153},{0,255,102},
                {0,153,204},{0,133,153},{0,153,180},{102,153,255},{192,153,204},{102,138,153},{102,153,102},{204,255,255},{204,255,204},
                {204,255,153},{204,255,102},{204,255,0},{255,255,153},{255,255,100}, {0,204,255},{0,180,180},{0,100,153},{0,204,102},
                {0,294,51},{0,255,255},{0,255,204},{50,255,153},{0,255,102},
                {0,153,204},{0,133,153},{0,153,180},{102,153,255},{192,153,204},{102,138,153},{102,153,102},{204,255,255},{204,255,204},
                {204,255,153},{204,255,102},{204,255,0},{255,255,153},{255,255,100}, {0,204,255},{0,180,180},{0,100,153},{0,204,102},
                {0,294,51},{0,255,255},{0,255,204},{50,255,153},{0,255,102},
                {0,153,204},{0,133,153},{0,153,180},{102,153,255},{192,153,204},{102,138,153},{102,153,102},{204,255,255},{204,255,204},
                {204,255,153},{204,255,102},{204,255,0},{255,255,153},{255,255,100}, {0,204,255},{0,180,180},{0,100,153},{0,204,102},
                {0,294,51},{0,255,255},{0,255,204},{50,255,153},{0,255,102},
                {0,153,204},{0,133,153},{0,153,180},{102,153,255},{192,153,204},{102,138,153},{102,153,102},{204,255,255},{204,255,204},
                {204,255,153},{204,255,102},{204,255,0},{255,255,153},{255,255,100}, {0,204,255},{0,180,180},{0,100,153},{0,204,102},
                {0,294,51},{0,255,255},{0,255,204},{50,255,153},{0,255,102},
                {0,153,204},{0,133,153},{0,153,180},{102,153,255},{192,153,204},{102,138,153},{102,153,102},{204,255,255},{204,255,204},
                {204,255,153},{204,255,102},{204,255,0},{255,255,153},{255,255,100}};
//        for(int i=0; i<h; i++) {
//            for(int j=0; j<w; j++) {
//                outputPaintedBlock.setRGB(j, i, imageBlock[0].getRGB(j, i));
//            }
//        }
        Graphics graphics = outputPaintedBlock.createGraphics();

        for(int i=0; i<numOfBlock*numOfBlock; i++) {
            for(int j=0; j<cluster.size(); j++) {
                if(cluster.get(j).contains(i)) {
                    //System.out.println("i: " + i + ", j: " + j);
                    BufferedImage block = imageBlock[i];

                    for(int k=0; k<bSize; k++) {
                        for(int l=0; l<bSize; l++) {
                            int r = colorPalette[j][0];
                            int g = colorPalette[j][1];
                            int b = colorPalette[j][2];
//                            int r =  iu.r(imageBlock[i].getRGB(l, k)) + colorPalette[j][0];
//                            if( iu.r(imageBlock[i].getRGB(l, k)) + colorPalette[j][0]>255){
//                                r = 255;
//                            }
//
//                            int g =  iu.g(imageBlock[i].getRGB(l, k)) + colorPalette[j][1];
//                            if(iu.g(imageBlock[i].getRGB(l, k)) + colorPalette[j][1]>255) {
//                                g = 255;
//                            }
//
//                            int b =  iu.b(imageBlock[i].getRGB(l, k)) + colorPalette[j][2];
//                            if(iu.g(imageBlock[i].getRGB(l, k)) + colorPalette[j][2]>255) {
//                                b = 255;
//                            }

                            block.setRGB(l, k, iu.argb(0, r, g, b));
                        }
                    }
                    //x: " + (i%numOfBlock)*bSize + ", y: " + (i/numOfBlock)*bSize);



                    graphics.drawImage(imageBlock[i], (i%numOfBlock)*bSize, (i/numOfBlock)*bSize, null);
//                    for(int k=0; k<bSize; k++) {
//                        for(int l=0; l<bSize; l++) {
//                           graphics.drawImage(imageBlock[]);
//                        }
//                    }
                    //image.paintIntegrateBlock(imageBlock[i], j);
                   // BufferedImage block  =  image.paintIntegrateBlock(imageBlock[i], j);;
//                    for(int k=0; k<w; k++) {
//                        for(int l=0; l<h; l++) {
//                            outputPaintedBlock.setRGB(i%numOfBlock*bSize+l, ((i+1)/numOfBlock-1)*bSize+k, block.getRGB(l,k));
//                        }
//                    }

                }
            }
        }
        graphics.dispose();
        //ImageIO.write(outputPaintedBlock, "jpg", file);

        return outputPaintedBlock;
    }








}