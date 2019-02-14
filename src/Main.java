import java.util.*;


public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        List<List<Integer>> dataList = new ArrayList();

        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            if(line.length() == 0) {
                break;
            }
            List<Integer> datas = new ArrayList<>();
            String[] str = line.split(" ");
            for(int i = 0; i<str.length; i++) {
                datas.add(Integer.valueOf(str[i]).intValue());
            }
            dataList.add(datas);

        }

        int H = dataList.get(0).get(0);
        int W = dataList.get(0).get(1);
        int N = dataList.get(0).get(2);
        int[][] t = new int[H][W];
        int[] score = new int[N];
        Arrays.fill(score,0);

        for(int i=0; i<H; i++) {
            for(int j=0; j<W; j++) {
                t[i][j] = dataList.get(i+1).get(j);

            }
        }

        int roop = dataList.get(t.length+1).get(0);
        int nowP = 0;

        for(int i=0; i< roop; i++) {
            int cNum1 = dataList.get(t.length+i+2).get(0);
            int cNum2 = dataList.get(t.length+i+2).get(1);
            int cNum3 = dataList.get(t.length+i+2).get(2);
            int cNum4 = dataList.get(t.length+i+2).get(3);

            if(t[cNum1-1][cNum2-1] == t[cNum3-1][cNum4-1]) {
                score[nowP]++;
            }else {
                nowP++;
            }
            if(nowP == N) {
                nowP = 0;
            }

        }
        for(int i=0;i<N;i++) {

            System.out.println(score[i]*2);
        }

    }
}