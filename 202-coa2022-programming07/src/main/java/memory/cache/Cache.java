package memory.cache;

import memory.Memory;
import memory.cache.cacheReplacementStrategy.ReplacementStrategy;
import util.Transformer;

import java.util.Arrays;
import java.util.Objects;

/**
 * 高速缓存抽象类
 */
public class Cache {

    public static final boolean isAvailable = true; // 默认启用Cache

    public static final int CACHE_SIZE_B = 32 * 1024; // 32 KB 总大小

    public static final int LINE_SIZE_B = 64; // 64 B 行大小

    private final CacheLine[] cache = new CacheLine[CACHE_SIZE_B / LINE_SIZE_B];

    private int SETS;   // 组数

    private int setSize;    // 每组行数

    // 单例模式
    private static final Cache cacheInstance = new Cache();

    private Cache() {
        for (int i = 0; i < cache.length; i++) {
            cache[i] = new CacheLine();
        }
    }

    public static Cache getCache() {
        return cacheInstance;
    }

    private ReplacementStrategy replacementStrategy;    // 替换策略

    public static boolean isWriteBack;   // 写策略

    /**
     * 读取[pAddr, pAddr + len)范围内的连续数据，可能包含多个数据块的内容
     *
     * @param pAddr 数据起始点(32位物理地址 = 26位块号 + 6位块内地址)
     * @param len   待读数据的字节数
     * @return 读取出的数据，以char数组的形式返回
     */
    public byte[] read(String pAddr, int len) {
        byte[] data = new byte[len];
        int addr = Integer.parseInt(Transformer.binaryToInt("0" + pAddr));
        int upperBound = addr + len;
        int index = 0;
        while (addr < upperBound) {
            int nextSegLen = LINE_SIZE_B - (addr % LINE_SIZE_B);
            if (addr + nextSegLen >= upperBound) {
                nextSegLen = upperBound - addr;
            }
            int rowNO = fetch(Transformer.intToBinary(String.valueOf(addr)));
            byte[] cache_data = cache[rowNO].getData();
            int i = 0;
            while (i < nextSegLen) {
                data[index] = cache_data[addr % LINE_SIZE_B + i];
                index++;
                i++;
            }
            addr += nextSegLen;
        }

        return data;
    }

    /**
     * 向cache中写入[pAddr, pAddr + len)范围内的连续数据，可能包含多个数据块的内容
     *
     * @param pAddr 数据起始点(32位物理地址 = 26位块号 + 6位块内地址)
     * @param len   待写数据的字节数
     * @param data  待写数据
     */
    public void write(String pAddr, int len, byte[] data) {
        int addr = Integer.parseInt(Transformer.binaryToInt("0" + pAddr));
        int upperBound = addr + len;
        int index = 0;
        while (addr < upperBound) {
            int nextSegLen = LINE_SIZE_B - (addr % LINE_SIZE_B);//到行末的字节数
            if (addr + nextSegLen >= upperBound) {//未跨行
                nextSegLen = upperBound - addr;//到写结束的行内字节数
            }
            int rowNO = fetch(Transformer.intToBinary(String.valueOf(addr)));//得到目前行所在行号
            byte[] cache_data = cache[rowNO].getData();//获取该行现在的数据
            int i = 0;
            while (i < nextSegLen) {
                cache_data[addr % LINE_SIZE_B + i] = data[index];//开始从对应起始位置写数据，不过有些不明白为什么要用cache_data来存储？直接修改cache不行吗？
                //哦，好像确实需要，直接修改cache无法实现部分修改
                index++;
                i++;
            }

            // TODO
            cache[rowNO].dirty = true;//脏位设置为true，表示被修改
            cache[rowNO].data = cache_data;//更新cache中的数据

//            突然觉得这一步毫无必要，就在cache里写，当然标记为早就计算好了，当然，也必定是有效的
//            cache[rowNO].validBit = true;
//            String sign = Transformer.intToBinary((String.valueOf(getBlockNO(Transformer.intToBinary(String.valueOf(addr))) / SETS)));
//            for (int j = 6; j < 32; j++) {//写入标志位，取32位的后26位
//                cache[rowNO].tag[j-6] = sign.charAt(j);
//            }
            addr += nextSegLen;//跨行，转换到下一行的开始位置
        }

        Memory memory = Memory.getMemory();
        if(!isWriteBack){//写直达策略，比助教简洁，win!!!
            memory.write(pAddr, len, data);
        }
    }


