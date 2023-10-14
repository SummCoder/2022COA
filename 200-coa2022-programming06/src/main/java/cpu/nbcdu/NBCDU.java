package cpu.nbcdu;

import cpu.alu.ALU;
import util.DataType;
import util.IEEE754Float;

public class NBCDU {
    private final String[][] trans = new String[][]{
            {"0000", "1001", "1010"},
            {"0001", "1000", "1001"},
            {"0010", "0111", "1000"},
            {"0011", "0110", "0111"},
            {"0100", "0101", "0110"},
            {"0101", "0100", "0101"},
            {"0110", "0011", "0100"},
            {"0111", "0010", "0011"},
            {"1000", "0001", "0010"},
            {"1001", "0000", "0001"}
    };
    /**
     * @param src  A 32-bits NBCD String
     * @param dest A 32-bits NBCD String
     * @return dest + src
     */

    String add1(String src, String dest){
        char[] result = new char[32];
        char[] mid1 = new char[4];//每四位进行运算，进行存储
        char[] mid2 = new char[4];
        char[] mid3 = new char[5];//截取后面五位
        int judge = 0;//用于判断比起10和16如何
        int whether_to_1 = 0;//判断是否需要进位
        for (int j = 7; j > 0; j--) {
            judge = 0;
            for (int i = 0; i < 4; i++) {
                mid1[i] = src.charAt(i+4*j);
                mid2[i] = dest.charAt(i+4*j);
            }
            ALU mid = new ALU();
            String mid_result = mid.add("0000000000000000000000000000" + new String(mid1), "0000000000000000000000000000" + new String(mid2));
            if(whether_to_1 == 1){
                mid_result = mid.add(mid_result, "00000000000000000000000000000001");
            }
            for (int i = 0; i <= 4; i++) {
                mid3[i] = mid_result.charAt(i+27);
                judge = 2 * judge + mid3[i]-'0';
//                System.out.println(judge);
            }
            if(judge >= 10){
                mid_result = mid.add(mid_result, "00000000000000000000000000000110");
                whether_to_1 = 1;
            }else {
                whether_to_1 = 0;
            }
//            System.out.println(mid_result);
            for (int i = 0; i < 4; i++) {
                result[i+4*j] = mid_result.charAt(i+28);
            }
        }
        for (int i = 0; i < 4; i++) {
            result[i] = dest.toString().charAt(i);
        }
        return new String(result);
    }



    DataType add(DataType src, DataType dest) {
        // TODO
        String src1 = src.toString();
        String dest1 = dest.toString();
        String src2;//符号不同用于转换
        String dest2;
        if(src1.equals("11000000000000000000000000000000") || src1.equals("11010000000000000000000000000000")){
            return dest;
        }
        if(dest1.equals("11000000000000000000000000000000") || dest1.equals("11010000000000000000000000000000")){
            return src;
        }
        char[] src3 = new char[32];
        char[] dest3 = new char[32];
        char[] result = new char[32];
        char[] mid1 = new char[4];//每四位进行运算，进行存储
        char[] mid2 = new char[4];
        char[] mid3 = new char[5];//截取后面五位
        int judge = 0;//用于判断比起10和16如何
        int whether_to_1 = 0;//判断是否需要进位
        if(src1.charAt(3) == dest1.charAt(3)){//同号相加
            for (int i = 0; i < 4; i++) {
                result[i] = src1.charAt(i);
            }
            src2 = src1;
            dest2 = dest1;
        }else {
            if(src1.charAt(3) == '1'){
                for (int i = 1; i <= 7 ; i++) {
                    for (int j = 0; j < 4; j++) {
                        mid1[j] = src1.charAt(j+4*i);
                    }
                    for(String[] tran : trans){
                        if(new String(mid1).equals(tran[0])){
                            for (int j = 0; j < 4; j++) {
                                src3[j+4*i] = tran[1].charAt(j);
                            }
                        }
                    }
                }
                for (int i = 0; i < 4; i++) {
                    src3[i] = dest1.charAt(i);
                }
                src2 = add1(new String(src3), "11000000000000000000000000000001");
                dest2 = dest1;
            }else {
                for (int i = 1; i <= 7 ; i++) {
                    for (int j = 0; j < 4; j++) {
                        mid2[j] = dest1.charAt(j+4*i);
                    }
                    for(String[] tran : trans){
                        if(new String(mid2).equals(tran[0])){
                            for (int j = 0; j < 4; j++) {
                                dest3[j+4*i] = tran[1].charAt(j);
                            }
                        }
                    }
                }
                for (int i = 0; i < 4; i++) {
                    dest3[i] = src1.charAt(i);
                }

                src2 = src1;
                dest2 = add1(new String(dest3), "11000000000000000000000000000001");
            }
            result[0] = '1';
            result[1] = '1';
            result[2] = '0';
            result[3] = '0';
        }
        for (int j = 7; j > 0; j--) {
            judge = 0;
            for (int i = 0; i < 4; i++) {
                mid1[i] = src2.charAt(i+4*j);
                mid2[i] = dest2.charAt(i+4*j);
            }
            ALU mid = new ALU();
            String mid_result = mid.add("0000000000000000000000000000" + new String(mid1), "0000000000000000000000000000" + new String(mid2));
            if(whether_to_1 == 1){
                mid_result = mid.add(mid_result, "00000000000000000000000000000001");
            }
            for (int i = 0; i <= 4; i++) {
                mid3[i] = mid_result.charAt(i+27);
                judge = 2 * judge + mid3[i]-'0';
            }
            if(judge >= 10){
                mid_result = mid.add(mid_result, "00000000000000000000000000000110");
                whether_to_1 = 1;
            }else {
                whether_to_1 = 0;
            }
            for (int i = 0; i < 4; i++) {
                result[i+4*j] = mid_result.charAt(i+28);
            }
        }
        if(src1.charAt(3) != dest1.charAt(3) && whether_to_1 == 0){
            for (int i = 1; i <= 7 ; i++) {
                for (int j = 0; j < 4; j++) {
                    mid1[j] = result[j+4*i];
                }
                for(String[] tran : trans){
                    if(new String(mid1).equals(tran[0])){
                        for (int j = 0; j < 4; j++) {
                            result[j+4*i] = tran[1].charAt(j);
                        }
                    }
                }
            }
            result[3] = '1';
            String result1 = add1(new String(result), "11010000000000000000000000000001");
            return new DataType(result1);
        }
        return new DataType(new String(result));
    }

    /***
     *
     * @param src A 32-bits NBCD String
     * @param dest A 32-bits NBCD String
     * @return dest - src
     */
    DataType sub(DataType src, DataType dest) {
        // TODO
        String src1 = src.toString();
        char[] src2 = new char[32];
        for (int i = 0; i < 32; i++) {
            src2[i] = src1.charAt(i);
        }
        if (src2[3] == '0') {
            src2[3] = '1';
        } else {
            src2[3] = '0';
        }
        return add(new DataType(new String(src2)), dest);
    }
}
