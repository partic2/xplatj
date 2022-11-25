package project.xplat.launcher.pxprpcapi;

import project.xplat.launcher.pxprpcapi.androidhelper.Map2;

import java.lang.reflect.Field;
import java.util.List;

public class Utils {
    public static String joinStringList(Iterable<String> s,String delim){
        StringBuilder sb=new StringBuilder();
        for(String e : s){
            sb.append(e);
            sb.append(delim);
        }
        return sb.toString();
    }
    public String repr(Object o){
        return o.toString();
    }

}
