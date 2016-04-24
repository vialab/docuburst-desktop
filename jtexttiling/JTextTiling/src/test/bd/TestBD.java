package test.bd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.MessagingException;

import uk.ac.man.cs.choif.nlp.seg.linear.texttile.TextTiling;

import es.project.algoritmo.configuracion.ConfigAlgoritmo;
import es.project.bd.objetos.Usuario;
import es.project.blindLight.AlgoritmoBlindLight;
import es.project.blindLight.DescomposicionNGrama;
import es.project.blindLight.GoodTuring;
import es.project.blindLight.ListaAArchivo;
import es.project.blindLight.NGrama;
import es.project.blindLight.NGramaException;
import es.project.blindLight.OperacionesNGrama;
import es.project.blindLight.estadisticos.EstadisticoPonderacion;
import es.project.blindLight.estadisticos.EstadisticoSI;
import es.project.borrarDirectorios.BorrarDirectorio;
import es.project.ficheros.configuracion.ConfigFicheros;
import es.project.ficheros.filtros.FiltroDirectorios;
import es.project.mail.Mail;
import es.project.mail.MailAlta;
import es.project.procesadorXSLT.ProcesadorXSLT;
import es.project.utilidades.ArchivoATexto;
import es.project.utilidades.QuickSort;
import es.project.utilidades.RectaRegresion;
import es.project.utilidades.RectaRegresionException;
import es.project.zip.CompresorZip;

public class TestBD {
	public TestBD() {
		long inicio = System.currentTimeMillis();
		//this.pruebaTextTiling();
		//this.pruebaCompleta();
		//this.descomponerNGramas();
		//this.blindLight();
		//this.pruebaAlgoritmo();
		//this.pruebaQuickSort();
		//this.pruebaRectaRegresion();
		//float[][]p =  new float[][]{{5,1,5,4,1},{3,2,4,5,6}};
		//System.out.println(p[0].length);
		this.pruebaCompletaGrupo();
		long finale = System.currentTimeMillis();
		
		System.out.println("tiempo: " + (finale - inicio) + " ms");
	}
	
	private void pruebaCompletaGrupo() {
		/**
		 * Obtenemos aquí la lista de ngramas de cada texto (por separado) con sus pesos/significatividades
		 * y los guardamos dentro de la carpeta \salida
		 */
		EstadisticoPonderacion ep = EstadisticoPonderacion.getEstadistico(EstadisticoPonderacion.SI);
		String rutaBaseDocumentos = "F:\\pruebasPFC\\completa\\grupo\\";
		File directorioBase = new File(rutaBaseDocumentos);
		String[] listaRutasDocumentos = directorioBase.list(new FiltroDirectorios());
		AlgoritmoBlindLight abl = null;
		ArrayList<NGrama> listasSalida[] = new ArrayList[listaRutasDocumentos.length];
		
		try {
			
			for (int i = 0; i < listaRutasDocumentos.length; i++) {
				String rutaHijos = rutaBaseDocumentos + ConfigFicheros.getSeparador() + listaRutasDocumentos[i];
				abl = new AlgoritmoBlindLight(rutaHijos,4,ep);
				abl.iniciarAlgoritmo();
				listasSalida[i] = abl.getListaSalida();
				ListaAArchivo.setFile(listasSalida[i], "F:\\pruebasPFC\\completa\\grupo\\salida\\signif_SI_" + i + ".txt");
			}
		
		/**
		 * ahora, mediante el algoritmo JTextTiling, descompondremos cada texto en sus correspondientes
		 * pasajes, que se guardarán en la carpeta \texttiling
		 */
			File directorio = new File("F:\\pruebasPFC\\completa\\grupo");
			String files[] = directorio.list(new FiltroDirectorios());
			
			for (int i = 0; i < files.length; i++) {
				TextTiling.setNombreArchivo(files[i]);
				TextTiling.setRutaArchivo("F:\\pruebasPFC\\completa\\grupo\\" + files[i]);
			
				String args[] = new String[]{"80", "20", 
						ConfigAlgoritmo.getStopwordsPath(), 
						"F:\\pruebasPFC\\completa\\grupo\\texttiling", 
						"manuel", 
						files[i]};
			
				TextTiling.main(args);
			}
			
			/**
			 * hasta el momento tenemos los tres documentos, cada uno con su lista de ngramas y los
			 * pesos de estos. También hemos troceado cada documento en pasajes. A partir de aquí ya
			 * se pueden comparar entre sí cualquier par de pasajes
			 */
			
			/**
			 * ahora compararemos dos pasajes al azar entre sí, basándonos en los pesos obtenidos
			 * para sus ngramas (que recordamos que están en la carpeta \salida). Los pasajes a
			 * comparar serán: cn_2.txt_8 y pink-floyd-award.txt_0.txt. Para obtener la lista de pesos de ngramas
			 * del primer pasaje es necesario trabajar con el fichero, obtenido previamente, signif_SI_0.txt.
			 * Para obtener la lista de pasajes del segundo debemos trabajar con signif_SI_0.txt
			 */
			
			OperacionesNGrama ong = new OperacionesNGrama();
			String ruta1 = "F:\\pruebasPFC\\completa\\grupo\\texttiling\\cn_2.txt_8.txt";
			String ruta2 = "F:\\pruebasPFC\\completa\\grupo\\texttiling\\pink-floyd-award.txt_0.txt";
			ong.calcular(ruta1, 4);
			ArrayList<NGrama> lista1 = ong.getListaNGramas();
			ong.calcular(ruta2, 4);
			ArrayList<NGrama> lista2 = ong.getListaNGramas();
			
			ArrayList<NGrama> pesosPasaje1 = ong.asignarPesoAPasaje(listasSalida[0], lista1);
			ListaAArchivo.setFile(pesosPasaje1, "F:\\pruebasPFC\\completa\\grupo\\salida\\documentoT.txt");
			ArrayList<NGrama> pesosPasaje2 = ong.asignarPesoAPasaje(listasSalida[2], lista2);
			ListaAArchivo.setFile(pesosPasaje2, "F:\\pruebasPFC\\completa\\grupo\\salida\\consultaQ.txt");
			
			ArrayList<NGrama> interseccion = abl.interseccionDocumentos(pesosPasaje1, pesosPasaje2);
			float significatividadTotal1 = abl.calcularSignificatividadTotal(pesosPasaje1);
			float significatividadTotal2 = abl.calcularSignificatividadTotal(pesosPasaje2);
			float significatividadInter = abl.calcularSignificatividadTotal(interseccion);
			
			/**
			 * la consulta Q es el texto 2, y el documento T es el texto 1
			 */
			float pi = significatividadInter/significatividadTotal2;
			float ro = significatividadInter/significatividadTotal1;
			
			System.out.println("PI: " + pi);
			System.out.println("RO: " + ro);
			ListaAArchivo.setFile(interseccion, "F:\\pruebasPFC\\completa\\grupo\\pasajes-pesos\\intersec1.8-3.0.txt");
			
		} catch (NGramaException e) {
			e.printStackTrace();
		}
	}
	
