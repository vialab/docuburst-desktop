package es.project.borrarDirectorios;

import java.io.File;

import es.project.ficheros.configuracion.ConfigFicheros;

/**
 * <p>Clase que borra el contenido del fichero o directorio que se recibe como parámetro,
 * borrando recursivamente si es necesario, los ficheros y directorios contenidos dentro
 * del directorio base</p>
 * @author Daniel Fernández Aller
 */
public class BorrarDirectorio {
	
	/**
	 * <p>Borra físicamente los archivos del usuario (si es que tiene): a partir de la ruta 
	 * base, obtiene la lista de ficheros y directorios y, para cada uno de ellos realiza 
	 * la siguiente operación:
	 * <ul>
	 * 	<li>si es un fichero, lo borra</li>
	 * 	<li>si es un directorio, hace una llamada recursiva en la cual se tomará como ruta
	 * la ruta de este directorio</li>
	 * </ul>
	 * </p>
	 * @param ruta Ruta base a partir de la cual se encuentran los archivos del usuario
	 * @return Verdadero si los borrados se realizaron con éxito, falso en caso contrario
	 */
	private boolean borrar(String ruta) {
		File inicial = new File(ruta);
		String[] lista = inicial.list();
		
		if (lista != null) {
		
			for(int i = 0; i < lista.length; i++) {
				String rutaNueva = ruta + ConfigFicheros.getSeparador() + lista[i];
				File aux = new File(rutaNueva);
				
				/* si el objeto File es un directorio, tenemos que actuar recursivamente */
				if (aux.isDirectory()) 
					borrar(rutaNueva);
				/* si es un archivo, se borra directamente */ 
				else 
					aux.delete();
			}
		}
		/* finalmente, borramos el directorio que ya estára vacío */
		return inicial.delete();
	}
	
	/**
	 * <p>Interfaz pública para la operación de borrado de ficheros/directorios: accede al método
	 * privado pasándole la ruta como parámetro</p>
	 * @param ruta Ruta del fichero/directorio a borrar
	 */
	public boolean borrarFicheros(String ruta) {
		return this.borrar(ruta);
	}
	
	/**
	 * <p>Interfaz pública para la operación de borrado de ficheros/directorios: recibe como parámetro
	 * un java.io.File y obtiene su ruta</p>
	 * @param file java.io.File
	 */
	public boolean borrarFicheros(File file) {
		return this.borrarFicheros(file.getAbsolutePath());
	}
}
