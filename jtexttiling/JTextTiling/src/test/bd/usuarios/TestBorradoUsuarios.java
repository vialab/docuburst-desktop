package test.bd.usuarios;

import es.project.bd.objetos.Archivo;
import es.project.bd.objetos.Usuario;
import es.project.facade.FacadeBD;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestBorradoUsuarios extends TestCase{
	private FacadeBD facadeBD;
	
	private int numUsuarios;
	private final String testBorrado = "Falla al contar los usuarios después de realizar un borrado";
	private final String testBorrarTodos = "Falla al contar los usuarios después de borrar todas las entradas";
	private final String testNumeroArchivos = "No cuenta bien el número de archivos por usuario";
	private final String testBusquedaCorrecta = "No encuentra un archivo que debería";
	private final String testBusquedaIncorrecta = "Encuentra un archivo que debería haber sido eliminado";
	
	private Usuario usuario1 = new Usuario("nombre1", "pass1","mail1");
	private Usuario usuario2 = new Usuario("nombre2", "pass2","mail2");
	private Archivo archivo1 = new Archivo("archivo1","nombre1","c:\\ruta");
	
	
	public void setUp() {
		facadeBD = new FacadeBD();
		numUsuarios = 0;
		insertarUsuario(usuario1);
		insertarUsuario(usuario2);
	}
	
	public void tearDown() {
		facadeBD.borrarUsuarios();
		numUsuarios = 0;
		facadeBD.tearDown();
		facadeBD = null;
	}
	
	public void testBorradoNombre() {
		borrarUsuario(usuario1);
		assertTrue(this.testBorrado, 
				numUsuarios == facadeBD.getNumeroUsuarios());
		
		borrarUsuario(usuario2);
		assertTrue(this.testBorrado,
				numUsuarios == facadeBD.getNumeroUsuarios());
	}
	
	public void testBorrarTodos() {
		borrarTodos();
		assertTrue(this.testBorrarTodos, 
				numUsuarios == facadeBD.getNumeroUsuarios());
	}
	
	private boolean insertarUsuario(Usuario user) {
		numUsuarios++;
		return facadeBD.insertarUsuario(user);
	}
	
	private void borrarTodos() {
		numUsuarios = 0;
		facadeBD.borrarUsuarios();
	}
	private void borrarUsuario(Usuario user) {
		numUsuarios--;
		facadeBD.borrarUsuario(user);
	}
	
	public void testArchivosPorUsuario() {
		facadeBD.insertarArchivo(archivo1);
		assertTrue(this.testNumeroArchivos, 
				facadeBD.numeroArchivosPorUsuario(usuario1) == 1);
		assertTrue(this.testBusquedaCorrecta, 
				facadeBD.buscarArchivo(archivo1));
		
		facadeBD.borrarUsuario(usuario1);
		assertFalse(this.testBusquedaIncorrecta, 
				facadeBD.buscarArchivo(archivo1));
	}
	
	public static Test suite() {
		return new TestSuite(TestBorradoUsuarios.class);
	}
	
	public static void main(String []args) {
		junit.textui.TestRunner.run(suite());
	}

}