	private void pruebaCompletaUnitaria() {
		TextTiling.setNombreArchivo("gilmour");
		TextTiling.setRutaArchivo("F:\\pruebasPFC\\completa\\unitaria\\gilmour.txt");
	
		String args[] = new String[]{"70", "20", 
				ConfigAlgoritmo.getStopwordsPath(), 
				"F:\\pruebasPFC\\completa\\unitaria\\texttiling", 
				"manuel", 
				"gilmour"};
	
		TextTiling.main(args);
		
		EstadisticoPonderacion ep = EstadisticoPonderacion.getEstadistico(EstadisticoPonderacion.SI);
		AlgoritmoBlindLight abl = new AlgoritmoBlindLight("F:\\pruebasPFC\\completa\\unitaria\\gilmour.txt"
				,4,ep);
		
		try {
			abl.iniciarAlgoritmo();
			ArrayList<NGrama> lista = abl.getListaSalida();
			ListaAArchivo.setFile(lista, "F:\\pruebasPFC\\completa\\unitaria\\salida\\signif_SI.txt");
			
			OperacionesNGrama ong = new OperacionesNGrama();
			String ruta = "F:\\pruebasPFC\\completa\\unitaria\\texttiling"; 
			File directorio = new File(ruta);
			String[] listaHijos = directorio.list();
			ArrayList<NGrama> listaSignificatividades[] = new ArrayList[listaHijos.length];
			float significatividadTotal[] = new float[listaHijos.length];
			String rutaFinal;
			ArrayList<NGrama> listaNGramas;
			String nombreFichero = "";
			
			for (int i = 0; i < listaHijos.length; i++) {
				nombreFichero = listaHijos[i];
				rutaFinal = ruta + ConfigFicheros.getSeparador() + nombreFichero;
				ong.calcular(rutaFinal, 4);
				listaNGramas = ong.getListaNGramas();
				listaSignificatividades[i] = ong.asignarPesoAPasaje(lista, listaNGramas);
				significatividadTotal[i] = abl.calcularSignificatividadTotal(listaSignificatividades[i]);
				ListaAArchivo.setFile(listaSignificatividades[i], "F:\\pruebasPFC\\completa\\unitaria\\pasajes-pesos\\" + nombreFichero + ".txt");
			}
			
			ArrayList<NGrama> interseccion = 
				abl.interseccionDocumentos(listaSignificatividades[0], listaSignificatividades[2]);
			ListaAArchivo.setFile(interseccion, "F:\\pruebasPFC\\completa\\unitaria\\pasajes-pesos\\interseccion1-3.txt");

		
			float significatividadTotalInterseccion = abl.calcularSignificatividadTotal(interseccion);
			float precision = significatividadTotalInterseccion/significatividadTotal[0];
			float recall = significatividadTotalInterseccion/significatividadTotal[2];
			
			System.out.println("precisión: " + precision);
			System.out.println("recall: " + recall);
		} catch (NGramaException e) {
			e.printStackTrace();
		}
	}
	
