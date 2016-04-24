package es.project.blindLight.estadisticos;

import java.util.ArrayList;

import es.project.blindLight.DescomposicionNGrama;
import es.project.blindLight.NGrama;

/**
 * <p>Clase que realiza los cálculos referidos al estadístico conocido como "SI" 
 * (información mutua).
 * La fórmula puede verse <a href="http://petra.euitio.uniovi.es/~i6952349/pmwiki/pmwiki.php?n=Main.Estadisticos">aquí</a></p>
 * @author Daniel Fernández Aller
 */
public class EstadisticoSI extends EstadisticoPonderacion{
	
	/**
	 * <p>Constructor de la clase</p>
	 */
	protected EstadisticoSI() {
		super();
	}

	/**
	 * <p>Calcula la significatividad del n-grama según la fórmula explicada. Para
	 * ello, se basa en varios métodos de su clase base.</p>
	 * @param ngrama N-grama del cual daremos un cálculo del peso
	 * @param probabilidad Probabilidad estimada mediante Good-Turing del n-grama 
	 * @param listaDesc Lista de fragmentos de los n-gramas del texto
	 * @return El resultado es la significatividad (peso) del n-grama en el texto
	 * calculado mediante el estimador SI
	 */
	public float calcularEstadistico(NGrama ngrama, 
			float probabilidad, ArrayList<DescomposicionNGrama> listaDesc) {
		
		float retorno = 0.0f;
		float avp = super.calcularAvp(ngrama, listaDesc);
		retorno = (float)Math.log10(probabilidad/avp);
		return retorno;
	}
}
