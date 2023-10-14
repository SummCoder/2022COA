package cpu.alu;

import util.DataType;

import java.util.Objects;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 */
public class ALU {

    public char[] add(String src, String dest) {
        // 运算过程中所需用到的加法运算
        char[] result = new char[32];
        for (int i = 0; i < 32; i++){
            result[i] = '0';
        }
        for (int i = 31 ; i >= 0; i--){
            if(src.charAt(i) == '0' && dest.charAt(i) == '0'){
                if(result[i] == '0'){
                    result[i] = '0';
                }else {
                    result[i] = '1';
                }
            } else if (src.charAt(i) == '0' && dest.charAt(i) == '1') {
                if(result[i] == '0'){
                    result[i] = '1';
                }else {
                    if(i != 0){
                        result[i-1] = '1';
                    }
                    result[i] = '0';
                }
            } else if (src.charAt(i) == '1' && dest.charAt(i) == '0') {
                if(result[i] == '0'){
                    result[i] = '1';
                }else {
                    if(i != 0){
                        result[i-1] = '1';
                    }
                    result[i] = '0';
                }
            } else {
                if (result[i] != '0') {
                    result[i] = '1';
                }
                if(i != 0){
                    result[i-1] = '1';
                }
            }
        }
        return result;
    }

    public char[] sub(String src, String dest) {
        // TODO
//        dest-src
        char[] src2 = new char[32];
        for (int i = 0; i < 32; i++){
            if(src.charAt(i) == '0'){
                src2[i] = '1';
            }else {
                src2[i] = '0';
            }
        }
        String src3 = new String(src2);
        char[] result1 = add(src3, dest);
//        取反加一记得，那他妈ALU有什么难的？突然疑惑了
        return add(new String(result1), ("00000000000000000000000000000001"));
    }


    DataType remainderReg;

    /**
     * 返回两个二进制整数的除法结果
     * dest ÷ src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType div(DataType src, DataType dest) {
        // TODO
//        恢复余数解法
        String src1;
        String dest1;
        if(src.toString().charAt(0) == '1'){//将负数化为正数进行除法运算
            src1 = new String(sub(src.toString(), "00000000000000000000000000000000"));
        }else {
            src1 = src.toString();
        }
        if(dest.toString().charAt(0) == '1'){
            dest1 = new String(sub(dest.toString(), "00000000000000000000000000000000"));
        }else {
            dest1 = dest.toString();
        }
        char[] result = new char[64];//64位用来存储余数和商
        char[] mid = new char[32];//用做每一次的余数参与运算
        char[] que = new char[32];
        for(int i = 0; i < 32; i++){
            result[i] = '0';
            mid[i] = '0';
        }
        for(int i = 32; i < 64; i++){
            result[i] = dest1.charAt(i-32);
        }
        if(Objects.equals(src1, "00000000000000000000000000000000")){
            throw new ArithmeticException();//ArithmeticException
        }else if(Objects.equals(dest1, "00000000000000000000000000000000")){//被除数为0
            remainderReg = new DataType("00000000000000000000000000000000");
            return new DataType("00000000000000000000000000000000");
        }else {//一般情况
            for(int k = 0; k < 32; k++){
                System.arraycopy(result, 1, result, 0, 63);//余数左移
                System.arraycopy(result, 0, mid, 0, 32);//拷贝余数
                if(mid[0] != src1.charAt(0)){//异号做加法
                    if(add(new String(mid), src1)[0] == mid[0]){//符号相同，够加
                        for(int i = 0; i < 32; i++){
                            result[i] = add(new String(mid), src1)[i];//更新余数
                        }
                        System.arraycopy(result, 0, mid, 0, 32);//拷贝余数
                        result[63] = '1';//上商为1
                    }else {
                        result[63] = '0';//上商为0
                    }
                }else {//同号做减法，同理
                    if(sub(src1, new String(mid))[0] == mid[0]){//符号相同，够减
                        for(int i = 0; i < 32; i++){
                            result[i] = sub(src1, new String(mid))[i];
                        }
                        System.arraycopy(result, 0, mid, 0, 32);
                        result[63] = '1';
                    }else {
                        result[63] = '0';
                    }
                }
            }
            remainderReg = new DataType(new String(mid));//当前余数
            if(dest.toString().charAt(0) == src.toString().charAt(0)){//判断原本是否符号相同
                System.arraycopy(result, 32, que, 0, 32);//拷贝商
                if(dest.toString().charAt(0) == '1'){
//                    负数、负数相除，余数取相反数
                    remainderReg = new DataType(new String(sub(new String(mid), "00000000000000000000000000000000")));
                }
            }else {
                System.arraycopy(result, 32, que, 0, 32);
                if(dest.toString().charAt(0) == '1'){
                    remainderReg = new DataType(new String(sub(new String(mid), "00000000000000000000000000000000")));
                }
//                正负相除，商取相反数
                return new DataType(new String(sub(new String(que), "00000000000000000000000000000000")));
            }
            return new DataType(new String(que));
        }
    }
}
