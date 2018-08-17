import java.util.ArrayList;
import java.util.Scanner;

public class lib {
    public static void main(String[] str){
        int M = 8;
        int N =5;
        //String bin = Integer.toBinaryString(M);
        String[][] b = new String[M-N+1][30];
        String[] len = Integer.toBinaryString(N).split("");
        int l = len.length;
        int[] result = new int[l];

        for(int i=0;i<M-N;i++){
            if(i==0){
                String[] bin = Integer.toBinaryString(N+i).split("");
                String[] bin2 = Integer.toBinaryString(N+i+1).split("");

                for(int j=0;j<l;j++) {
                    if (Integer.valueOf(bin[l - 1 - j]) == 1 && Integer.valueOf(bin2[l - 1 - j]) == 1) {
                        result[l - 1 - j] = 0;
                    } else {
                        result[l - 1 - j] = 1;
                    }
                }
            }else{
                for(int j=0;j<l;j++) {
//                    if (Integer.valueOf(bin[l - 1 - j]) == 1 && Integer.valueOf(bin2[l - 1 - j]) == 1) {
//                        result[l - 1 - j] = 0;
//                    } else {
//                        result[l - 1 - j] = 1;
//                    }
                }
                }
            }
//            for(int j=0;j<bin.length;j++){

//                System.out.println(bin[j]);
//            }
//            System.out.println();
        }
//        String bin = Integer.toBinaryString(1000000000);
//        String[] b = bin.split("");
//
//
//
//        System.out.println(bin);
//        System.out.println(b.length);

    }

//    public static void test(){
//        ArrayList<Integer> list = new ArrayList<>();
//        Scanner sc = new Scanner(System.in);
//
//        while(sc.hasNext()){
//            String line = sc.nextLine();
//            String[] str = line.split(" ");
//            line.replace(" ","");
//        }

    //}
//}
