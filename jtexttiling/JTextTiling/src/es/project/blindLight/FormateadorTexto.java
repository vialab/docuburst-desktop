package es.project.blindLight;

import java.util.ArrayList;

/**
 * <p>Recibe un texto y lo formatea según las siguientes premisas:
 * <ul>
 * 	<li>Los espacios tales como espacios en blanco, tabuladores, saltos de línea, etc, se
 * sustituyen por espacios en blanco. Esto es: el documento será una única línea cuyas palabras
 * estarán separadas por un espacio en blanco.
 *  </li>
 *  <li>Los separadores (símbolos de puntuación) se sustituyen por el carácter '>'
 *  </li>
 * </ul>
 * </p>
 * @author Daniel Fernández Aller, basado en código de Eugenia Pérez, Saúl González y Miguel Martínez
 */
public class FormateadorTexto {
	
	private String texto;
	private ArrayList<NGrama> listaParcial;
	private int sizeLista;
	
	/**
	 * <p>Constructor de la clase: inicializa los atributos necesarios y arranca la ejecución del
	 * método "formatearTexto"</p>
	 * @param texto Texto a formatear
	 */
	public FormateadorTexto(String texto) {
		this.texto = texto;
		sizeLista = 0;
		this.formatearTexto();
	}
	
	/**
	 * <p>Crea una nueva lista</p>
	 */
	private void inicializarLista() {
		listaParcial = new ArrayList<NGrama>();
	}
	
	/**
	 * <p>Convierte el texto de manera que pasa a ser una única línea donde todos los espacios
	 * tales como saltos de línea, tabulaciones, etc, pasan a ser espacios en blanco; los signos
	 * de puntuación pasan a ser el carácter '>'</p>
	 */
	private void formatearTexto() {
        boolean beginning = true;
        char c = ' ';
        char buffer = 0;
        StringBuffer result = new StringBuffer(texto.length());

        for (int i=0; i<texto.length(); i++) {
            c = texto.charAt(i); 
            if((c & 0x80)== 0){ //ascii
                if(esEspacio(c)){
                    if(buffer!='>')
                        buffer = ' ';
            }else{
                if(esSeparador(c))
                    buffer = '>';
                else{
                    if((buffer!=0) && (beginning==false))
                    {
                        result.append(buffer);
                        buffer = 0;
                    }
                    if (beginning)
                    {
                        beginning = false;
                        buffer = 0;
                    }
                    result.append(c);
                  }
                }
            }else{//extendido
                if((buffer!=0) && (beginning==false))
                {
                   result.append(buffer);
                   buffer = 0;
                }
                if(beginning)
                {
                    beginning = false;
                    buffer = 0;
                }
               result.append(c); 
            }  
          }//end_while
        texto = result.toString();
    }//end formatearTexto

	/**
	 * <p>Obtiene la lista de frases una vez el texto ha sido formateado. Una frase viene
	 * determinada por los signos de puntuación</p>
	 * @return Devuelve un array de String`s con tantas cadenas como frases haya en el texto
	 */
	public String[] getListaFrases() {
		String[] lista = texto.split("\\>");
		return lista;
	}	
	
	/**
	 * <p>Indica si el carácter recibido es espaciador</p>
	 * @param c Carácter a comprobar
	 * @return Verdadero si el carácter es espaciador
	 */
	private boolean esEspacio(int c) {
		return ((c>=7)&&(c<=13)||(c==32));
    }
	
	/**
	 * <p>Indica si el carácter recibido es separador</p>
	 * @param c Carácter a comprobar
	 * @return Verdadero si el carácter es separador
	 */
	private boolean esSeparador(int c) {
		if(esEspacio(c))
            return false;
        else{
           return ((c<=31)||((c>=33)&&(c<=38))||
           ((c>=40)&&(c<=44))||((c>=46)&&(c<=47))||
           ((c>=58)&&(c<=63))||((c>=91)&&(c<=96))||
           ((c>=123)&&(c<=191))||((c==215)||(c==247)));
        }
	}
	
