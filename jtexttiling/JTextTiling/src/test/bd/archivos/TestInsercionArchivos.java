package test.bd.archivos;

import es.project.bd.objetos.Archivo;
import es.project.bd.objetos.Usuario;
import es.project.facade.FacadeBD;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestInsercionArchivos extends TestCase{
	private int numArchivos;
	private FacadeBD facadeBD;
	
	private final String testVacio = "Debería indicar que hay 0 usuarios, muestra "; 
	private final String testNoVacio = "Falla al realizar el recuento de usuarios";
	private final String test_FK_correcta = "Debería permitir la inserción del archivo: indica" +
			"violación de FK cuando no la hay";
	private final String test_FK_invalida = "No debería permitir la inserción del archivo: el nombre" +
			"del propietario no concuerda con un usuario de la tabla";
	private final String testNumeroArchivos = "Falla al contar el número de archivos del usuario";
	
	Usuario usuario1 = new Usuario("nombre1", "pass1","mail1");
	Usuario usuario2 = new Usuario("nombre2", "pass2","mail2");
	Archivo archivo1 = new Archivo("archivo1","nombre1","c:\\ruta");
	Archivo archivo2 = new Archivo("archivo2","nombre1","d:\\ruta");
	
	private final int numArchivosUsuario1 = 2;
	private final int numArchivosUsuario2 = 0;
	
	public void setUp() {
		numArchivos = 0;
		facadeBD = new FacadeBD();
		this.insertarUsuarios();
	}
	
	public void tearDown() {
		this.borrarUsuarios();
		this.borrarArchivos();
		facadeBD.tearDown();
		facadeBD = null;
	}
	
	public void testVacio() {
		int n = facadeBD.getNumeroArchivos();
		assertTrue(this.testVacio + n, 
				this.numArchivos == n);
	}
	
	public void testCorrectaFK() {
		Archivo archivo00 = new Archivo("archivo3",usuario1.getNombre(),"c:\\ruta");
		assertTrue(this.test_FK_correcta, 
				facadeBD.insertarArchivo(archivo00));
	}
	
	public void testNoVacio() {
		this.insertarArchivo(archivo1);
		assertTrue(this.testNoVacio, 
				this.numArchivos == facadeBD.getNumeroArchivos());
		
		this.insertarArchivo(archivo2);
		assertTrue(this.testNoVacio,
				this.numArchivos == facadeBD.getNumeroArchivos());
	}
	
	public void testViolacionFK() {
		Archivo archivo01 = new Archivo("archivo4","nombre99","c:\\ruta");
		assertFalse(this.test_FK_invalida, 
				facadeBD.insertarArchivo(archivo01));
	}
	
	public void testArchivosPorUsuario() {
		//archivos del usuario 1
		this.insertarArchivo(archivo1);
		this.insertarArchivo(archivo2);
		assertTrue(this.testNumeroArchivos,
				facadeBD.numeroArchivosPorUsuario(usuario1) == this.numArchivosUsuario1);
		
		assertTrue(this.testNumeroArchivos,
				facadeBD.numeroArchivosPorUsuario(usuario2) == this.numArchivosUsuario2);
	}
	
	private void insertarArchivo(Archivo archivo) {
		facadeBD.insertarArchivo(archivo);
		numArchivos++;
	}
	
	private void insertarUsuarios() {
		facadeBD.insertarUsuario(usuario1);
		facadeBD.insertarUsuario(usuario2);
	}
	
	private void borrarUsuarios() {
		facadeBD.borrarUsuarios();
	}
	
	private void borrarArchivos() {
		facadeBD.borrarArchivos();
		numArchivos = 0;
	}
	
	public static Test suite() {
		return new TestSuite(TestInsercionArchivos.class);
	}
	
	public static void main(String []args) {
		junit.textui.TestRunner.run(suite());
	}
}
