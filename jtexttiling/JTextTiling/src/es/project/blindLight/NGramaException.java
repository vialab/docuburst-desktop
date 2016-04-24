package es.project.blindLight;

/**
 * <p>Excepción que se lanza en el momento de llamar al constructor de un n-grama si la longitud
 * del texto del n-grama no coincide con la longitud indicada por su atributo "n"</p>
 * @author Daniel Fernández Aller
 */
public class NGramaException extends Exception {
	private static final long serialVersionUID = -1;
	
	/**
	 * <p>Constructor de la clase: se lanza cuando la longitud del texto del n-grama no 
	 * coincide con su atributo "n"</p>
	 * @param texto Texto del n-grama
	 * @param n Longitud del n-grama
	 */
	public NGramaException(String texto, int n) {
		super("El n-grama '" + texto + "' tiene una longitud diferente de la especificada: " + n);
	}
}
