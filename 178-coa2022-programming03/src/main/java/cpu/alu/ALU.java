package cpu.alu;


import util.DataType;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 */
public class ALU {


    public char[] add(String src, String dest) {
        // TODO
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
//        String out = new String(result);
        return result;
    }


    /**
     * 返回两个二进制整数的乘积(结果低位截取后32位)
     * dest * src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType mul(DataType src, DataType dest) {
        // TODO
        String src1 = src.toString();
        String dest1 = dest.toString();
        char[] src2 = new char[32];//为计算-src做准备
        char[] result = new char[64];//记录结果（部分积）
        char[] middle = new char[32];//每次做加法（减法）的n位结果
        char[] md = new char[32];
        for(int i = 0; i < 64; i++){
            result[i] = '0';
        }
        for(int i = 0; i < 32; i++){//取反，为计算-src做准备
            if(src1.charAt(i) == '0'){
                src2[i] = '1';
            }else {
                src2[i] = '0';
            }
        }
        String src3 = new String(src2);
        String src0 = new String(add(src3, "00000000000000000000000000000001"));//实现src1的取反加一操作，src0即为-src1
        for(int i = 0; i < 32; i++){
            middle[i] = '0';
        }
        //第一次运算
        if(dest1.charAt(31) == '1'){
            for(int i = 0; i < 32; i++){
                result[i] = src0.charAt(i);
            }
        }
        int len = 0;//记录已经进行了几次运算，右移了几位
        //右移
        System.arraycopy(result, 0, result, 1, 32);
        //补符号位
        if(result[1] == '0'){
            result[0] = '0';
        }else {
            result[0] = '1';
        }
        //赋值前n位，准备下一次运算
        System.arraycopy(result,0,middle,0,32);
        len++;
        for (int i = 30; i >= 0 ; i--) {
            if(dest1.charAt(i) == '1'&&dest1.charAt(i+1) == '0'){//减
                System.arraycopy(add(new String(middle), src0),0,md,0,32);//用一个md存储前n位的结果
                System.arraycopy(md, 0, result, 0, 32);//赋值前n位计算的结果
            } else if (dest1.charAt(i) == '0'&&dest1.charAt(i+1)== '1') {//加
                System.arraycopy(add(new String(middle), src1),0,md,0,32);
                System.arraycopy(md, 0, result, 0, 32);//赋值前n位计算的结果
            }
            System.arraycopy(result,0,result,1,len+32);//右移
            if(result[1] == '0'){//补符号位
                result[0] = '0';
            }else {
                result[0] = '1';
            }
            //赋值前n位，准备下一次运算
            System.arraycopy(result,0,middle,0,32);
            len++;
        }
//        char[] re = new char[32];//都多余啦！！！
//        System.arraycopy(result, 32, re, 0, 32);
        return new DataType(new String(result).substring(32));
    }

}
