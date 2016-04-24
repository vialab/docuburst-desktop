package es.project.blindLight;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

/**
 * <p>Clase que recibe una lista de n-gramas con sus frecuencias y la escribe en el
 * fichero cuya ruta se obtiene como parámetro</p>
 * @author Daniel Fernández Aller
 */
public class ListaAArchivo {
	
	/**
	 * <p>Crea un java.io.BufferedWriter a partir de la ruta que recibe como parámetro
	 * y escribe en él, línea a línea, la información de los n-gramas de la siguiente forma: 
	 * texto|frecuenciaAbsoluta|frecuenciaRelativa</p>
	 * @param lista Lista de n-gramas a escribir
	 * @param rutaFichero Ruta en la cual se va a crear el fichero con la información
	 * sobre los n-gramas
	 */
	public static void setFile(List<NGrama> lista, String rutaFichero) {
		try {
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(new File(rutaFichero))));
			
			Iterator<NGrama> i = lista.iterator();
			
			while (i.hasNext()) {
				NGrama o = i.next();
				bw.write(o.toString());
				bw.newLine();
			}
			
			bw.close();
			
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