    /**
     * 查询{@link Cache#cache}表以确认包含pAddr的数据块是否在cache内
     * 如果目标数据块不在Cache内，则将其从内存加载到Cache
     *
     * @param pAddr 数据起始点(32位物理地址 = 26位块号 + 6位块内地址)
     * @return 数据块在Cache中的对应行号
     */
    private int fetch(String pAddr) {
        // TODO
        int block_num = getBlockNO(pAddr);//获得对应的块号
        int cache_line = map(block_num);//获得块号所对应行号
        int group_num = block_num % SETS;//组号
        if(cache_line == -1){
//            未命中，去主存寻找
            Memory memory = Memory.getMemory();
            long min_time = 0;
            if(cache[group_num * setSize].validBit){
                min_time = cache[group_num * setSize].timeStamp;
            }
            for (int i = group_num * setSize; i < group_num * setSize + setSize; i++) {
                if(cache[i].validBit && cache[i].timeStamp < min_time){
                    min_time = cache[i].timeStamp;
                }
            }
            for (int i = group_num * setSize; i < group_num * setSize + setSize; i++) {
                if (!cache[i].validBit) {//无效数据，可以直接将新数据放于此
                    cache[i].visited = 0;//这里做一个重置，防止前面一次测试对后面产生影响
                    cache[i].timeStamp = 0L;
//                    cache[i].dirty = false;
                    cache[i].validBit = true;//有效位设置为true
                    cache[i].data = memory.read(pAddr.substring(0, 26) + "000000", LINE_SIZE_B);//以该块首个字节地址获取整块内容，并存在cache的data中
                    String sign = Transformer.intToBinary((String.valueOf(block_num / SETS)));//组内编号困扰许久，应该举个例子就清楚了，0和512块共享同一行，0对应0，512对应1，以此类推
                    for (int j = 6; j < 32; j++) {//写入标志位，取32位的后26位
                        cache[i].tag[j-6] = sign.charAt(j);
                    }
                    cache[i].timeStamp = min_time - 1;//即使后进入，也是最近访问，nice！！！
                    cache[i].visited = 1;
                    return i;
                }
            }
            //没有空位，需要执行替换策略
            String sign = Transformer.intToBinary((String.valueOf(block_num / SETS)));
            char[] change_tag = new char[26];
            for (int i = 6; i < 32; i++) {
                change_tag[i-6] = sign.charAt(i);
            }
            byte[] input = memory.read(pAddr.substring(0, 26) + "000000", LINE_SIZE_B);
            return replacementStrategy.replace(group_num * setSize, group_num * setSize + setSize - 1, change_tag, input);
        }else{
//            命中
//            调用各自的hit()方法
            replacementStrategy.hit(cache_line);
            long min_time = Long.MAX_VALUE;
            for (int i = group_num * setSize; i < group_num * setSize + setSize; i++) {
                if(cache[i].validBit && cache[i].timeStamp < min_time){
                    min_time = cache[i].timeStamp;
                }
            }
            if (Objects.equals(replacementStrategy.show(), "LRUR")){
                cache[cache_line].timeStamp = min_time - 1;
            }
            return cache_line;
        }
    }


    /**
     * 根据目标数据内存地址前26位的int表示，进行映射
     *
     * @param blockNO 数据在内存中的块号
     * @return 返回cache中所对应的行，-1表示未命中
     */
    private int map(int blockNO) {
        // TODO
        int group_num = blockNO % SETS;//组号
        String sign = Transformer.intToBinary((String.valueOf(blockNO / SETS)));//将组内号化为二进制表示
        for (int i = group_num * setSize; i < group_num * setSize + setSize; i++) {
            if (cache[i].validBit && new String(cache[i].getTag()).equals(sign.substring(6))) {//数据有效且标志位相同，找到！
                return i;//命中，返回对应行号
            }
        }
        return -1;//未命中
    }


    /**
     * 更新cache
     *
     * @param rowNO 需要更新的cache行号
     * @param tag   待更新数据的Tag
     * @param input 待更新的数据
     */
    public void update(int rowNO, char[] tag, byte[] input) {
        // TODO
        //在update时执行写回策略
        if(isWriteBack){
            if(cache[rowNO].dirty){//数据经过修改
                //计算主存相应的地址？
                int group_num = rowNO / setSize;//计算组号
                int sign_num = Integer.parseInt(Transformer.binaryToInt("0" + new String(cache[rowNO].tag)));//组内号
                int block_num = group_num + sign_num * SETS;//获得块号
                String addr = Transformer.intToBinary(String.valueOf(block_num));
                String paddr = addr.substring(6) + "000000";//行的起始地址
                Memory memory = Memory.getMemory();
                memory.write(paddr, LINE_SIZE_B, cache[rowNO].data);
                cache[rowNO].dirty = false;
                cache[rowNO].validBit = true;
            }
        }
        cache[rowNO].data = input;
        cache[rowNO].tag = tag;
    }


