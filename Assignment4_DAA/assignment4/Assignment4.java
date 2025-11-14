package assignment4;

import java.io.*;
import java.util.*;
import assignment4.Models.*;

public class Assignment4 {

    private static final String[] FILES = new String[]{
            "small1.json","small2.json","small3.json",
            "medium1.json","medium2.json","medium3.json",
            "large1.json","large2.json","large3.json"
    };

    public static void main(String[] args) throws Exception{
        List<Object> results = new ArrayList<>();

        for(String path: FILES){
            File f = new File(path);
            if(!f.exists()){
                System.err.println("Warning: missing input file: " + path);
                continue;
            }

            Dataset ds = loadDataset(f);
            Result res = processDataset(ds, f.getName());
            results.add(toMap(res));
        }

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("results", results);

        try(PrintWriter pw = new PrintWriter(new FileWriter("output.json"))){
            pw.print(SimpleJson.toJsonPretty(out));
        }

        System.out.println("Wrote output.json");
    }

    private static Dataset loadDataset(File f) throws IOException{
        Object raw = SimpleJson.parse(f);
        Map<String,Object> m = (Map<String,Object>)raw;

        Dataset ds = new Dataset();
        ds.directed = (Boolean)m.getOrDefault("directed", Boolean.TRUE);
        ds.n = ((Number)m.get("n")).intValue();
        ds.source = ((Number)m.getOrDefault("source", 0)).intValue();
        ds.weight_model = (String)m.getOrDefault("weight_model", "edge");

        List<Object> edges = (List<Object>)m.get("edges");
        for(Object o: edges){
            Map<String,Object> em = (Map<String,Object>)o;
            int u = ((Number)em.get("u")).intValue();
            int v = ((Number)em.get("v")).intValue();
            int w = ((Number)em.getOrDefault("w", 1)).intValue();
            ds.edges.add(new Edge(u,v,w));
        }
        return ds;
    }

    private static Result processDataset(Dataset ds, String filename){
        Graph g = new Graph(ds.n, ds.directed);
        for(Edge e: ds.edges) g.addEdge(e.u,e.v,e.w);

        TarjanSCC tarjan = new TarjanSCC(g);
        tarjan.run();
        List<List<Integer>> comps = tarjan.getComponents();
        int[] compId = tarjan.getCompId();

        Condensation cond = new Condensation(g, compId);

        List<int[]> condEdges = cond.edgesList;
        List<Integer> compTopo = TopoSort.kahnOrder(cond.compN, condEdges);

        List<Integer> tasksOrder = new ArrayList<>();
        if(compTopo!=null){
            Map<Integer,List<Integer>> compMembers = new HashMap<>();
            for(int i=0;i<comps.size();i++) compMembers.put(i, comps.get(i));
            for(int c: compTopo){
                List<Integer> memb = compMembers.get(c);
                if(memb!=null) tasksOrder.addAll(memb);
            }
        }

        int sourceComp = compId[ds.source];
        Map<Integer,Long> dist = DAGSP.shortestFrom(cond.compN, cond.adj, sourceComp);
        Map<Integer,List<Integer>> paths = reconstructPaths(cond.compN, cond.adj, sourceComp);

        DAGSP.Pair<Long,int[]> lp = DAGSP.longestPath(cond.compN, cond.adj);
        long lpLen = lp.a;
        int[] prev = lp.b;

        List<Integer> lpPath = new ArrayList<>();
        if(lpLen>0){
            long[] vals = new long[cond.compN];
            Arrays.fill(vals, Long.MIN_VALUE/4);

            List<int[]> edgesFlat = new ArrayList<>();
            for(int u=0;u<cond.compN;u++)
                for(int[] e: cond.adj.get(u))
                    edgesFlat.add(new int[]{u,e[0]});

            List<Integer> topo = TopoSort.kahnOrder(cond.compN, edgesFlat);
            int[] indeg = new int[cond.compN];
            for(int[] e: edgesFlat) indeg[e[1]]++;

            for(int i=0;i<cond.compN;i++)
                if(indeg[i]==0) vals[i]=0;

            if(topo!=null){
                for(int u: topo){
                    if(vals[u]==Long.MIN_VALUE/4) continue;
                    for(int[] e: cond.adj.get(u)){
                        int v=e[0], w=e[1];
                        long nd = vals[u]+w;
                        if(nd > vals[v]) vals[v]=nd;
                    }
                }
            }

            int best=-1; long bestv=Long.MIN_VALUE;
            for(int i=0;i<cond.compN;i++)
                if(vals[i]>bestv){ bestv=vals[i]; best=i; }

            int cur = best;
            while(cur!=-1){
                lpPath.add(0, cur);
                cur = prev[cur];
            }
        }

        Result r = new Result();
        r.file = filename;

        SCCResult sr = new SCCResult();
        sr.count = comps.size();
        sr.components = comps;
        r.scc = sr;

        CondResult cr = new CondResult();
        cr.n = cond.compN;
        cr.edges = new ArrayList<>();
        for(int[] e: cond.edgesList) cr.edges.add(new int[]{e[0], e[1]});
        r.condensation = cr;

        TopoResult tr = new TopoResult();
        tr.comp_topo_order = compTopo==null? new ArrayList<>() : compTopo;
        tr.tasks_order = tasksOrder;
        r.topo = tr;

        SPResult spr = new SPResult();
        spr.source_component = sourceComp;
        spr.dist = dist;
        spr.paths = paths;
        r.shortest_paths = spr;

        LPResult lpr = new LPResult();
        lpr.length = lpLen;
        lpr.comp_path = lpPath;
        r.longest_path = lpr;

        return r;
    }