	private void pruebaAlgoritmo() {
		EstadisticoPonderacion ep = EstadisticoPonderacion.getEstadistico(EstadisticoPonderacion.infogain);
		AlgoritmoBlindLight abl = new AlgoritmoBlindLight("F:\\pruebasPFC\\blindlight\\unitaria\\gilmour.txt"
				,4,ep);
		
		try {
			abl.iniciarAlgoritmo();
			ArrayList<NGrama> lista = abl.getListaSalida();
			/*Iterator<NGrama> i = lista.iterator();
			NGrama aux;
			
			while (i.hasNext()) {
				aux = i.next();
				System.out.println(aux.getTexto() + "|" + aux.getSignificatividad());
			}*/
			
			ListaAArchivo.setFile(lista, "F:\\pruebasPFC\\blindlight\\unitaria\\salida\\signif_Info_1.txt");
				
		} catch (NGramaException e) {
			e.printStackTrace();
		}
		
	}
	
	private void pruebaRectaRegresion() {
		float []x = new float[]{398,390,410,502,590,305,210,252,395,410,281,502};
		float []y = new float[]{40,38,42,50,60,30,20,25,40,42,30,50};
		
		try {
			RectaRegresion rr = new RectaRegresion(x,y);
			rr.calcularRectaRegresion();
			float b0 = rr.getB0();
			float b1 = rr.getB1();
			
			System.out.println("y = " + b0 + " + " + b1 + "x");
			float x0 = 250;
			System.out.println("y(" + x0 +") = " + (b0 + (b1*x0)));
			
		} catch (RectaRegresionException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void pruebaQuickSort() {
		int[][] arrayUno = new int[][]{{5,3},{6,4},{7,1},{1,4},{2,1},{23,15},{4,6},{42,10}};
		
		String retorno = "r\tn";
		for (int i = 0; i < arrayUno.length; i++) {
			retorno += "\n";
			for (int j = 0; j < arrayUno[0].length; j++)
				retorno += arrayUno[i][j] + "\t";
		}
		System.out.println(retorno);
		
		QuickSort qs = new QuickSort(arrayUno);
		qs.quicksort(0, (arrayUno.length - 1));
		qs.getArray();
	}
	
	private void pruebaCompleta() {
		File directorio = new File("F:\\pruebasPFC\\blindlight\\grupo");
		String files[] = directorio.list(new FiltroDirectorios());
		
		for (int i = 0; i < files.length; i++) {
			TextTiling.setNombreArchivo(files[i]);
			TextTiling.setRutaArchivo("F:\\pruebasPFC\\blindlight\\grupo\\" + files[i]);
		
			String args[] = new String[]{"80", "20", 
					ConfigAlgoritmo.getStopwordsPath(), 
					"F:\\pruebasPFC\\blindlight\\grupo\\texttiling", 
					"manuel", 
					files[i]};
		
			TextTiling.main(args);
		}
		
		EstadisticoPonderacion ep = EstadisticoPonderacion.getEstadistico(EstadisticoPonderacion.infogain);
		AlgoritmoBlindLight abl = new AlgoritmoBlindLight("F:\\pruebasPFC\\blindlight\\grupo\\texttiling\\"
				,4,ep);
		
		int count = 1;
		try {
			abl.iniciarAlgoritmo();
			ArrayList<NGrama> lista = abl.getListaSalida();
			/*Iterator<NGrama> i = lista.iterator();
			NGrama aux;
			
			while (i.hasNext()) {
				aux = i.next();
				System.out.println(aux.getTexto() + "|" + aux.getSignificatividad());
			}*/
			
			ListaAArchivo.setFile(lista, "F:\\pruebasPFC\\blindlight\\grupo\\salida\\signif_InfoGain_" +  count + ".txt");
				
		} catch (NGramaException e) {
			e.printStackTrace();
		}
	}
	
	private void pruebaTextTiling() {
		TextTiling.setNombreArchivo("gilmour");
		TextTiling.setRutaArchivo("F:\\pruebasPFC\\blindlight\\grupo\\gilmour.txt");
	
		String args[] = new String[]{"90", "20", 
				ConfigAlgoritmo.getStopwordsPath(), 
				"F:\\pruebasPFC\\blindlight\\grupo\\texttiling", 
				"manuel", 
				"gilmour"};
	
		TextTiling.main(args);
	}
	
	private void blindLight() {
		try {
			OperacionesNGrama ong = new OperacionesNGrama();
			ong.calcular("F:\\pruebasPFC\\blindlight\\unitaria\\gilmour.txt", 4);
			ArrayList<NGrama> lista = ong.getListaNGramas();
			ArrayList<DescomposicionNGrama> listaDesc = ong.getListaDescomposiciones();
			
			GoodTuring gt = new GoodTuring(lista);
			gt.componerPrimerVector();
			gt.componerSegundoVector();
			
			ArrayList<NGrama> listaNueva = gt.cruzarListas(lista);
			NGrama davi = listaNueva.get(0);
			float probabilidad = davi.getProbabilidadEstimada();
			
			System.out.println("prob estimada: " + probabilidad);
			EstadisticoPonderacion ep = EstadisticoPonderacion.getEstadistico(EstadisticoPonderacion.SI);
			
			//CALCULA LA SIGNIFICATIVIDAD DEL NGRAMA QUE RECIBE COMO PARÁMETRO
			System.out.println("SI_f: " + ep.calcularEstadistico(davi, probabilidad, listaDesc));
			
			ListaAArchivo.setFile(lista, "F:\\pruebasPFC\\blindlight\\unitaria\\salida\\salida.txt");
			
		} catch (NGramaException e) {
			e.printStackTrace();
		} 
	}
	
	private void descomponerNGramas() {
		try {
			OperacionesNGrama ong = new OperacionesNGrama();
			ong.calcular("F:\\pruebasPFC\\blindlight\\unitaria\\crede.txt", 4);
			ArrayList<DescomposicionNGrama> listaDesc = ong.getListaDescomposiciones();
			System.out.println(listaDesc);
		
			EstadisticoPonderacion ep = EstadisticoPonderacion.getEstadistico(EstadisticoPonderacion.SI);
			NGrama.setN(4);
			NGrama crede = new NGrama("Cred");;
			
		} catch (NGramaException e) {
			e.printStackTrace();
		} 
	}
	
	private void pruebaFiltro(){
		File directorio = new File("F:\\pruebasPFC\\blindlight\\grupo");
		String files[] = directorio.list(new FiltroDirectorios());
		System.out.println(files.length);
	}

	private void extension() {
		String cadena = "cn.txt";
		String cadena2 = "knopfler.rtf";
		
		String args[] = cadena.split("\\.");
		System.out.println(args[1]);
		
	}
	
	private void conjuntos() {
		try {
			NGrama.setN(4);
			List<NGrama> set = new LinkedList<NGrama>();
			set.add(new NGrama("hola"));
			set.add(new NGrama("_oli"));
			set.add(new NGrama("baca"));
			
			NGrama aux = new NGrama("baca");
			boolean bool = set.contains(aux);
			boolean bul = set.contains(new NGrama("_oli"));
			
			System.out.print("bool: " + bool);
			System.out.print("\nbul: " + bul);
			
		} catch (NGramaException e) {
			e.printStackTrace();
		}
	}
	
	private void borrar() {
		BorrarDirectorio bd = new BorrarDirectorio();
		bd.borrarFicheros("C:\\pruebasFicheros\\dani\\cn.txt_JTT");
	}
	
	private void comprimirDos() {
		try {
			CompresorZip cz = new CompresorZip();
			cz.comprimirArchivo("c:\\pruebasFicheros\\dani\\cn.txt_JTT", 
					"c:\\pruebasFicheros\\dani\\cn.txt_JTT.zip","dani","cn.txt_JTT.zip");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	private void comprimir() {
		File file = new File("c:\\pruebasFicheros\\temp.zip");
		
		try {
			FileInputStream entrada = new FileInputStream(new File("c:\\pruebasFicheros\\temp\\mailActivacion.dtd"));
			
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
			ZipEntry entry = new ZipEntry("prueba");
			zos.putNextEntry(entry);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(entrada));
			
			while (br.ready()) {
				zos.write(br.read());
			}
			
			br.close();
			zos.close();
			entrada.close();
			
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	private void transformarXSLT() {
		String contenido = "mailActivacion";
		String rutaBase = "./WebRoot/WEB-INF/classes/es/project/temp/mail/";
		String rutaXsl = rutaBase + contenido + ".xsl";
		String rutaXml = rutaBase + contenido + ".xml";
		String rutaHtml = rutaBase + contenido + ".html";
		
		String args[] = new String[]{rutaXsl, rutaXml, rutaHtml};
		try {
			ProcesadorXSLT.validarDocumento(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void pruebaEnvioMail() {
		Mail enviaCorreo = new MailAlta();
		Usuario usuario = new Usuario("dani","may","danimk@gmail.com");
		try {
			enviaCorreo.enviarMail(usuario);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	public static void main (String []args) {
		new TestBD();
	}
}
