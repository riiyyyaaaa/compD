import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        //List<List<Integer>> dataList = new ArrayList();

        int N = Integer.valueOf(sc.nextLine());
       // System.out.println(N);

        List<Integer> date = new ArrayList<>();

        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            if(line.length() == 0) {
                break;
            }
            String[] str = line.split(" ");
            for(int i = 0; i<str.length; i++) {
                date.add(Integer.valueOf(str[i]).intValue());
            }
        }
        //System.out.println(date);

        int len = date.size();
        int workDay = 0;
        int restDay = 0;
        boolean flag = false;

        for(int i=0;i<N-6;i++) {
            restDay = 0;
            for(int j=0;j<7;j++) {
                System.out.print(date.get(i+j) + " ");

                if(date.get(i+j) == 0) {
                    restDay ++;
                    if(restDay == 2) {
                        flag = true;
                        break;
                    }
                }
            }
            System.out.println();

            if(flag == true) {
                workDay++;
            }else{
                flag = false;
            }
        }
        if(workDay > 0) {
            System.out.println(workDay+6);
        }else{
            System.out.println("0");
        }

//
//        int H = dataList.get(0).get(0);
//        int W = dataList.get(0).get(1);
//        int N = dataList.get(0).get(2);
//        int[][] t = new int[H][W];
//        int[] score = new int[N];
//        Arrays.fill(score,0);
//
//        for(int i=0; i<H; i++) {
//            for(int j=0; j<W; j++) {
//                t[i][j] = dataList.get(i+1).get(j);
//
//            }
//        }
//
//        int roop = dataList.get(t.length+1).get(0);
//        int nowP = 0;
//
//        for(int i=0; i< roop; i++) {
//            int cNum1 = dataList.get(t.length+i+2).get(0);
//            int cNum2 = dataList.get(t.length+i+2).get(1);
//            int cNum3 = dataList.get(t.length+i+2).get(2);
//            int cNum4 = dataList.get(t.length+i+2).get(3);
//
//            if(t[cNum1-1][cNum2-1] == t[cNum3-1][cNum4-1]) {
//                score[nowP]++;
//            }else {
//                nowP++;
//            }
//            if(nowP == N) {
//                nowP = 0;
//            }
//
//        }
//        for(int i=0;i<N;i++) {
//
//            System.out.println(score[i]*2);
//        }

    }
}