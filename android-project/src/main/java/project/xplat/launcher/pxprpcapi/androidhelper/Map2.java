package project.xplat.launcher.pxprpcapi.androidhelper;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

public class Map2 extends HashMap<String,Object> {
    StringBuilder sb=new StringBuilder();
    public String describe(){
        for(Map.Entry<String,Object> kv:this.entrySet()){
            if(kv.getValue()!=null){
                sb.append(kv.getKey()).append(":").append(kv.getValue().toString()).append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return describe();
    }
}
