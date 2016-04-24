package es.project.blindLight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>Clase que lee archivos de un tipo determinado y los convierte a listas de n-gramas</p>
 * @author Daniel Fernández Aller
 */
public class ArchivoALista {
	
	/**
	 * <p>Lee el archivo línea a línea y va creando n-gramas para luego incluirlos en la lista
	 * de salida</p>
	 * @param file Archivo a leer
	 * @return Lista que contiene los n-gramas del fichero
	 */
	public List<NGrama> getLista(File file) {
		List<NGrama> lista = new LinkedList<NGrama>();
		
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream(file)));
			
			while (br.ready()) {
				String linea[] = br.readLine().split("\\|");
				String texto = linea[0];
				/* podríamos necesitar cambiar estas llamadas para adaptarlas a los
				 * n-gramas con sus pesos */
				int frecAbsoluta = Integer.valueOf(linea[1]);
				float frecRelativa = Float.valueOf(linea[2]);
				
				NGrama aux = new NGrama(texto, frecRelativa, frecAbsoluta);
				lista.add(aux);
			}
			
			br.close();
			
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (NGramaException nge) {
			nge.printStackTrace();
		}
		
		return lista;
	}
}
