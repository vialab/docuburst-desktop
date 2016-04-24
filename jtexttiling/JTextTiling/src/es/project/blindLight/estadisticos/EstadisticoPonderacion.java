package es.project.blindLight.estadisticos;

import java.util.ArrayList;
import java.util.LinkedList;

import es.project.blindLight.DescomposicionNGrama;
import es.project.blindLight.NGrama;

/**
 * <p>Clase abstracta que contiene operaciones comunes al cálculo de los diferentes
 * índices estadísticos, los cuales se utilizan para dar una aproximación del
 * peso de un n-grama en un texto.</p>
 * @author Daniel Fernández Aller
 */
public abstract class EstadisticoPonderacion {
	public static final int SI = 1;
	public static final int SCP = 2;
	public static final int chi2 = 3;
	public static final int Dice = 4;
	public static final int infogain = 5;
	
	private int N;

	/**
	 * <p>Constructor de la clase</p>
	 */
	protected EstadisticoPonderacion() {}
	
	/**
	 * <p>Realiza el cálculo del peso del ngrama que se recibe como parámetro. Se 
	 * implementa de forma particular en cada una de las clases que heredan de
	 * ésta</p>
	 * @param ngrama N-grama del cual daremos un cálculo del peso
	 * @param probabilidad Probabilidad estimada mediante Good-Turing del n-grama
	 * @param listaDesc Lista de fragmentos de los n-gramas del texto
	 * @return El resultado es la significatividad (peso) del n-grama en el texto
	 */
	public abstract float calcularEstadistico(NGrama ngrama, 
			float probabilidad, ArrayList<DescomposicionNGrama> listaDesc);
	
	/**
	 * <p>Calcula un valor en base a las probabilidades de los fragmentos iniciales
	 * y finales del n-grama. La fórmula puede verse <a href="http://petra.euitio.uniovi.es/~i6952349/pmwiki/pmwiki.php?n=Main.Estadisticos">aquí</a></p>
	 * @param ngrama N-grama del que calcularemos su valor Avp
	 * @param listaDesc Lista de fragmentos de los n-gramas del texto
	 * @return Devuelve un valor utilizado en el cálculo del peso del n-grama por los
	 * estadísticos SI, SCP y CHI2
	 */
	protected float calcularAvp(NGrama ngrama, ArrayList<DescomposicionNGrama> listaDesc) {
		float avp = 0.0f;
		int longitud = (ngrama.getLongitud() - 1);
		LinkedList<Float> listaParesProbabilidad = getParesProbabilidad(ngrama, listaDesc);

		for (int i = 0; i < listaParesProbabilidad.size(); i++) 
			avp += listaParesProbabilidad.get(i).floatValue();
		
		float retorno = avp/longitud;
		return retorno;
	}
	
	/**
	 * <p>Descompone el n-grama para obtener sus pares de fragmentos iniciales y finales, de los
	 * cuales busca su probabilidad. Una vez obtenida, multiplica estos valores y los
	 * incluye en la lista que será devuelta por el método. </p>
	 * @param ngrama N-grama del que estamos calculando su peso
	 * @param listaDesc Lista de fragmentos de los n-gramas del texto, para buscar la
	 * probabilidad de los fragmentos que se obtengan del n-grama que se recibe como
	 * parámetro
	 * @return Devuelve una lista de valores reales que son los productos de las probabilidades
	 * de cada fragmento inicial por su correspondiente fragmento final
	 */
	private LinkedList<Float> getParesProbabilidad(NGrama ngrama, ArrayList<DescomposicionNGrama> listaDesc) {
		LinkedList<Float> retorno = new LinkedList<Float>();
		float p1, p2, aux = 0.0f;
		
		int longitud = ngrama.getLongitud();
		String texto = ngrama.getTexto();
		DescomposicionNGrama fragmentoInicial, fragmentoFinal;
		
		for (int i = 1; i < longitud; i++) {
			fragmentoInicial = new DescomposicionNGrama(texto.substring(0, i));
			fragmentoFinal = new DescomposicionNGrama(texto.substring(i, (longitud)));
			p1 = this.buscarCaracteristica(fragmentoInicial, listaDesc, true);
			p2 = this.buscarCaracteristica(fragmentoFinal, listaDesc, true);
			aux = p1 * p2;
			retorno.add(aux);
		}
		return retorno;
	}
	
	/**
	 * <p>Busca en la lista de fragmentos la frecuencia, absoluta o relativa, del fragmento
	 * que se recibe como parámetro. Este método es utilizado en varios casos, dependiendo de
	 * si es necesario obtener la frecuencia absoluta del fragmento de n-grama o su probabilidad
	 * de aparición.</p>
	 * @param desc Fragmento de un n-grama
	 * @param listaDesc Lista de fragmento de n-gramas del texto
	 * @param frecRelativa Variable que indica si estamos buscando la frecuencia
	 * relativa (true) o la frecuencia absoluta (false) del fragmento
	 * @return Devuelve un valor que será, dependiendo del valor del parámetro frecRelativa,
	 * la frecuencia relativa o la frecuencia absoluta del fragmento
	 */
	protected float buscarCaracteristica(DescomposicionNGrama desc, ArrayList<DescomposicionNGrama> listaDesc,
			boolean frecRelativa) {
		float retorno = 0.0f;
		for (int i = 0; i < listaDesc.size(); i++) {
			DescomposicionNGrama aux = listaDesc.get(i);
			if (desc.equals(aux)) {
				if (frecRelativa)
					retorno = aux.getFrecuenciaRelativa();
				else retorno = aux.getFrecuenciaAbsoluta();
				
				i = (listaDesc.size() + 1);
			}
		}
		return retorno;
	}
	
