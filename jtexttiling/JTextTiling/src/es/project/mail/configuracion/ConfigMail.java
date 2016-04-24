package es.project.mail.configuracion;

import java.util.ResourceBundle;

/**
 * <p>Clase que accede al fichero de propiedades que contiene la información referente al
 * envío de mails</p>
 * @author Daniel Fernández Aller
 */
public class ConfigMail {

	private static ResourceBundle propiedades = ResourceBundle.getBundle("es.project.utilidades.mail");
	
	/**
	 * <p>Accede a la propiedad "host"</p>
	 * @return Devuelve una cadena con el servidor smtp a utilizar
	 */
	public static String getHost() {
		return propiedades.getString("host");
	}
	
	/**
	 * <p>Accede a la propiedad "puertoEnvio"</p>
	 * @return Devuelve una cadena con el puerto a través del cual se va a enviar el mensaje
	 */
	public static String getPuerto() {
		return propiedades.getString("puerto");
	}
	
	/**
	 * <p>Accede a la propiedad "from"</p>
	 * @return Devuelve una cadena con el remitente del mensaje
	 */
	public static String getFrom() {
		return propiedades.getString("from");
	}
	
	/**
	 * <p>Accede a la propiedad "password"</p>
	 * @return Devuelve una cadena con el password para autenticarse en el servidor
	 */
	public static String getPassword() {
		return propiedades.getString("password");
	}
	
	/**
	 * <p>Accede a la propiedad "asuntoAlta"</p>
	 * @return Devuelve una cadena con el asunto del mensaje cuando se trata del mail
	 * de confirmación de alta
	 */
	public static String getAsuntoAlta() {
		return propiedades.getString("asuntoAlta");
	}
	
	/**
	 * <p>Accede a la propiedad "asuntoAdjuntos"</p>
	 * @return Devuelve una cadena con el asunto que lleva el mensaje de envío de sus 
	 * ficheros como adjuntos al usuario
	 */
	public static String getAsuntoAdjuntos() {
		return propiedades.getString("asuntoAdjuntos");
	}
	
	/**
	 * <p>Accede a la propiedad "asunto"</p>
	 * @return Devuelve una cadena con el asunto del mensaje (general)
	 */
	public static String getAsunto() {
		return propiedades.getString("asunto");
	}
	
	/**
	 * <p>Accede a la propiedad "cabeceraXmlAlta"</p>
	 * @return Devuelve una cadena con la cabecera del fichero xml a partir del cual
	 * se creará el fichero html para enviar en el mail de confirmación de alta
	 */
	public static String getCabeceraXmlAlta() {
		return propiedades.getString("cabeceraXmlAlta");
	}
	
	/**
	 * <p>Accede a la propiedad "cabeceraXmlAdjuntos"</p>
	 * @return Devuelve una cadena con la cabecera del fichero xml a partir del cual
	 * se creará el fichero html para enviar en el mail de ficheros adjuntos
	 */
	public static String getCabeceraXmlAdjuntos() {
		return propiedades.getString("cabeceraXmlAdjuntos");
	}
	
	/**
	 * <p>Accede a la propiedad "rutaXml"</p>
	 * @return Devuelve una cadena con la ruta del fichero xml
	 */
	public static String getRutaXml() {
		return propiedades.getString("rutaXml");
	}
	
	/**
	 * <p>Accede a la propiedad "url_base"</p>
	 * @return Devuelve una cadena con la dirección base de la página donde se
	 * confirma la activación de la cuenta
	 */
	public static String getUrlBase() {
		return propiedades.getString("url_base");
	}
	
	/**
	 * <p>Accede a la propiedad "rutaXslAlta"</p>
	 * @return Devuelve una cadena con la ruta donde se encuentra el fichero xsl 
	 * utilizado en la creación del html del mail de confirmación del alta de la
	 * cuenta 
	 */
	public static String getXslAlta() {
		return propiedades.getString("rutaXslAlta");
	}
	
	/**
	 * <p>Accede a la propiedad "rutaXslAdjuntos"</p>
	 * @return Devuelve una cadena con la ruta donde se encuentra el fichero xsl
	 * utilizado en la creación del html del mail de envío de ficheros adjuntos
	 */
	public static String getXslAdjuntos() {
		return propiedades.getString("rutaXslAdjuntos");
	}
	
	/**
	 * <p>Accede a la propiedad "rutaHtml"</p>
	 * @return Devuelve una cadena con la ruta donde se encuentra el fichero html
	 * a incluir en el mail
	 */
	public static String getRutaHtml() {
		return propiedades.getString("rutaHtml");
	}
 }
