import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;


/**
 * 二次統計量を用いてテクスチャ解析
 */
public class TexGLCM {

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
    public static void calGCLM(File file) throws IOException {
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

        for(int i=0; i<biarr.length; i++) {
            System.out.println("\n---------- " + i + ": Mat ----------");
            int[][] matArr = calMat(0, biarr[i]);
            for(int y = 0; y<matArr.length; y++) {
                for(int x = 0; x<matArr.length; x++) {
                    System.out.printf("%3d", matArr[y][x]);
                }
                System.out.println();
            }
        }

//        List<List<List<List<Integer>>>> results = new ArrayList<>();
//        for(int blockNum=0; blockNum<1 /*biarr.length*/; blockNum++) {    //block
//            System.out.println("---------------BLOCK: " + blockNum + " -------------------");
//            List<List<List<Integer>>> rads = new ArrayList<>();
//
//            for(int rad=0;rad<4;rad++) {   //rad
//
//                System.out.println("-----------rad: "+ rad + " ------------");
//                List<List<Integer>> lists = new ArrayList<>();
//                List<List<Point>> hashConc = get32Hash(biarr[blockNum]);
//
//                System.out.println("----------- matrix -----------");
//                for(int i=0; i<concNum+1; i++) {
//                    List<Integer> list = new ArrayList<>();
//                    List<Point> points = hashConc.get(i); //濃度iである座標を見つける
//
//                    if(points != null) {
//                        for (int j = 0; j < concNum + 1; j++) {
//                            int sum = calProbability(rad, biarr[blockNum], points, j);
//                            list.add(sum);
//
//                            //System.out.printf("%3d",sum);
//                        }
//                    } else {
//                        for (int j = 0; j < concNum+1; j++) {
//                            list.add(0);
//                            //System.out.print("  0");
//                        }
//                    }
//                    //System.out.println();
//                    lists.add(list);
//                }
//                rads.add(lists);
//            }
//            results.add(rads);
//        }
        //return results;
    }

