package es.project.utilidades;

/**
 * <p>Clase que construye una recta de regresión lineal a partir de dos variables:
 * Y = b0 + b1*X.
 * La fórmula desglosada de una recta de regresión puede verse 
 * <a href="http://www.eumed.net/cursecon/medir/estima.htm">aquí</a></p>
 * @author Daniel Fernández Aller
 */
public class RectaRegresion {
	private float[] x, y;
	private float xMedia, yMedia, b0, b1;
	
	/**
	 * <p>Constructor de la clase: controla que el número de elementos en las dos
	 * variables sea el mismo, y si no, lanza una excepción. Inicializa los atributos
	 * necesarios</p>
	 * @param x Array que contiene los valores de la variable X
	 * @param y Array que contiene los valores de la variable Y
	 * @throws RectaRegresionException Se lanza si los arrays no tienen la misma longitud
	 */
	public RectaRegresion(float[] x, float[] y) throws RectaRegresionException{
		if (x.length != y.length)
			throw new RectaRegresionException(x.length, y.length);
		else {
			this.x = x;
			this.y = y;
		}
	}
	
	/**
	 * <p>Inicia los cálculos</p>
	 */
	public void calcularRectaRegresion() {
		calcularB0();
	}
	
	/**
	 * <p>Calcula el coeficiente b0, con lo cual ya quedan determinados
	 * los dos coeficientes de la recta de regresión.</p>
	 */
	private void calcularB0() {
		yMedia = this.calcularMediaArray(y);
		xMedia = this.calcularMediaArray(x);
		this.b0 = yMedia - (this.calcularB1() * xMedia);
	}
	
	/**
	 * <p>Calcula el coeficiente b1 a partir de los valores contenidos en los
	 * arrays x (variable de partida) e y (variable explicada a partir de la
	 * regresión)</p>
	 * @return Devuelve un valor real con el coeficiente b1
	 */
	private float calcularB1() {
		float aux1, aux2, aux3, aux4 = 0.0f;
		aux1 = sumatorioVariables(x,y);
		aux2 = sumatorioConMedia(x, yMedia);
		aux3 = sumatorioVariables(x, x);
		aux4 = sumatorioConMedia(x, xMedia);
		
		b1 = ((aux1 - aux2)/(aux3 - aux4));
		return b1;
	}
	
	/**
	 * <p>Realiza el cálculo de la media para la variable que se recibe como
	 * parámetro (en forma de vector de elementos)</p>
	 * @param array Array del cual se va a calcular la media
	 * @return Devuelve un real con la media del array
	 */
	private float calcularMediaArray(float[] array) {
		float aux = 0;
		for (int i = 0; i < array.length; i++)
			aux += array[i];
		
		return (aux/array.length);
	}
	
	/**
	 * <p>Realiza el cálculo del sumatorio de las dos variables: recorre
	 * los arrays, multiplica los pares de elementos y va añadiendo cada
	 * valor a una suma.</p>
	 * @param array1 Elementos de la primera variable
	 * @param array2 Elementos de la segunda variable
	 * @return Devuelve un valor real con el sumatorio de la variable X
	 * multiplicada por la variable Y
	 */
	private float sumatorioVariables(float[] array1, float[] array2) {
		float aux = 0.0f;
		for (int i = 0; i < array1.length; i++)
				aux += array1[i] * array2[i];
				             
		return aux;
	}
	
	/**
	 * <p>Calcula el sumatorio de una variable y lo multiplica por la media
	 * de la otra</>
	 * @param array Variable de la cual se va a calcular el sumatorio
	 * @param media Media de la otra variable 
	 * @return Devuelve un valor real con el resultado explicado anteriormente
	 */
	private float sumatorioConMedia(float[] array, float media) {
		float aux = 0.0f;
		for (int i = 0; i < array.length; i++)
			aux += array[i];
		
		return (aux * media);
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve el coeficiente b0 de la recta
	 */
	public float getB0() {
		return b0;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve el coeficiente b1 de la recta
	 */
	public float getB1() {
		return b1;
	}
}