	/**
	 * <p>Añade un n-grama a la lista, para lo cual previamente se debe controlar que el
	 * texto del n-grama esté bien formado. Una vez hecha la comprobación, se modifica el
	 * texto del n-grama para cambiar los espacios en blanco por el carácter '_'</p>
	 * @param ngrama N-grama a incluir en la lista
	 * @param n Tamaño del n-grama
	 */
	private void addNGrama(NGrama ngrama, int n) {
		if (esValido(ngrama, n)) {
			NGrama aux = modificarEspacios(ngrama);
			listaParcial.add(aux);
			this.aumentarSizeLista();
		}
	}
	
	/**
	 * <p>Comprueba si un n-grama es válido, lo cual supone que el carácter '>'
	 * sólo puede ser el primero, ya que de otra manera supondría que el ngrama
	 * ha saltado de una frase a otra</p>
	 * @param ngrama N-grama a comprobar
	 * @param n Tamaño del n-grama
	 * @return Verdadero si es válido y falso en caso contrario
	 */
	private boolean esValido(NGrama ngrama,int n) {
		for (int i = n - 1; i >= 1; i--) {
			if (ngrama.getTexto().charAt(i) == '>')
				return false;
		}
		return true;
	}
	
	/**
	 * <p>Modifica los espacios del n-grama para convertirlos todos en el carácter '_'</p>
	 * @param ngrama N-Grama a modificar
	 * @return Devuelve el n-grama con los espacios modificados
	 */
	private NGrama modificarEspacios (NGrama ngrama) {
		String texto = ngrama.getTexto();
		String modificado1 = texto.replace(' ', '_');
		String modificado2 = modificado1.replace('>', '_');
		ngrama.setTexto(modificado2);
		return ngrama;
	}

	/**
	 * <p>Recibe la frase y la descompone en n-gramas del tamaño indicado como
	 * parámetro. Una vez obtenido el n-grama, llama al método "addNGrama" para que
	 * se compruebe si es válido y que trate los espacios en blanco. </p>
	 * @param frase Frase a descomponer
	 * @param n Tamaño del n-grama
	 * @throws NGramaException Esta excepción se lanza si la longitud del texto de un n-grama no 
	 * coincide con el tamaño de n-grama estipulado
	 */
	public void calcularNGramas(String frase, int n) throws NGramaException {
		NGrama.setN(n);
		this.inicializarLista();
		String textoNGrama;
		NGrama aux;
	
		int inicio;
		int tope;
		
		for (int i = 0; i < frase.length(); i++) {
			textoNGrama = "";
			inicio = i;
			tope = inicio + n;
			
			for (inicio = i; inicio < tope; inicio++) {
				if (tope <= frase.length()) {
					textoNGrama += frase.charAt(inicio);
					
					if ((textoNGrama.length() == n)) {
						aux = new NGrama(textoNGrama);
						this.addNGrama(aux, n);
					}
				}
			}
		}
	}
	
	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve el texto a formatear
	 */
	public String getTexto() {
		return texto;
	}

	/**
	 * <p>Da valor al atributo</p>
	 * @param texto Cadena a asignar al atributo
	 */
	public void setTexto(String texto) {
		this.texto = texto;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve la lista de n-gramas obtenidos
	 */
	public ArrayList<NGrama> getListaParcial() {
		return listaParcial;
	}
	
	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve el tamaño de la lista
	 */
	public int getSizeLista() {
		return sizeLista;
	}
	
	/**
	 * <p>Aumenta en uno el tamaño de la lista</p>
	 */
	public void aumentarSizeLista() {
		sizeLista++;
	}

	/**
	 * <p>Da valor al atributo</p>
	 * @param listaParcial Lista a asignar al atributo
	 */
	public void setListaParcial(ArrayList<NGrama> listaParcial) {
		this.listaParcial = listaParcial;
	}
}
