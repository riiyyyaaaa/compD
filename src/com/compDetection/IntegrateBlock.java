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
    static Block image;
    static ImageUtility iu;

    String[] featureNumStr = propertyUtil.getProperty("featureNum").split(",");
    int numOfBlock = Integer.valueOf(propertyUtil.getProperty("numOfBlock"));
    int imageSize = Integer.valueOf(propertyUtil.getProperty("imageSize"));
    int lengthOfASide = Integer.valueOf(propertyUtil.getProperty("imageSize")); //画像の一片の長さ
    int bSize = lengthOfASide/numOfBlock;// 1つのブロックの一辺の長さ
    List<List<Integer>> group = new ArrayList<>();
    List<List<Double>> process = new ArrayList<>(); // 25回分の統合したクラスと距離

    public IntegrateBlock() {
        for(int i=0; i<numOfBlock*numOfBlock; i++) {
            List list = new ArrayList();
            for(int j=0; j<numOfBlock*numOfBlock; j++) {
                list.add(0);
            }
            //Collections.addAll(list, 0);
            group.add(list);
            //System.out.println(group);
        }

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

    public static void main(String[] arg) throws IOException {
        String cd = new File(".").getAbsoluteFile().getParent();
        File dir = new File(cd + "\\src\\input\\");

        File[] list = dir.listFiles();
        for(int i=0; i<1; i++) {
            System.out.println(list[i]);
            int[][][][] mat_test = TexGLCM.calGLCM(list[i]);
            double[][][] featureMat = TexGLCM.calFeature((mat_test));
            BufferedImage read = ImageIO.read(list[i]);


            iB.calFirstDistanceMat(featureMat);
            for(int j=0; j<iB.numOfBlock*iB.numOfBlock-2; j++) {
                System.out.println("count: " + j);
                iB.calDistanceMatRepeat(featureMat);
                File file = new File(cd + "\\src\\output\\IntegrateOutput\\test" + j + ".jpg");
                read = iu.scaleImage(read, iB.imageSize, iB.imageSize);
                iB.showIntegrationBlock(iB.showIntegration(), read, file);
            }

            for(int j=0; j<iB.numOfBlock*iB.numOfBlock; j++) {
                System.out.println("group(" + j+ "): " + iB.group.get(j));
            }
            System.out.println();
            for(int j=0; j<iB.numOfBlock*iB.numOfBlock-1; j++) {
                System.out.println("Integ(" + j + ")" + iB.process.get(j));
            }


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

        // TODO テスト用データ
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
                    horizon.add(j, calDis(material, calAve(material)));
                    //System.out.println(calDis(material, calAve(material)));

                } else if(i>j) {
                    // TODO 合ってるか要チェック
                    horizon.add(j, disMat.get(j).get(i));
                } else {
                    horizon.add(j, 0.0);
                }
            }

            disMat.add(horizon);
            //System.out.println(horizon);
        }

        //System.out.println("column: " + disMat.size() + ", line: " + disMat.get(0).size() + ", data size: " + disMat.get(0).size());

        for(int i=0; i<disMat.size(); i++) {
            for(int j=0; j<disMat.get(i).size(); j++) {
                System.out.print(disMat.get(i).get(j) + " ");
            }
            System.out.println();
        }

        calMinClass(disMat);

        return disMat;
    }

    public void calDistanceMatRepeat(double[][][] featureMat) {
        // 使用する特徴の番号をcompDetection.propertiesから持ってくる
        int[] fNum = new int[featureNumStr.length];
        for(int i=0; i<featureNumStr.length; i++) {
            fNum[i] = Integer.valueOf(featureNumStr[i]);
        }

        int fNumLen = featureMat.length;

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
                        horizon.add(j, distance);
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

        for(int i=0; i<disMat.size(); i++) {
            for(int j=0; j<disMat.get(i).size(); j++) {
                System.out.printf("%6.1f ", disMat.get(i).get(j));
                //System.out.print(disMat.get(i).get(j) + " ");
            }
            System.out.println();
        }
        calMinClass(disMat);

        System.out.println("!!!!!!!!!!!!!!!!!!!!!");
        showIntegration();
        System.out.println("!!!!!!!!!!!!!!!!!!!!!");
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
                if(min>disMat.get(i).get(j) && disMat.get(i).get(j) != 0){
                    min = disMat.get(i).get(j);
                    classes = Arrays.asList(i, j);
                }
            }
        }

        //System.out.println(classes);
        System.out.println(classes + "\n");
        refInteg(classes.get(0), classes.get(1));
        List processData = Arrays.asList((double)classes.get(0), (double)classes.get(1), min);
        //System.out.println(processData);
        this.process.add(processData);

        return classes;
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

        System.out.println();
        System.out.println("group(" + class1 + ")" +this.group.get(class1));
        System.out.println("group(" + class2 + ")" + this.group.get(class2));
        System.out.println();

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
    public List<Double> calAve(List<List<Double>> data) { ;
        int numOfData = data.size();
        int dimension = data.get(0).size();
        List<Double> ave = new ArrayList<>();
        for(int i=0; i<dimension; i++) {
            double sum = 0;
            for (int j = 0; j < numOfData; j++) {
                sum += data.get(j).get(i);
                //ave.add(i, sum);
            }
            double valueAve = sum / numOfData;
            ave.add(i, valueAve);

        }

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

    public List<List<Integer>> showIntegration() {
        int[] flag = new int[25];
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
        System.out.println();
        for(int i=0; i<cluster.size(); i++) {
            System.out.println(cluster.get(i));
        }
        System.out.println();

        return(cluster);
    }

    public void showIntegrationBlock(List<List<Integer>> cluster, BufferedImage input, File file) throws IOException {
        BufferedImage[] imageBlock = image.intoBlock(input);
        BufferedImage outputPaintedBlock = new BufferedImage(lengthOfASide, lengthOfASide, BufferedImage.TYPE_INT_RGB);
        int w = imageBlock[0].getWidth();
        int h = imageBlock[0].getHeight();
        int[][] colorPalette = {{0,204,255},{0,204,204},{0,204,153},{0,204,102},{0,294,51},{0,255,255},{0,255,204},{0,255,153},{0,255,102},
                {0,153,204},{0,153,153},{0,153,102},{102,153,255},{192,153,204},{102,153,153},{102,153,102},{204,255,255},{204,255,204},
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
                    System.out.println("x: " + (i%numOfBlock)*bSize + ", y: " + (i/numOfBlock)*bSize);



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
        ImageIO.write(outputPaintedBlock, "jpg", file);
    }

}