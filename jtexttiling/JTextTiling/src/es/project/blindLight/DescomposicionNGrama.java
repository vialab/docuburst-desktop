package es.project.blindLight;

/**
 * <p>Clase que modela a los "fragmentos" de los n-gramas, lo cual es necesario para calcular
 * el valor de varios estadísticos. </p>
 * @author Daniel Fernández Aller
 */
public class DescomposicionNGrama {
	private String texto;
	private int frecuenciaAbsoluta;
	private float frecuenciaRelativa;
	
	/**
	 * <p>Constructor de la clase: asigna al fragmento un texto y una frecuencia absoluta = 1</p>
	 * @param texto Texto a asignar
	 */
	public DescomposicionNGrama(String texto) {
		this.texto = texto;
		this.frecuenciaAbsoluta = 1;
	}
	
	/**
	 * <p>Constructor de la clase: asigna al fragmento un texto y una frecuencia absoluta</p>
	 * @param texto Texto a asignar
	 * @param frecuenciaAbsoluta Frecuencia absoluta a asignar
	 */
	public DescomposicionNGrama (String texto, int frecuenciaAbsoluta) {
		this(texto);
		this.frecuenciaAbsoluta = frecuenciaAbsoluta;
	}
	
	/**
	 * <p>Constructor de la clase: asigna al fragmento un texto y una frecuencia relativa</p>
	 * @param texto Texto a asignar
	 * @param frecuenciaRelativa Frecuencia relativa a asignar
	 */
	public DescomposicionNGrama (String texto, float frecuenciaRelativa) {
		this(texto);
		this.frecuenciaRelativa = frecuenciaRelativa;
	}
	
	/**
	 * <p>Aumenta en 1 el valor de la frecuencia absoluta del fragmento</p>
	 */
	public void aumentarFrecuenciaAbsoluta() {
		this.frecuenciaAbsoluta++;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve el texto del fragmento 
	 */
	public String getTexto() {
		return texto;
	}

	/**
	 * <p>Da valor al atributo texto</p>
	 * @param texto Valor a asignar al atributo
	 */
	public void setTexto(String texto) {
		this.texto = texto;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve la frecuencia absoluta del fragmento
	 */
	public int getFrecuenciaAbsoluta() {
		return frecuenciaAbsoluta;
	}

	/**
	 * <p>Da valor al atributo frecuenciaAbsoluta</p>
	 * @param frecuenciaAbsoluta Valor a asignar al atributo
	 */
	public void setFrecuenciaAbsoluta(int frecuenciaAbsoluta) {
		this.frecuenciaAbsoluta = frecuenciaAbsoluta;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve la frecuencia relativa del fragmento
	 */
	public float getFrecuenciaRelativa() {
		return frecuenciaRelativa;
	}

	/**
	 * <p>Da valor al atributo frecuenciaRelativa</p>
	 * @param frecuenciaRelativa Valor a asignar al atributo
	 */
	public void setFrecuenciaRelativa(float frecuenciaRelativa) {
		this.frecuenciaRelativa = frecuenciaRelativa;
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
		if (o instanceof DescomposicionNGrama) {
			DescomposicionNGrama aux = (DescomposicionNGrama)o;
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
	 * <p>Muestra los atributos del objeto</p>
	 * @return Devuelve una cadena con los atributos del objeto
	 */
	public String toString() {
		return this.getTexto() + "|" + this.getFrecuenciaAbsoluta() + "|" + this.getFrecuenciaRelativa() + "\n";
	}
}