	/**
	 * <p>Calcula un valor en base a las frecuencias absolutas de los fragmentos
	 * iniciales del n-grama dado</p>
	 * @param ngrama N-grama del que estamos calculando su peso
	 * @param listaDesc Lista de fragmentos de los n-gramas del texto
	 * @return Devuelve un valor utilizado en el cálculo del peso del n-grama por los
	 * estadísticos CHI2 y Dice
	 */
	public float calcularAvx(NGrama ngrama, ArrayList<DescomposicionNGrama> listaDesc) {
		return this.calculoInterno(ngrama, listaDesc, true);
	}
	
	/**
	 * <p>Calcula un valor en base a las frecuencias absolutas de los fragmentos
	 * finales del n-grama dado</p>
	 * @param ngrama N-grama del que estamos calculando su peso
	 * @param listaDesc Lista de fragmentos de los n-gramas del texto
	 * @return Devuelve un valor utilizado en el cálculo del peso del n-grama por los
	 * estadísticos CHI2 y Dice
	 */
	public float calcularAvy(NGrama ngrama, ArrayList<DescomposicionNGrama> listaDesc) {
		return this.calculoInterno(ngrama, listaDesc, false);
	}
	
	/**
	 * <p>Método utilizado para calcular los valores Avx y Avy, según sea necesario (se
	 * distingue mediante el parámetro esAvx). Este valor es el resultado de sumar la
	 * frecuencia absoluta de los fragmentos (iniciales o finales), y dividirlo por la
	 * longitud del n-grama - 1. La fórmula puede verse <a href="http://petra.euitio.uniovi.es/~i6952349/pmwiki/pmwiki.php?n=Main.Estadisticos">aquí</a></p>
	 * @param ngrama N-grama del que estamos calculando su peso
	 * @param listaDesc Lista de fragmentos de los n-gramas del texto
	 * @param esAvx Si es true, calcula el valor avx, y si es false el valor avy 
	 * @return Devuelve un valor utilizado en el cálculo del peso del n-grama por los
	 * estadísticos CHI2 y Dice
	 */
	private float calculoInterno(NGrama ngrama, ArrayList<DescomposicionNGrama> listaDesc, boolean esAvx) {
		float retorno = 0.0f;
		int longitud = (ngrama.getLongitud() - 1);
		float sumaFrecuencias = this.getSumaFrecuencias(ngrama, listaDesc, esAvx);
		retorno = (float)sumaFrecuencias/longitud;
		return retorno;
		
	}
	
	/**
	 * <p>Calcula la suma de frecuencias absolutas de los fragmentos iniciales, o finales del
	 * n-grama, según lo indicado por el parámetro esAvx</p>
	 * @param ngrama N-grama del que estamos calculando su peso
	 * @param listaDesc Lista de fragmentos de los n-gramas del texto
	 * @param esAvx Si es true, calcula el valor avx, y si es false el valor avy
	 * @return Devuelve la suma de las frecuencias absolutas de los fragmentos iniciales
	 * o finales, según indique el parámetro
	 */
	public float getSumaFrecuencias (NGrama ngrama, ArrayList<DescomposicionNGrama> listaDesc, boolean esAvx) {
		float sumaFrecuencias = 0;
		int longitud = ngrama.getLongitud();
		String texto = ngrama.getTexto();
		DescomposicionNGrama desc;
		
		for (int i = 1; i < longitud; i++) {
			if (esAvx) 
				desc = new DescomposicionNGrama(texto.substring(0, i));
			else desc = new DescomposicionNGrama(texto.substring(i, (longitud - 1)));
			
			sumaFrecuencias += this.buscarCaracteristica(desc, listaDesc, false);
		}
		return sumaFrecuencias;
	}
	
	/**
	 * <p>Construye un objeto del método estadístico que se indique según el
	 * parámetro</p>
	 * @param estadistico Índice que representa al estadístico que queremos 
	 * utilizar. Los valores vienen dados por unas variables estáticas finales
	 * @return Devuelve un objeto derivado de la clase que modela el estadístico
	 * requerido
	 */
	public static EstadisticoPonderacion getEstadistico(int estadistico) {
		switch (estadistico) {
			case SI:
				return new EstadisticoSI();
		
			case SCP:
				return new EstadisticoSCP();
		
			case chi2:
				return new EstadisticoCHI2();
			
			case Dice:
				return new EstadisticoDice();
			
			case infogain:
				return new EstadisticoInfoGain();
		}
		return null;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve el número de n-gramas diferentes en el documento
	 */
	public int getN() {
		return N;
	}

	/**
	 * <p>Asigna un valor al atributo</p>
	 * @param n Valor a asignar
	 */
	public void setN(int n) {
		N = n;
	}
}

