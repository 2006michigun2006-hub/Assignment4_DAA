package assignment4;

import java.util.*;
import assignment4.Models.Edge;

public class Condensation {
    public final int compN;
    public final List<List<int[]>> adj;
    public final List<int[]> edgesList = new ArrayList<>();

    public Condensation(Graph g, int[] compId){
        this.compN = Arrays.stream(compId).max().orElse(-1)+1;
        adj = new ArrayList<>();
        for(int i=0;i<compN;i++) adj.add(new ArrayList<>());

        Map<Long,Integer> minWeight = new HashMap<>();

        for(Edge e: g.edges){
            int cu = compId[e.u];
            int cv = compId[e.v];
            if(cu==cv) continue;
            long key = ((long)cu<<32) | (cv & 0xffffffffL);
            int prev = minWeight.getOrDefault(key, Integer.MAX_VALUE);
            if(e.w < prev) minWeight.put(key, e.w);
        }

        for(Map.Entry<Long,Integer> en: minWeight.entrySet()){
            long key = en.getKey();
            int cu = (int)(key>>>32);
            int cv = (int)(key & 0xffffffffL);
            int w = en.getValue();
            adj.get(cu).add(new int[]{cv,w});
            edgesList.add(new int[]{cu,cv});
        }
    }
}
