package es.project.bd.configuracion;

import java.util.ResourceBundle;

/**
 * <p>Clase que accede al fichero de propiedades que contiene la información referente a la conexión
 * con la base de datos</p>
 * @author Daniel Fernández Aller
 */
public class ConfigBD {
	private static ResourceBundle propiedades = ResourceBundle.getBundle("es.project.utilidades.datosBD");
	
	/**
	 * <p>Accede a la propiedad "user"</p>
	 * @return Devuelve una cadena con el nombre de usuario
	 */
	public static String getUser() {
		return propiedades.getString("user");
	}
	
	/**
	 * <p>Accede a la propiedad "pass"</p>
	 * @return Devuelve una cadena con el password
	 */
	public static String getPassword() {
		return propiedades.getString("pass");
	}
	
	/**
	 * <p>Accede a la propiedad "bdurl"</p>
	 * @return Devuelve la cadena de conexión
	 */
	public static String getBDUrl() {
		return propiedades.getString("bdurl");
	}
	
	/**
	 * <p>Accede a la propiedad "esquema", el cual contiene el nombre del esquema de la base de datos
	 * utilizado en labores de explotación del servicio</p>
	 * @return Devuelve una cadena con el nombre del esquema de la base de datos
	 */
	public static String getEsquema() {
		return propiedades.getString("esquema");
	}
	
	/**
	 * <p>Accede a la propiedad "esquema_2", el cual contiene el nombre del esquema de la base de datos
	 * utilizado en labores de pruebas</p>
	 * @return Devuelve una cadena con el nombre del esquema de la base de datos
	 */
	public static String getEsquema_2() {
		return propiedades.getString("esquema_2");
	}
}
