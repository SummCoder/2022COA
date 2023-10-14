package memory.cache.cacheReplacementStrategy;

import memory.cache.Cache;

/**
 * TODO 最近不经常使用算法
 */
public class LFUReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        Cache cache = Cache.getCache();
        cache.addVisited(rowNO);
    }

    @Override
    public int replace(int start, int end, char[] addrTag, byte[] input) {
        Cache cache = Cache.getCache();
        int min_visited = cache.getVisited(start);//记录最少访问次数
        int index = start;//记录最少访问的行
        for (int i = start + 1; i <= end; i++) {
            if(cache.getVisited(i) < min_visited){
                min_visited = cache.getVisited(i);
                index = i;
            }
        }
        cache.setVisited(index);//更新该位置访问次数
        cache.update(index, addrTag, input);
        return index;
    }

    @Override
    public String show() {
        return "LFUR";
    }
}
