package assignment4;

import java.io.*;
import java.util.*;

public class SimpleJson {

    private final String s;
    private int i = 0;

    private SimpleJson(String s) { this.s = s; }

    public static Object parse(File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new FileReader(f))){
            String line;
            while((line = br.readLine()) != null){ sb.append(line).append('\n'); }
        }
        SimpleJson p = new SimpleJson(sb.toString());
        p.skipWS();
        return p.parseValue();
    }

    private void skipWS(){ while(i<s.length() && Character.isWhitespace(s.charAt(i))) i++; }

    private char peek(){ return i < s.length() ? s.charAt(i) : '\0'; }
    private char next(){ return i < s.length() ? s.charAt(i++) : '\0'; }

    private Object parseValue(){
        skipWS();
        char c = peek();
        if(c=='{') return parseObject();
        if(c=='[') return parseArray();
        if(c=='\"') return parseString();
        if(c=='t' || c=='f') return parseBoolean();
        if(c=='n') { parseNull(); return null; }
        return parseNumber();
    }

    private void parseNull(){ expect("null"); }
    private Boolean parseBoolean(){ if(peek()=='t'){ expect("true"); return Boolean.TRUE; } else { expect("false"); return Boolean.FALSE; } }

    private void expect(String tok){ for(char ch: tok.toCharArray()){ if(next()!=ch) throw new RuntimeException("Invalid token expected " + tok); } }

    private Number parseNumber(){
        skipWS();
        int start = i;
        if(peek()=='-') next();
        while(Character.isDigit(peek())) next();
        String num = s.substring(start,i);
        return Integer.parseInt(num);
    }

    private String parseString(){
        expect("\"");
        StringBuilder sb = new StringBuilder();
        while(true){
            char c = next();
            if(c=='\0') throw new RuntimeException("Unterminated string");
            if(c=='\\'){
                char e = next();
                if(e=='\"') sb.append('\"');
                else if(e=='n') sb.append('\n');
                else if(e=='t') sb.append('\t');
                else sb.append(e);
            } else if(c=='\"') break;
            else sb.append(c);
        }
        return sb.toString();
    }

    private List<Object> parseArray(){
        expect("[");
        List<Object> list = new ArrayList<>();
        skipWS();
        if(peek()==']'){ next(); return list; }
        while(true){
            Object v = parseValue(); list.add(v);
            skipWS();
            if(peek()==','){ next(); continue; }
            else if(peek()==']'){ next(); break; }
            else throw new RuntimeException("Invalid array");
        }
        return list;
    }

    private Map<String,Object> parseObject(){
        expect("{");
        Map<String,Object> map = new LinkedHashMap<>();
        skipWS();
        if(peek()=='}'){ next(); return map; }
        while(true){
            skipWS();
            String key = parseString();
            skipWS();
            if(next()!=':') throw new RuntimeException("Expected :");
            skipWS();
            Object val = parseValue();
            map.put(key,val);
            skipWS();
            if(peek()==','){ next(); continue; }
            else if(peek()=='}'){ next(); break; }
            else throw new RuntimeException("Invalid object");
        }
        return map;
    }

    public static String escape(String s){
        return s.replace("\\","\\\\").replace("\"","\\\"");
    }

    public static String toJson(Map<String,Object> obj){
        StringBuilder sb = new StringBuilder();
        appendValue(sb, obj);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void appendValue(StringBuilder sb, Object v){
        if(v==null) sb.append("null");
        else if(v instanceof String) { sb.append('"').append(escape((String)v)).append('"'); }
        else if(v instanceof Number) sb.append(v.toString());
        else if(v instanceof Boolean) sb.append(v.toString());
        else if(v instanceof Map){
            sb.append("{");
            boolean first=true;
            for(Map.Entry<String,Object> e: ((Map<String,Object>)v).entrySet()){
                if(!first) sb.append(",");
                first=false;
                sb.append('"').append(escape(e.getKey())).append('"').append(":");
                appendValue(sb, e.getValue());
            }
            sb.append("}");
        }
        else if(v instanceof List){
            sb.append("[");
            boolean first=true;
            for(Object o: (List<Object>)v){
                if(!first) sb.append(",");
                first=false;
                appendValue(sb,o);
            }
            sb.append("]");
        }
        else sb.append('"').append(escape(v.toString())).append('"');
    }

    // Новый метод: pretty-print с отступами
    public static String toJsonPretty(Map<String,Object> obj){
        StringBuilder sb = new StringBuilder();
        appendValuePretty(sb, obj, 0);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void appendValuePretty(StringBuilder sb, Object v, int indent){
        String ind = "  ".repeat(indent); // 2 пробела на уровень
        if(v==null) sb.append("null");
        else if(v instanceof String) sb.append('"').append(escape((String)v)).append('"');
        else if(v instanceof Number) sb.append(v.toString());
        else if(v instanceof Boolean) sb.append(v.toString());
        else if(v instanceof Map){
            sb.append("{\n");
            boolean first=true;
            for(Map.Entry<String,Object> e: ((Map<String,Object>)v).entrySet()){
                if(!first) sb.append(",\n");
                first=false;
                sb.append(ind).append("  ").append('"').append(escape(e.getKey())).append('"').append(": ");
                appendValuePretty(sb, e.getValue(), indent+1);
            }
            sb.append("\n").append(ind).append("}");
        }
        else if(v instanceof List){
            sb.append("[\n");
            boolean first=true;
            for(Object o: (List<Object>)v){
                if(!first) sb.append(",\n");
                first=false;
                sb.append(ind).append("  ");
                appendValuePretty(sb,o, indent+1);
            }
            sb.append("\n").append(ind).append("]");
        }
        else sb.append('"').append(escape(v.toString())).append('"');
    }
}
