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
    final static int concNum = 15; // 濃度数の最大値
    final static int imagesize = 200; // リサイズ後の画像サイズ
    final static int oneSideBlockLength = 20; // ブロックの一辺の長さ
    final static int numOfBlock = 10; //分割するブロックの数

    public static void main(String[] args) throws IOException {
        String cd = new File(".").getAbsoluteFile().getParent();
        File f = new File(cd + "\\src\\output\\1.jpg");
        int[][][][] mat = calGCLM(f);
        calFeature(mat);
    }

    /**
     * 濃度共起行列からテクスチャ特徴(エネルギー、慣性、エントロピー、相関)を抽出する
     * @param mat
     * @return テクスチャ特徴となる各ブロック4つの値
     */
    public static double[][][] calFeature(int[][][][] mat) {
        double[][][] feature = new double[100][4][4];

        for(int i=0; i<numOfBlock*numOfBlock; i++) {
            for(int rad = 0; rad<4; rad++) {
                double[] sigma = calSigma(mat[i][rad]);
                for(int z = 0; z<4; z++) {
                    feature[i][rad][z] = 0;
                }
                for (int y = 0; y < concNum + 1; y++) {
                    for (int x = 0; x < concNum + 1; x++) {
                        // エネルギー
                        feature[i][rad][0] += mat[i][rad][y][x]*mat[i][rad][y][x];
                        // 慣性、分散、コントラスト
                        feature[i][rad][1] += (x-y)*(x-y)*mat[i][rad][y][x];
                        // エントロピー
                        if(mat[i][rad][y][x] != 0) {
                            feature[i][rad][2] += mat[i][rad][y][x] * (Math.log(mat[i][rad][y][x])) / Math.log(2);
                        }
                        // 相関
                        feature[i][rad][3] += (x*y*mat[i][rad][y][x]-sigma[2]*sigma[3])/(sigma[0]*sigma[1]);
                    }
                }
                //feature[i][rad][2] = -feature[i][rad][2];
                System.out.println("num is " + i);
                System.out.println("エネルギー: " + feature[i][rad][0]);
                System.out.println("慣性: " + feature[i][rad][1]);
                System.out.println("エントロピー: " + feature[i][rad][2]);
                System.out.println("相関: " + feature[i][rad][3]);
            }
        }

        return feature;
    }

    /**
     * 相関に用いるシグマ、ミューを求める
     * @param mat　あるブロックの1つの濃度共起行列
     * @return σx, σy, μx, μy　の順に入れた配列
     */
    public static double[] calSigma(int[][] mat) {
        double[] sigmaAndMu = {0,0,0,0};
        double preSigmaX, preSigmaY;
        double preMuX, preMuY;

        // μを求める
        for(int x=0; x<concNum+1; x++) {
            preMuX = 0;
            for(int y=0; y<concNum+1; y++) {
                preMuX += mat[y][x];
            }
            sigmaAndMu[2] += x*preMuX;
        }
        for(int y=0; y<concNum+1; y++) {
            preMuY = 0;
            for(int x=0; x<concNum+1; x++) {
                preMuY += mat[y][x];
            }
            sigmaAndMu[3]+= y*preMuY;
        }

        for(int x=0; x<concNum+1; x++) {
            preSigmaX = 0;
            for (int y = 0; y < concNum + 1; y++) {
                preSigmaX += mat[y][x];
                //System.out.println(mat[y][x]);
            }
            sigmaAndMu[0] += (x - sigmaAndMu[2])*(x - sigmaAndMu[2])*preSigmaX;
            //System.out.println( "sigmasndmu"+(x - sigmaAndMu[2]));
            //System.out.println(preSigmaX);
        }
        sigmaAndMu[0] *= 1/(double)(concNum+1);
        sigmaAndMu[0] = Math.sqrt(sigmaAndMu[0]);

        for(int y=0; y<concNum+1; y++) {
            preSigmaY = 0;
            for (int x = 0; x < concNum + 1; x++) {
                preSigmaY += mat[y][x];
            }
            sigmaAndMu[1] += (y - sigmaAndMu[3])*(y - sigmaAndMu[3])*preSigmaY;
            //System.out.println( "preSig" + preSigmaY + ", PRESIG-: " + (y-sigmaAndMu[3]) + ", res:" + sigmaAndMu[1]);
        }
        sigmaAndMu[1] *= 1/(double)(concNum+1);
        //System.out.print("/con: " + sigmaAndMu[1]);
        sigmaAndMu[1] = Math.sqrt(sigmaAndMu[1]);
        //System.out.println(", result: " + sigmaAndMu[1]);

        //System.out.println(" mu1: " + sigmaAndMu[2] + " mu2: " + sigmaAndMu[3] + " sig1: " + sigmaAndMu[0] + " sig2: " + sigmaAndMu[1]);

        return sigmaAndMu;
    }

    /**
     * 各ブロックにおける各角度の濃度共起行列を求める
     * @param file 濃度共起行列を求める対象の画像(File)
     * @return 全ブロック、全角度の濃度共起行列　nullは入らない
     * @throws IOException
     */
    public static int[][][][] calGCLM(File file) throws IOException {

        file = iu.Mono(file);
        BufferedImage read = ImageIO.read(file);
        read = convertConc(read); //濃度値を圧縮

        //System.out.println("width: "  + read.getWidth());
        read = iu.scaleImage(read, imagesize, imagesize);
        String cd = new File(".").getAbsoluteFile().getParent();
        File testFile = new File(cd + "\\src\\output\\convertImage.jpg");
        ImageIO.write(read, "jpg", testFile);
        int[][][][] mat = new int[numOfBlock*numOfBlock][4][concNum+1][concNum+1];

        BufferedImage[] biarr = di.intoBlock(read);

        for(int i=0; i<biarr.length; i++) {
            for(int rad = 0; rad<4; rad++) {
                System.out.println("\n---------- num:" + i + ", rad:" + rad + ": Mat ----------");
                //int[][] matArr = calMat(rad, biarr[i]);
                mat[i][rad] = calMat(rad, biarr[i]);
                int sum = 0;
                for (int y = 0; y < mat[i][rad].length; y++) {
                    for (int x = 0; x < mat[i][rad].length; x++) {
                        //System.out.printf("%3d", mat[i][rad][y][x]);
                        sum += mat[i][rad][y][x];
                    }
                    System.out.println();
                }
                System.out.println("\n sum mat = " + sum);
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
        return mat;
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
                } else if(rad == 1) {
                    if (i != 0 && i != oneSideBlockLength - 1) {
                        colorP = iu.r(block.getRGB(j, i+1));
                        matArr[colorC][colorP] ++;
                        colorP = iu.r(block.getRGB(j, i-1));
                        matArr[colorC][colorP] ++;
                    } else if (i == 0) {
                        colorP = iu.r(block.getRGB(j, i+1));
                        matArr[colorC][colorP] ++;
                    } else {
                        colorP = iu.r(block.getRGB(j, i-1));
                        matArr[colorC][colorP] ++;
                    }
                } else if (rad == 2) {
                    if(j != 0 && i != 0 && j != oneSideBlockLength-1 && i != oneSideBlockLength-1) {
                        colorP = iu.r(block.getRGB(j-1, i-1));
                        matArr[colorC][colorP] ++;
                        colorP = iu.r(block.getRGB(j+1, i+1));
                        matArr[colorC][colorP] ++;
                    } else if((j == 0 && i != oneSideBlockLength-1) || j !=  oneSideBlockLength-1 && i == 0) {
                        colorP = iu.r(block.getRGB(j+1, i+1));
                        matArr[colorC][colorP] ++;
                    } else if((j == oneSideBlockLength-1 && i != 0) || (j != 0 && i == oneSideBlockLength-1)) {
                        colorP = iu.r(block.getRGB(j-1, i-1));
                        matArr[colorC][colorP] ++;}
                } else {
                    if(j != 0 && i != 0 && j != oneSideBlockLength-1 && i != oneSideBlockLength-1) {
                        colorP = iu.r(block.getRGB(j-1, i+1));
                        matArr[colorC][colorP] ++;
                        colorP = iu.r(block.getRGB(j+1, i-1));
                        matArr[colorC][colorP] ++;
                    } else if((j != 0 && i == 0) || (j == oneSideBlockLength-1 && i != oneSideBlockLength-1)) {
                        colorP = iu.r(block.getRGB(j-1, i+1));
                        matArr[colorC][colorP] ++;
                    } else if((j == 0 && i != 0) ||  j!=  oneSideBlockLength-1 && i == oneSideBlockLength-1) {
                        colorP = iu.r(block.getRGB(j+1, i-1));
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
     * 0~最大濃度値まででハッシュを作成(ある濃度値である座標をその濃度値のindexに入れる), Listのコピー？上手くいかない
     * @param block 対象のブロック(BufferedImage)
     * @return ある濃度値である座標のList
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
