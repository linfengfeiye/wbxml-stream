//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.08.25 at 05:48:09 PM CEST 
//


package es.rickyepoderi.wbxml.bind.wvcsp;

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
    "contactList",
    "nickList",
    "contactListProperties"
})
@XmlRootElement(name = "CreateList-Request")
public class CreateListRequest {

    @XmlElement(name = "ContactList", required = true)
    protected ContactList contactList;
    @XmlElement(name = "NickList")
    protected NickList nickList;
    @XmlElement(name = "ContactListProperties")
    protected ContactListProperties contactListProperties;

    /**
     * Gets the value of the contactList property.
     * 
     * @return
     *     possible object is
     *     {@link ContactList }
     *     
     */
    public ContactList getContactList() {
        return contactList;
    }

    /**
     * Sets the value of the contactList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactList }
     *     
     */
    public void setContactList(ContactList value) {
        this.contactList = value;
    }

    /**
     * Gets the value of the nickList property.
     * 
     * @return
     *     possible object is
     *     {@link NickList }
     *     
     */
    public NickList getNickList() {
        return nickList;
    }

    /**
     * Sets the value of the nickList property.
     * 
     * @param value
     *     allowed object is
     *     {@link NickList }
     *     
     */
    public void setNickList(NickList value) {
        this.nickList = value;
    }

    /**
     * Gets the value of the contactListProperties property.
     * 
     * @return
     *     possible object is
     *     {@link ContactListProperties }
     *     
     */
    public ContactListProperties getContactListProperties() {
        return contactListProperties;
    }

    /**
     * Sets the value of the contactListProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactListProperties }
     *     
     */
    public void setContactListProperties(ContactListProperties value) {
        this.contactListProperties = value;
    }

}
