package es.project.junit.configuracion;

import java.util.ResourceBundle;

/**
 * <p>Clase que accede al fichero de propiedades que contiene la información referente a la 
 * configuración de las pruebas junit</p>
 * @author Daniel Fernández Aller
 */
public class ConfigJunit {
	
	private static ResourceBundle propiedades = getPropiedades();
	
	/**
	 * <p>Crea un ResourceBundle dependiendo del sistema operativo que estemos utilizando, para
	 * lo cual se accede a la propiedad del sistema "os.name"</p>
	 * @return Devuelve un ResourceBundle que apunta a las propiedades contenidas en un fichero
	 */
	private static ResourceBundle getPropiedades() {
		String os = System.getProperty("os.name");
		
		return (os.compareTo("Windows XP") == 0)?
				(ResourceBundle.getBundle("es.project.utilidades.junitRutas_windows")):
				(ResourceBundle.getBundle("es.project.utilidades.junitRutas_linux"));
	}
	
	/**
	 * <p>Accede a la propiedad "rutaXsl"</p>
	 * @return Obtiene el valor de la ruta donde se encuentra el fichero junit.xsl
	 */
	public static String getRutaXsl() {
		return propiedades.getString("rutaXsl");
	}
	
	/**
	 * <p>Accede a la propiedad "rutaXml"</p>
	 * @return Obtiene el valor de la ruta donde se encuentra el fichero junit.xml
	 */
	public static String getRutaXml(){
		return propiedades.getString("rutaXml");
	}
	
	/**
	 * <p>Accede a la propiedad "rutaHtml"</p>
	 * @return Obtiene el valor de la ruta donde se encuentra el fichero junit.html
	 */
	public static String getRutaHtml() {
		return propiedades.getString("rutaHtml");
	}
	
	/**
	 * <p>Accede a la propiedad "cabecera"</p>
	 * @return Obtiene el valor de una cadena de texto que contiene la cabecera de los
	 * ficheros xml
	 */
	public static String getCabecera() {
		return propiedades.getString("cabecera");
	}

}
