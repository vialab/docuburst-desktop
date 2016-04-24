package es.project.blindLight;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import es.project.utilidades.ArchivoATexto;

/**
 * <p>Clase que recibe un fichero, obtiene sus n-gramas y sus descomposiciones, y calcula
 * las frecuencias absolutas y relativas.</p>
 * @author Daniel Fernández Aller
 */
public class OperacionesNGrama {
	private ArrayList<NGrama> listaNGramas;
	private int sizeInicialLista, sizeInicialDescomposiciones;
	private ArrayList<DescomposicionNGrama> listaDescomposiciones;
	
	/**
	 * <p>Llama a todos los métodos que realizan las operaciones explicadas anteriormente: primero
	 * calcula los n-gramas y sus frecuencias; una vez hecho, descompone esa lista de n-gramas
	 * y vuelve a calcular sus frecuencias absolutas y relativas.</p>
	 * @param ruta Ruta del fichero del cual vamos a obtener sus n-gramas y sus descomposiciones
	 * @param n Tamaño del n-grama
	 * @throws NGramaException Esta excepción se lanza si la longitud del texto de un n-grama no 
	 * coincide con el tamaño de n-grama estipulado
	 */
	public void calcular(String ruta, int n) throws NGramaException {
		this.calcularNGramas(ruta, n);
		this.calcularFrecuenciasAbsolutas();
		this.calcularFrecuenciasRelativas();
		this.descomponerLista(listaNGramas);
		this.calcularFrecuenciasAbsolutasDescomposiciones();
		this.calcularFrecuenciasRelativasDescomposiciones();
	}
	
	/**
	 * <p>Calcula una lista de n-gramas a partir del fichero dado por la ruta. Primero, formatea
	 * el texto para convertirlo en una lista de frases, de la cual se van a obtener los n-gramas.</p>
	 * @param ruta Ruta del fichero del cual vamos a obtener sus n-gramas y sus descomposiciones
	 * @param n Tamaño del n-grama
	 * @throws NGramaException Esta excepción se lanza si la longitud del texto de un n-grama no 
	 * coincide con el tamaño de n-grama estipulado
	 */
	public void calcularNGramas(String ruta, int n) throws NGramaException {
		NGrama.setN(n);
		File file = new File(ruta);
		String textoAux = "";
		FormateadorTexto ft;
		listaNGramas = new ArrayList<NGrama>();
		
		textoAux += ArchivoATexto.getTexto(file);
		ft = new FormateadorTexto(textoAux);
		String listaFrases = ft.getTexto();
		ft.calcularNGramas(listaFrases, n);
		listaNGramas.addAll(ft.getListaParcial());
		sizeInicialLista = ft.getSizeLista();
	}
	
	/**
	 * <p>Calcula la frecuencia absoluta de los n-gramas. Para ello, recorre la lista que
	 * contiene los n-gramas obtenidos y los incluye en una lista nueva, sólo si no están
	 * incluidos ya. Si el n-grama ya estaba en la nueva lista, se aumenta en 1 el valor
	 * de su frecuencia mediante el método "aumentarFrecuencia".</p>
	 */
	private void calcularFrecuenciasAbsolutas() {
		ArrayList<NGrama> listaAuxiliar = new ArrayList<NGrama>();
	
		for (int j = 0; j < listaNGramas.size(); j++) {
			NGrama aux = listaNGramas.get(j);
			if (!listaAuxiliar.contains(aux))
				listaAuxiliar.add(aux);
			else 
				this.aumentarFrecuencia(listaAuxiliar, listaNGramas.get(j));
		}
		
		this.setListaNGramas(listaAuxiliar);
	}
	
	/**
	 * <p>Busca el n-grama dentro de la lista para aumentar su frecuencia absoluta</p>
	 * @param lista Lista de n-gramas donde buscar el n-grama que se recibe como parámetro
	 * @param ngrama N-grama del cual queremos aumentar la frecuencia
	 */
	private void aumentarFrecuencia(ArrayList<NGrama> lista, NGrama ngrama) {
		for (int i = 0; i < lista.size(); i++) {
			if (lista.get(i).equals(ngrama))
				lista.get(i).aumentarFrecuenciaAbsoluta();
		}
	}
	
