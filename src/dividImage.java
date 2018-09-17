package com.compDetection;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.interfaces.RSAMultiPrimePrivateCrtKey;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.util.*;
import javax.swing.JFileChooser;

/**
 * 画像を縦横num個のブロックに分割。 それらを基に2値で大まかな物体の配置を把握する
 */
public class dividImage {
    public static final ImageUtility iu = new ImageUtility();
    public static final int num = 12; // 分割するブロックの数(1辺)

    public static void main(String[] args) throws IOException {
        // String dir = "C:\\detectEdge\\fl.jpg";
        // 画像をモノクロで出力
        // File file = iu.Mono(new File(dir));
        // BufferedImage read = ImageIO.read(file);
        // int colorF[][] = extrColorF(intoBlock(read));

        // outputBlock(colorF);
        // paintBlock(colorF);
        // new dividImage(colorF);

        // ファイルの中の画像で回す
        int numI = 35;
        int count = 0;
        BufferedImage write = new BufferedImage(num * 20, num * 20, BufferedImage.TYPE_INT_RGB);
        // File file1 = new File("C:\\detectEdge\\searched2\\img (" +
        // String.valueOf(count + 1) + ").jpg");
        double width = 200;
        double hight = 200;

        while (count < numI) {
            File file1 = new File("C:\\detectEdge\\searched2\\img (" + String.valueOf(count + 1) + ").jpg");
            // file1 = iu.scaleImage(file1, width, hight);
            BufferedImage read2 = ImageIO.read(iu.Mono(file1));

            int block[][] = extrColorF(intoBlock(read2));
            block = mvaveFileter(block);
            File outputfile = new File(
                    "C:\\detectEdge\\blockresult\\imgPre" + String.valueOf(count + 1) + "_2" + ".jpg");

            write = outputBlock(block);
            // ImageIO.write(write, "jpg", outputfile);

            write = iu.outputResult(read2, write);

            write = iu.outputResult(write, paintBlock(block));
            // outputfile = new File(("C:\\detectEdge\\blockresult\\img" +
            // String.valueOf(count + 1) + ".jpg"));
            ImageIO.write(write, "jpg", outputfile);

            System.out.println("filename is " + count + ".jpg");
            count++;

        }

    }

    /**
     * 引数のデータからブロックにわけたBufferedImageを配列で返却
     */
    public static BufferedImage[] intoBlock(BufferedImage origin) {
        int w = origin.getWidth();
        int h = origin.getHeight();
        int intw = w / num;
        int inth = h / num;
        BufferedImage[] block = new BufferedImage[num * num];
        int count = 0;

        System.out.println("w:" + intw + ", h:" + inth);

        for (int i = 0; i < num; i++) {
            for (int j = 0; j < num; j++) {
                block[count] = origin.getSubimage(j * intw, i * inth, intw, inth);
                count++;
            }
        }
        return block;
    }

    /**
     * ブロックごとの色特徴ベクトルとなるRGB値を返却
     */
    public static int[][] extrColorF(BufferedImage[] read) {
        int[][] colorF = new int[num * num][3];

        int count = 0;
        while (count < num * num) {
            int valr = 0; // とりあえず今は平均値
            int valg = 0;
            int valb = 0;
            for (int i = 0; i < read[count].getHeight(); i++) {
                for (int j = 0; j < read[count].getWidth(); j++) {
                    valr += iu.r(read[count].getRGB(j, i));
                    valg += iu.g(read[count].getRGB(j, i));
                    valb += iu.b(read[count].getRGB(j, i));
                }
            }
            colorF[count][0] = valr / (read[count].getHeight() * read[count].getWidth());
            colorF[count][1] = valg / (read[count].getHeight() * read[count].getWidth());
            colorF[count][2] = valb / (read[count].getHeight() * read[count].getWidth());
            // System.out.println(colorF[count][0] + ", " + colorF[count][1] + "," +
            // colorF[count][2]);
            count++;
        }
        return colorF;
    }

