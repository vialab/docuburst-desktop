package es.project.mail;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import es.project.bd.objetos.Usuario;
import es.project.mail.configuracion.ConfigMail;

/**
 * <p>Clase abstracta que implementa las funcionalidades básicas para enviar emails</p>
 * @author Daniel Fernández Aller
 */
public abstract class Mail {
	private String user = ConfigMail.getFrom();
	private String password = ConfigMail.getPassword();
	private String port = ConfigMail.getPuerto();
	private String host = ConfigMail.getHost();
	private String subject = ConfigMail.getAsunto();
	protected String rutaXml = ConfigMail.getRutaXml();
	protected String rutaHtml = ConfigMail.getRutaHtml();
	
	protected Message mensaje;
	protected Multipart mp;

	/**
	 * <p>Interfaz de la clase con el resto de la aplicación: encapsula la serie de pasos
	 * que se siguen a la hora de enviar los correos</p>
	 * @param usuario Objeto que representa al usuario que va a recibir el mail
	 * @throws MessagingException Posibles errores en el envío del mensaje
	 */
	public abstract void enviarMail(Usuario usuario) throws MessagingException;
	
	/**
	 * <p>Borra archivos residuales derivados de la creación del fichero html que se
	 * enviará en el cuerpo del correo.</p>
	 */
	protected abstract void borrarArchivos();
	
	/**
	 * <p>Crea los objetos necesarios para enviar el mensaje e inicializa las propiedades
	 * según los valores obtenidos del fichero de propiedades.</p>
	 * <p>Como último paso, adjunta una imagen que será la cabecera del mensaje</p>
	 * @param usuario Objeto que representa al usuario que va a recibir el mail
	 * @throws MessagingException Posibles errores en el envío del mensaje
	 */
	protected void pasosInicialesMail(Usuario usuario) throws MessagingException {
		Properties props = System.getProperties();
		this.setProperties(props);
		Authenticator auth = new SMTPAuthenticator();
		Session sesion = Session.getInstance(props, auth);
		
		mensaje = new MimeMessage(sesion);
		mensaje.setSubject(subject);
		mensaje.setFrom(new InternetAddress(user));
		mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(usuario.getEmail()));
		mp = new MimeMultipart();
	    //this.adjuntarArchivo(rutaImagen);
	}
	
	/**
	 * <p>Crea el texto personalizado de cada mensaje y llama al método "crearCuerpo", que se
	 * encarga de incluirlo en el mensaje</p>
	 * @param usuario Objeto que representa al usuario que va a recibir el mail
	 * @param mensaje Mensaje en el que se incluirá el texto
	 * @param mp Objeto contenedor en el que se incluyen las partes del mail
	 * @throws MessagingException Posibles errores en el envío del mensaje
	 */
	protected abstract void crearTextoMail(Usuario usuario, Message mensaje, Multipart mp)
		throws MessagingException;
	
	/**
	 * <p>Añade el texto e indica que el mensaje es de tipo "Multipart", para permitir que contenga
	 * tanto imágenes como texto en formato html</p>
	 * @param mensaje Mensaje en el que añadimos el texto
	 * @param mp Objeto contenedor en el que se incluyen las partes del mail: en este caso, se le
	 * añade el texto y se indica el tipo de mensaje
	 * @param texto Texto del mensaje
	 * @throws MessagingException Posibles errores en el envío del mensaje
	 */
	protected void crearCuerpo(Message mensaje, Multipart mp, String texto) throws MessagingException{
		BodyPart bodyText = new MimeBodyPart();
		bodyText.setContent(texto, "text/html");
		mp.addBodyPart(bodyText);
		mensaje.setContent(mp, "multipart/mixed");
	}
	
	/**
	 * <p>Adjunta un archivo en el mensaje, en base a la ruta que se recibe como parámetro. Esto
	 * quiere decir que sirve tanto para adjuntar ficheros de texto, imágenes, etc</p>
	 * @param ruta Ruta del fichero a adjuntar
	 * @throws MessagingException Posibles errores en el envío del mensaje
	 */
	protected void adjuntarArchivo(String ruta) throws MessagingException{
		BodyPart adjunto = new MimeBodyPart();
		File fichero = new File(ruta);
		DataSource source = new FileDataSource(fichero);
		adjunto.setDataHandler(new DataHandler(source));
		adjunto.setFileName(fichero.getName());
		mp.addBodyPart(adjunto);
	}
	
	/**
	 * <p>Establece las propiedades de la conexión, tales como el host, el puerto, el usuario para
	 * autenticarse, etc. Estos valores se obtienen del fichero de propiedades</p>
	 * @param props Objeto que permite manejar el conjunto de propiedades del sistema
	 */
	private void setProperties(Properties props) {
		props.put("mail.smtp.user", user);
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.starttls.enable","true");
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.socketFactory.port", port);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
	}
	
	/**
	 * <p>Clase que proporciona la autenticación a la hora de registrar las propiedades en
	 * la sesión</p>
	 * @author Daniel Fernández Aller
	 */
	private class SMTPAuthenticator extends Authenticator {
		/**
		 * <p>Método invocado desde el sistema cuando la autenticación es requerida</p>
		 */
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(user, password);
        }
    }
}