    /**
     * 33*33の確率を示す濃度共起行列を返却
     * @param rad
     * @param block
     * @return
     */
    public static int[][] calMat(int rad, BufferedImage block) {
        List<List<Integer>> mat = new ArrayList<>(33);
        List<Integer> ele = new ArrayList<>(33);

        int[][] matArr = new int[33][33];
        //Arrays.fill(matArr, 0);
        int colorC;
        int colorP;
        int w = block.getWidth();
        int h = block.getHeight();

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                colorC = iu.r(block.getRGB(j, i));
                if(rad == 0) {
                    if (j != 0 && j != oneSideBlockLength - 1) {
                        colorP = iu.r(block.getRGB(j+1, i));
                        matArr[colorC][colorP] ++;
                        colorP = iu.r(block.getRGB(j-1, i));
                        matArr[colorC][colorP] ++;
                    } else if (j == 0) {
                        colorP = iu.r(block.getRGB(j+1, i));
                        matArr[colorC][colorP] ++;
                    } else {
                        colorP = iu.r(block.getRGB(j-1, i));
                        matArr[colorC][colorP] ++;
                    }
                }
            }
        }
        return matArr;
    }

    /**
     * 濃度iである着目点から角度rad方向に濃度jがある確率をListで返却
     * @param rad
     */
    public static int calProbability(int rad, BufferedImage block, List<Point> points, int expectedVal) {

        int sum = 0;

        // TODO check oneSideVlockLength == block.length();

        if(rad == 0){
            for (Point point : points) {
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
                    sum += checkVal(x, y+1, block, expectedVal);
                }else{
                    sum += checkVal(x, y-1, block, expectedVal);
                }
            }

        }else if(rad == 2) {
            for (Point point : points) {
                int x = point.x;
                int y = point.y;
                if(x != 0 && y != 0 && x != oneSideBlockLength-1 && y != oneSideBlockLength-1) {
                    sum += checkVal(x - 1, y-1, block, expectedVal);
                    sum += checkVal(x + 1, y+1, block, expectedVal);
                } else if((x == 0 && y != oneSideBlockLength-1) || x !=  oneSideBlockLength-1 && y == 0) {
                    sum += checkVal(x + 1, y+1, block, expectedVal);
                } else if((x == oneSideBlockLength-1 && y != 0) || (x != 0 && y == oneSideBlockLength-1)) {
                    sum += checkVal(x - 1, y-1, block, expectedVal);
                }
            }
        }else{
            for (Point point : points) {
                int x = point.x;
                int y = point.y;
                if(x != 0 && y != 0 && x != oneSideBlockLength-1 && y != oneSideBlockLength-1) {
                    sum += checkVal(x - 1, y + 1, block, expectedVal);
                    sum += checkVal(x + 1, y - 1, block, expectedVal);
                } else if((x != 0 && y == 0) || (x == oneSideBlockLength-1 && y != oneSideBlockLength-1)) {
                    sum += checkVal(x - 1, y + 1, block, expectedVal);
                } else if((x == 0 && y != 0) || x !=  oneSideBlockLength-1 && y == oneSideBlockLength-1) {
                    sum += checkVal(x + 1, y - 1, block, expectedVal);
                }
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
        List<List<Point>> lists = new LinkedList<>();
        List<Point> listPoints = new LinkedList<>();

        //Point[][] points = new Point[32][];
        int w = block.getWidth();
        int h = block.getHeight();
        Point point;
        int color = 0;

        // Initialize
        lists.clear();
        for(int x=0; x<concNum+1; x++) {
            lists.add(x, null);
        }

        System.out.println("initial lists size: " + lists.size());
        int sumsum = 0;

        for(int i=0; i<w; i++) {
            for(int j=0; j<h; j++) {

                color = iu.r(block.getRGB(j,i));
                point = new Point(j, i);
                System.out.println("(" + j + ", " + i + ") = " + color + " :" + point);

                if(lists.get(color) != null) {
//                    ArrayList<Point> list = new ArrayList<Point>(lists.get(color).size());
                    //List<Point> list = new ArrayList<>();
                    lists.get(color).add(point);
//                    for(Point po : lists.get(color)){
//                        list.add(po);
//                    }
//                    list.add(point);
//                    lists.set(color, list);//list　のめそっどにコピー

                    System.out.println("num: " + sumsum + ",color: " + color + "\n"+ lists.get(color));

                    sumsum ++;
                } else {
                    LinkedList<Point> list = new LinkedList<>();
                    list.add(point);
                    lists.add(color, list);
                   // if(list.size() != lists.get(color).size()){
                       // System.out.println("!!!!!!!!!!!!!!!!");
                   // }
                    sumsum ++;
                }
                //System.out.println("i, j: " + j + ", " + i + " = " + color);
            }
        }



        //System.out.println("------output the number of point ------");
        //System.out.println(sumsum);


//        System.out.println("------output ireg -------");
//        System.out.println(ire);
        int sumpoint=0;
        for(int x=0; x<concNum+1; x++) {
            System.out.println(x + ": " + lists.get(x));
            if(lists.get(x) != null) {
                sumpoint += lists.get(x).size();
            }
        }
        System.out.println("this sum = " + sumpoint);

        System.out.println();
        System.out.println("------ output size ------");
        int sum = 0;
        for(int x = 0; x<lists.size(); x++ ) {
            if (lists.get(x) != null) {
                sum += lists.get(x).size();
                System.out.print(x + ": " + sum + " ");
            }
        }
//        for(int x = 0; x<lists.size(); x++ ) {
//            if (lists.get(x) != null) {
//                System.out.println(x + ": " + lists.get(x));
//            }
//        }
        if(lists.size() == 66){
            System.out.println(lists.get(46));
        }

        System.out.println();
        System.out.println("--------- output lists size ---------");
        System.out.println(lists.size());
        System.out.println(lists.get(lists.size()-1));

        return lists;
    }


    public static int checkVal(int i, int j, BufferedImage oneOfBlock, int expectedVal){
        int color = iu.r(oneOfBlock.getRGB(i, j));

        if(color == expectedVal){
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
                if(c>concNum || c<0){
                    System.out.println("not Color");
                }
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