    /**
     * 分割し平均にしたブロックを1つのBUufferedImageとして出力
     */
    public static BufferedImage outputBlock(int[][] block) throws IOException {
        int bsize = 20;
        int numB = 0;
        BufferedImage output = new BufferedImage(num * bsize, num * bsize, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < num * bsize; i++) {
            for (int j = 0; j < num * bsize; j++) {
                // System.out.println("(i,j) = (" + i + "," + j + ")");
                numB = (int) (i / (bsize)) * num + (int) (j / (bsize));
                // System.out.print((int) (i / (bsize)) * 4 + (int) (j / (bsize)) + " ");

                output.setRGB(j, i, iu.argb(0, block[numB][0], block[numB][1], block[numB][2]));

            }
            // System.out.println();
        }
        File file = new File("C://detectEdge//resultbockImagefl.jpg");
        ImageIO.write(output, "jpg", file);

        return output;
    }

    public static BufferedImage binaryBlock(BuferedImage){

    }

    /**
     * ブロックごとの数値から画像全体でのブロックの平均値(モノクロ)を求める 新たな配列ブロックにブロックの値が平均以上であれば1,
     * 平均以下であれば-1としていれていく
     */
    public static int[] aveBlock(int[][] block) {
        int ave = 0;
        int[] resultBlock = new int[num * num];

        for (int i = 0; i < block.length; i++) {
            // モノクロ画像であればRGBのうち一つでいい
            ave += block[i][0];
        }
        ave /= num * num;

        for (int i = 0; i < block.length; i++) {
            if (block[i][0] > ave) {
                resultBlock[i] = 1;
            } else {
                resultBlock[i] = -1;
            }
            // System.out.print(resultBlock[i]);
        }
        // System.out.println();
        return resultBlock;
    }

    /**
     * aveBlockから得られた配列を基に画像を1, -1で塗分け, BufferedImageを返却
     */
    public static BufferedImage paintBlock(int[][] block) throws IOException {
        int[] resultBlock = aveBlock(block);
        int bsize = 20;
        int numB = 0;
        BufferedImage output = new BufferedImage(num * bsize, num * bsize, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < num * bsize; i++) {
            for (int j = 0; j < num * bsize; j++) {
                numB = (int) (i / (bsize)) * num + (int) (j / (bsize));
                if (resultBlock[numB] == 1) {
                    output.setRGB(j, i, iu.argb(0, 0, 0, 0));
                    // System.out.print("k" + numB);
                } else {
                    output.setRGB(j, i, iu.argb(0, 255, 255, 255));
                    // System.out.print("w" + numB);
                }

            }
            // System.out.println();
        }
        File file = new File("blockImageMono.jpg");
        ImageIO.write(output, "jpg", file);
        return output;
    }

    /**
     * 移動平均フィルタをかける (dividImgae用) int配列のblockとブロックを引数とする blockであるint配列を返却
     */
    public static int[][] mvaveFileter(int[][] block) {
        int ave = 0;
        int[][] result = new int[num * num][3];
        for (int i = 0; i < num; i++) {
            for (int j = 0; j < num; j++) {
                if (i == 0 || i == num - 1 || j == 0 || j == num - 1) {
                    result[i * num + j][0] = block[i * num + j][0];
                    result[i * num + j][1] = block[i * num + j][1];
                    result[i * num + j][2] = block[i * num + j][2];
                } else {
                    ave = block[(i - 1) * num + j - 1][0] + block[(i - 1) * num + j][0]
                            + block[(i - 1) * num + j + 1][0] + block[i * num + j - 1][0] + block[i * num + j][0]
                            + block[i * num + j + 1][0] + block[(i + 1) * num + j - 1][0] + block[(i + 1) * num + j][0]
                            + block[(i + 1) * num + j + 1][0];

                    result[i * num + j][0] = ave / 9;
                    result[i * num + j][1] = ave / 9;
                    result[i * num + j][2] = ave / 9;
                }
            }
        }

        return result;
    }

    /**
     * 一番外側のブロックを削除
     */

    // /**
    // * ブロックを一つの画像として出力 画像サイズは一定
    // */
    // public dividImage(int[] block) {
    // this.setSize(300, 200);
    // this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // this.setVisible(true);
    // this.g = this.getGraphics();
    // g.setColor(Color.RED);
    // g.fillRect(50, 50, 100, 100);

    // // for (int i = 0; i < num; i++) {
    // // for (int j = 0; j < num; j++) {
    // // g.setColor(new Color(block[i * 4 + j], 0, 0));
    // // g.fillRect(j * 100, i * 100, 100, 100);

    // // }
    // // }
    // g.dispose();
    // }
}