package memory.cache.cacheReplacementStrategy;

import memory.Memory;
import memory.cache.Cache;

/**
 * TODO 先进先出算法
 */
public class FIFOReplacement implements ReplacementStrategy {
    /**
     * 结合具体的替换策略，进行命中后进行相关操作
     * @param rowNO 行号
     */
    @Override
    public void hit(int rowNO) {
        //感觉对于这个策略没啥用啊
    }
    /**
     * 结合具体的映射策略，在给定范围内对cache中的数据进行替换
     * @param start 起始行
     * @param end 结束行 闭区间
     * @param addrTag tag
     * @param input  数据
     */
    @Override
    public int replace(int start, int end, char[] addrTag, byte[] input) {
        Cache cache = Cache.getCache();
        long max_time = cache.getTimeStamp(start);//记录最长时间
        long min_time = cache.getTimeStamp(start);
        int index = start;//记录最长时间的行
        for (int i = start + 1; i <= end; i++) {
            if(cache.getTimeStamp(i) > max_time){
                max_time = cache.getTimeStamp(i);//获得最先停留的行
                index = i;
            }
            if(cache.getTimeStamp(i) < min_time){
                min_time = cache.getTimeStamp(i);
            }
        }
        cache.setTimeStamp(index, min_time);//将该位置更新为最小
        cache.update(index, addrTag, input);
        return index;//返回需要替换的行号
    }
    public String show() {return "FIFO";}
}
