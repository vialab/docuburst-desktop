package es.project.mail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;

import es.project.bd.objetos.Archivo;
import es.project.bd.objetos.Usuario;
import es.project.mail.configuracion.ConfigMail;
import es.project.procesadorXSLT.ProcesadorXSLT;

/**
 * <p>Envía el mail con los ficheros que el usuario tiene subidos en el servidor de la
 * aplicación</p>
 * @author Daniel Fernández Aller
 */
public class MailFicheros extends Mail {
	private String cabecera = ConfigMail.getCabeceraXmlAdjuntos();
	private String rutaXsl = ConfigMail.getXslAdjuntos();

	/**
	 * <p>Crea el mensaje con el texto apropiado, le añade los ficheros del usuario,
	 * y lo envía al mail del usuario</p>
	 * @param usuario Objeto que representa al usuario que va a recibir el mail
	 * @throws MessagingException Posibles errores en el envío del mensaje
	 */
	public void enviarMail(Usuario usuario) throws MessagingException {
		this.pasosInicialesMail(usuario);
		this.crearTextoMail(usuario, mensaje, mp);
		this.incluirFicheros(usuario);
        Transport.send(mensaje);
	}
	
	/**
	 * <p>Borra archivos residuales derivados de la creación del fichero html que se
	 * enviará en el cuerpo del correo.</p>
	 */
	protected void borrarArchivos() {}

	/**
	 * <p>Obtiene la lista de los archivos y los va incluyendo en el mail en base
	 * a su ruta</p>
	 * @param usuario Objeto que representa al usuario que va a recibir el mail
	 * @throws MessagingException Posibles errores en el envío del mensaje
	 */
	private void incluirFicheros(Usuario usuario) throws MessagingException {
		List<Archivo> lista = usuario.getListaArchivos();
        Iterator<Archivo> i = lista.iterator();
        		
        while (i.hasNext()) {
        	Archivo aux = i.next();
        	this.adjuntarArchivo(aux.getRutaArchivo());
        }
	}
	
	/**
	 * <p>Compone el texto del mail</p>
	 * @param usuario Objeto que representa al usuario que va a recibir el mail
	 * @throws MessagingException Posibles errores en el envío del mensaje
	 */
	protected void crearTextoMail(Usuario usuario, Message mensaje, Multipart mp)
		throws MessagingException {
		
		try {
			FileOutputStream fos = new FileOutputStream(new File(super.rutaXml));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			bw.write(cabecera);
			bw.write("<mensaje>Ha recibido como adjuntos los ficheros alojados en el servidor</mensaje>");
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
