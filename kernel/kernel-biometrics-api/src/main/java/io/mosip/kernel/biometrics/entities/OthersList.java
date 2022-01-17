package io.mosip.kernel.biometrics.entities;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.Vector;

@XmlRootElement
@XmlType(name="OthersListType")
public class OthersList {

    public List<Entry> entry;

    public OthersList(){
        entry = new Vector<Entry>();
    }

}
