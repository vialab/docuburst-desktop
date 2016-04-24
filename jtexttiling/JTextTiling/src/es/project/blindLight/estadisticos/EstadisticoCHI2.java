package es.project.blindLight.estadisticos;

import java.util.ArrayList;

import es.project.blindLight.DescomposicionNGrama;
import es.project.blindLight.NGrama;

/**
 * <p>Clase que realiza los cálculos referidos al estadístico conocido como "Chi-cuadrado".
 * La fórmula puede verse <a href="http://petra.euitio.uniovi.es/~i6952349/pmwiki/pmwiki.php?n=Main.Estadisticos">aquí</a></p>
 * @author Daniel Fernández Aller
 */
public class EstadisticoCHI2 extends EstadisticoPonderacion{;
	
	/**
	 * <p>Constructor de la clase</p>
	 */
	protected EstadisticoCHI2() {
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
	 * calculado mediante el estimador CHI2
	 */
	public float calcularEstadistico(NGrama ngrama, 
			float probabilidad, ArrayList<DescomposicionNGrama> listaDesc) {
		
		float retorno = 0.0f;
		int frecAbs = ngrama.getFrecuenciaAbsoluta();
		int numerador1 = frecAbs * this.getN();
		float avp = super.calcularAvp(ngrama, listaDesc);
		float restaNumerador = numerador1 - avp;
		float numeradorFinal = (float)Math.pow(restaNumerador, 2);
		
		float avx = super.calcularAvx(ngrama, listaDesc);
		float avy = super.calcularAvy(ngrama, listaDesc);
		
		float denominador1 = avp;
		float denominador2 = this.getN() - avx;
		float denominador3 = this.getN() - avy;
		float denominadorFinal = denominador1 * denominador2 * denominador3;
		
		retorno = numeradorFinal/denominadorFinal;
		return retorno;
	}
}
