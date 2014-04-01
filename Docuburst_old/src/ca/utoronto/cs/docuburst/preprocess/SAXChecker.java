package ca.utoronto.cs.docuburst.preprocess;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;
import java.io.IOException;

public class SAXChecker {

  public static void main(String[] args) {
  
    if (args.length <= 0) {
      System.out.println("Usage: java SAXChecker URL");
      return;
    }
    
    try {
      XMLReader parser = XMLReaderFactory.createXMLReader();
      parser.parse(args[0]);
      System.out.println(args[0] + " is well-formed.");
    }
    catch (SAXException e) {
      System.out.println(args[0] + " is not well-formed.");
    }
    catch (IOException e) { 
      System.out.println(
       "Due to an IOException, the parser could not check " 
       + args[0]
      ); 
    }
  }

}
