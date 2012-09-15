/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.rickyepoderi.wbxml.document;

import es.rickyepoderi.wbxml.definition.IanaCharset;
import es.rickyepoderi.wbxml.definition.WbXmlDefinition;
import es.rickyepoderi.wbxml.definition.WbXmlInitialization;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *
 * @author ricky
 */
public class Test {
  
    /*
     * <?xml version="1.0"?>
     * <!DOCTYPE WV-CSP-Message PUBLIC "-//OMA//DTD WV-CSP 1.1//EN" "http://www.openmobilealliance.org/DTD/WV-CSP.XML">
     * <WV-CSP-Message xmlns="http://www.wireless-village.org/CSP1.1">
     *    <Session>
     *       <SessionDescriptor>
     *          <SessionType>Inband</SessionType>
     *          <SessionID>im.user.com#48815@server.com</SessionID>
     *       </SessionDescriptor>
     *       <Transaction>
     *          <TransactionDescriptor>
     *             <TransactionMode>Request</TransactionMode>
     *             <TransactionID>IMApp01#12345@NOK5110</TransactionID>
     *          </TransactionDescriptor>
     *          <TransactionContent xmlns="http://www.wireless-village.org/TRC1.1">
     *             <Logout-Request />
     *          </TransactionContent>
     *       </Transaction>
     *    </Session>
     * </WV-CSP-Message>
     */
     static private WbXmlDocument createWVCSP(WbXmlDefinition def) {
         return new WbXmlDocument(
                 WbXmlVersion.VERSION_1_3, 
                 def, 
                 IanaCharset.UTF_8, 
                 new WbXmlBody(
                     new WbXmlElement(
                         "WV-CSP-Message", 
                         //new WbXmlAttribute("xmlns", "http://www.wireless-village.org/CSP1.1"),
                         new WbXmlElement("Session", new WbXmlElement[] {
                             new WbXmlElement("SessionDescriptor", new WbXmlElement[] {
                                 new WbXmlElement("SessionType", new WbXmlContent("Inband")),
                                 new WbXmlElement("SessionID", new WbXmlContent("im.user.com#48815@server.com")),
                             }),
                             new WbXmlElement("Transaction", new WbXmlElement[] {
                                 new WbXmlElement("TransactionDescriptor", new WbXmlElement[] {
                                     new WbXmlElement("TransactionMode", new WbXmlContent("Request")),
                                     new WbXmlElement("TransactionID", new WbXmlContent("IMApp01#12345@NOK5110")),
                                 }),
                                 new WbXmlElement(
                                     "TransactionContent", 
                                     //new WbXmlAttribute("xmlns", "http://www.wireless-village.org/TRC1.1"), 
                                     new WbXmlElement("Logout-Request")
                                 )
                             }),
                         })
                    )
                 )
          );
     }
    
    /* 
     * <?xml version="1.0"?>
     * <!DOCTYPE SyncML PUBLIC "-//SYNCML//DTD SyncML 1.1//EN" "http://www.syncml.org/docs/syncml_represent_v11_20020213.dtd">
     * <SyncML>
     *   <SyncHdr>
     *     <VerDTD>1.1</VerDTD>
     *     <VerProto>SyncML/1.1</VerProto>
     *     <SessionID>1</SessionID>
     *     <MsgID>2</MsgID>
     *     <Target><LocURI>IMEI:493005100592800</LocURI></Target>
     *     <Source><LocURI>http://www.syncml.org/sync-server</LocURI></Source>
     *   </SyncHdr>
     *   <SyncBody>
     *     <Status>
     *       <CmdID>1</CmdID>
     *       <MsgRef>2</MsgRef>
     *       <CmdRef>0</CmdRef>
     *       <Cmd>SyncHdr</Cmd>
     *       <TargetRef>http://www.syncml.org/sync-server</TargetRef>
     *       <SourceRef>IMEI:493005100592800</SourceRef>
     *       <Data>101</Data>
     *     </Status>
     *   </SyncBody>
     * </SyncML>
     */
     static private WbXmlDocument createSyncML(WbXmlDefinition def) {
         return new WbXmlDocument(
                 WbXmlVersion.VERSION_1_3, 
                 def, 
                 IanaCharset.UTF_8, 
                 new WbXmlBody(
                     new WbXmlElement(
                         "SyncML",
                         new WbXmlElement[] {
                                 new WbXmlElement(
                                     "SyncHdr", new WbXmlElement[] {
                                        new WbXmlElement("VerDTD", "1.1"),
                                        new WbXmlElement("VerProto", "SyncML/1.1"),
                                        new WbXmlElement("SessionID", "1"),
                                        new WbXmlElement("MsgID", "2"),
                                        new WbXmlElement("Target", new WbXmlElement("LocURI", "IMEI:493005100592800")),
                                        new WbXmlElement("Source", new WbXmlElement("LocURI", "http://www.syncml.org/sync-server")),
                                 }
                             ),
                             new WbXmlElement(
                                 "SyncBody", 
                                 new WbXmlElement(
                                     "Status", new WbXmlElement[] {
                                         new WbXmlElement("CmdID", "1"),
                                         new WbXmlElement("MsgRef", "2"),
                                         new WbXmlElement("CmdRef", "0"),
                                         new WbXmlElement("Cmd", "SyncHdr"),
                                         new WbXmlElement("TargetRef", "http://www.syncml.org/sync-server"),
                                         new WbXmlElement("SourceRef", "IMEI:493005100592800"),
                                         new WbXmlElement("Data", "101"),
                                     }
                                 )
                             )
                         }
                     )
                 )
         );
     }    
    
