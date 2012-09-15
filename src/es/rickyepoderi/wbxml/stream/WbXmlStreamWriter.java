/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.rickyepoderi.wbxml.stream;

import com.sun.org.apache.xerces.internal.util.XMLChar;
import es.rickyepoderi.wbxml.definition.IanaCharset;
import es.rickyepoderi.wbxml.definition.WbXmlDefinition;
import es.rickyepoderi.wbxml.definition.WbXmlInitialization;
import es.rickyepoderi.wbxml.definition.WbXmlNamespaceDef;
import es.rickyepoderi.wbxml.document.WbXmlAttribute;
import es.rickyepoderi.wbxml.document.WbXmlBody;
import es.rickyepoderi.wbxml.document.WbXmlContent;
import es.rickyepoderi.wbxml.document.WbXmlDocument;
import es.rickyepoderi.wbxml.document.WbXmlElement;
import es.rickyepoderi.wbxml.document.WbXmlEncoder;
import es.rickyepoderi.wbxml.document.WbXmlVersion;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * <p>The XMLStreamWriter interface specifies how to write XML. The XMLStreamWriter
 * does not perform well formedness checking on its input. However the 
 * writeCharacters method is required to escape & , &lt; and &gt;. For attribute 
 * values the writeAttribute method will escape the above characters plus " 
 * to ensure that all character content and attribute values are well formed. 
 * Each NAMESPACE and ATTRIBUTE must be individually written. The interface is explained in 
 * <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/stream/XMLStreamWriter.html">XMLStreamWriter</a>.</p>
 * 
 * <p>In the WbXml library this stream writer constructs while writing the
 * memory representation of the WbXmlDocument object (all the elements, 
 * attributes, contents and so on) and in the final writeEndDocument() the
 * document is encoded in the real output stream. So only in-memory processing
 * is done until the final method.</p>
 * 
 * <p>Currently the output stream has two possibilities:</p>
 * 
 * <ul>
 * <li>encoderType: This encoder is the same that the WbXmlEncoder uses. This
 * means the encoding should be configured to not use the strtbl (NO), 
 * only use it if needed (ID_NEEDED) or always (ALWAYS). By default it is
 * configured IF_NEEDED.</li>
 * <li>skipSpaces: if true values which are completely blank spaces are not
 * considered, and real values are trimmed (start and end). if false all
 * contents are written to the WBXML. "true" by default.</li>
 * </ul>
 * 
 * TODO: Why about create properties to the parser and reader!!!
 * 
 * @author ricky
 */
public class WbXmlStreamWriter implements XMLStreamWriter {
    
    /**
     * logger for the class.
     */
    private static final Logger log = Logger.getLogger(WbXmlStreamWriter.class.getName());
    
    /**
     * Stream to write the wbxml
     */
    private OutputStream stream;
    
    /**
     * The namespace context, initialized with definition contexts
     */
    private WbXmlNamespaceContext nsctx;
    
    /**
     * User namespace context
     */
    private NamespaceContext userctx;
    
    /**
     * The document which is being constructed on the fly
     */
    private WbXmlDocument doc;
    
    /**
     * The parents queued to continue the in-memory encoding
     */
    private Deque<WbXmlElement> parents;
    
    /**
     * Current element being encoded
     */
    private WbXmlElement currentElement;
    
    /**
     * Boolean value that marks if the document is already encoded
     */
    private boolean encoded;
    
    /**
     * The definition used in the document
     */
    private WbXmlDefinition def;
    
    /**
     * The type of encoding it is being used by the writer
     */
    private WbXmlEncoder.StrtblType encoderType;
    
    /**
     * Boolean that marks if the writer should considers whitespaces or not
     */
    private boolean skipSpaces = true;
    
    /**
     * Construtor using all the input values: output stream, language definition,
     * type of encoding and boolean to set skip spaces or not.
     * @param os The Ouput Stream to write the WBXML to
     * @param def The language definition to use
     * @param encoderType The type of encoding to perform (strtbl use)
     * @param skipSpaces The parser skip spaces or consider them
     */
    public WbXmlStreamWriter(OutputStream os, WbXmlDefinition def, 
            WbXmlEncoder.StrtblType encoderType, boolean skipSpaces) {
        this.stream = os;
        this.nsctx = new WbXmlNamespaceContext();
        if (def != null) {
            // add posibly namespaces
            for (WbXmlNamespaceDef nsDef: def.getNamespaces()) {
                nsctx.addPrefix(nsDef.getPrefix(), nsDef.getNamespace());
            }
        }
        this.userctx = null;
        this.doc = null;
        this.parents = new ArrayDeque<WbXmlElement>();
        this.currentElement = null;
        this.encoded = false;
        this.def = def;
        this.encoderType = encoderType;
        this.skipSpaces = skipSpaces;
    }
    
