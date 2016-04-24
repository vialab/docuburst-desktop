package test.bd.archivos;

import es.project.bd.objetos.Archivo;
import es.project.bd.objetos.Usuario;
import es.project.facade.FacadeBD;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestBorradoArchivos extends TestCase{
	private FacadeBD facadeBD;
	
	private final String testNumeroArchivos = "Falla al contar el número de archivos del usuario";
	
	private Usuario usuario1 = new Usuario("nombre1","pass1","mail1");
	private Usuario usuario2 = new Usuario("nombre2","pass2","mail2");
	private Archivo archivo1 = new Archivo("archivo1","nombre1","c:\\ruta");
	private Archivo archivo2 = new Archivo("archivo2","nombre1","d:\\ruta");
	
	private final int numArchivosUsuario1 = 2;
	private final int numArchivosUsuario2 = 0;
	
	public void setUp() {
		facadeBD = new FacadeBD();
		insertarUsuarios();
		insertarArchivos();
	}
	
	public void tearDown() {
		borrarArchivos();
		borrarUsuarios();
		facadeBD.tearDown();
		facadeBD = null;
	}
	
	public void testBorrarArchivos () {
		facadeBD.borrarArchivo(archivo1);
		assertTrue(this.testNumeroArchivos,
				facadeBD.numeroArchivosPorUsuario(usuario1) == (this.numArchivosUsuario1 - 1));
		
		assertTrue(this.testNumeroArchivos,
				facadeBD.numeroArchivosPorUsuario(usuario2) == this.numArchivosUsuario2);
		
		facadeBD.borrarArchivo(archivo2);
		assertTrue(this.testNumeroArchivos,
				facadeBD.numeroArchivosPorUsuario(usuario1) == (this.numArchivosUsuario1 - 2));
		
		assertTrue(this.testNumeroArchivos,
				facadeBD.numeroArchivosPorUsuario(usuario2) == this.numArchivosUsuario2);
	}
	
	public void testNumeroArchivos() {
		assertTrue(this.testNumeroArchivos,
				facadeBD.numeroArchivosPorUsuario(usuario1) == this.numArchivosUsuario1);
		
		assertTrue(this.testNumeroArchivos,
				facadeBD.numeroArchivosPorUsuario(usuario2) == this.numArchivosUsuario2);
	}
	
	private void insertarUsuarios() {
		facadeBD.insertarUsuario(usuario1);
		facadeBD.insertarUsuario(usuario2);
	}
	
	private void insertarArchivos(){
		facadeBD.insertarArchivo(archivo1);
		facadeBD.insertarArchivo(archivo2);
	}
	
	private void borrarUsuarios() {
		facadeBD.borrarUsuarios();
	}
	
	private void borrarArchivos() {
		facadeBD.borrarArchivos();
	}
	
	public static Test suite() {
		return new TestSuite(TestBorradoArchivos.class);
	}
	
	public static void main(String []args) {
		junit.textui.TestRunner.run(suite());
	}
}
