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
    final static int imagesize = 400; // リサイズ後の画像サイズ
    final static int oneSideBlockNum = 10; // the number of bock

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
        File f = new File(cd + "\\output\\image2.jpg");
        calGCLM(f);
    }

    /**
     * グレースケール画像から 0, 45, 90, 135, 180°の濃度共起行列を求める
     */
    public static void calGCLM(File file) throws IOException {
        int numb = oneSideBlockNum; //ブロック分割数(1辺)
        // 正規化
        file = iu.Mono(file);
        BufferedImage read = ImageIO.read(file);
        read = convConc(read);
        read = iu.scaleImage(read, imagesize / read.getWidth(), imagesize / read.getHeight());
        BufferedImage[] biarr = di.intoBlock(read);
        System.out.println("size is : " + biarr[0].getHeight() + " ,  " + biarr[0].getWidth());

        int rad = 0;
        double[][][] probabilityArray = new double[4][concNum+1][concNum+1]; // 4方向*濃度数*濃度数

        for(int i=0;i<biarr.length;i++) {    //block
            for(int j=0;j<4;j++) {   //rad

            }
        }

        // TODO 分割の順番をあとで考える<-なんのことか忘れた
        //BufferedImage[] arr = di.intoBlock(read,numb);

    }

    /**
     * 濃度iである着目点から角度rad方向に濃度jがある確率をListで返却
     * @param rad
     */
    public static List<Integer> calProbability(int rad, BufferedImage block, List<List<Point>> lists) {
        List<Point> points = new ArrayList<>();
        List<Integer> probability = new ArrayList<>();
        int rem = 0;


        if(rad == 0){
            for(int conc = 0; conc<concNum+1; conc++) {
                if(!lists.get(conc).isEmpty()) {
                    points = lists.get(conc);
                    int sum = 0;
                    for (Point point : points) {
                        int x = point.x;
                        int y = point.y;
                        if(x != 0 && x != oneSideBlockNum-1) {
                            sum += checkVal(x, y, x + 1, y, block);
                            sum += checkVal(x, y, x - 1, y, block);
                        }else if(x == 0) {
                            sum += checkVal(x, y, x + 1, y, block);
                        }else{
                            sum += checkVal(x, y, x - 1, y, block);
                        }
                    }
                    probability.add(conc, sum);
                }
//                points.stream().forEach(point -> {
//                    checkVal(point.x, point.y, point.x+1, point.y, block);
//                });
            }
        }
        else if(rad == 1) {
            for(int conc = 0; conc<concNum+1; conc++) {
                if(!lists.get(conc).isEmpty()) {
                    points = lists.get(conc);
                    int sum = 0;
                    for (Point point : points) {
                        int x = point.x;
                        int y = point.y;
                        if(y != 0 && y != oneSideBlockNum-1) {
                            sum += checkVal(x, y, x, y+1, block);
                            sum += checkVal(x, y, x, y-1, block);
                        }else if(y == 0) {
                            sum += checkVal(x, y, x, y-1, block);
                        }else{
                            sum += checkVal(x, y, x, y+1, block);
                        }
                    }
                    probability.add(conc, sum);
                }
//                points.stream().forEach(point -> {
//                    checkVal(point.x, point.y, point.x+1, point.y, block);
//                });
            }
        }else if(rad == 2) {
            for(int conc = 0; conc<concNum+1; conc++) {
                if(!lists.get(conc).isEmpty()) {
                    points = lists.get(conc);
                    int sum = 0;
                    for (Point point : points) {
                        int x = point.x;
                        int y = point.y;
                        if(x != 0 && y != 0 && x != oneSideBlockNum-1 && y != oneSideBlockNum-1) {
                            //TODO　場合分けチェック
                            sum += checkVal(x, y, x - 1, y-1, block);
                            sum += checkVal(x, y, x + 1, y+1, block);
                        }else if(x == 0 || y == 0) {
                            sum += checkVal(x, y, x + 1, y+1, block);
                        }else{
                            sum += checkVal(x, y, x - 1, y-1, block);
                        }
                    }
                    probability.add(conc, sum);
                }
//                points.stream().forEach(point -> {
//                    checkVal(point.x, point.y, point.x+1, point.y, block);
//                });
            }
        }else{
            for(int conc = 0; conc<concNum+1; conc++) {
                if(!lists.get(conc).isEmpty()) {
                    points = lists.get(conc);
                    int sum = 0;
                    for (Point point : points) {
                        int x = point.x;
                        int y = point.y;
                        if(x != 0 && y != 0 && x != oneSideBlockNum-1 && y != oneSideBlockNum-1) {
                            //TODO　場合分けチェック
                            sum += checkVal(x, y, x + 1, y-1, block);
                            sum += checkVal(x, y, x - 1, y+1, block);
                        }else if(x == 0 || y == 0) {
                            sum += checkVal(x, y, x + 1, y-1, block);
                        }else{
                            sum += checkVal(x, y, x - 1, y+1, block);
                        }
                    }
                    probability.add(conc, sum);
                }
//                points.stream().forEach(point -> {
//                    checkVal(point.x, point.y, point.x+1, point.y, block);
//                });
            }
        }
        return probability;
    }

    /**
     * 0~最大濃度値まででハッシュを作成
     * @param block
     * @return
     */
    public List<List<Point>> get32Hash(BufferedImage block) {
        List<List<Point>> lists = new ArrayList<>();
        int w = block.getWidth();
        int h = block.getHeight();
        Point point;
        int r = 0;

        for(int i=0; i<w; i++) {
            List<Point> list = new ArrayList<>();
            for(int j=0; j<h; j++) {
                r = iu.r(block.getRGB(j,i));
                point = new Point(j, i);
                list.add(point);
                lists.add(r%concNum, list);
            }
        }

        return lists;
    }

    public static int checkVal(int fromI, int fromJ, int toI, int toJ, BufferedImage oneOfBlock){
        int startColor = oneOfBlock.getRGB(fromI, fromJ);
        int endColor = oneOfBlock.getRGB(toI, toJ);
        if(startColor == endColor){
            return 1;
        }else{
            return 0;
        }
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

        return bImage;
    }
}