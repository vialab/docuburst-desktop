package es.project.ficheros.configuracion;

import java.util.ResourceBundle;

/**
 * <p>Clase que accede al fichero de propiedades que contiene la información referente al 
 * tratamiento de ficheros</p>
 * @author Daniel Fernández Aller
 */
public class ConfigFicheros {
	private static ResourceBundle propiedades = getPropiedades();
	
	/**
	 * <p>Crea un ResourceBundle dependiendo del sistema operativo que estemos utilizando, para
	 * lo cual se accede a la propiedad del sistema "os.name"</p>
	 * @return Devuelve un ResourceBundle que apunta a las propiedades contenidas en un fichero
	 */
	private static ResourceBundle getPropiedades() {
		String os = System.getProperty("os.name");
		
		return (os.compareTo("Windows XP") == 0)?
				(ResourceBundle.getBundle("es.project.utilidades.ficheros_windows")):
				(ResourceBundle.getBundle("es.project.utilidades.ficheros_linux"));
	}
	
	/**
	 * <p>Accede a la propiedad "ruta_base"</p>
	 * @return Devuelve una cadena con la ruta base común a todos los ficheros almacenados
	 */
	public static String getRutaBase() {
		return propiedades.getString("ruta_base");
	}
	
	/**
	 * <p>Accede a la propiedad "ejecutable_programa"</p>
	 * @return Devuelve una cadena con la ruta del ejecutable del programa editor con el que
	 * se van a abrir los ficheros
	 */
	public static String getEjecutablePrograma() {
		return propiedades.getString("ejecutable_programa");
	}
	
	/**
	 * <p>Accede a la propiedad "programa"</p>
	 * @return Devuelve una cadena con el nombre del programa editor
	 */
	public static String getPrograma() {
		return propiedades.getString("programa");
	}
	
	/**
	 * <p>Accede a la propiedad "separador"</p>
	 * @return Devuelve una cadena con el tipo de separador utilizado por el sistema operativo
	 */
	public static String getSeparador() {
		return propiedades.getString("separador");
	}
}
