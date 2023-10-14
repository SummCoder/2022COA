package util;

import java.util.Arrays;

public class Transformer {

    public static String intToBinary(String numStr) {
        // TODO:
//        十进制转化为补码,助教那个不咋地，这个我自己感觉更加贴近于补码与无符号整数关系
        long value = Long.parseLong(numStr);//字符串转化为值
        if(value < 0){//负数
            value = value + (long) Math.pow(2, 32);//向右平移回无符号整数
        }
        char[] str = new char[32];//用32位加以存储
        for(int i = 31; i >= 0 ; i--){//计算每一位是0还是1
            if(value % 2 == 1){
                str[i] = '1';
            }
            else {
                str[i] = '0';
            }
            value /= 2;
        }
        return new String(str);//注意char数组，转化为字符串使用new String(),不要使用toString!!!
    }

    public static String binaryToInt(String binStr) {
        // TODO:
//        补码转化为十进制
        int numInt = 0;
//        负数
        if(binStr.charAt(0) == '1'){
            numInt = -1;
        }
        for (int i = 1; i < binStr.length(); i++){
            if(binStr.charAt(i) == '1'){
                numInt = 2 * numInt + 1;
            }else {
                numInt = 2 * numInt;
            }
        }
        return String.valueOf(numInt);
    }

    public static String decimalToNBCD(String decimalStr) {
        // TODO:
//        十进制转NBCD，符号位为四位，即+ 1100，- 1101，0~9 0000-1001
        long value = Long.parseLong(decimalStr);
        long num;
        int cnt = 0;
        char[] str = new char[32];
        for(int i = 0; i < 32; i++){
            str[i] = '0';
        }
        str[0] = '1';
        str[1] = '1';
        str[2] = '0';
        if (value >= 0){
            str[3] = '0';
        }else {
            str[3] = '1';
            value = -1 * value;
        }
        while (value > 0){
            num = value % 10;
//            if(num == 0){
//                str[31-cnt] = '0';
//                str[30-cnt] = '0';
//                str[29-cnt] = '0';
//                str[28-cnt] = '0';
//            } else
            if (num == 1) {
                str[31-cnt] = '1';
            } else if (num == 2) {
                str[30-cnt] = '1';
            } else if (num == 3) {
                str[31-cnt] = '1';
                str[30-cnt] = '1';
            } else if (num == 4) {
                str[29-cnt] = '1';
            } else if (num == 5) {
                str[29-cnt] = '1';
                str[31-cnt] = '1';
            } else if (num == 6) {
                str[29-cnt] = '1';
                str[30-cnt] = '1';
            } else if (num == 7) {
                str[29-cnt] = '1';
                str[30-cnt] = '1';
                str[31-cnt] = '1';
            } else if (num == 8) {
                str[28-cnt] = '1';
            } else if (num == 9) {
                str[28-cnt] = '1';
                str[31-cnt] = '1';
            }
            cnt += 4;
            value /= 10;
        }
        return new String(str);
    }

    public static String NBCDToDecimal(String NBCDStr) {
        // TODO:
//        NBCD转十进制
        long value = 0;
        for (int i = 0; i < 7; i++){
            if(NBCDStr.startsWith("0000", 4 + 4 * i)){
                value = value * 10;
            } else if (NBCDStr.startsWith("0001", 4 + 4 * i)) {
                value = 10 * value + 1;
            } else if (NBCDStr.startsWith("0010", 4 + 4 * i)) {
                value = 10 * value + 2;
            } else if (NBCDStr.startsWith("0011", 4 + 4 * i)) {
                value = 10 * value + 3;
            } else if (NBCDStr.startsWith("0100", 4 + 4 * i)) {
                value = 10 * value + 4;
            } else if (NBCDStr.startsWith("0101", 4 + 4 * i)) {
                value = 10 * value + 5;
            } else if (NBCDStr.startsWith("0110", 4 + 4 * i)) {
                value = 10 * value + 6;
            } else if (NBCDStr.startsWith("0111", 4 + 4 * i)) {
                value = 10 * value + 7;
            } else if (NBCDStr.startsWith("1000", 4 + 4 * i)) {
                value = 10 * value + 8;
            } else if (NBCDStr.startsWith("1001", 4 + 4 * i)) {
                value = 10 * value + 9;
            }
        }
        if(NBCDStr.charAt(3) == '1'){
            value = -1 * value;
        }
        return String.valueOf(value);
    }

