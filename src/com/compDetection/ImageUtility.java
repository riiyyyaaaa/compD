package com.compDetection;

import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.Graphics2D;
import javax.swing.JFileChooser;

import java.io.File;
import java.awt.*;

public class ImageUtility {
    public static int a(int c) {
        return c >>> 24;
    }

    public static int r(int c) {
        return c >> 16 & 0xff;
    }

    public static int g(int c) {
        return c >> 8 & 0xff;
    }

    public static int b(int c) {
        return c & 0xff;
    }

    public static int gray(int c) {
        return c << 16 | c << 8 | c;
    }

    public static int rgb(int r, int g, int b) {
        return 0xff000000 | r << 16 | g << 8 | b;
    }

    public static int argb(int a, int r, int g, int b) {
        return a << 24 | r << 16 | g << 8 | b;
    }

    /**
     * resizeX, resizeY に画像サイズを変更
     */
    public static BufferedImage scaleImage(BufferedImage org, int resizeX, int resizeY) throws IOException {
        // System.out.println("scale is " + scale);
        ImageFilter filter = new AreaAveragingScaleFilter(resizeX, resizeY);
        // JFileChooser filechooser = new JFileChooser();
        ImageProducer p = new FilteredImageSource(org.getSource(), filter);
        java.awt.Image dstImage = Toolkit.getDefaultToolkit().createImage(p);
        // BufferedImage dst = new BufferedImage(dstImage.getWidth(null),
        // dstImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
        BufferedImage dst = new BufferedImage(dstImage.getWidth(null), dstImage.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.drawImage(dstImage, 0, 0, null);
        g.dispose();

        return dst;
    }

    /**
     * カラー画像をモノクロで返却
     */
    public static File Mono(File file) throws IOException {
        BufferedImage readImage = ImageIO.read(file);
        int w = readImage.getWidth(), h = readImage.getHeight();
        BufferedImage writeImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        ImageUtility iu = new ImageUtility();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // ピクセル値を取得
                int c = readImage.getRGB(x, y);
                // 0.299や0.587といった値はモノクロ化の定数値
                int mono = (int) (0.299 * r(c) + 0.587 * g(c) + 0.114 * b(c));
                // モノクロ化したピクセル値をint値に変換
                int rgb = (a(c) << 24) + (mono << 16) + (mono << 8) + mono;
                writeImage.setRGB(x, y, rgb);
            }
        }
        // イメージをファイルに出力する
        String cd = new File(".").getAbsoluteFile().getParent();
        JFileChooser jfilechooser = new JFileChooser();
        String fileName = cd + "\\src\\output\\Mono" + jfilechooser.getName(file);
        File file2 = new File(fileName);
        ImageIO.write(writeImage, "jpg", file2);

        return file2;
    }

    static BufferedImage toMono(BufferedImage src) {
        BufferedImage dist = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        dist.getGraphics().drawImage(src, 0, 0, null);
        return dist;
    }

    /**
     * 文字列を画像に書き込む
     */
    public static BufferedImage drawStr(BufferedImage read, double value, int width) {
        Graphics graphics = read.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.drawString(String.valueOf(value), width - 50, width - 20);

        return read;
    }

    /**
     * 2つ画像を横に並べた画像の作成
     */
    public static BufferedImage outputResult(BufferedImage img1, BufferedImage img2) {
        int h = img1.getHeight();
        if (img1.getHeight() < img2.getHeight()) {
            h = img2.getHeight();
        }
        BufferedImage write = new BufferedImage(img1.getWidth() + img2.getWidth(), h, BufferedImage.TYPE_INT_RGB);
        Graphics g = write.getGraphics();
        g.drawImage(img1, 0, 0, null);
        g.drawImage(img2, img1.getWidth(), 0, null);

        return write;
    }

    /**
     * 2つ画像を縦に並べた画像の作成
     */
    public static BufferedImage outputResultLongi(BufferedImage img1, BufferedImage img2) {
        int w = img1.getWidth();
        // int h = img1.getHeight();
        if (img1.getWidth() < img2.getWidth()) {
            w = img2.getWidth();
        }
        BufferedImage write = new BufferedImage(w, img1.getHeight() + img2.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = write.getGraphics();
        g.drawImage(img1, 0, 0, null);
        g.drawImage(img2, 0, img1.getHeight(), null);

        return write;
    }

    /**
     * 2値画像の反転
     */
    public static BufferedImage revMono(BufferedImage img) {
        int val = 0;
        BufferedImage write = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                int c = img.getRGB(j, i);
                int r = r(c);
                if (r >= 255 / 2) {
                    val = 255 - r;
                } else {
                    val = r - 255;
                }
                int rgb = rgb(val, val, val);
                write.setRGB(j, i, rgb);
            }
        }

        return write;
    }

    public static void main(String[] args) {

    }

    public BufferedImage prossFilter(BufferedImage image) throws IOException{

        int cal = 0;
        int filterSize = 5;
        int w = image.getWidth(), h = image.getHeight();
        BufferedImage filteredImage  = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);;
        double ro = 5;
        double[][] filter = new double[filterSize][filterSize];

        for (double i = -filterSize / 2; i <= filterSize / 2; i++) {
            for (double j = -filterSize / 2; j <= filterSize/ 2; j++) {
                filter[(int) (i + filterSize / 2)][(int) (j + filterSize/ 2)] = 1 / (2 * Math.PI * ro * ro)
                        * Math.exp(-(j * j + i * i) / (2 * ro * ro));
            }

        }

        for (int i = 1; i < w - filter.length + 2; i++) {
            for (int j = 1; j < h - filter.length + 2; j++) {
                cal = 0;
                for (int k = 0; k < filter.length; k++) {
                    for (int l = 0; l < filter.length; l++) {
                        int c = image.getRGB(i - 1 + l, j - 1 + k);
                        int r = r(c);
                        cal += r * filter[k][l];
                        // System.out.print(" " + cal);
                    }
                }
                // 計算した結果が0<cal<255以外の時の処理
                if (cal > 225) {
                    cal = 225;
                } else if (cal < 0) {
                    cal = 0;
                }
                // グレースケール画像を返却
                int rgb = rgb((int) cal, (int) cal, (int) cal);
                filteredImage.setRGB(i, j, (int) rgb);
            }
        }
        // イメージをファイルに出力する
        String cd = new File(".").getAbsoluteFile().getParent();
        JFileChooser jfilechooser = new JFileChooser();
        String fileName = cd + "\\src\\output\\Mono.jpg";
        File file2 = new File(fileName);
        ImageIO.write(filteredImage, "jpg", file2);

        return filteredImage;
    }


}
