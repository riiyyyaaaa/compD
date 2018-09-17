import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

import static jdk.nashorn.internal.objects.NativeDebug.map;

/**
 * 二次統計量を用いてテクスチャ解析
 */
public class texGLCM {
    final static ImageUtility iu = new ImageUtility();
    final static Block di = new Block();
    final static int concNum = 32; // 濃度数の最大値
    final static int imagesize = 200; // リサイズ後の画像サイズ
    final static int oneSideBlockLength = 20; // ブロックの一辺の長さ
    final static int numOfBlock = 10; //分割するブロックの数

    public static void main(String[] args) throws IOException {
//        File file = new File(
//                "c:\\Users\\riya\\Documents\\compdetection\\src\\main\\java\\com\\compDetection\\lena.jpg");
//
//        BufferedImage bi = ImageIO.read(iu.Mono(file));
//        bi = iu.scaleImage(bi, (double) imagesize / bi.getWidth(), (double) imagesize / bi.getHeight());
//        File outputfile = new File("c:\\Users\\riya\\Documents\\compdetection\\output\\output2.jpg");
//        ImageIO.write(bi, "jpg", outputfile);
//        bi = convConc(bi);
        String cd = new File(".").getAbsoluteFile().getParent();
        File f = new File(cd + "\\src\\output\\1.jpg");
        calGCLM(f);
    }

    /**
     * グレースケール画像から 0, 45, 90, 135, 180°の濃度共起行列を求める
     */
    public static List<List<List<List<Integer>>>> calGCLM(File file) throws IOException {
        // 正規化
        file = iu.Mono(file);
        BufferedImage read = ImageIO.read(file);
        read = convertConc(read); //濃度値を圧縮



        //System.out.println("width: "  + read.getWidth());
        read = iu.scaleImage(read, imagesize, imagesize);
        String cd = new File(".").getAbsoluteFile().getParent();
        File testFile = new File(cd + "\\src\\output\\convertImage.jpg");
        ImageIO.write(read, "jpg", testFile);

        BufferedImage[] biarr = di.intoBlock(read);


        List<List<List<List<Integer>>>> results = new ArrayList<>();
        for(int blockNum=0; blockNum<biarr.length; blockNum++) {    //block
            System.out.println("---------------BLOCK: " + blockNum + " -------------------");
            List<List<List<Integer>>> rads = new ArrayList<>();

            for(int rad=0;rad<4;rad++) {   //rad

                System.out.println("-----------rad: "+ rad + " ------------");
                List<List<Integer>> lists = new ArrayList<>();
                List<List<Point>> hashConc = get32Hash(biarr[blockNum]);

                for(int i=0; i<concNum+1; i++) {
                    List<Integer> list = new ArrayList<>();

                    if(!hashConc.get(i).isEmpty()) {
                        List<Point> points = hashConc.get(i); //濃度iである座標を見つける

                        for (int j = 0; j < concNum + 1; j++) {
                            int sum = calProbability(rad, biarr[blockNum], points, j);
                            list.add(sum);

                            System.out.print(" " + sum);
                        }
                    } else {
                        for (int j=0; j<concNum+1; j++) {
                            list.add(0);

                            System.out.print(" 0");
                        }
                    }
                    System.out.println();
                    lists.add(list);
                }
                rads.add(lists);
            }
            results.add(rads);
        }
        return results;
    }