    public static String floatToBinary(String floatStr) {
        // TODO:
        /*
          5. 将浮点数真值转化成32位单精度浮点数表示
             - 负数以"
             -"开头，正数不需要正号
             - 考虑正负无穷的溢出（"+Inf", "-Inf"，见测试用例格式）
         */
//        float value = Float.parseFloat(floatStr);
//        long num1;
//        float num2;
//        int e = 0;
//        int mark = 0;
//        char[] str = new char[32];
//        char[] n1 = new char[130];
//        char[] n2 = new char[130];
//        int cnt1 = 0;
//        int cnt2 = 0;
//        for(int i = 0; i < 32; i++){
//            str[i] = '0';
//        }
//        for(int i = 0; i < 130; i++){
//            n1[i] = '0';
//        }
//        for(int i = 0; i < 130; i++){
//            n2[i] = '0';
//        }
//        if(value > Float.MAX_VALUE){
//            return "+Inf";
//        } else if (value < -Float.MAX_VALUE) {
//            return "-Inf";
//        }
//        else {
//            if(value >= 0){
//                str[0] = '0';
//            }else {
//                str[0] = '1';
//                value = -1 * value;
//            }
//            num1 = (long) Math.floor(value);
//            num2 = value - num1;
//            for(int i = 129; num1 > 0; i--){
//                if(num1 % 2 == 1){
//                    n1[i] = '1';
//                }
//                else {
//                    n1[i] = '0';
//                }
//                num1 /= 2;
//                cnt1++;
//            }
//            for (int i = 0; num2 > 0 && i < 130; i++){
//                num2 *= 2;
//                if ((int) Math.floor(num2) == 1){
//                    n2[i] = '1';
//                    num2 = num2 - (int) Math.floor(num2);
//                }else {
//                    n2[i] = '0';
//                }
//                cnt2++;
//            }
//            if(value >= (float) (1)){
//                for (int i = 9; i < 32; i++){
//                    if (i < cnt1 - 1 + 9){
//                        str[i] = n1[131-cnt1+i-9];
//                    }else {
//                        str[i] = n2[i-cnt1+1-9];
//                    }
//                }
//                e = cnt1 + 127 - 1;
//            }else if(value < 1 && value > (float) Math.pow(2,-127)){
//                for (int j = 0; j < cnt2; j++){
//                    if (n2[j] == '1'){
//                        mark = j+1;
//                        break;
//                    }
//                }
//                e = -1 * mark + 127;
//                for (int i = 9; i < 32 && mark + i - 9 < 130; i++){
//                    str[i] = n2[mark+i-9];
//                }
//            }else {
//                str[9] = '1';
//                if(value == 0){
//                    str[9] = '0';
//                }
//            }
//            for(int i = 8; e > 0; i--){
//                if(e % 2 == 1){
//                    str[i] = '1';
//                }
//                else {
//                    str[i] = '0';
//                }
//                e /= 2;
//            }
//        }
//        return (String.valueOf(str));

//        助教这个看起来更有条理性，复现一下
        float value = Float.parseFloat(floatStr);//转化为浮点数
        if(Float.isNaN(value)){
            return "Nan";//nan的情况
        }
        if(value > Float.MAX_VALUE){//正负无穷的情况
            return "+Inf";
        } else if (value < - 1 * Float.MAX_VALUE) {
            return "-Inf";
        }
        //普通情况
        char[] result = new char[32];//记录转化的结果
        if(value >= 0){
            result[0] = '0';
        }//写入符号位
        else {
            result[0] = '1';
        }
        if(value == 0.0){//值为0的情况
            return result[0] + "0000000000000000000000000000000";
        }else {//更加一般的情况
            value = Math.abs(value);//浮点数正负除了符号位没有区别，取绝对值
            int bias = 127;//感觉没必要，可能表示更清楚吧，指数的基础指数

        }
        return null;
    }

    public static String binaryToFloat(String binStr) {
        // TODO:
        /*
        6. 将32位单精度浮点数表示转化成浮点数真值
        - 特殊情况同上
         */
        float value;
        int e = 0;
        float s = 0;
        float s1;
        for (int i = 1; i < 9; i++){
            if(binStr.charAt(i) == '1'){
                e = 2 * e + 1;
            }else {
                e = 2 * e;
            }
        }
        e -= 127;
        for(int i = 9; i < 32; i++){
            if(binStr.charAt(i) == '1'){
                s = 2 * s + 1;
            }else {
                s = 2 * s;
            }
        }
        if(binStr.charAt(0) == '0' && e == 128 && s == 0){
            return "+Inf";
        } else if (binStr.charAt(0) == '1' && e == 128 && s == 0) {
            return "-Inf";
        }
        s1 = s;
        s1 *= (float)Math.pow(2, -23);
        s1 += 1;
        if(e == -127){
            s *= Math.pow(2, -23);
//            s += 1;
            value = (s * (float)Math.pow(2, -126));
            if(binStr.charAt(0) == '1'){
                value = (float) (-1.0 * value);
            }
            return String.valueOf(value);
        }
        value = (s1 * (float)Math.pow(2, e));
        if(binStr.charAt(0) == '1'){
            value = (float) (-1.0 * value);
        }
        return String.valueOf(value);
    }

}
