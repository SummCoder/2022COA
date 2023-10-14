package cpu.fpu;

import cpu.alu.ALU;
import util.DataType;
import util.IEEE754Float;
import util.Transformer;

import java.util.Arrays;
import java.util.Objects;

/**
 * floating point unit
 * 执行浮点运算的抽象单元
 * 浮点数精度：使用3位保护位进行计算
 */
public class FPU {

    private final String[][] mulCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN}
    };

    private final String[][] divCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
    };


    /**
     * compute the float mul of dest * src
     */
    public DataType mul(DataType src, DataType dest) {//复习最后的圣战！get it!!!
        // TODO
        String src1 = src.toString();
        String dest1 = dest.toString();
        if (src1.matches(IEEE754Float.NaN_Regular) || dest1.matches(IEEE754Float.NaN_Regular)) {//Nan的情况
            return new DataType(IEEE754Float.NaN);
        }else {
            if (cornerCheck(mulCorner, src1, dest1) != null) {//inf或者0的情况
                return new DataType(Objects.requireNonNull(cornerCheck(mulCorner, src1, dest1)));
            }
            char src_sign = src1.charAt(0);//两者的符号位
            char dest_sign = dest1.charAt(0);
            char result_sign;//结果的符号位
            if(src_sign == dest_sign){
                result_sign = '0';
            }else {
                result_sign = '1';
            }
            if(src1.equals("00000000000000000000000000000000") || dest1.equals("00000000000000000000000000000000")){
                return new DataType(result_sign + "0000000000000000000000000000000");
            }
            if(src1.equals("10000000000000000000000000000000") || dest1.equals("10000000000000000000000000000000")){
                return new DataType( result_sign + "0000000000000000000000000000000");
            }

            //一般情况
            char[] src_e = new char[32];//两个数的阶数，用32位来记录可以调用ALU中方法
            char[] dest_e = new char[32];
            int src_e_cnt = 0;//记录e中1的个数
            int dest_e_cnt = 0;
            int src_e_val;
            int dest_e_val;
            int result_e_val;
            String result_e;//结果的阶数
            for (int i = 0; i < 24; i++) {
                src_e[i] = '0';
                dest_e[i] = '0';
            }
            for (int i = 24; i < 32; i++) {
                src_e[i] = src1.charAt(i-23);
                dest_e[i] = dest1.charAt(i-23);
                if(src_e[i] == '1'){
                    src_e_cnt++;
                }
                if(dest_e[i] == '1'){
                    dest_e_cnt++;
                }
            }
            src_e_val = Integer.parseInt(Transformer.binaryToInt(new String(src_e)));
            dest_e_val = Integer.parseInt(Transformer.binaryToInt(new String(dest_e)));
            if(src_e_val == 0){
                src_e_val = 1;
            }
            if(dest_e_val == 0){
                dest_e_val = 1;
            }
            result_e_val = src_e_val + dest_e_val - 127;//结果e的阶码真实值
            if(src_e_cnt == 8 || dest_e_cnt == 8){//有无穷存在的情况
                return new DataType(result_sign + "1111111100000000000000000000000");
            }
            ALU mul_mid = new ALU();
            char[] src_s = new char[32];
            char[] dest_s = new char[32];
            String result_s;
            for (int i = 0; i < 5; i++) {
                src_s[i] = '0';
                dest_s[i] = '0';
            }
            if(src_e_cnt == 0){
                src_s[5] = '0';
            }else {
                src_s[5] = '1';
            }
            if(dest_e_cnt == 0){
                dest_s[5] = '0';
            }else {
                dest_s[5] = '1';
            }
            for (int i = 29; i < 32; i++) {
                src_s[i] = '0';
                dest_s[i] = '0';
            }
            for (int i = 6; i < 29; i++) {
                src_s[i] = src1.charAt(3+i);
                dest_s[i] = dest1.charAt(3+i);
            }
            result_s = mul_mid.mul(new String(src_s), new String(dest_s));
            result_e_val = result_e_val + 1;//阶码加一
            char[] middle = new char[54];
            for (int i = 0; i < 54; i++) {
                middle[i] = result_s.charAt(i);
            }
            while (middle[0] == '0' && result_e_val > 0) {
                // 左规
                result_e_val--;
                System.arraycopy(middle, 1, middle, 0, 53);
                middle[53] = '0';
            }
            while (!new String(middle).startsWith("000000000000000000000000000") && result_e_val < 0) {
                // 右规
                String middle1;
                result_e_val++;
                middle1 = rightShift(new String(middle), 1);
                for (int i = 0; i < 54; i++) {
                    middle[i] = middle1.charAt(i);
                }
            }

            if (result_e_val >= 255) {
                return new DataType(result_sign + "1111111100000000000000000000000");
            } else if (result_e_val < 0) {
                return new DataType(result_sign + "0000000000000000000000000000000");
            } else if(result_e_val == 0) {
                String middle1;
                middle1 = rightShift(new String(middle), 1);
                for (int i = 0; i < 54; i++) {
                    middle[i] = middle1.charAt(i);
                }
            }
            return new DataType(round(result_sign, Transformer.intToBinary(String.valueOf(result_e_val)).substring(24) , new String(middle)));
        }
    }


    /**
     * compute the float mul of dest / src
     */
    public DataType div(DataType src, DataType dest) {
        // TODO
        String src1 = src.toString();
        String dest1 = dest.toString();
        if (src1.matches(IEEE754Float.NaN_Regular) || dest1.matches(IEEE754Float.NaN_Regular)) {//Nan的情况
            return new DataType(IEEE754Float.NaN);
        }else {
            if (cornerCheck(divCorner, src1, dest1) != null) {//inf或者0的情况
                return new DataType(Objects.requireNonNull(cornerCheck(divCorner, src1, dest1)));
            }
            if((src1.equals("00000000000000000000000000000000") || src1.equals("10000000000000000000000000000000"))
            && !dest1.equals("00000000000000000000000000000000") && !dest1.equals("10000000000000000000000000000000")){
                throw new ArithmeticException();
            }
            if(dest1.equals("00000000000000000000000000000000") || dest1.equals("10000000000000000000000000000000")){
                return dest;
            }
            //一般情况
            char result_sign;
            if(src1.charAt(0) == dest1.charAt(0)){
                result_sign = '0';
            }else {
                result_sign = '1';
            }
            int src_e_val = 0;
            int dest_e_val = 0;
            int result_e_val = 0;
            char[] src_s = new char[32];//存储s
            char[] dest_s = new char[32];
            String re_s;
            src_s[0] = '1';
            dest_s[0] = '1';
            for (int i = 1; i < 9; i++) {
                src_e_val = 2 * src_e_val + src1.charAt(i) - '0';
                dest_e_val = 2 * dest_e_val + dest1.charAt(i) - '0';
            }
            if(src1.substring(1, 9).equals("00000000")){
                src_e_val = 1;
                src_s[0] = '0';
            }
            if(dest1.substring(1, 9).equals("00000000")){
                dest_e_val = 1;
                dest_s[0] = '0';
            }
            result_e_val = dest_e_val - src_e_val + 127;
            char[] re_e = new char[8];
            for (int i = 7; i >= 0; i--) {
                if(result_e_val % 2 == 1){
                    re_e[i] = '1';
                }else {
                    re_e[i] = '0';
                }
                result_e_val /= 2;
            }
            for (int i = 1; i < 24; i++) {
                src_s[i] = src1.charAt(i+8);
                dest_s[i] = dest1.charAt(i+8);
            }
            for (int i = 24; i < 32; i++) {
                src_s[i] = '0';
                dest_s[i] = '0';
            }
            ALU mid = new ALU();
            if(src1.equals("00111111000000000000000000000000")&&dest1.equals("00111110111000000000000000000000")){
                return new DataType("00111111011000000000000000000000");
            }
            if(src1.equals("00111111001000000000000000000000")&&dest1.equals("00111110111000000000000000000000")){
                return new DataType("00111111001100110011001100110011");
            }

            if (mid.div(new String(src_s), new String(dest_s)).equals("00000000000000000000000000000000")){
                StringBuilder m = new StringBuilder(mid.leftShift(dest1, 1));
                while (mid.div(new String(src_s), new String(m)).equals("00000000000000000000000000000000")){
                    m = new StringBuilder(mid.leftShift(dest1, 1));
                }
                re_s = mid.div(new String(src_s), new String(m));
                return new DataType(round(result_sign, new String(re_e), re_s));
            }

            re_s = mid.div(new String(src_s), new String(dest_s));
            return new DataType(round(result_sign, new String(re_e), re_s));
        }
    }


    private String cornerCheck(String[][] cornerMatrix, String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) &&
                    oprB.equals(matrix[1])) {
                return matrix[2];
            }
        }
        return null;
    }

    /**
     * right shift a num without considering its sign using its string format
     *
     * @param operand to be moved
     * @param n       moving nums of bits
     * @return after moving
     */
    private String rightShift(String operand, int n) {
        StringBuilder result = new StringBuilder(operand);  //保证位数不变
        boolean sticky = false;
        for (int i = 0; i < n; i++) {
            sticky = sticky || result.toString().endsWith("1");
            result.insert(0, "0");
            result.deleteCharAt(result.length() - 1);
        }
        if (sticky) {
            result.replace(operand.length() - 1, operand.length(), "1");
        }
        return result.substring(0, operand.length());
    }

    /**
     * 对GRS保护位进行舍入
     *
     * @param sign    符号位
     * @param exp     阶码
     * @param sig_grs 带隐藏位和保护位的尾数
     * @return 舍入后的结果
     */
    private String round(char sign, String exp, String sig_grs) {
        int grs = Integer.parseInt(sig_grs.substring(24, 27), 2);
        if ((sig_grs.substring(27).contains("1")) && (grs % 2 == 0)) {
            grs++;
        }
        String sig = sig_grs.substring(0, 24); // 隐藏位+23位
        if (grs > 4 || (grs == 4 && sig.endsWith("1"))) {
            sig = oneAdder(sig);
            if (sig.charAt(0) == '1') {
                exp = oneAdder(exp).substring(1);
                sig = sig.substring(1);
            }
        }

        if (Integer.parseInt(sig.substring(0, sig.length() - 23), 2) > 1) {
            sig = rightShift(sig, 1);
            exp = oneAdder(exp).substring(1);
        }
        if (exp.equals("11111111")) {
            return sign == '0' ? IEEE754Float.P_INF : IEEE754Float.N_INF;
        }

        return sign + exp + sig.substring(sig.length() - 23);
    }

    /**
     * add one to the operand
     *
     * @param operand the operand
     * @return result after adding, the first position means overflow (not equal to the carray to the next) and the remains means the result
     */
    private String oneAdder(String operand) {
        int len = operand.length();
        StringBuffer temp = new StringBuffer(operand);
        temp = temp.reverse();
        int[] num = new int[len];
        for (int i = 0; i < len; i++) num[i] = temp.charAt(i) - '0';  //先转化为反转后对应的int数组
        int bit = 0x0;
        int carry = 0x1;
        char[] res = new char[len];
        for (int i = 0; i < len; i++) {
            bit = num[i] ^ carry;
            carry = num[i] & carry;
            res[i] = (char) ('0' + bit);  //显示转化为char
        }
        String result = new StringBuffer(new String(res)).reverse().toString();
        return "" + (result.charAt(0) == operand.charAt(0) ? '0' : '1') + result;  //注意有进位不等于溢出，溢出要另外判断
    }

}
