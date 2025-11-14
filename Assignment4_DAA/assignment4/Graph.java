package assignment4;

import java.util.*;
import static assignment4.Models.Edge;

public class Graph {
    public final int n;
    public final boolean directed;
    public final List<Edge> edges = new ArrayList<>();
    public final List<List<Integer>> adj;
    public final List<List<Integer>> adjIndices;

    public Graph(int n, boolean directed){
        this.n=n; this.directed=directed;
        adj = new ArrayList<>();
        adjIndices = new ArrayList<>();
        for(int i=0;i<n;i++){
            adj.add(new ArrayList<>());
            adjIndices.add(new ArrayList<>());
        }
    }

    public void addEdge(int u, int v, int w){
        edges.add(new Edge(u,v,w));
        adj.get(u).add(v);
        adjIndices.get(u).add(edges.size()-1);
        if(!directed){
            adj.get(v).add(u);
            adjIndices.get(v).add(edges.size()-1);
        }
    }
}
