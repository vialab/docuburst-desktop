package es.project.ficheros.filtros;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.Set;

import es.project.algoritmo.configuracion.ConfigAlgoritmo;

/**
 * <p>Clase que implementa un filtro para controlar las extensiones de los
 * archivos que se permiten en el uso del algoritmo JTextTiling. Esta clase 
 * se utiliza cuando se quieren obtener los ficheros presentes en un directorio 
 * al que se va a aplicar el algoritmo, y se necesita filtrar ficheros de
 * otras extensiones o incluso subdirectorios presentes en el directorio.</p>
 * @author Daniel Fernández Aller
 */
public class FiltroDirectorios implements FilenameFilter{
	private Set<String> extensiones = ConfigAlgoritmo.getExtensiones();
	
	/**
	 * <p>Devuelve verdadero si el objeto file tiene una extensión compatible
	 * con las permitidas.</p>
	 */
	public boolean accept (File file, String cadena) {
		Iterator<String> i = extensiones.iterator();
		String extension = "";
		
		while (i.hasNext()) {
			extension = i.next();
			if (cadena.endsWith(extension))
				return true;
		}
		return false;
	}

}