	/**
	 * <p>Calcula la frecuencia relativa de los n-gramas: recorre la lista y va dividiendo
	 * la frecuencia absoluta de cada n-grama por el número total de n-gramas</p>
	 */
	private void calcularFrecuenciasRelativas() {
		ArrayList<NGrama> listaAuxiliar = new ArrayList<NGrama>();
		Iterator<NGrama> i = listaNGramas.iterator();
		int size = sizeInicialLista;
		
		while (i.hasNext()) {
			NGrama aux = i.next();
			aux.setFrecuenciaRelativa(aux.getFrecuenciaAbsoluta()/(float)size);
			listaAuxiliar.add(aux);
		}
		
		this.setListaNGramas(listaAuxiliar);
	}
	
	/**
	 * <p>Descompone cada n-grama y obtiene sus fragmentos iniciales y finales, los cuales
	 * incluye en una nueva lista. Por ejemplo, el 4-grama "Davi" tendría los siguientes
	 * fragmentos iniciales: {D, Da, Dav}, y los siguientes fragmentos finales: {avi, vi, i}</p>
	 * @param lista Lista de n-gramas para descomponer 
	 */
	public void descomponerLista(ArrayList<NGrama> lista) {
		listaDescomposiciones = new ArrayList<DescomposicionNGrama>();
		Iterator<NGrama> i = lista.iterator();
		
		while (i.hasNext()) 
			this.descomponerNGrama(i.next());
	
		sizeInicialDescomposiciones = listaDescomposiciones.size();
	}
	
	/**
	 * <p>Obtiene los fragmentos iniciales y finales del n-grama que se recibe como
	 * parámetro. Una vez hecho, incluye estos fragmentos en una lista para, posteriormente,
	 * calcular sus frecuencias absolutas y relativas.</p>
	 * @param ngrama N-grama del cual vamos a obtener sus fragmentos
	 */
	private void descomponerNGrama(NGrama ngrama) {
		int longitud = ngrama.getLongitud();
		String texto = ngrama.getTexto();
		DescomposicionNGrama fragmentoInicial, fragmentoFinal;
		
		for (int i = 1; i < longitud; i++) {
			fragmentoInicial = new DescomposicionNGrama(texto.substring(0, i));
			fragmentoFinal = new DescomposicionNGrama(texto.substring(i, (longitud)));
			listaDescomposiciones.add(fragmentoInicial);
			listaDescomposiciones.add(fragmentoFinal);
		}
	}
	
	/**
	 * <p>Calcula las frecuencias absolutas de los fragmentos de n-gramas de la misma
	 * manera que se calcula la frecuencia absoluta de los n-gramas (método 
	 * "calcularFrecuenciasAbsolutas")</p>
	 */
	private void calcularFrecuenciasAbsolutasDescomposiciones() {
		ArrayList<DescomposicionNGrama> listaAuxiliar = new ArrayList<DescomposicionNGrama>();
		
		for (int i = 0; i < listaDescomposiciones.size(); i++) {
			DescomposicionNGrama aux = listaDescomposiciones.get(i);
			if (!listaAuxiliar.contains(aux))
				listaAuxiliar.add(aux);
			else 
				this.aumentarFrecuenciaDescomposicion(listaAuxiliar, listaDescomposiciones.get(i));
		}
		
		this.setListaDescomposiciones(listaAuxiliar);
	}
	
	/**
	 * <p>Busca el fragmento dentro de la lista para aumentar su frecuencia absoluta</p>
	 * @param lista Lista de fragmentos de n-gramas donde vamos a buscar el fragmento
	 * que se recibe como parámetro
	 * @param descNgrama Fragmento de n-grama del cual queremos aumentar su frecuencia 
	 * absoluta
	 */
	private void aumentarFrecuenciaDescomposicion(ArrayList<DescomposicionNGrama> lista, 
			DescomposicionNGrama descNgrama) {
		
		for (int i = 0; i < lista.size(); i++) 
			if (lista.get(i).equals(descNgrama))
				lista.get(i).aumentarFrecuenciaAbsoluta();
	}
	
