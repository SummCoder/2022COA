package cpu.alu;

import util.DataType;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 */
public class ALU {

    /**
     * 返回两个二进制整数的和
     * dest + src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType add(DataType src, DataType dest) {
        // TODO
        String s1 = src.toString();
        String s2 = dest.toString();
        char[] result = new char[32];
        for (int i = 0; i < 32; i++){
            result[i] = '0';
        }
        for (int i = 31 ; i >= 0; i--){
            if(s1.charAt(i) == '0' && s2.charAt(i) == '0'){
                if(result[i] == '0'){
                    result[i] = '0';
                }else {
                    result[i] = '1';
                }
            } else if (s1.charAt(i) == '0' && s2.charAt(i) == '1') {
                if(result[i] == '0'){
                    result[i] = '1';
                }else {
                    if(i != 0){
                        result[i-1] = '1';
                    }
                    result[i] = '0';
                }
            } else if (s1.charAt(i) == '1' && s2.charAt(i) == '0') {
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
        String out = new String(result);
        return new DataType(out);
    }

    /**
     * 返回两个二进制整数的差
     * dest - src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType sub(DataType src, DataType dest) {
        // TODO
//        dest-src
        String src1 = src.toString();
        char[] src2 = new char[32];
        for (int i = 0; i < 32; i++){
            if(src1.charAt(i) == '0'){
                src2[i] = '1';
            }else {
                src2[i] = '0';
            }
        }
        String src3 = new String(src2);
        DataType result1 = add(new DataType(src3), dest);
        return add(result1, new DataType("00000000000000000000000000000001"));
    }

}
