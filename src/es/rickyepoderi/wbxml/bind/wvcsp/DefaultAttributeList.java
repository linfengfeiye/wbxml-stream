//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.08.25 at 05:48:09 PM CEST 
//


package es.rickyepoderi.wbxml.bind.wvcsp;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "presenceSubList"
})
@XmlRootElement(name = "DefaultAttributeList")
public class DefaultAttributeList {

    @XmlElement(name = "PresenceSubList")
    protected List<PresenceSubList> presenceSubList;

    /**
     * Gets the value of the presenceSubList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the presenceSubList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPresenceSubList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PresenceSubList }
     * 
     * 
     */
    public List<PresenceSubList> getPresenceSubList() {
        if (presenceSubList == null) {
            presenceSubList = new ArrayList<PresenceSubList>();
        }
        return this.presenceSubList;
    }

}
