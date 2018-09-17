import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;


public class testcv {
    public static final Mat binarize(Mat src, double thresholdValue) {
        Mat grayed = new Mat(src.size(), CvType.CV_8UC1);
        Mat bin = new Mat(src.size(), CvType.CV_8UC1);
        Block block = new Block();

        Imgproc.cvtColor(src, grayed, Imgproc.COLOR_RGB2GRAY);//COLOR_BGR2GRAY);
        Imgproc.threshold(grayed, bin, thresholdValue, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        grayed.release();
        return bin;
    }
    public static final Mat adbinarize(Mat src, double thresholdValue) {
        Mat grayed = new Mat(src.size(), CvType.CV_8UC1);
        Mat bin = new Mat(src.size(), CvType.CV_8UC1);

        Imgproc.cvtColor(src, grayed, Imgproc.COLOR_RGB2GRAY);//COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(grayed, bin, thresholdValue, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY ,7,8);

        grayed.release();
        return bin;
    }
    public static void main(String[] str) throws IOException {
        //System.load("C:\\opencv\\build\\bin");

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String cd = new File(".").getAbsoluteFile().getParent();
        System.out.println(cd);

        int num = 35;
        int i = 2;
        while (i <= num) {
            File testF = new File("C:\\detectEdge\\resizeImage\\img (" + String.valueOf(i) + ").jpg");
            if(testF.exists()){
                System.out.println("exist");
            }else{
                System.out.println("not exist");
            }
            BufferedImage bf = Block.outputNumB("C:\\detectEdge\\resizeImage\\img (" + String.valueOf(i) + ").jpg", String.valueOf(i));
            File outfile = new File(cd + "\\src\\output\\image" + String.valueOf(i) + ".jpg");
            ImageIO.write(bf, "jpg", outfile);
            //Mat input = Imgcodecs.imread("C:\\detectEdge\\resizeImage\\img (" + String.valueOf(i)+ ").jpg");

            Mat input = Imgcodecs.imread(cd + "\\output\\image" + String.valueOf(i) + ".jpg");
//            Mat output = binarize(input, 255);
//            Imgcodecs.imwrite("C:\\detectEdge\\binary\\" + String.valueOf(i) +"noraml.jpg", output);

            Mat output2 = adbinarize(input, 50);
            Imgcodecs.imwrite("C:\\detectEdge\\binary\\" + String.valueOf(i)+ "adaptive2.jpg", output2);
            i++;
        }
    }
//    Mat srcMat = Highgui.imread("img1.jpg", 1);
//    Mat grayMat = new Mat();
//    Imgproc.cvtColor(srcMat,grayMat,Imgproc.COLOR_BGR2GRAY);
//
//    int count = 0;
//
//    while(count< 4){
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//        String imgname = "img" + String.valueOf(count) + ".jpg";
//        String resultimg = "result" + String.valueOf(count) + ".jpg";
//    }
}

