package es.project.root.estadisticas;

import java.util.List;

import es.project.bd.objetos.Archivo;
import es.project.facade.FacadeBD;

/**
 * <p>Clase que implementa la interfaz "Estadisticas" y permite al usuario administrador
 * obtener la lista completa de los archivos de los usuarios y el número de éstos</p>
 * @author Daniel Fernández Aller
 */
public class EstadisticasArchivos implements Estadisticas{
	
	private FacadeBD facadeBD = new FacadeBD();
	
	/**
	 * <p>Devuelve una lista conteniendo todos los archivos presentes en la aplicación</p>
	 */
	public List<Archivo> getLista() {
		return facadeBD.getTodosArchivos();
	}

	/**
	 * <p>Devuelve el número total de archivos presentes en la aplicación</p>
	 */
	public int getNum() {
		return facadeBD.getNumeroArchivos();
	}
}
