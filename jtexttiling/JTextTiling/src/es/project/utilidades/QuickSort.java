package es.project.utilidades;

/**
 * <p>Clase que implementa el algoritmo de ordenación "quicksort", particularizado para
 * vectores de dos dimensiones, con n filas y dos columnas: se ordena la primera columna, 
 * y la segunda va en función de ésta. </p>
 * @author Daniel Fernández Aller
 */
public class QuickSort {
	private int arrayOrdenable[][];
	
	/**
	 * <p>Constructor de la clase: recibe el array de dos dimensiones y lo copia en otro
	 * array declarado como atributo.</p>
	 * @param array <p>Array que contiene: en la primera columna, las diferentes frecuencias
	 * observadas en la muestra; y en la segunda, el número de veces que se ha observado esa
	 * frecuencia</p>
	 */
	public QuickSort(int array[][]) {
		copiarArray(array);
	}
	
	/**
	 * <p>Realiza la copia del array elemento a elemento</p>
	 * @param array <p>Array de origen</p>
	 */
	private void copiarArray(int [][]array) {
		arrayOrdenable = new int[array.length][array[0].length];
		
		for (int i = 0; i < array.length; i++)
			for (int j = 0; j < array[0].length; j++)
				arrayOrdenable[i][j] = array[i][j];
	}
	
	/**
	 * <p>Método que implementa el algoritmo de ordenación "quicksort". El funcionamiento del
	 * algoritmo es el siguiente: 
	 * <ol>
	 * 	<li>elige un elemento, al que llamaremos pivote, y ordena los objetos de su izquierda
	 * y su derecha. En este caso, el pivote será el elemento central del array.
	 *  </li>
	 *  <li>tras ordenar los elementos de la izquierda y la derecha del pivote, la lista queda
	 * dividida en dos sublistas ordenadas (una a cada lado del pivote)
	 *  </li>
	 *  <li>repetimos el proceso recursivamente para cada una de esas sublistas. Al terminar el
	 * proceso obtenemos un array ordenado
	 *  </li>
	 * </ol>
	 * En este caso particular, también es necesario "mover" los valores de la segunda columna a
	 * la vez que se mueven los valores de la primera
	 * </p> 
	 * @param izq <p>Índice que indica el comienzo de la lista o sublista (su límite izquierdo)</p>
	 * @param der <p>Índice que indica el final de la lista o sublista (su límite derecho)</p>
	 */
	public void quicksort(int izq, int der) {
		int i = izq;
	    int j = der;
	    int pivote = arrayOrdenable[ (izq + der) / 2][0];
	    do {
	      while (arrayOrdenable[i][0] < pivote) {
	        i++;
	      }
	      while (arrayOrdenable[j][0] > pivote) {
	        j--;
	      }
	      if (i <= j) {
	        int auxR = arrayOrdenable[i][0];
	        int auxN = arrayOrdenable [i][1];
	        arrayOrdenable[i][0] = arrayOrdenable[j][0];
	        arrayOrdenable[i][1] = arrayOrdenable[j][1];
	        arrayOrdenable[j][0] = auxR;
	        arrayOrdenable[j][1] = auxN;
	        i++;
	        j--;
	      }
	    }
	    while (i <= j);
	    if (izq < j) {
	      quicksort(izq, j);
	    }
	    if (i < der) {
	      quicksort(i, der);
	    }

	}
	
	/**
	 * <p>Método de ordenación que se usa como alternativa a quicksort para listas de menor
	 * tamaño: va ordenando subsecuencias de la lista cada vez mayores</p>
	 */
	public void insercionDirecta() {
		int pivote, j, segundaColumna;
		for (int i = 1; i < arrayOrdenable.length; i++) {
			pivote = arrayOrdenable[i][0];
			segundaColumna = arrayOrdenable[i][1];
			j = i - 1;
			
			while (j >= 0 && pivote < arrayOrdenable[j][0]) {
				arrayOrdenable[j+1][0] = arrayOrdenable[j][0];
				arrayOrdenable[j+1][1] = arrayOrdenable[j][1];
				j--;
			}
			arrayOrdenable[j+1][0] = pivote;
			arrayOrdenable[j+1][1] = segundaColumna;
		}
	}
	
	/**
	 * <p>Devuelve el array una vez ordenado</p>
	 * @return <p>Array de enteros de dos dimensiones</p>
	 */
	public int[][] getArray() {
		return this.arrayOrdenable;
	}
	
	/**
	 * <p>Guarda los elementos del array en una variable y la devuelve</p>
	 */
	public String toString() {
		String retorno = "r\tn";
		for (int i = 0; i < arrayOrdenable.length; i++) {
			retorno += "\n";
			for (int j = 0; j < arrayOrdenable[0].length; j++)
				retorno += arrayOrdenable[i][j] + "\t";
		}
				
		return retorno;
	}
}
