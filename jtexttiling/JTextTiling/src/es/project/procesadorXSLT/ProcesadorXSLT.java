package es.project.procesadorXSLT;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * <p>Clase que realiza una transformación xslt a partir de un fichero xml validado
 * frente a un xsl</p>
 * @author Daniel Fernández Aller
 */
public class ProcesadorXSLT {
  static Document document;
  static FileOutputStream miFicheroSt;
  static DocumentBuilderFactory factory;

  /**
   * <p>Crea un fichero html mediante una validación de un fichero xml frente
   * a un documento xsl</p>
   * @param args Array de parámetros utilizados por el método: 0: fichero xsl, 
   * 1: fichero xml, 2: fichero de salida
   */
  public static void validarDocumento(String args[]) {
	  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	  //opcional:
	  //factory.setValidating(true);

	  //Cogemos de la entrada los dos ficheros XSL y XML
	  File stylesheet = new File(args[0]);//cogemos el fichero xsl
	  File datafile = new File(args[1]); //y el fichero xml a validar

	  try {
		//Decimos cual va a ser el fichero de salida
		  miFicheroSt = new FileOutputStream(args[2]);
		    
		  //Generamos el arbol parseando el fichero.XML
		  DocumentBuilder builder = factory.newDocumentBuilder();
		  document = builder.parse(datafile);
		  // Usamos un Transformer para la salida
		  TransformerFactory tFactory = TransformerFactory.newInstance();
		  StreamSource stylesource = new StreamSource(stylesheet);
		  Transformer transformer = tFactory.newTransformer(stylesource);
		  //Con el Transformer generamos la pagina HTML con los datos XML
		  DOMSource source = new DOMSource(document);
		      
		  StreamResult result = new StreamResult(miFicheroSt); 
		  transformer.transform(source, result);
		  
	} catch (FileNotFoundException e) {
		System.err.println(e.getMessage());
	} catch (TransformerConfigurationException e) {
		System.err.println(e.getMessage());
	} catch (ParserConfigurationException e) {
		System.err.println(e.getMessage());
	} catch (SAXException e) {
		System.err.println(e.getMessage());
	} catch (IOException e) {
		System.err.println(e.getMessage());
	} catch (TransformerFactoryConfigurationError e) {
		System.err.println(e.getMessage());
	} catch (TransformerException e) {
		System.err.println(e.getMessage());
	}
  }
}