    private static Map<String,Object> toMap(Result r){
        Map<String,Object> m = new LinkedHashMap<>();

        m.put("file", r.file);

        Map<String,Object> scc = new LinkedHashMap<>();
        scc.put("count", r.scc.count);
        List<Object> comps = new ArrayList<>();
        for(List<Integer> comp: r.scc.components){
            List<Object> c = new ArrayList<>();
            for(int v: comp) c.add(v);
            comps.add(c);
        }
        scc.put("components", comps);
        m.put("scc", scc);

        Map<String,Object> cond = new LinkedHashMap<>();
        cond.put("n", r.condensation.n);
        List<Object> ce = new ArrayList<>();
        for(int[] e: r.condensation.edges){
            Map<String,Object> eobj = new LinkedHashMap<>();
            eobj.put("u", e[0]);
            eobj.put("v", e[1]);
            ce.add(eobj);
        }
        cond.put("edges", ce);
        m.put("condensation", cond);

        Map<String,Object> topo = new LinkedHashMap<>();
        topo.put("comp_topo_order", r.topo.comp_topo_order);
        topo.put("tasks_order", r.topo.tasks_order);
        m.put("topo", topo);

        Map<String,Object> sp = new LinkedHashMap<>();
        sp.put("source_component", r.shortest_paths.source_component);
        Map<String,Object> distm = new LinkedHashMap<>();
        for(Map.Entry<Integer,Long> en: r.shortest_paths.dist.entrySet()){
            long val = en.getValue();
            if(val>=Long.MAX_VALUE/4) distm.put(String.valueOf(en.getKey()), null);
            else distm.put(String.valueOf(en.getKey()), val);
        }
        sp.put("distances", distm);

        Map<String,Object> paths = new LinkedHashMap<>();
        for(Map.Entry<Integer,List<Integer>> en: r.shortest_paths.paths.entrySet()){
            List<Object> list = new ArrayList<>();
            for(int v: en.getValue()) list.add(v);
            paths.put(String.valueOf(en.getKey()), list);
        }
        sp.put("paths", paths);
        m.put("shortest_paths", sp);

        Map<String,Object> lp = new LinkedHashMap<>();
        lp.put("length", r.longest_path.length);
        List<Object> p = new ArrayList<>();
        for(int c: r.longest_path.comp_path) p.add(c);
        lp.put("comp_path", p);
        m.put("longest_path", lp);

        return m;
    }

    private static Map<Integer,List<Integer>> reconstructPaths(int n, List<List<int[]>> adj, int source){
        List<int[]> edgesFlat = new ArrayList<>();
        for(int u=0;u<n;u++) for(int[] e: adj.get(u)) edgesFlat.add(new int[]{u,e[0]});

        List<Integer> topo = TopoSort.kahnOrder(n, edgesFlat);

        final long INF = Long.MAX_VALUE/4;
        long[] dist = new long[n]; Arrays.fill(dist, INF);
        int[] prev = new int[n]; Arrays.fill(prev, -1);

        dist[source]=0;

        if(topo!=null){
            for(int u: topo){
                if(dist[u]==INF) continue;
                for(int[] e: adj.get(u)){
                    int v=e[0], w=e[1];
                    long nd = dist[u]+w;
                    if(nd < dist[v]){
                        dist[v]=nd;
                        prev[v]=u;
                    }
                }
            }
        }

        Map<Integer,List<Integer>> map = new HashMap<>();
        for(int i=0;i<n;i++){
            List<Integer> path = new ArrayList<>();
            if(dist[i]>=INF){
                map.put(i, path);
                continue;
            }
            int cur = i;
            while(cur!=-1){
                path.add(0, cur);
                cur = prev[cur];
            }
            map.put(i, path);
        }

        return map;
    }
}
