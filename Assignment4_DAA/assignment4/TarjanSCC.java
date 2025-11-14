package assignment4;

import java.util.*;

public class TarjanSCC {
    private final Graph g;
    private int time = 0;
    private final int[] disc, low, compId;
    private final boolean[] onStack;
    private final Deque<Integer> stack = new ArrayDeque<>();
    private int compCount = 0;
    private final List<List<Integer>> components = new ArrayList<>();

    public TarjanSCC(Graph g){
        this.g=g;
        disc = new int[g.n];
        low = new int[g.n];
        compId = new int[g.n];
        onStack = new boolean[g.n];
        Arrays.fill(disc, -1);
        Arrays.fill(compId, -1);
    }

    public void run(){
        for(int v=0; v<g.n; v++)
            if(disc[v]==-1) dfs(v);
    }

    private void dfs(int v){
        disc[v]=low[v]=time++;
        stack.push(v);
        onStack[v]=true;

        for(int to: g.adj.get(v)){
            if(disc[to]==-1){
                dfs(to);
                low[v]=Math.min(low[v], low[to]);
            }
            else if(onStack[to]){
                low[v]=Math.min(low[v], disc[to]);
            }
        }

        if(low[v]==disc[v]){
            List<Integer> comp = new ArrayList<>();
            while(true){
                int w = stack.pop();
                onStack[w]=false;
                compId[w]=compCount;
                comp.add(w);
                if(w==v) break;
            }
            components.add(comp);
            compCount++;
        }
    }

    public int[] getCompId(){ return compId; }
    public List<List<Integer>> getComponents(){ return components; }
    public int getCompCount(){ return compCount; }
}