    /**
     * 濃度iである着目点から角度rad方向に濃度jがある確率をListで返却
     * @param rad
     */
    public static int calProbability(int rad, BufferedImage block, List<Point> points, int expectedVal) {

        List<Integer> probability = new ArrayList<>();
        int sum = 0;

        // TODO check oneSideVlockLength == block.length();

        if(rad == 0){
            for (Point point : points) {
                // TODO null check
                int x = point.x;
                int y = point.y;
                if (x != 0 && x != oneSideBlockLength - 1) {
                    sum += checkVal(x + 1, y, block, expectedVal);
                    sum += checkVal(x - 1, y, block, expectedVal);
                } else if (x == 0) {
                    sum += checkVal(x + 1, y, block, expectedVal);
                } else {
                    sum += checkVal(x - 1, y, block, expectedVal);
                }
            }
//                points.stream().forEach(point -> {
//                    checkVal(point.x, point.y, point.x+1, point.y, block);
//                });
        }
        else if(rad == 1) {
            for (Point point : points) {
                int x = point.x;
                int y = point.y;
                if(y != 0 && y != oneSideBlockLength-1) {
                    sum += checkVal(x, y+1, block, expectedVal);
                    sum += checkVal(x, y-1, block, expectedVal);
                }else if(y == 0) {
                    sum += checkVal(x, y-1, block, expectedVal);
                }else{
                    sum += checkVal(x, y+1, block, expectedVal);
                }
            }
//                points.stream().forEach(point -> {
//                    checkVal(point.x, point.y, point.x+1, point.y, block);
//                });
        }else if(rad == 2) {
            for (Point point : points) {
                int x = point.x;
                int y = point.y;
                if(x != 0 && y != 0 && x != oneSideBlockLength-1 && y != oneSideBlockLength-1) {
                    //TODO　場合分けチェック
                    sum += checkVal(x - 1, y-1, block, expectedVal);
                    sum += checkVal(x + 1, y+1, block, expectedVal);
                }else if(x == 0 || y == 0) {
                    sum += checkVal(x + 1, y+1, block, expectedVal);
                }else{
                    sum += checkVal(x - 1, y-1, block, expectedVal);
                }
            }
//                points.stream().forEach(point -> {
//                    checkVal(point.x, point.y, point.x+1, point.y, block);
//                });

        }else{

            for (Point point : points) {
                int x = point.x;
                int y = point.y;
                if(x != 0 && y != 0 && x != oneSideBlockLength-1 && y != oneSideBlockLength-1) {
                    //TODO　場合分けチェック
                    sum += checkVal(x + 1, y-1, block,expectedVal);
                    sum += checkVal(x - 1, y+1, block, expectedVal);
                }else if(x == 0 || y == 0) {
                    sum += checkVal(x + 1, y-1, block, expectedVal);
                }else{
                    sum += checkVal(x - 1, y+1, block, expectedVal);
                }
//                points.stream().forEach(point -> {
//                    checkVal(point.x, point.y, point.x+1, point.y, block);
//                });
            }
        }
        return sum;
    }

    /**
     * 0~最大濃度値まででハッシュを作成
     * @param block
     * @return
     */
    public static List<List<Point>> get32Hash(BufferedImage block) {
        List<List<Point>> lists = new ArrayList<>();
        int w = block.getWidth();
        int h = block.getHeight();
        Point point;
        int color = 0;
        System.out.println("w,h " + w + ", " + h);




        for(int i=0; i<w; i++) {
            List<Point> list = new ArrayList<>();
            for(int x=0; x<concNum+1; x++) {
                list.add(null);
            }
            for(int j=0; j<h; j++) {
                color = iu.r(block.getRGB(j,i));
                System.out.println("i, j: " + j + ", " + i + " = " + color);
                point = new Point(j, i);
                list.add(color, point);
            }
            lists.add(list);
        }

        return lists;
    }


    public static int checkVal(int i, int j, BufferedImage oneOfBlock, int expectedVal){
        int color = oneOfBlock.getRGB(i, j);

        if((int)color == expectedVal){
            return 1;
        }else{
            return 0;
        }
    }



