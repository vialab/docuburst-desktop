package es.project.utilidades;

/**
 * <p>Excepción lanzada en el momento de construir una recta de regresión si la longitud
 * de los arrays que almacenan las variables no coincide</p>
 * @author Daniel Fernández Aller
 */
public class RectaRegresionException extends Exception {
	private static final long serialVersionUID = -1;
	
	/**
	 * <p>Constructor de la clase: se lanza si el valor de los parámetros no coincide</p>
	 * @param length1 Longitud del primer vector
	 * @param length2 Longitud del segundo vector
	 */
	public RectaRegresionException(int length1, int length2) {
		super("Los arrays tienen longitudes diferentes: " + length1 + " != " + length2);
	}

}
