package es.project.blindLight;

/**
 * <p>Clase que modela las propiedades de los n-gramas (secuencias de n caracteres correlativos 
 * de la misma frase).</p>
 * @author Daniel Fernández Aller
 */
public class NGrama {
	
	private String texto;
	private float frecuenciaRelativa, probabilidadEstimada, significatividad;
	private int frecuenciaAbsoluta;
	private static int n = -1;
	
	/**
	 * <p>Constructor de la clase: inicializa los atributo y comprueba que el texto del n-grama 
	 * tenga la longitud indicada en su atributo "n". </p>
	 * @param texto Texto del n-grama
	 * @throws NGramaException Esta excepción se lanza si la longitud del texto de un n-grama no 
	 * coincide con el tamaño de n-grama estipulado 
	 */
	public NGrama(String texto) throws NGramaException {
		if (texto.length() != this.getN()) 
			throw new NGramaException(texto, n);
		else {
			this.texto = texto;
			this.frecuenciaAbsoluta = 1;
		}
	}
	
	/**
	 * <p>Constructor de la clase: inicializa los atributo y comprueba que el texto del n-grama 
	 * tenga la longitud indicada en su atributo "n". </p> 
	 * @param texto Texto del n-grama
	 * @param frecuenciaRelativa Frecuencia relativa de aparición del n-grama en un texto
	 * @param frecuenciaAbsoluta Frecuencia absoluta de aparición del n-grama en un texto
	 * @throws NGramaException Esta excepción se lanza si la longitud del texto de un n-grama no 
	 * coincide con el tamaño de n-grama estipulado 
	 */
	public NGrama(String texto, float frecuenciaRelativa, int frecuenciaAbsoluta) 
		throws NGramaException{
		
		this(texto, frecuenciaRelativa);
		this.frecuenciaAbsoluta = frecuenciaAbsoluta;
	}
	
	/**
	 * <p>Constructor de la clase: inicializa los atributo y comprueba que el texto del n-grama 
	 * tenga la longitud indicada en su atributo "n". </p> 
	 * @param texto Texto del n-grama
	 * @param probabilidadEstimada Probabilidad estimada del n-grama en un texto 
	 * @throws NGramaException Esta excepción se lanza si la longitud del texto de un n-grama no 
	 * coincide con el tamaño de n-grama estipulado
	 */
	public NGrama(String texto, float probabilidadEstimada) throws NGramaException {
		this(texto);
		this.probabilidadEstimada = probabilidadEstimada;
	}
	
	/**
	 * <p>Aumenta en 1 la frecuencia absoluta del n-grama</p>
	 */
	public void aumentarFrecuenciaAbsoluta() {
		this.frecuenciaAbsoluta++;
	}
	
	/**
	 * <p>Determina si el objeto actual (this) y el que se recibe como parámetro, son iguales.
	 * Para determinar si son iguales, primero hay que saber si el recibido como parámetro es
	 * una instancia de esta clase, y si lo es, comprobar que tenga el mismo texto que el
	 * objeto actual.</p>
	 * @param o Objeto a comparar con el actual 
	 * @return Verdadero si son iguales, falso en caso contrario
	 */
	public boolean equals(Object o) {
		if (o instanceof NGrama) {
			NGrama aux = (NGrama)o;
			if(this.getTexto().compareTo(aux.getTexto()) == 0)
				return true;
			else return false;
		} 
		else 
			return false;
	}
	
	/**
	 * <p>Calcula el código hash del texto del objeto actual</p>
	 * @return Devuelve un número entero con el valor del código hash del texto del objeto actual
	 */
	public int hashCode() {
		return this.getTexto().hashCode();
	}
	
	/**
	 * <p>Concatena el texto y las propiedades del n-grama separadas por el carácter '|'</p>
	 * @return Devuelve una cadena con el texto explicado
	 */
	public String verFrecuencias() {
		return this.getTexto() + "|" + this.getFrecuenciaAbsoluta() + "|" + this.getFrecuenciaRelativa() +
		"|" + this.getProbabilidadEstimada();
	}
	
	/**
	 * <p>Concatena el texto del n-grama y su significatividad separados por el carácter '|'</p>
	 * @return Devuelve una cadena con el texto explicado
	 */
	public String toString() {
		return this.getTexto() + "|" + this.getSignificatividad();
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve la longitud del n-grama
	 */
	public static int getN() {
		return n;
	}

	/**
	 * <p>Asigna valor al atributo</p>
	 * @param valor Longitud a asignar los n-gramas
	 */
	public static void setN(int valor) {
		n = valor;
	}
	
	/**
	 * <p>Accede al atributo</p>
	 * @return Longitud del texto del n-grama
	 */
	public int getLongitud() {
		return this.getTexto().length();
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Texto del n-grama
	 */
	public String getTexto() {
		return texto;
	}

	/**
	 * <p>Asigna valor al atributo</p>
	 * @param texto Texto a asignar al n-grama
	 */
	public void setTexto(String texto) {
		this.texto = texto;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve la frecuencia absoluta del n-grama dentro de un texto
	 */
	public int getFrecuenciaAbsoluta() {
		return frecuenciaAbsoluta;
	}

	/**
	 * <p>Asigna un valor al atributo</p>
	 * @param frecuenciaAbsoluta Frecuencia absoluta a asignar al n-grama
	 */
	public void setFrecuenciaAbsoluta(int frecuenciaAbsoluta) {
		this.frecuenciaAbsoluta = frecuenciaAbsoluta;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve la frecuencia relativa del n-grama dentro de un texto
	 */
	public float getFrecuenciaRelativa() {
		return frecuenciaRelativa;
	}

	/**
	 * <p>Asigna un valor al atributo</p>
	 * @param frecuenciaRelativa Frecuencia relativa a asignar al n-grama
	 */
	public void setFrecuenciaRelativa(float frecuenciaRelativa) {
		this.frecuenciaRelativa = frecuenciaRelativa;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve la probabilidad estimada del n-grama dentro de un texto
	 */
	public float getProbabilidadEstimada() {
		return probabilidadEstimada;
	}

	/**
	 * <p>Asigna un valor al atributo</p>
	 * @param probabilidadEstimada Probabilidad estimada a asignar al atributo
	 */
	public void setProbabilidadEstimada(float probabilidadEstimada) {
		this.probabilidadEstimada = probabilidadEstimada;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve la significatividad del n-grama dentro de un texto
	 */
	public float getSignificatividad() {
		return significatividad;
	}

	/**
	 * <p>Asigna un valor al atributo</p>
	 * @param significatividad Significatividad a asignar al atributo
	 */
	public void setSignificatividad(float significatividad) {
		this.significatividad = significatividad;
	}
}
