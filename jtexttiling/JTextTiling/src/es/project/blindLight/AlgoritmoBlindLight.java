package es.project.blindLight;

import java.util.ArrayList;
import java.util.Iterator;

import es.project.blindLight.estadisticos.EstadisticoPonderacion;

/**
 * <p>Clase que implementa algunos de los pasos del algoritmo blindLight: recibe un texto, obtiene
 * sus n-gramas y estima la probabilidad de éstos. Por último, les asigna sus significatividades o
 * "pesos" en base a varios estadísticos.</p>
 * @author Daniel Fernández Aller
 */
public class AlgoritmoBlindLight {
	
	private OperacionesNGrama ong;
	private EstadisticoPonderacion ep;
	private GoodTuring gt;
	
	private int sizeNGrama;
	private String ruta;
	
	private ArrayList<NGrama> listaNGramas;
	private ArrayList<DescomposicionNGrama> listaDescomposiciones;
	private ArrayList<NGrama> listaSalida;
	
	/**
	 * <p>Constructor de la clase: inicia los atributos necesarios</p>
	 * @param rutaDirectorio Ruta del directorio donde vamos a buscar el texto a tratar
	 * @param sizeNGrama Tamaño del n-grama
	 * @param ep Tipo de estadístico a utilizar para realizar el cálculo de la significatividad
	 * de los n-gramas
	 */
	public AlgoritmoBlindLight(String rutaDirectorio, int sizeNGrama, EstadisticoPonderacion ep) {
		this.ruta = rutaDirectorio;
		this.sizeNGrama = sizeNGrama;
		ong = new OperacionesNGrama();
		this.ep = ep;
	}
	
	/**
	 * <p>Método que realiza todas las operaciones del algoritmo blindLight nombradas anteriormente:
	 * obtiene los n-gramas del texto, sus frecuencias absoluta y relativa, estima su probabilidad
	 * mediante el estimador Simple Good-Turing y, por último, aplica el estadístico elegido a cada
	 * n-grama para establecer su significatividad (que es, en definitiva, nuestra meta en estos
	 * momentos).</p>
	 * @throws NGramaException Esta excepción se lanza si la longitud del texto de un n-grama no 
	 * coincide con el tamaño de n-grama estipulado 
	 */
	public void iniciarAlgoritmo() throws NGramaException {
		ong.calcular(ruta, this.sizeNGrama);
		
		gt = new GoodTuring(ong.getListaNGramas());
		gt.componerPrimerVector();
		gt.componerSegundoVector();
		
		listaNGramas = gt.cruzarListas(ong.getListaNGramas());
		ep.setN(listaNGramas.size());
		this.aplicarEstadistico();
	}
	
	/**
	 * <p>Recorre la lista que contiene los n-gramas y sus probabilidades estimadas, y 
	 * para cada uno calcula su "peso" o significatividad, mediante el estimador que haya
	 * sido indicado. Una vez realizada la operación, el n-grama se incluye en otra lista
	 * de salida. </p>
	 * @throws NGramaException Esta excepción se lanza si la longitud del texto de un n-grama no 
	 * coincide con el tamaño de n-grama estipulado 
	 */
	private void aplicarEstadistico() throws NGramaException {
		listaDescomposiciones = ong.getListaDescomposiciones();
		Iterator<NGrama> i = listaNGramas.iterator();
		NGrama aux, nuevo;
		listaSalida = new ArrayList<NGrama>();
		
		while (i.hasNext()) {
			aux = i.next();
			float probabilidad = aux.getProbabilidadEstimada();
			float resultado = ep.calcularEstadistico(aux, probabilidad, listaDescomposiciones);
			nuevo = new NGrama(aux.getTexto());
			nuevo.setSignificatividad(resultado);
			listaSalida.add(nuevo);
		}
	}
	
	/**
	 * <p>Calcula la significatividad total de una lista de n-gramas: suma las significatividades
	 * de cada uno de sus n-gramas</p>
	 * @param lista Lista de n-gramas a sumar
	 * @return Número real que almacena el valor de la significatividad de la lista de n-gramas
	 */
	public float calcularSignificatividadTotal(ArrayList<NGrama> lista) {
		float significatividad = 0.0f;
		Iterator<NGrama> i = lista.iterator();
		
		while (i.hasNext())
			significatividad += i.next().getSignificatividad();
		
		return significatividad;
	}
	
	/**
	 * <p>Implementa otro de los pasos del algoritmo blindLight, que es la intersección de la lista
	 * de n-gramas de dos documentos: el resultado será otra lista de n-gramas conteniendo los n-gramas
	 * que aparezcan en los dos documentos, y a los cuales se les atribuirá la menor significatividad
	 * (o bien la que el n-grama tiene en la primera lista, o bien la de la segunda).</p>
	 * @param lista1 Primer vector de n-gramas
	 * @param lista2 Segundo vector de n-gramas
	 * @return Devuelve otro vector con el resultado explicado anteriormente
	 */
	public ArrayList<NGrama> interseccionDocumentos(ArrayList<NGrama> lista1, ArrayList<NGrama> lista2) {
		ArrayList<NGrama> listaSalida = new ArrayList<NGrama>();
		Iterator<NGrama> i = lista1.iterator();
		NGrama aux;
		NGrama nuevo;
		
		while (i.hasNext()) {
			aux = i.next();
			nuevo = this.buscarCoincidencia(aux, lista2);
			if (nuevo != null)
				listaSalida.add(nuevo);	
		}
		
		return listaSalida;
	}
	
	/**
	 * <p>Busca si el n-grama recibido como parámetro está en la segunda lista: si está, 
	 * se elige la menor de las dos significatividades.</p>
	 * @param aux N-grama a buscar en la lista
	 * @param lista Lista donde comprobaremos si el n-grama se encuentra contenido
	 * @return Se devuelve un nuevo n-grama con la menor de las significatividades, y null
	 * si no existe la coincidencia
	 */
	private NGrama buscarCoincidencia(NGrama aux, ArrayList<NGrama> lista) {
		NGrama nuevo = null;
		Iterator<NGrama> i = lista.iterator();
		float significatividad = 0.0f;
		boolean seguirIterando = true;
		
		while (i.hasNext() && seguirIterando) {
			nuevo = i.next();
			if (nuevo.equals(aux)) {
				significatividad = Math.min(aux.getSignificatividad(), nuevo.getSignificatividad());
				nuevo.setSignificatividad(significatividad);
				seguirIterando = false;
			} else nuevo = null;
		}
		return nuevo;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve la lista que contiene los n-gramas y sus pesos
	 */
	public ArrayList<NGrama> getListaSalida() {
		return listaSalida;
	}

	/**
	 * <p>Da valor al atributo</p>
	 * @param listaSalida Valor que se le va a dar al atributo
	 */
	public void setListaSalida(ArrayList<NGrama> listaSalida) {
		this.listaSalida = listaSalida;
	}

}
