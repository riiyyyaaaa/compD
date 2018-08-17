import java.util.ArrayList;
import java.util.Scanner;

public class Main {
   public static void main(String[] args) {

       int[] A = {1,0,2,0,0,2};
       //int[] A = {1,3,0,2,0};
       int len = A.length;
       int all = 0;
       for(int i=0;i<len;i++){
           all += Math.pow(2,A[i]);
       }

//       while(all>1){
//           all/=2;
//           count++;
//       }
       String bin = Integer.toBinaryString(all);
       String[] num = bin.split("");

       System.out.println(bin);
       int count = 0;
       for(int i=0;i<num.length;i++){
           if(Integer.valueOf(num[i])==1){
               count ++;
           }
       }
       System.out.println(count);

       System.out.println();
//        Scanner sc = new Scanner(System.in);
//        String line = sc.nextLine();
//        String[] arr = line.split(" ");
////        int line = sc.nextInt();
//        System.out.println(line);
//
//
//
//        int[] count = new int[1000000];
//        int len = arr.length;
//        int res=0;
//
//        for(int i=0;i<len;i++){
//            if(Integer.valueOf(arr[i])>0){
//                count[Integer.valueOf(arr[i])]++;
//                System.out.println(Integer.valueOf(arr[i]));
//            }
//        }
//
//        for(int i=1;i<len;i++){
//            if(count[i]<1){
//                res = i;
//                break;
//            }
//        }
//        System.out.println(res);



        //System.out.println(arr[0] +" " + arr[arr.length-1]);


//
//        ArrayList<ArrayList<Integer>> arrays = new ArrayList<ArrayList<Integer>>();
//
//        for(int i=0;i<3;i++){
//            ArrayList<Integer> array = new ArrayList<>();
//            for(int j=0;j<4;j++){
//                array.add(j+i);
//            }
//            arrays.add(array);
//        }
//        System.out.println(arrays);


//
//        int n = sc.nextInt();
//        //System.out.println(n);
//        String[] num = new String[n+1];
//
//
//        for (int i = 0; i < n+1; i++) {
//            num[i] = sc.nextLine();
//            //System.out.println(num[i]);
//        }
//
//
//
////        int d=0;
////        while((num[d] = sc.nextLine()).equals("\n\n")){
////            d++;
////        }
//        for(int i=0;i<num.length;i++){
//            System.out.println(num[i]);
//        }
//
//
//        for (int i = 1; i < num.length; i++) {
//            String[] arr = num[i].split("");
//            int even=0;
//            int odd = 0;
//            int resultnum = 0;
//            for(int j=arr.length-2;j>-1;j--){
//                int x = Integer.valueOf(arr[j]);
//                if (j % 2 == 0) {
//                    x *= 2;
//                    //System.out.println(x);
//                    if(x/10>0){
//                        String[] keta = String.valueOf(x).split("");
//                        //System.out.println(keta.length);
//                        x = Integer.valueOf(keta[0]) + Integer.valueOf(keta[1]);
//                    }
//                    even += x;
//                }else{
//                    odd += x;
//                }
//
//            }
//            resultnum = even + odd;
//            if(resultnum%10==0){
//                resultnum = 0;
//            }else{
//                String[] y = String.valueOf(resultnum).split("");
//                String a = "";
//                int temp = 10;
//                temp -= Integer.valueOf(y[y.length-1]);
//                resultnum = temp;
////                for(int k=0;k<y.length;k++){
////                    if(k==(y.length-2)){
////                        a += String.valueOf(Integer.valueOf(y[k])+1));
////                    }else if(k==(y.length-1)){
////                        a += "0";
////                    }else{
////                        a += y;
////                    }
////
//            }
//            System.out.println(resultnum);
//        }


    }
}
