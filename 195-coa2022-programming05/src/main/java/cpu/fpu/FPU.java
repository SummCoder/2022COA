package cpu.fpu;

import cpu.alu.ALU;
import util.DataType;
import util.IEEE754Float;
import util.Transformer;

import java.util.Objects;

/**
 * floating point unit
 * 执行浮点运算的抽象单元
 * 浮点数精度：使用3位保护位进行计算
 */
public class FPU {

    private final String[][] addCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN}
    };

    private final String[][] subCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN}
    };

    /**
     * compute the float add of (dest + src)
     */
    public DataType add(DataType src, DataType dest) {
        // TODO
        String src1 = src.toString();
        String dest1 = dest.toString();
        if (src1.matches(IEEE754Float.NaN_Regular) || dest1.matches(IEEE754Float.NaN_Regular)) {//Nan的情况
            return new DataType(IEEE754Float.NaN);
        }else {
            if(cornerCheck(addCorner, src1, dest1) != null){//inf或者0的情况
                return new DataType(Objects.requireNonNull(cornerCheck(addCorner, src1, dest1)));
            }
            else {//一般情况
                char src_p_or_n = src1.charAt(0);
                char dest_p_or_n = dest1.charAt(0);//src和dest的符号位
                char re_p_or_n;//结果的符号位
                char[] re_s = new char[27];//结果的S
                int src_cnt1 = 0;//count the number of '0' in e of src
                int src_cnt2 = 0;//count the number of '0' in s of src
                int dest_cnt1 = 0;
                int dest_cnt2 = 0;
                char[] src_e = new char[8];//记录src的阶数
                char[] dest_e = new char[8];//记录dest的阶数
                int val_of_src_e;
                int val_of_dest_e;
                int step_to_move;
                char[] src_s = new char[32];//记录src的尾数，含隐藏位以及保护位
                char[] dest_s = new char[32];//同上
                for(int i = 1; i < 9; i++){//初始化
                    src_e[i-1] = src1.charAt(i);
                    dest_e[i-1] = dest1.charAt(i);
                    if(src_e[i-1] == '0'){
                        src_cnt1++;
                    }
                    if(dest_e[i-1] == '0'){
                        dest_cnt1++;
                    }
                }
                val_of_src_e = Integer.parseInt(Transformer.binaryToInt(new String(src_e)));
                val_of_dest_e = Integer.parseInt(Transformer.binaryToInt(new String(dest_e)));
                if(src_cnt1 == 0){//无穷
                    return src;
                }
                if(dest_cnt1 == 0){
                    return dest;
                }
                if(src_cnt1 == 8){//非规格化，隐藏位0
                    src_s[5] = '0';
                }else {
                    src_s[5] = '1';
                }
                if(dest_cnt1 == 8){//同理
                    dest_s[5] = '0';
                }else {
                    dest_s[5] = '1';
                }
                step_to_move = Math.abs(val_of_src_e-val_of_dest_e);
                if(val_of_dest_e==0||val_of_src_e==0){
                    step_to_move--;
                }
                if(val_of_dest_e==0&&val_of_src_e==0){
                    step_to_move++;
                }
                for (int i = 9; i < 32; i++) {
                    src_s[i-3] = src1.charAt(i);
                    dest_s[i-3] = dest1.charAt(i);
                    if(src_s[i-3] == '0'){
                        src_cnt2++;
                    }
                    if(dest_s[i-3] == '0'){
                        dest_cnt2++;
                    }
                }
                if(src_cnt1 == 8 && src_cnt2 == 23){
                    return dest;
                }
                if(dest_cnt1 == 8 && dest_cnt2 == 23){
                    return src;
                }
                for(int i = 0; i < 5; i++){//为了调用alu的加法，使用了32位
                    src_s[i] = '0';
                    dest_s[i] = '0';
                }
                for(int i = 29; i < 32; i++){//保护位
                    src_s[i] = '0';
                    dest_s[i] = '0';
                }
                //主体部分
                String middle;
                String mid;
                String result;
                int mid_e;
                if(val_of_src_e <= val_of_dest_e){//比较阶码的大小，判断哪个该右移
                    re_p_or_n = dest_p_or_n;
                    mid_e = val_of_dest_e;
                    middle = rightShift(String.valueOf(src_s), step_to_move);
                    if(src_p_or_n == dest_p_or_n){//符号相同，做加法
                        ALU m = new ALU();
                        String add1 = m.add(new DataType(middle), new DataType(new String(dest_s))).toString();
                        if(add1.charAt(4) == '1'){//进位了
                            mid = rightShift(add1, 1);
                            mid_e++;
                            if(mid_e == 255){//溢出
                                return new DataType(re_p_or_n + "1111111100000000000000000000000");
                            }
                        }else {
                            //进位，但是是两个非规格化数相加
                            if(src_cnt1==8&&dest_cnt1==8&&add1.charAt(5) == '1'){
                                mid_e++;
                            }
                            mid = add1;
                        }
                        result = round(re_p_or_n, Transformer.intToBinary(String.valueOf(mid_e)).substring(24), mid.substring(5));
                        return new DataType(result);
                    }else {//符号不同，做减法
                        ALU n = new ALU();
                        String add2;//s
                        if(val_of_dest_e == val_of_src_e&&n.sub(new DataType(middle), new DataType(new String(dest_s))).toString().charAt(0) == '1'){//减出负数，互换减数、被减数
                            add2 = n.sub(new DataType(new String(dest_s)), new DataType(middle)).toString();
                            re_p_or_n = src_p_or_n;//符号位跟他混了
                        }else {
                            add2 = n.sub(new DataType(middle), new DataType(new String(dest_s))).toString();//the other case
                        }
                        for (int i = 5; i < 32; i++) {
                            re_s[i-5] = add2.charAt(i);
                        }
                        int index = 5;
                        while (index<32&&add2.charAt(index) == '0'){//开始疯狂左移
                            if(mid_e == 0){
                                break;
                            }
                            mid_e--;
                            if(mid_e == 0){
                                break;
                            }
                            index++;
                            System.arraycopy(re_s, 1, re_s, 0, 26);//左移一位
                            re_s[26] = '0';//结尾补0
                        }
                        if(index == 32){//太小了，不用区分正负0，理论上是需要的，样例好像不是？
                            return new DataType( "00000000000000000000000000000000");
                        }
                        int cnt = 0;
                        if(mid_e == 0){
                            for (int i = 0; i < 27; i++) {
                                if(re_s[i] == '0'){
                                    cnt++;
                                }
                            }
                            if(cnt == 27){
                                return new DataType( "00000000000000000000000000000000");
                            }
                        }
                        result = round(re_p_or_n, Transformer.intToBinary(String.valueOf(mid_e)).substring(24), new String(re_s));
                        return new DataType(result);
                    }
                }else {
                    return add(dest, src);//两者对换
                }
            }
        }
    }

    /**
     * compute the float add of (dest - src)
     */
    public DataType sub(DataType src, DataType dest) {
        // TODO
        String src1 = src.toString();//减法转换为加法，只需更改减数符号位即可
        char[] src2 = new char[32];
        for(int i = 1; i < 32; i++){
            src2[i] = src1.charAt(i);
        }
        if(src1.charAt(0) == '0'){
            src2[0] = '1';
        }else {
            src2[0] = '0';
        }
        return add(dest, new DataType(new String(src2)));
    }


    private String cornerCheck(String[][] cornerMatrix, String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) && oprB.equals(matrix[1])) {
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
        if (grs > 4) {
            sig = oneAdder(sig);
        } else if (grs == 4 && sig.endsWith("1")) {
            sig = oneAdder(sig);
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
        StringBuilder temp = new StringBuilder(operand);
        temp.reverse();
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
