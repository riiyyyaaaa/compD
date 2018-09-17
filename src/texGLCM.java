package com.compDetection;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import javax.imageio.ImageIO;

/**
 * 二次統計量を用いてテクスチャ解析
 */
public class texGLCM {
    final static ImageUtility iu = new ImageUtility();
    final static dividImage di = new dividImage();
    final static int concNum = 32; // 濃度数の最大値
    final static int imagesize = 400; // リサイズ後の画像サイズ

    public static void main(String[] args) throws IOException {
        File file = new File(
                "c:\\Users\\riya\\Documents\\compdetection\\src\\main\\java\\com\\compDetection\\lena.jpg");

        BufferedImage bi = ImageIO.read(iu.Mono(file));
        bi = iu.scaleImage(bi, (double) imagesize / bi.getWidth(), (double) imagesize / bi.getHeight());
        File outputfile = new File("c:\\Users\\riya\\Documents\\compdetection\\output\\output2.jpg");
        ImageIO.write(bi, "jpg", outputfile);
        bi = convConc(bi);

    }

    /**
     * グレースケール画像から 0, 45, 90, 135, 180°の濃度共起行列を求める
     */
    public static void GCLM(File file) throws IOException {
        // 正規化
        file = iu.Mono(file);
        BufferedImage read = ImageIO.read(file);
        read = iu.scaleImage(read, imagesize / read.getWidth(), imagesize / read.getHeight());
        BufferedImage[] biarr = di.intoBlock(read);

        // TODO 分割の順番をあとで考える<-なんのことか忘れた
        BufferedImage[] arr = di.intoBlock(read);
        int rad = 0;

    }

    /**
     * グレースケール画像の濃度を0~255では大きすぎるので0~32くらいに丸めこむ テクスチャ特徴をより細かくとりたければconcNumを大きくすればよい
     * 出力画像はかなり暗いので画面の明度を上げないと見えない
     */
    public static BufferedImage convConc(BufferedImage bImage) throws IOException {
        int h = bImage.getHeight();
        int w = bImage.getWidth();
        int c, rgb;
        int num = 256 / concNum;

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                c = iu.r(bImage.getRGB(j, i)) / num;
                // System.out.println(" " + iu.r(bImage.getRGB(j, i)) + " , " + c);
                rgb = iu.rgb(c, c, c);
                bImage.setRGB(j, i, rgb);
            }
        }
        File file2 = new File("c:\\Users\\riya\\Documents\\compdetection\\output\\output.jpg");
        ImageIO.write(bImage, "jpg", file2);

        return bImage;
    }
}