package io.mosip.kernel.biometrics.entities;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AdapterOthersListToHashMap extends XmlAdapter<OthersList, HashMap> {
    public AdapterOthersListToHashMap(){}

    // Convert a value type to a bound type.
    // read xml content and put into Java class.
    public HashMap unmarshal(OthersList v){
        HashMap<String, String> aHashMap = new HashMap();
        int cnt = v.entry.size();
        for(int i=0; i < cnt; i++){
            Entry tmpE = (Entry)v.entry.get(i);
            aHashMap.put(tmpE.key, tmpE.value);
        }
        return aHashMap;
    }

    // Convert a bound type to a value type.
    // write Java content into class that generates desired XML
    public OthersList marshal(HashMap v){
        if(v == null)
            return null;

        OthersList pList = new OthersList();
        Map<String, String> tMap = new HashMap(v);
        for(Iterator i = tMap.keySet().iterator(); i.hasNext();){
            String key = (String) i.next();
            pList.entry.add(new Entry(key, tMap.get(key)));
        }
        return pList;
    }
}
