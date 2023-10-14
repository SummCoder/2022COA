package memory.disk;

import java.util.Arrays;

public class Scheduler {

    /**
     * 先来先服务算法
     *
     * @param start   磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double FCFS(int start, int[] request) {
        // TODO
        int sum = 0;
        int index = start;
        for (int j : request) {
            sum += Math.abs(j - index);
            index = j;
        }
        return 1.0 * sum / request.length;
    }

    /**
     * 最短寻道时间优先算法
     *
     * @param start   磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double SSTF(int start, int[] request) {
        // TODO
//        int sum = 0;
//        int index = request[0];
//        int mid1 = Math.abs(request[0] - start);
//        int max = request[0];
//        int min = request[0];
//        for (int j : request) {
//            if (Math.abs(j - start) < mid1) {
//                mid1 = Math.abs(j - start);
//                index = j;
//            }
//            if(j > max){
//                max = j;
//            }
//            if(j < min){
//                min = j;
//            }
//        }
//        if(index < start){
//            if(max <= start){
//                sum = start;
//            }else {
//                sum = start + max - 2 * min;
//            }
//        }else {
//            if(min >= start){
//                sum = max - start;
//            }else {
//                sum = 2 * max - start - min;
//            }
//        }
//        return 1.0 * sum / request.length;
//        助教这个才更符合，我那个看来是凑巧了
        double sum = 0;
        boolean[] visited = new boolean[request.length];
        for (int i = 0; i < request.length; i++) {
            int min = Disk.TRACK_NUM;
            int minIndex = -1;
            for (int j = 0; j < request.length; j++) {
                if (Math.abs(request[j] - start) < min && !visited[j]) {
                    min = Math.abs(request[j] - start);
                    minIndex = j;
                }
            }
            sum += min;
            start = request[minIndex];
            visited[minIndex] = true;
        }
        return sum / request.length;
    }

    /**
     * 扫描算法
     *
     * @param start     磁头初始位置
     * @param request   请求访问的磁道号
     * @param direction 磁头初始移动方向，true表示磁道号增大的方向，false表示磁道号减小的方向
     * @return 平均寻道长度
     */
    public double SCAN(int start, int[] request, boolean direction) {
        // TODO
        int sum;
        int max = request[0];
        int min = request[0];
        for (int j : request) {
            if(j > max){
                max = j;
            }
            if(j < min){
                min = j;
            }
        }
        if(direction){
            if(min >= start){
                sum = max - start;
            }else {
                sum = 2 * (Disk.TRACK_NUM - 1 ) - start - min;
//                从0开始编号
            }
        }else {
            if(max <= start){
                sum = start - min;
            }else {
                sum = start + max;
            }
        }
        return 1.0 * sum / request.length;
    }

}
