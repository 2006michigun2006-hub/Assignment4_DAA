package assignment4;

import java.util.*;

public class DAGSP {

    public static Map<Integer,Long> shortestFrom(int n, List<List<int[]>> adj, int source){
        List<int[]> edgesFlat = new ArrayList<>();
        for(int u=0; u<n; u++)
            for(int[] e: adj.get(u))
                edgesFlat.add(new int[]{u,e[0]});

        List<Integer> topo = TopoSort.kahnOrder(n, edgesFlat);

        final long INF = Long.MAX_VALUE/4;
        Map<Integer,Long> dist = new HashMap<>();
        for(int i=0;i<n;i++) dist.put(i, INF);
        dist.put(source, 0L);

        if(topo==null) return dist;

        for(int u: topo){
            long du = dist.get(u);
            if(du==INF) continue;
            for(int[] e: adj.get(u)){
                int v=e[0], w=e[1];
                long nd = du + w;
                if(nd < dist.get(v))
                    dist.put(v, nd);
            }
        }
        return dist;
    }

    public static Pair<Long,int[]> longestPath(int n, List<List<int[]>> adj){
        List<int[]> edgesFlat = new ArrayList<>();
        for(int u=0;u<n;u++)
            for(int[] e: adj.get(u))
                edgesFlat.add(new int[]{u,e[0]});

        List<Integer> topo = TopoSort.kahnOrder(n, edgesFlat);
        final long NEG = Long.MIN_VALUE/4;

        long[] dp = new long[n];
        Arrays.fill(dp, NEG);
        int[] prev = new int[n];
        Arrays.fill(prev, -1);

        int[] indeg = new int[n];
        for(int[] e: edgesFlat) indeg[e[1]]++;

        for(int i=0;i<n;i++) if(indeg[i]==0) dp[i]=0;

        if(topo!=null){
            for(int u: topo){
                if(dp[u]==NEG) continue;
                for(int[] e: adj.get(u)){
                    int v=e[0], w=e[1];
                    long nd = dp[u]+w;
                    if(nd > dp[v]){
                        dp[v]=nd;
                        prev[v]=u;
                    }
                }
            }
        }

        long best = NEG;
        int bestV = -1;
        for(int i=0;i<n;i++){
            if(dp[i] > best){
                best=dp[i];
                bestV=i;
            }
        }

        return new Pair<>(best<0?0:best, prev);
    }

    public static class Pair<A,B>{
        public final A a; public final B b;
        public Pair(A a,B b){ this.a=a; this.b=b; }
    }
}