    /**
     * 从32位物理地址(26位块号 + 6位块内地址)获取目标数据在内存中对应的块号
     *
     * @param pAddr 32位物理地址
     * @return 数据在内存中的块号
     */
    private int getBlockNO(String pAddr) {
        return Integer.parseInt(Transformer.binaryToInt("0" + pAddr.substring(0, 26)));
    }


    /**
     * 该方法会被用于测试，请勿修改
     * 使用策略模式，设置cache的替换策略
     *
     * @param replacementStrategy 替换策略
     */
    public void setReplacementStrategy(ReplacementStrategy replacementStrategy) {
        this.replacementStrategy = replacementStrategy;
    }

    /**
     * 该方法会被用于测试，请勿修改
     *
     * @param SETS 组数
     */
    public void setSETS(int SETS) {
        this.SETS = SETS;
    }

    /**
     * 该方法会被用于测试，请勿修改
     *
     * @param setSize 每组行数
     */
    public void setSetSize(int setSize) {
        this.setSize = setSize;
    }

    /**
     * 告知Cache某个连续地址范围内的数据发生了修改，缓存失效
     * 该方法仅在memory类中使用，请勿修改
     *
     * @param pAddr 发生变化的数据段的起始地址
     * @param len   数据段长度
     */
    public void invalid(String pAddr, int len) {
        int from = getBlockNO(pAddr);
        int to = getBlockNO(Transformer.intToBinary(String.valueOf(Integer.parseInt(Transformer.binaryToInt("0" + pAddr)) + len - 1)));

        for (int blockNO = from; blockNO <= to; blockNO++) {
            int rowNO = map(blockNO);
            if (rowNO != -1) {
                cache[rowNO].validBit = false;
            }
        }
    }

    /**
     * 清除Cache全部缓存
     * 该方法会被用于测试，请勿修改
     */
    public void clear() {
        for (CacheLine line : cache) {
            if (line != null) {
                line.validBit = false;
            }
        }
    }

    /**
     * 输入行号和对应的预期值，判断Cache当前状态是否符合预期
     * 这个方法仅用于测试，请勿修改
     *
     * @param lineNOs     行号
     * @param validations 有效值
     * @param tags        tag
     * @return 判断结果
     */
    public boolean checkStatus(int[] lineNOs, boolean[] validations, char[][] tags) {
        if (lineNOs.length != validations.length || validations.length != tags.length) {
            return false;
        }
        for (int i = 0; i < lineNOs.length; i++) {
            CacheLine line = cache[lineNOs[i]];
            if (line.validBit != validations[i]) {
                return false;
            }
            if (!Arrays.equals(line.getTag(), tags[i])) {
                return false;
            }
        }
        return true;
    }


    /**
     * Cache行，每行长度为(1+22+{@link Cache#LINE_SIZE_B})
     */
    private static class CacheLine {

        // 有效位，标记该条数据是否有效
        boolean validBit = false;

        // 脏位，标记该条数据是否被修改
        boolean dirty = false;

        // 用于LFU算法，记录该条cache使用次数
        int visited = 0;

        // 用于LRU和FIFO算法，记录该条数据时间戳
        Long timeStamp = 0L;

        // 标记，占位长度为26位，有效长度取决于映射策略：
        // 直接映射: 17 位
        // 全关联映射: 26 位
        // (2^n)-路组关联映射: 26-(9-n) 位
        // 注意，tag在物理地址中用高位表示，如：直接映射(32位)=tag(17位)+行号(9位)+块内地址(6位)，
        // 那么对于值为0b1111的tag应该表示为00000000000000000000001111，其中低12位为有效长度
        char[] tag = new char[26];

        // 数据
        byte[] data = new byte[LINE_SIZE_B];

        byte[] getData() {
            return this.data;
        }

        char[] getTag() {
            return this.tag;
        }

    }

    // 获取有效位
    public boolean isValid(int rowNO){
        return cache[rowNO].validBit;
    }

    // 获取脏位
    public boolean isDirty(int rowNO){
        return cache[rowNO].dirty;
    }

    // LFU算法增加访问次数
    public void addVisited(int rowNO){
        cache[rowNO].visited++;
    }

    // 获取访问次数
    public int getVisited(int rowNO){
        return cache[rowNO].visited;
    }

    // 重置访问次数
    public void setVisited(int rowNO){
        cache[rowNO].visited = 1;
    }

    // 用于LRU算法，重置时间戳
    public void setTimeStamp(int rowNO, long min_time){
        cache[rowNO].timeStamp = min_time - 1;
    }

    // 获取时间戳
    public long getTimeStamp(int rowNO){
        return cache[rowNO].timeStamp;
    }

    // 获取该行数据
    public byte[] getData(int rowNO){
        return cache[rowNO].data;
    }
}
