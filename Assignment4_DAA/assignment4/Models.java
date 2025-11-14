package assignment4;
import java.util.*;
public class Models {
    public static class Edge {
        public int u, v, w;
        public Edge(int u, int v, int w) { this.u=u; this.v=v; this.w=w; }
    }

    public static class Dataset {
        public boolean directed;
        public int n;
        public List<Edge> edges = new ArrayList<>();
        public int source;
        public String weight_model;
    }

    public static class Result {
        public String file;
        public SCCResult scc;
        public CondResult condensation;
        public TopoResult topo;
        public SPResult shortest_paths;
        public LPResult longest_path;
    }

    public static class SCCResult {
        public int count;
        public List<List<Integer>> components;
    }

    public static class CondResult {
        public int n;
        public List<int[]> edges;
    }

    public static class TopoResult {
        public List<Integer> comp_topo_order;
        public List<Integer> tasks_order;
    }

    public static class SPResult {
        public int source_component;
        public Map<Integer, Long> dist;
        public Map<Integer, List<Integer>> paths;
    }

    public static class LPResult {
        public long length;
        public List<Integer> comp_path;
    }
}