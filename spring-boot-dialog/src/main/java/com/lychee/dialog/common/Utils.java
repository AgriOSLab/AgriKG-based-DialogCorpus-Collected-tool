package com.lychee.dialog.common;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

@Component
public class Utils {

    public ArrayList<ArrayList<String>> formatTriples(ArrayList<HashMap<String, String>> triples){
        ArrayList<ArrayList<String>> results = new ArrayList<>();

        for (HashMap<String, String> triple : triples) {
            ArrayList<String> temp = new ArrayList<>();
            temp.add(triple.get("from"));
            temp.add(triple.get("label"));
            temp.add(triple.get("to"));
            results.add(temp);
        }
        return results;
    }

    public ArrayList<ArrayList<String>> formatProperties(HashMap<String, Object> properties, String subject){
        ArrayList<ArrayList<String>> results = new ArrayList<>();
        for (String s : properties.keySet()) {

            ArrayList<String> temp = new ArrayList<>();
            temp.add(subject);
            temp.add(s);
            if (s.equals("施用作物及剂量")||s.equals("组成成分")){
                StringBuilder builder = new StringBuilder();
                ArrayList<String> prop = (ArrayList<String>) properties.get(s);
                for (String s1 : prop) {
                    builder.append(s1);
                }
                temp.add(builder.toString());
            }
            else
                temp.add(properties.get(s).toString());
            results.add(temp);
        }

        return results;
    }
}
