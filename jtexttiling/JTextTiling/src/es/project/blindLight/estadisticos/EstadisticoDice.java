package es.project.blindLight.estadisticos;

import java.util.ArrayList;

import es.project.blindLight.DescomposicionNGrama;
import es.project.blindLight.NGrama;

/**
 * <p>Clase que realiza los cálculos referidos al estadístico conocido como "Dice".
 * La fórmula puede verse <a href="http://petra.euitio.uniovi.es/~i6952349/pmwiki/pmwiki.php?n=Main.Estadisticos">aquí</a></p>
 * @author Daniel Fernández Aller
 */
public class EstadisticoDice extends EstadisticoPonderacion {
	
	/**
	 * <p>Constructor de la clase</p>
	 */
	protected EstadisticoDice () {
		super();
	}
	
	/**
	 * <p>Calcula la significatividad del n-grama según la fórmula explicada. Para
	 * ello, se basa en varios métodos de su clase base.</p>
	 * @param ngrama N-grama del cual daremos un cálculo del peso
	 * @param probabilidad Probabilidad estimada mediante Good-Turing del n-grama 
	 * (no se utiliza en este método)
	 * @param listaDesc Lista de fragmentos de los n-gramas del texto
	 * @return El resultado es la significatividad (peso) del n-grama en el texto
	 * calculado mediante el estimador Dice
	 */
	public float calcularEstadistico(NGrama ngrama, 
			float probabilidad, ArrayList<DescomposicionNGrama> listaDesc) {
		
		float retorno = 0.0f;
		int frecAbs = ngrama.getFrecuenciaAbsoluta();
		float numerador = 2 * frecAbs;
		
		float avx = super.calcularAvx(ngrama, listaDesc);
		float avy = super.calcularAvy(ngrama, listaDesc);
		float denominador = avx + avy;
		
		retorno = numerador/denominador;
		return retorno;
	}

}
