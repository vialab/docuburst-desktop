package test.bd.archivos;

import es.project.bd.objetos.Archivo;
import es.project.bd.objetos.Usuario;
import es.project.facade.FacadeBD;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestActualizarArchivos extends TestCase{
	private FacadeBD facadeBD;
	
	private final String testActualizarNombreArchivo_1 = 
		"No almacena correctamente la información sobre los NOMBRES de archivos";
	private final String testActualizarNombreArchivo_2 = "No actualiza correctamente el NOMBRE del archivo";
	private final String testActualizarRutaArchivo_1 = 
		"No almacena correctamente la información sobre las RUTAS de archivos";
	private final String testActualizarRutaArchivo_2 = "No actualiza correctamente la RUTA del archivo";
	
	
	private Usuario usuario1 = new Usuario("nombre1","pass1","mail1");
	private Archivo archivo1 = new Archivo("archivo1",usuario1.getNombre(),"c:\\ruta");
	
	public void setUp() {
		facadeBD = new FacadeBD();
		this.insertar();
	}
	
	public void tearDown(){
		this.borrar();
		facadeBD.tearDown();
		facadeBD = null;
	}
	
	public void testActualizarNombreArchivo() {
		Archivo auxiliar = 
			facadeBD.getArchivo(archivo1.getNombreArchivo(), archivo1.getNombrePropietario());
		assertTrue(this.testActualizarNombreArchivo_1, 
				auxiliar.getNombreArchivo().compareToIgnoreCase(archivo1.getNombreArchivo()) == 0);
		
		String nombreArchivoNuevo = "nuevoNombreArchivo";
		facadeBD.actualizarNombreArchivo(archivo1, nombreArchivoNuevo);
		auxiliar = facadeBD.getArchivo(nombreArchivoNuevo, archivo1.getNombrePropietario());
		assertTrue(this.testActualizarNombreArchivo_2,
				auxiliar.getNombreArchivo().compareToIgnoreCase(nombreArchivoNuevo) == 0);
	}
	
	public void testActualizarRutaArchivo() {
		Archivo auxiliar = 
			facadeBD.getArchivo(archivo1.getNombreArchivo(), archivo1.getNombrePropietario());
		assertTrue(this.testActualizarRutaArchivo_1,
				auxiliar.getRutaArchivo().compareToIgnoreCase(archivo1.getRutaArchivo()) == 0);
		
		String rutaNueva = "d:\\rutaNueva";
		facadeBD.actualizarRutaArchivo(archivo1, rutaNueva);
		auxiliar = facadeBD.getArchivo(archivo1.getNombreArchivo(), archivo1.getNombrePropietario());
		assertTrue(this.testActualizarRutaArchivo_2,
				auxiliar.getRutaArchivo().compareToIgnoreCase(rutaNueva) == 0);
	}
	
	public void insertar() {
		facadeBD.insertarUsuario(usuario1);
		facadeBD.insertarArchivo(archivo1);
	}
	
	public void borrar() {
		facadeBD.borrarUsuarios();
		facadeBD.borrarArchivos();
	}
	
	public static Test suite() {
		return new TestSuite(TestActualizarArchivos.class);
	}
	
	public static void main(String []args) {
		junit.textui.TestRunner.run(suite());
	}
}
