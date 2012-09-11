
package org.fcrepo.apia;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for Validation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Validation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="asOfDateTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="objModels">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="model" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="objProblems">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="problem" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="datastreamProblems">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="datastream" type="{http://www.fedora.info/definitions/1/0/types/}datastreamProblem" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="pid" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="valid" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Validation", propOrder = {
    "asOfDateTime",
    "objModels",
    "objProblems",
    "datastreamProblems"
})
public class Validation {

    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar asOfDateTime;
    @XmlElement(required = true, nillable = true)
    protected Validation.ObjModels objModels;
    @XmlElement(required = true, nillable = true)
    protected Validation.ObjProblems objProblems;
    @XmlElement(required = true, nillable = true)
    protected Validation.DatastreamProblems datastreamProblems;
    @XmlAttribute(required = true)
    protected String pid;
    @XmlAttribute(required = true)
    protected boolean valid;

    /**
     * Gets the value of the asOfDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAsOfDateTime() {
        return asOfDateTime;
    }

    /**
     * Sets the value of the asOfDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAsOfDateTime(XMLGregorianCalendar value) {
        this.asOfDateTime = value;
    }

    /**
     * Gets the value of the objModels property.
     * 
     * @return
     *     possible object is
     *     {@link Validation.ObjModels }
     *     
     */
    public Validation.ObjModels getObjModels() {
        return objModels;
    }

    /**
     * Sets the value of the objModels property.
     * 
     * @param value
     *     allowed object is
     *     {@link Validation.ObjModels }
     *     
     */
    public void setObjModels(Validation.ObjModels value) {
        this.objModels = value;
    }

    /**
     * Gets the value of the objProblems property.
     * 
     * @return
     *     possible object is
     *     {@link Validation.ObjProblems }
     *     
     */
    public Validation.ObjProblems getObjProblems() {
        return objProblems;
    }

    /**
     * Sets the value of the objProblems property.
     * 
     * @param value
     *     allowed object is
     *     {@link Validation.ObjProblems }
     *     
     */
    public void setObjProblems(Validation.ObjProblems value) {
        this.objProblems = value;
    }

    /**
     * Gets the value of the datastreamProblems property.
     * 
     * @return
     *     possible object is
     *     {@link Validation.DatastreamProblems }
     *     
     */
    public Validation.DatastreamProblems getDatastreamProblems() {
        return datastreamProblems;
    }

    /**
     * Sets the value of the datastreamProblems property.
     * 
     * @param value
     *     allowed object is
     *     {@link Validation.DatastreamProblems }
     *     
     */
    public void setDatastreamProblems(Validation.DatastreamProblems value) {
        this.datastreamProblems = value;
    }

    /**
     * Gets the value of the pid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPid() {
        return pid;
    }

    /**
     * Sets the value of the pid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPid(String value) {
        this.pid = value;
    }

    /**
     * Gets the value of the valid property.
     * 
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets the value of the valid property.
     * 
     */
    public void setValid(boolean value) {
        this.valid = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="datastream" type="{http://www.fedora.info/definitions/1/0/types/}datastreamProblem" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "datastream"
    })
    public static class DatastreamProblems {

        protected List<DatastreamProblem> datastream;

        /**
         * Gets the value of the datastream property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the datastream property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getDatastream().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link DatastreamProblem }
         * 
         * 
         */
        public List<DatastreamProblem> getDatastream() {
            if (datastream == null) {
                datastream = new ArrayList<DatastreamProblem>();
            }
            return this.datastream;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="model" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "model"
    })
    public static class ObjModels {

        protected List<String> model;

        /**
         * Gets the value of the model property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the model property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getModel().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getModel() {
            if (model == null) {
                model = new ArrayList<String>();
            }
            return this.model;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="problem" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "problem"
    })
    public static class ObjProblems {

        protected List<String> problem;

        /**
         * Gets the value of the problem property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the problem property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getProblem().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getProblem() {
            if (problem == null) {
                problem = new ArrayList<String>();
            }
            return this.problem;
        }

    }

}
