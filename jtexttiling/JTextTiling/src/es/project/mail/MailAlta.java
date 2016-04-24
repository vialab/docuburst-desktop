package es.project.mail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;

import es.project.bd.objetos.Usuario;
import es.project.mail.configuracion.ConfigMail;
import es.project.procesadorXSLT.ProcesadorXSLT;

/**
 * <p>Envía el mail de confirmación de alta a los usuarios</p>
 * @author Daniel Fernández Aller
 */
public class MailAlta extends Mail{
	private String cabecera = ConfigMail.getCabeceraXmlAlta();
	private String url = ConfigMail.getUrlBase();
	private String rutaXsl = ConfigMail.getXslAlta();
	
	/**
	 * <p>Crea el mensaje con el texto apropiado y lo envía al mail del usuario</p>
	 * @param usuario Objeto que representa al usuario que va a recibir el mail
	 * @throws MessagingException Posibles errores en el envío del mensaje
	 */
	public void enviarMail(Usuario usuario) throws MessagingException{
		this.pasosInicialesMail(usuario);
		this.crearTextoMail(usuario, mensaje, mp);
		Transport.send(mensaje);
		borrarArchivos();
	}
	
	/**
	 * <p>Borra archivos residuales derivados de la creación del fichero html que se
	 * enviará en el cuerpo del correo.</p>
	 */
	protected void borrarArchivos() {
		//TODO poco importante
		//borrar mail.xml
		//borrar mail.html si se puede
	}
 	
	/**
	 * <p>Compone el texto del mensaje. El texto contiene el nombre y el password del
	 * usuario y la ruta del enlace que tiene que seguir para completar el alta en
	 * la aplicación</p>
	 * @param usuario Objeto que representa al usuario que va a recibir el mail
	 * @throws MessagingException Posibles errores en el envío del mensaje
	 */
	protected void crearTextoMail(Usuario usuario, Message mensaje, Multipart mp)
		throws MessagingException {
		
		try {
			FileOutputStream fos = new FileOutputStream(new File(super.rutaXml));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			bw.write(cabecera);
			bw.write("<usuario><nombre>" + usuario.getNombre() + "</nombre>");
			bw.write("<password>" + usuario.getPassword() + "</password>");
			bw.write("<url>" + url + usuario.getUuid() + "</url></usuario>");
			bw.close();
			
			String args[] = new String[]{this.rutaXsl, super.rutaXml, super.rutaHtml};
 			ProcesadorXSLT.validarDocumento(args);
			
		} catch (FileNotFoundException fnfe) {
			System.err.println("file not found exception: \n" + fnfe.getMessage());
		} catch (IOException ioe) {
			System.err.println("fioexception: \n" + ioe.getMessage());
		} catch (Exception e) {
			System.err.println("exception la que sea en la clase mail: \n" + e.getMessage());
		}
		
		String cuerpo = super.rutaHtml;		
		this.crearCuerpo(mensaje, mp, cuerpo);
	}
}
