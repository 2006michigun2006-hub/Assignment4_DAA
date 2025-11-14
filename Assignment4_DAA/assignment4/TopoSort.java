package assignment4;

import java.util.*;

public class TopoSort {
    public static List<Integer> kahnOrder(int n, List<int[]> edges){
        List<Integer> order = new ArrayList<>();
        int[] indeg = new int[n];
        for(int[] e: edges) indeg[e[1]]++;

        Deque<Integer> dq = new ArrayDeque<>();
        for(int i=0;i<n;i++) if(indeg[i]==0) dq.add(i);

        while(!dq.isEmpty()){
            int u = dq.removeFirst();
            order.add(u);
            for(int[] e: edges){
                if(e[0]==u){
                    indeg[e[1]]--;
                    if(indeg[e[1]]==0) dq.add(e[1]);
                }
            }
        }

        if(order.size()!=n) return null;
        return order;
    }
}