    /*
     * <?xml version="1.0"?>
     * <!DOCTYPE si PUBLIC "-//WAPFORUM//DTD SI 1.0//EN" "http://www.wapforum.org/DTD/si.dtd">
     * <si>
     *   <indication href="http://www.xyz.com/email/123/abc.wml"
     *             created="1999-06-25T15:23:15Z"
     *             si-expires="1999-06-30T00:00:00Z">
     *       You have 4 new emails
     *   </indication>
     * </si>
     */
    static private WbXmlDocument createSi(WbXmlDefinition def) {
        return new WbXmlDocument(
                WbXmlVersion.VERSION_1_3, 
                def, 
                IanaCharset.UTF_8, 
                new WbXmlBody(
                    new WbXmlElement(
                        "si", 
                        new WbXmlElement(
                            "indication",
                            new WbXmlAttribute[] {
                                new WbXmlAttribute("href", "http://www.xyz.com/email/123/abc.wml"),
                                new WbXmlAttribute("created", "1999-06-25T15:23:15Z"),
                                new WbXmlAttribute("si-expires", "1999-06-30T00:00:00Z")
                            },
                            "You have 4 new emails"
                        )
                    )
                )
        );
    }
    
    static public void main(String[] args) throws Exception {
        
        WbXmlDocument syncML;
        WbXmlDocument wvcsp;
        WbXmlDocument si;
        FileOutputStream fos;
        FileInputStream fis;
        ByteArrayOutputStream bos;
        WbXmlEncoder encoder;
        WbXmlParser parser;
        
        si = createSi(WbXmlInitialization.getDefinitionByRoot("si", null));
        fos = new FileOutputStream("/home/ricky/si.wbxml");
        encoder = new WbXmlEncoder(fos, si, WbXmlEncoder.StrtblType.ALWAYS);
        encoder.encode(si);
        fos.close();
        
        syncML = createSyncML(WbXmlInitialization.getDefinitionByRoot("SyncML", "SYNCML:SYNCML1.1"));
        fos = new FileOutputStream("/home/ricky/syncml.wbxml");
        encoder = new WbXmlEncoder(fos, syncML, WbXmlEncoder.StrtblType.ALWAYS);
        encoder.encode(syncML);;
        fos.close();
        
        wvcsp = createWVCSP(WbXmlInitialization.getDefinitionByRoot("WV-CSP-Message", null));
        fos = new FileOutputStream("/home/ricky/wvcsp.wbxml");
        encoder = new WbXmlEncoder(fos, wvcsp, WbXmlEncoder.StrtblType.ALWAYS);
        encoder.encode(wvcsp);
        fos.close();
        
        fis = new FileInputStream("/home/ricky/syncml.wbxml");
        parser = new WbXmlParser(fis);
        syncML = parser.parse();
        System.err.println(syncML);
        fis.close();
        
        fis = new FileInputStream("/home/ricky/wvcsp.wbxml");
        parser = new WbXmlParser(fis);
        wvcsp = parser.parse();
        System.err.println(wvcsp);
        fis.close();
        
        fis = new FileInputStream("/home/ricky/si.wbxml");
        parser = new WbXmlParser(fis);
        si = parser.parse();
        System.err.println(si);
        fis.close();
        
    }
}
