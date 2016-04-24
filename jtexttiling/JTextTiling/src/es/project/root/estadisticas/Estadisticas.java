package es.project.root.estadisticas;

import java.util.List;

/**
 * <p>Interfaz que van a implementar las clases que permiten al root obtener las estadísticas
 * de los usuarios y los archivos</p>
 * @author Daniel Fernández Aller
 */
public interface Estadisticas {
	
	/**
	 * <p>Recorre, en cada caso, la tabla de usuarios o la de archivos, y devuelve una lista que
	 * contendrá todos los elementos</p>
	 * @return Lista de usuarios o de archivos
	 */
	public abstract List getLista();
	
	/**
	 * <p>Hace un recuento del número de usuarios o archivos, según el caso</p>
	 * @return Número total de usuarios o de archivos
	 */
	public abstract int getNum();
	
}