    /**
     * Constructor with only stream and language definition. Encoder type
     * is IF_NEEDED and spaces are skipped.
     * @param os The ouput stream  to write the WBXML to
     * @param def The language definition to use
     */
    public WbXmlStreamWriter(OutputStream os, WbXmlDefinition def) {
        this(os, def, WbXmlEncoder.StrtblType.IF_NEEDED, true);
    }
    
    /**
     * Constructor only using the ouput stream. The language is guessed 
     * using the root element or DOCTYPE definition.
     * @param os The language definition to use
     */
    public WbXmlStreamWriter(OutputStream os) {
        this(os, null, WbXmlEncoder.StrtblType.IF_NEEDED, true);
    }

    // START DOCUMENT
    
    /**
     * Write the XML Declaration. Defaults the WBXML version to 1.3, and the encoding to utf-8
     * @throws XMLStreamException 
     */
    @Override
    public void writeStartDocument() throws XMLStreamException {
        log.fine("writeStartDocument()");
        writeStartDocument("UTF-8", null);
    }

    /**
     * Write the XML Declaration. Defaults the WBXML to 1.3 (other versions
     * not supported)
     * @param version version of the xml document
     * @throws XMLStreamException 
     */
    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        log.log(Level.FINE, "writeStartDocument({0})", version);
        writeStartDocument("UTF-8", null);
    }

    /**
     * Write the XML Declaration. Defaults the XML version to 1.0, and the 
     * encoding to utf-8. In WBXML stream the version is always set to 1.3
     * (other versions are not supported).
     * @param encoding encoding of the xml declaration
     * @param version version of the xml document
     * @throws XMLStreamException If given encoding does not match encoding of the underlying stream
     */
    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        log.log(Level.FINE, "writeStartDocument({0}, {1})", new Object[] {encoding, version});
        doc = new WbXmlDocument(WbXmlVersion.VERSION_1_3, IanaCharset.getIanaCharset(encoding));
        if (def != null) {
            log.log(Level.FINE, "Setting definition {0}", def.getName());
            doc.setDefinition(def);
        }
    }
    
    /**
     * Writes a start tag to the output. All writeStartElement methods open a 
     * new scope in the internal namespace context. Writing the corresponding 
     * EndElement causes the scope to be closed
     * @param localName local name of the tag, may not be null
     * @throws XMLStreamException 
     */
    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        writeStartElement(null, localName, null);
    }

    /**
     * Writes a start tag to the output
     * @param namespaceURI the namespaceURI of the prefix to use, may not be null
     * @param localName local name of the tag, may not be null
     * @throws XMLStreamException 
     */
    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        writeStartElement(null, localName, namespaceURI);
    }

    /**
     * Writes a start tag to the output. This is the only method implemented of
     * this king (all other versions finally call this one). Different implementations
     * uses this metho differently, so it is thought very versatile. The prefix 
     * or namespace of the tag is obtained from the arguments (prefix or namespace)
     * and if they are not passed is inherited from the parent element.If the
     * caller later call writeNamespace then the namespace is reassigned.
     * @param prefix local name of the tag, may not be null
     * @param localName the prefix of the tag, may not be null
     * @param namespaceURI the uri to bind the prefix to, may not be null
     * @throws XMLStreamException 
     */
    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        log.log(Level.FINE, "writeStartElement({0}, {1}, {2})", 
                new Object[] {prefix, localName, namespaceURI});
        // if definition is not already guessed get it using the root element
        if (def == null) {
            def = WbXmlInitialization.getDefinitionByRoot(localName, namespaceURI);
            if (def == null) {
                throw new XMLStreamException(String.format("Definition not found for root element '%s'", localName));
            }
            log.log(Level.FINE, "Setting definition {0}", def.getName());
            doc.setDefinition(def);
        }
        // if there is a current element set to parent for later adding
        WbXmlElement parent = currentElement;
        // parent can be the current element or first parent in queue
        if (parent == null) {
            // peek the parent
            parent = this.parents.peek();
        } else {
            // put the element as in the parent queue
            parents.push(parent);
        }
        // sometimes localName is already prefixed: clean
        int idx = localName.indexOf(':');
        if (idx >= 0) {
            prefix = localName.substring(0, idx);
            localName = localName.substring(idx + 1);
        }
        // locate the prefix if used
        if (namespaceURI != null && !namespaceURI.isEmpty()) {
            // using the namespace directly
            prefix = def.getPrefix(namespaceURI);
        } else if (prefix != null && !prefix.isEmpty()) {
            // using the prefix
            namespaceURI = nsctx.getNamespaceURI(prefix);
            if (namespaceURI != null) {
                prefix = def.getPrefix(namespaceURI);
            }
        } else if (parent != null) {
            // using previous namespace if inherited
            prefix = parent.getTagPrefix();
        }
        if (prefix != null && !prefix.isEmpty()) {
            localName = new StringBuilder(prefix).append(":").append(localName).toString();
        }
        // create a new current element with this tag
        currentElement = new WbXmlElement(localName);
        // add the current element to the parent or to the doc
        if (parent != null) {
            parent.addContent(new WbXmlContent(currentElement));
        } else {
            // first element => add to doc
            if (doc.getBody() != null) {
                throw new XMLStreamException(String.format("Trying to add a second root element '%s'", localName));
            }
            doc.setBody(new WbXmlBody(currentElement));
        }
    }

    /**
     * Writes an empty element tag to the output
     * @param localName local name of the tag, may not be null
     * @throws XMLStreamException 
     */
    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        writeEmptyElement(null, localName, null);
        
    }

    /**
     * Writes an empty element tag to the output
     * @param namespaceURI the uri to bind the tag to, may not be null
     * @param localName local name of the tag, may not be null
     * @throws XMLStreamException 
     */
    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        writeEmptyElement(null, localName, null);
    }

    /**
     * Writes an empty element tag to the output. Real method all the other
     * variants call this one. And it really calls to writeStartelement
     * @param prefix the prefix of the tag, may not be null
     * @param localName local name of the tag, may not be null
     * @param namespaceURI the uri to bind the tag to, may not be null
     * @throws XMLStreamException 
     */
    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        log.log(Level.FINE, "writeEmptyElement({0}, {1}, {2})", 
                new Object[]{prefix, localName, namespaceURI});
        // just like start element but then set to null
        writeStartElement(prefix, localName, namespaceURI);
        // empty element => reset current to null
        currentElement = null;
        // check is the last element, a only one empty element document
    }

    /**
     * Writes an end tag to the output relying on the internal state of the 
     * writer to determine the prefix and local name of the event
     * @throws XMLStreamException 
     */
    @Override
    public void writeEndElement() throws XMLStreamException {
        log.log(Level.FINE, "writeEndElement()");
        if (this.parents.isEmpty()) {
            currentElement = null;
        } else {
            currentElement = this.parents.pop();
        }
    }

    /**
     * Closes any start tags and writes corresponding end tags. In the WBXML 
     * implementation the method also performs the real encoding of the WbXmlDocument
     * object (all the previous staff just construct in memory/Java
     * representation object).
     * @throws XMLStreamException  Some error performing the real encode
     */
    @Override
    public void writeEndDocument() throws XMLStreamException {
        log.log(Level.FINE, "writeEndDocument()");
        // close any pendind tags
        while (currentElement != null) {
            writeEndElement();
        }
        // encode the resulting document
        try {
            WbXmlEncoder encoder = new WbXmlEncoder(stream, doc, encoderType);
            encoder.encode();
            encoded = true;
        } catch (IOException e) {
            throw new XMLStreamException("Error encoding the WbXML document", e);
        }
    }

    /**
     * Close this writer and free any resources associated with the writer. 
     * This must not close the underlying output stream.
     * @throws XMLStreamException 
     */
    @Override
    public void close() throws XMLStreamException {
        //try {
        //    log.log(Level.FINE, "flush()");
        //    this.stream.close();
        //} catch (IOException e) {
        //    throw new XMLStreamException("Error closing the stream", e);
        //}
    }

    /**
     * Write any cached data to the underlying output mechanism. The method
     * only flush the real stream if encoded.
     * @throws XMLStreamException 
     */
    @Override
    public void flush() throws XMLStreamException {
        try {
            log.log(Level.FINE, "flush()");
            if (encoded) {
                this.stream.flush();
            }
        } catch (IOException e) {
            throw new XMLStreamException("Error flushing the stream", e);
        }
    }

    /**
     * Writes an attribute to the output stream without a prefix
     * @param localName the local name of the attribute
     * @param value the value of the attribute
     * @throws XMLStreamException 
     */
    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        writeAttribute(null, null, localName, value);
    }

    /**
     * Writes an attribute to the output stream
     * @param namespaceURI the uri of the prefix for this attribute
     * @param localName the local name of the attribute
     * @param value the value of the attribute
     * @throws XMLStreamException 
     */
    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        writeAttribute(null, namespaceURI, localName, value);
    }

    /**
     * Writes an attribute to the output stream. The real method that all the
     * other variants call. As in writeStartElement it tries to be very
     * versatile cos different implementations (jaxb, transformer,...) uses
     * it quite differently.
     * @param prefix the prefix for this attribute
     * @param namespaceURI the uri of the prefix for this attribute
     * @param localName the local name of the attribute
     * @param value the value of the attribute
     * @throws XMLStreamException 
     */
    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
        log.log(Level.FINE, "writeAttribute({0}, {1}, {2}, {3})", 
                new Object[]{prefix, namespaceURI, localName, value});
        // sometimes localName is already prefixed: clean
        int idx = localName.indexOf(':');
        if (idx >= 0) {
            prefix = localName.substring(0, idx);
            localName = localName.substring(idx + 1);
        }
        // locate the prefix if namespace defined
        if (namespaceURI != null && !namespaceURI.isEmpty()) {
            // namespace used directly
            prefix = def.getPrefix(namespaceURI);
        } else if (prefix != null && !prefix.isEmpty()) {
            // using the prefix
            namespaceURI = nsctx.getNamespaceURI(prefix);
            if (namespaceURI != null) {
                prefix = def.getPrefix(namespaceURI);
            }
        } else {
            // using previous namespace if inherited
            prefix = currentElement.getTagPrefix();
        }
        if (prefix != null && !prefix.isEmpty()) {
            localName = new StringBuilder(prefix).append(":").append(localName).toString();
        }
        // add an atribute to the current element
        if (currentElement == null) {
            throw new XMLStreamException(String.format("No elemento to add the attribute '%s'='%s' to", localName, value));
        }
        currentElement.addAttribute(new WbXmlAttribute(localName, value));
    }

    /**
     * Writes a namespace to the output stream If the prefix argument to this 
     * method is the empty string, "xmlns", or null this method will delegate 
     * to writeDefaultNamespace. In the WBXML implementation the prefix
     * of the current element is updated to the one passed.
     * @param prefix the prefix to bind this namespace to
     * @param namespaceURI the uri to bind the prefix to
     * @throws XMLStreamException 
     */
    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        log.log(Level.FINE, "writeNamespace({0}, {1})", new Object[]{prefix, namespaceURI});
        if (namespaceURI == null || namespaceURI.isEmpty()) {
            //throw new XMLStreamException("namespaceURI cannot be null!");
            return;
        }
        if (prefix == null || prefix.isEmpty() || prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            // default namespace for the document
            if (currentElement != null) {
                prefix = def.getPrefix(namespaceURI);
                if (prefix == null) {
                    currentElement.setTag(currentElement.getTagWithoutPrefix());
                } else {
                    currentElement.setTag(prefix + ":" + currentElement.getTagWithoutPrefix());
                }
            }
        } else {
            // when more that one definition is used this threw an exception
            // deleted!!!!
            // not default => add to the prefixes
            //if (def.getPrefix(namespaceURI) == null) {
            //    throw new XMLStreamException(
            //            String.format("namespaceURI '%s' is not defined in the WbXML definition", namespaceURI));
            //}
            // add it
            this.nsctx.addPrefix(prefix, namespaceURI);
        }
    }

    /**
     * Writes the default namespace to the stream. 
     * NOTE: Not implemented, throws an exception
     * @param namespaceURI the uri to bind the default namespace to
     * @throws XMLStreamException 
     */
    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Writes an xml comment with the data enclosed. Nothing is done cos WBXML 
     * does not use comments.
     * @param data the data contained in the comment, may be null
     * @throws XMLStreamException 
     */
    @Override
    public void writeComment(String data) throws XMLStreamException {
        log.log(Level.FINE, "writeComment({0})", data);
        // no comment to write in wbxml
    }

    /**
     * Writes a processing instruction.
     * NOTE: Not implemented, throws an exception
     * @param target the target of the processing instruction, may not be null
     * @throws XMLStreamException 
     */
    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Writes a processing instruction.
     * NOTE: Not implemented, throws an exception
     * @param target the target of the processing instruction, may not be null
     * @param data the data contained in the processing instruction, may not be null
     * @throws XMLStreamException 
     */
    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Writes a CData section. In delegates to writeCharsInternal.
     * @param data the data contained in the CData Section, may not be null
     * @throws XMLStreamException 
     */
    @Override
    public void writeCData(String data) throws XMLStreamException {
        log.log(Level.FINE, "writeCData({0})", data);
        writeCharsInternal(data, false);
    }

    /**
     * Write a DTD section. This string represents the entire doctypedecl 
     * production from the XML 1.0 specification. Nothing is really written
     * just it is used to guess the language definition if it was not passed
     * in the constructor.
     * @param dtd the DTD to be written
     * @throws XMLStreamException 
     */
    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        log.log(Level.FINE, "writeDTD({0})", dtd);
        // get definition if not chosen based on the formal public identifier (FPI) or root element
        if (def == null) {
            // the format is: 
            // <!DOCTYPE root-element PUBLIC "FPI" ["URI"] [ 
            // <!-- internal subset declarations -->
            // ]>
            int idxRoot = dtd.indexOf("<!DOCTYPE ") + 10;
            int idxEndRoot = dtd.indexOf(' ', idxRoot);
            int idxFPI = dtd.indexOf('"', idxEndRoot + 1) + 1;
            int idxEndFPI = dtd.indexOf('"', idxFPI);
            if ( idxRoot > 0 && idxEndRoot > idxRoot && idxFPI > idxEndRoot && idxEndFPI > idxFPI) {
                String root = dtd.substring(idxRoot, idxEndRoot).trim();
                String fpi = dtd.substring(idxFPI, idxEndFPI);
                // try first using the FPI
                def = WbXmlInitialization.getDefinitionByFPI(fpi);
                log.log(Level.FINE, "FPI: {0}", def.getName());
                if (def == null) {
                    def = WbXmlInitialization.getDefinitionByRoot(root, null);
                    log.log(Level.FINE, "root: {0}", def.getName());
                }
                if (def != null) {
                    doc.setDefinition(def);
                }
            }
        }
    }

    /**
     * Writes an entity reference.
     * NOTE: Not implemented, it should write the reference char or the
     * real data corresponding to the entity (WBXML only admits numeric entities).
     * @param name the name of the entity
     * @throws XMLStreamException 
     */
    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Private method that skips whitespaces at the beginning of the data.
     * @param data The data to skip spaces from the beginning
     * @return  The data with the whitespaces skipped
     */
    static private char[] skipWhitespacesPrefix(char[] data) {
        for (int i = 0; i < data.length; i++) {
            if (!XMLChar.isSpace(data[i])) {
                if (i == 0) {
                    return data;
                } else {
                    return Arrays.copyOfRange(data, i, data.length);
                }
            }
        }
        return new char[0];
    }
    
    /**
     * Private method that skips whitespaces at the end of the data.
     * @param data The data to skip spaces from the end
     * @return  The data with the whitespaces skipped
     */
    static private char[] skipWhitespacesSuffix(char[] data) {
        for (int i = data.length - 1; i >= 0; i--) {
            if (!XMLChar.isSpace(data[i])) {
                if (i == data.length - 1) {
                    return data;
                } else {
                    return Arrays.copyOfRange(data, 0, i+1);
                }
            }
        }
        return new char[0];
    }
    
    /**
     * Private method that trims whitespaces from the beginning and the end.
     * It calls to the other two methods.
     * @param data The data to skip spaces
     * @return The data with the whitespaces skipped
     */
    static private char[] skipWhitespaces(char[] data) {
        data = skipWhitespacesPrefix(data);
        data = skipWhitespacesSuffix(data);
        return data;
    }
    
    /**
     * Private method that writes the chars skipping the whitespaces if needed
     * @param text The test to write
     * @param skip true if whitespaces should be skipped, false if not
     */
    private void writeCharsInternal(String text, boolean skip) {
        log.log(Level.FINE, "writeCharsInternal({0})", text);
        if (currentElement != null) {
            //throw new XMLStreamException(String.format("No element to add the text '%s' to", text));
            if (skip) {
                text = new String(skipWhitespaces(text.toCharArray()));
                if (!text.isEmpty()) {
                    currentElement.addContent(new WbXmlContent(text));
                } else {
                    log.log(Level.WARNING, "Skipping characters {0}", text);
                }
            } else {
                // just create a text content in the current element
                currentElement.addContent(new WbXmlContent(text));
            }
        }
    }
    
    /**
     * Write text to the output.
     * @param text the value to write
     * @throws XMLStreamException 
     */
    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        log.log(Level.FINE, "writeCharacters({0})", text);
        writeCharsInternal(text, skipSpaces);
    }

    /**
     * Write text to the output
     * @param text the value to write
     * @param start the starting position in the array
     * @param len the number of characters to write
     * @throws XMLStreamException 
     */
    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        log.log(Level.FINE, "writeCharacters({0}, {1}, {2})", new Object[]{text, start, len});
         writeCharsInternal(new String(text, start, len), skipSpaces);
    }

    /**
     * Gets the prefix the uri is bound to
     * @param uri the prefix or null
     * @return The namespace associated to the prefix
     * @throws XMLStreamException 
     */
    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        log.log(Level.FINE, "getPrefix({0})", uri);
        String res = this.nsctx.getPrefix(uri);
        if (res == null && this.userctx != null) {
            res = this.userctx.getPrefix(uri);
        }
        return res;
    }

    /**
     * Sets the prefix the uri is bound to. This prefix is bound in the scope 
     * of the current START_ELEMENT / END_ELEMENT pair. If this method is called
     * before a START_ELEMENT has been written the prefix is bound in the root 
     * scope.
     * @param prefix the prefix to bind to the uri, may not be null
     * @param uri the uri to bind to the prefix, may be null
     * @throws XMLStreamException 
     */
    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        log.log(Level.FINE, "setPrefix({0}, {1})", new Object[]{prefix, uri});
        if (prefix == null) {
            throw new XMLStreamException("Prefix cannot be null");
        }
        if (uri == null) {
            throw new XMLStreamException("URI cannot be null");
        }
        this.nsctx.addPrefix(prefix, uri);
    }

    /**
     * Binds a URI to the default namespace This URI is bound in the scope of 
     * the current START_ELEMENT / END_ELEMENT pair. If this method is called 
     * before a START_ELEMENT has been written the uri is bound in the root scope.
     * @param namespaceURI the uri to bind to the default namespace, may be null
     * @throws XMLStreamException 
     */
    @Override
    public void setDefaultNamespace(String namespaceURI) throws XMLStreamException {
        log.log(Level.FINE, "setDefaultNamespace({0})", namespaceURI);
        this.nsctx.setDefaultNamespace(namespaceURI);
    }

    /**
     * Sets the current namespace context for prefix and uri bindings. This 
     * context becomes the root namespace context for writing and will replace 
     * the current root namespace context. Subsequent calls to setPrefix and 
     * setDefaultNamespace will bind namespaces using the context passed to the
     * method as the root context for resolving namespaces. This method may only
     * be called once at the start of the document. It does not cause the 
     * namespaces to be declared. If a namespace URI to prefix mapping is found 
     * in the namespace context it is treated as declared and the prefix may 
     * be used by the StreamWriter.
     * @param nc the namespace context to use for this writer, may not be null
     * @throws XMLStreamException 
     */
    @Override
    public void setNamespaceContext(NamespaceContext nc) throws XMLStreamException {
        log.log(Level.FINE, "setNamespaceContext({0})", nc);
        this.userctx = nc;
    }

    /**
     * Returns the current namespace context.
     * @return the current NamespaceContext
     */
    @Override
    public NamespaceContext getNamespaceContext() {
        log.fine("getNamespaceContext()");
        return nsctx;
    }

    /**
     * Get the value of a feature/property from the underlying implementation
     * NOTE: Not implemented, it throws an exception
     * @param string The name of the property, may not be null
     * @return The value of the property
     * @throws IllegalArgumentException 
     */
    @Override
    public Object getProperty(String string) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