	/**
	 * <p>Calcula la frecuencia relativa de cada n-grama, de la misma manera que en el método
	 * "calcularFrecuenciasRelativas"</p>
	 */
	private void calcularFrecuenciasRelativasDescomposiciones() {
		ArrayList<DescomposicionNGrama> listaAuxiliar = new ArrayList<DescomposicionNGrama>();
		Iterator<DescomposicionNGrama> i = listaDescomposiciones.iterator();
		int size = sizeInicialDescomposiciones;
		
		while (i.hasNext()) {
			DescomposicionNGrama aux = i.next();
			aux.setFrecuenciaRelativa(aux.getFrecuenciaAbsoluta()/(float)size);
			listaAuxiliar.add(aux);
		}
		
		this.setListaDescomposiciones(listaAuxiliar);
	}
	
	/**
	 * <p>Método que asigna pesos a la lista de n-gramas de un pasaje de un texto. Cada
	 * n-grama tiene un peso que se calcula previamente mediante el método Good-Turing y 
	 * la aplicación de un estadístico; estos pesos se asignan a los n-gramas correspondientes de 
	 * la lista que se recibe como parámetro.</p>
	 * @param listaPesos Lista de n-gramas con sus pesos
	 * @param nGramasPasaje Lista de n-gramas de un pasaje
	 * @return Devuelve una nueva lista con la lista de n-gramas del texto y sus pesos 
	 */
	public ArrayList<NGrama> asignarPesoAPasaje(ArrayList<NGrama> listaPesos, ArrayList<NGrama> nGramasPasaje) {
		Iterator<NGrama> i = nGramasPasaje.iterator();
		NGrama aux;
		ArrayList<NGrama> listaSalida = new ArrayList<NGrama>();
		
		while (i.hasNext()) {
			aux = i.next();
			aux.setSignificatividad(this.buscarSignificatividad(aux, listaPesos));
			listaSalida.add(aux);
		}
		return listaSalida;
	}
	
	/**
	 * <p>Busca la significatividad del n-grama dentro de la lista de pesos de los
	 * n-gramas de un texto.</p>
	 * @param ngrama N-grama del que queremos obtener su significatividad (peso)
	 * @param listaPesos Lista de pesos de los n-gramas
	 * @return Devuelve un valor real que representa el peso del n-grama dentro de
	 * un texto
	 */
	private float buscarSignificatividad(NGrama ngrama, ArrayList<NGrama> listaPesos) {
		float retorno = 0.0f;
		Iterator<NGrama> i = listaPesos.iterator();
		NGrama aux;
		
		while (i.hasNext()) {
			aux = i.next();
			if (aux.equals(ngrama)) {
				retorno = aux.getSignificatividad();
				break;
			}
		}
		
		return retorno;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve la lista de n-gramas del texto dado
	 */
	public ArrayList<NGrama> getListaNGramas() {
		return listaNGramas;
	}

	/**
	 * <p>Asigna un valor al atributo</p>
	 * @param listaNGramas Asigna valor a la lista de n-gramas 
	 */
	public void setListaNGramas(ArrayList<NGrama> listaNGramas) {
		this.listaNGramas = listaNGramas;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve un entero con el número total de n-gramas del texto
	 */
	public int getSizeInicialLista() {
		return sizeInicialLista;
	}

	/**
	 * <p>Asigna un valor al atributo</p>
	 * @param sizeInicialLista Valor a asignar al tamaño de la lista (número total
	 * de n-gramas)
	 */
	public void setSizeInicialLista(int sizeInicialLista) {
		this.sizeInicialLista = sizeInicialLista;
	}
	
	/**
	 * <p>Accede al atributo</p>
	 * @return
	 */
	public ArrayList<DescomposicionNGrama> getListaDescomposiciones() {
		return listaDescomposiciones;
	}
	
	/**
	 * <p>Asigna un valor al atributo</p>
	 * @param listaDescomposiciones
	 */
	public void setListaDescomposiciones(ArrayList<DescomposicionNGrama> listaDescomposiciones) {
		this.listaDescomposiciones = listaDescomposiciones;
	}
}
