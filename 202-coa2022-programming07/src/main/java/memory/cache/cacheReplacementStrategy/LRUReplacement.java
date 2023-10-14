package memory.cache.cacheReplacementStrategy;

import memory.cache.Cache;

/**
 * TODO 最近最少用算法
 */
public class LRUReplacement implements ReplacementStrategy {
    @Override
    public void hit(int rowNO) {
//        do nothing
    }

    @Override
    public int replace(int start, int end, char[] addrTag, byte[] input) {
        Cache cache = Cache.getCache();
        long max_time = cache.getTimeStamp(start);//记录最长时间
        long min_time = cache.getTimeStamp(start);
        int index = start;//记录最长时间的行
        for (int i = start + 1; i <= end; i++) {
            if(cache.getTimeStamp(i) > max_time){
                max_time = cache.getTimeStamp(i);//获得最长时间未被访问的行
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

    public String show() {
        return "LRUR";
    }
}





