    /**
     * グレースケール画像の濃度を0~255では大きすぎるので0~32くらいに丸めこむ テクスチャ特徴をより細かくとりたければconcNumを大きくすればよい
     * 出力画像はかなり暗いので画面の明度を上げないと見えない
     */
    public static BufferedImage convertConc(BufferedImage bImage) throws IOException {
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

        return bImage;
    }
}
//
//    /**
//     * 濃度iである着目点から角度rad方向に濃度jがある確率をListで返却
//     * @param rad
//     */
//    public static List<Integer> calProbability(int rad, BufferedImage block, List<List<Point>> lists, int expectedVal) {
//        List<Point> points = new ArrayList<>();
//        List<Integer> probability = new ArrayList<>();
//        int rem = 0;
//
//        // TODO check oneSideVlockLength == block.length();
//
//        if(rad == 0){
//            for(int conc = 0; conc<concNum+1; conc++) {
//                points = lists.get(conc);
//                int sum = 0;
//                for (Point point : points) {
//                    int x = point.x;
//                    int y = point.y;
//                    if (x != 0 && x != oneSideBlockLength - 1) {
//                        sum += checkVal(x + 1, y, block, expectedVal);
//                        sum += checkVal(x - 1, y, block, expectedVal);
//                    } else if (x == 0) {
//                        sum += checkVal(x + 1, y, block, expectedVal);
//                    } else {
//                        sum += checkVal(x - 1, y, block, expectedVal);
//                    }
//                    probability.add(conc, sum);
//                }
////                points.stream().forEach(point -> {
////                    checkVal(point.x, point.y, point.x+1, point.y, block);
////                });
//            }
//        }
//        else if(rad == 1) {
//            for(int conc = 0; conc<concNum+1; conc++) {
//                points = lists.get(conc);
//                int sum = 0;
//                for (Point point : points) {
//                    int x = point.x;
//                    int y = point.y;
//                    if(y != 0 && y != oneSideBlockLength-1) {
//                        sum += checkVal(x, y+1, block, expectedVal);
//                        sum += checkVal(x, y-1, block, expectedVal);
//                    }else if(y == 0) {
//                        sum += checkVal(x, y-1, block, expectedVal);
//                    }else{
//                        sum += checkVal(x, y+1, block, expectedVal);
//                    }
//                }
//                probability.add(conc, sum);
////                points.stream().forEach(point -> {
////                    checkVal(point.x, point.y, point.x+1, point.y, block);
////                });
//            }
//        }else if(rad == 2) {
//            for(int conc = 0; conc<concNum+1; conc++) {
//                points = lists.get(conc);
//                int sum = 0;
//                for (Point point : points) {
//                    int x = point.x;
//                    int y = point.y;
//                    if(x != 0 && y != 0 && x != oneSideBlockLength-1 && y != oneSideBlockLength-1) {
//                        //TODO　場合分けチェック
//                        sum += checkVal(x - 1, y-1, block, expectedVal);
//                        sum += checkVal(x + 1, y+1, block, expectedVal);
//                    }else if(x == 0 || y == 0) {
//                        sum += checkVal(x + 1, y+1, block, expectedVal);
//                    }else{
//                        sum += checkVal(x - 1, y-1, block, expectedVal);
//                    }
//                    probability.add(conc, sum);
//                }
////                points.stream().forEach(point -> {
////                    checkVal(point.x, point.y, point.x+1, point.y, block);
////                });
//            }
//        }else{
//            for(int conc = 0; conc<concNum+1; conc++) {
//                points = lists.get(conc);
//                int sum = 0;
//                for (Point point : points) {
//                    int x = point.x;
//                    int y = point.y;
//                    if(x != 0 && y != 0 && x != oneSideBlockLength-1 && y != oneSideBlockLength-1) {
//                        //TODO　場合分けチェック
//                        sum += checkVal(x + 1, y-1, block,expectedVal);
//                        sum += checkVal(x - 1, y+1, block, expectedVal);
//                    }else if(x == 0 || y == 0) {
//                        sum += checkVal(x + 1, y-1, block, expectedVal);
//                    }else{
//                        sum += checkVal(x - 1, y+1, block, expectedVal);
//                    }
//                }
//                probability.add(conc, sum);
////                points.stream().forEach(point -> {
////                    checkVal(point.x, point.y, point.x+1, point.y, block);
////                });
//            }
//        }
//        return probability;
//    }