/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.storage.dom;

import java.io.DataOutputStream;
import java.io.FileOutputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import xxl.core.collections.containers.recordManager.RecordManager;
import xxl.core.collections.containers.recordManager.TId;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.xml.storage.BulkLoadingHandler;
import xxl.core.xml.storage.EXTree;
import xxl.core.xml.storage.SAXHandlers;

/**
 * Implements DocumentBuilder from JAXP.
 */
public class DocumentBuilder extends javax.xml.parsers.DocumentBuilder {

	protected org.xml.sax.EntityResolver entityResolver;
	protected org.xml.sax.ErrorHandler errorHandler;
	protected SAXParserFactory saxParserFactory;
	protected SAXParser parser;
	protected boolean tupleInsertion;

	private DocumentBuilderFactory dBFactory;

	/** Creates a new instance of DocumentBuilder */
	public DocumentBuilder(DocumentBuilderFactory dBFactory) {
		this.dBFactory = dBFactory;
	}

	public org.w3c.dom.DOMImplementation getDOMImplementation() {
		return new org.w3c.dom.DOMImplementation() {
			public boolean hasFeature(String feature, String version) {
				return false;
			}
			public org.w3c.dom.DocumentType createDocumentType( String qualifiedName,String publicId, String systemId)
				throws org.w3c.dom.DOMException{
				return null;
			}
			public org.w3c.dom.Document createDocument(String str, String str1,org.w3c.dom.DocumentType documentType)
				throws org.w3c.dom.DOMException {
				return null;
			}
			public Object getFeature(String feature, String version) {
				throw new UnsupportedOperationException("not implemented yet");
			}
		};
	}

	public boolean isNamespaceAware() {
		return false;
	}

	public boolean isValidating() {
		return false;
	}

	/**
	 * Not supported at the moment (throws an Exception).
	 */
	public org.w3c.dom.Document newDocument() {
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Use parse");
	}

	/**
	 * Parses the document and returns it. This is extremly efficient and
	 * takes nearly no time.
	 */
	public org.w3c.dom.Document parse(final xxl.core.xml.storage.dom.StorageInputSource inputSource) throws org.xml.sax.SAXException, java.io.IOException {
		try {
			EXTree tree = (EXTree) dBFactory.getAttribute("EXTree");

			return new Document(tree);
		}
		catch(Exception e) {
			throw new WrappingRuntimeException(e);
		}
	 }

	/**
	 * Prefix for the Container is taken from the attribute "xmlContainer", ...
	 */
	public org.w3c.dom.Document parse(org.xml.sax.InputSource inputSource) throws org.xml.sax.SAXException, java.io.IOException {
		try{
/*			String url = inputSource.getSystemId();
			
			//Alle (default) Attribute setzen/hollen
			if(dBFactory.getAttribute("pageSize")==null)
				dBFactory.setAttribute("pageSize", new Integer(4096));
			
			if(dBFactory.getAttribute("containerFilename")==null)
				dBFactory.setAttribute("containerFilename", 
					url.substring(0,url.lastIndexOf(".xml")) + "XmlContainer");
			
			// System.out.println((String)dBFactory.getAttribute("containerFilename"));
			
			if(dBFactory.getAttribute("container")==null)
				dBFactory.setAttribute("container", 
					new BlockFileContainer(
						(String)dBFactory.getAttribute("containerFilename"), 
						((Integer)dBFactory.getAttribute("pageSize")).intValue()
					)
				);
			
			FixedSizeConverter idConverter =  
				((Container)dBFactory.getAttribute("container")).objectIdConverter();
			
			if(dBFactory.getAttribute("strategy")==null)
				dBFactory.setAttribute("strategy", new FirstFitStrategy());
			
			if(dBFactory.getAttribute("tidmanager")==null)
				dBFactory.setAttribute("tidmanager", new IdentityTIdManager(idConverter));

			if(dBFactory.getAttribute("recordManager")==null)
				dBFactory.setAttribute("recordManager", 
					new RecordManager(
						(Container)dBFactory.getAttribute("container"),
						((Integer)dBFactory.getAttribute("pageSize")).intValue(),
						(Strategy)dBFactory.getAttribute("strategy"),
						(TIdManager)dBFactory.getAttribute("tidmanager"),
						0
					)
				); 
			
			if(dBFactory.getAttribute("subtreeconverter")==null)
				dBFactory.setAttribute("subtreeconverter",
					Node.getSubtreeConverter(TId.getConverter(idConverter))
				); 

			if(dBFactory.getAttribute("split")==null)
				dBFactory.setAttribute("split",
					new SimpleSplit(
						(RecordManager)dBFactory.getAttribute("recordManager"),
						(SubtreeConverter)dBFactory.getAttribute("nodeconverter"),
						((RecordManager)dBFactory.getAttribute("recordManager")).getMaxObjectSize()
					)
				);
			
			//Dateiname, wo RootId gespeichert wird
			if(dBFactory.getAttribute("configFilename")==null)
				dBFactory.setAttribute("configFilename", 
					url.substring(0,url.lastIndexOf(".xml")) + "RootDescriptor.mtd"
				);
			
			//den kompletten Baum in Attribut speichern
			if(dBFactory.getAttribute("EXTree")==null)
				dBFactory.setAttribute("EXTree", 
					new EXTree (
						(RecordManager)dBFactory.getAttribute("recordManager"), 
						((RecordManager)dBFactory.getAttribute("recordManager")).getMaxObjectSize(),
						(Split)dBFactory.getAttribute("split"),
						(SizeConverter)dBFactory.getAttribute("subtreeconverter")
					)
				);
*/
			final EXTree tree = (EXTree) dBFactory.getAttribute("EXTree");
			Boolean tI = (Boolean) dBFactory.getAttribute("tupleInsertion");
			if (tI!=null)
				tupleInsertion =  tI.booleanValue();
			
			saxParserFactory = SAXParserFactory.newInstance();
			
			parser = saxParserFactory.newSAXParser();
			if (tupleInsertion) {
				parser.parse(
					inputSource,
					// new ExceptionCatcherDecoratorHandler(
					SAXHandlers.getTreeInsertionHandler(tree)
					// )
				);
			}
			else {
				parser.parse(
					inputSource,
					new BulkLoadingHandler(tree, new TId(new Long(0),(short) 0))
				);
			}
			
			// RootID speichern
			writeRootID(
				(RecordManager)dBFactory.getAttribute("recordManager"),
				tree.getRootId(),
				(String)dBFactory.getAttribute("configFilename")
			);

			return new Document(tree);
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
	}

	public void setEntityResolver(org.xml.sax.EntityResolver entityResolver) {
		this.entityResolver=entityResolver;
	}

	public void setErrorHandler(org.xml.sax.ErrorHandler errorHandler) {
		this.errorHandler=errorHandler;
	}

	private static void writeRootID(RecordManager recordManager, Object rootID, String configFilename) {
		try {
			DataOutputStream d = new DataOutputStream(new FileOutputStream(configFilename));
			recordManager.objectIdConverter().write(d,rootID);
			d.close();
		}
		catch (Exception e) {
			throw new RuntimeException("Config file could not be read");
		}
	}
}
