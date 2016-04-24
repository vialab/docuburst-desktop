package test.bd.usuarios;

import es.project.bd.objetos.Usuario;
import es.project.facade.FacadeBD;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestInsercionUsuarios extends TestCase{
	private FacadeBD facadeBD;
	
	private final String testVacio = "Debería indicar que hay 0 usuarios, muestra "; 
	private final String testNoVacio = "Falla al realizar el recuento de usuarios: ";
	private final String testDuplicados = "Permite insertar dos usuarios con el mismo nombre";
	private final String testDuplicados2 = "No permite insertar un usuario con un nombre que había sido" +
			"previamente borrado";
	private final String testFalloNombre = "Nos permite dar de alta un usuario sin nombre";
	private final String testFalloPassword = "Nos permite dar de alta un usuario sin password";
	private final String testFalloMail = "Nos permite dar de alta un usuario sin mail";
	
	private final String testFalloDatosUltimaAlta_1 = "La última alta debería ser la del usuario 'root'";
	private final String testFalloDatosUltimaAlta_2 = "La última alta debería ser la del usuario ";
	private final String testFalloDatosUltimoLogin_1 = "El último login debería ser el del usuario 'root'";
	private final String testFalloDatosUltimoLogin_2 = "El último login debería ser el del usuario ";
	
	private final String NOMBRE_USUARIO_INICIAL = "root";
	
	Usuario usuario1 = new Usuario("nombre1", "pass1","mail");
	Usuario usuario2 = new Usuario("nombre2", "pass2","mail");
	
	Usuario fallo1 = new Usuario(null,"pass","mail");
	Usuario fallo2 = new Usuario("nombre",null,"mail");
	Usuario fallo3 = new Usuario("nombre","pass",null);
	
	private int contador = 0;
	
	public void setUp() {
		facadeBD = new FacadeBD();
		facadeBD.borrarUsuarios();
	}
	
	public void tearDown() {
		Usuario root = new Usuario("root");
		facadeBD.actualizarDatosUltimaAlta(root);
		facadeBD.actualizarDatosUltimoLogin(root);
		facadeBD.borrarUsuarios();
		contador = 0;
		facadeBD.tearDown();
		facadeBD = null;
	}
	
	public void testDatosUltimoLogin() {
		Usuario auxLogin = facadeBD.getDatosUltimoLogin();
		assertTrue(this.testFalloDatosUltimoLogin_1, 
				auxLogin.getNombre().compareTo(this.NOMBRE_USUARIO_INICIAL) == 0);
		
		this.insertarUsuario(usuario1);
		facadeBD.actualizarDatosUltimoLogin(usuario1);
		auxLogin = facadeBD.getDatosUltimoLogin();
		assertTrue(this.testFalloDatosUltimoLogin_2 + usuario1.getNombre(), 
				auxLogin.getNombre().compareTo(usuario1.getNombre()) == 0);
		
		this.insertarUsuario(usuario2);
		facadeBD.actualizarDatosUltimoLogin(usuario2);
		auxLogin = facadeBD.getDatosUltimoLogin();
		assertTrue(this.testFalloDatosUltimoLogin_2 + usuario2.getNombre(), 
				auxLogin.getNombre().compareTo(usuario2.getNombre()) == 0);
	}
	
	public void testDatosUltimaAlta() {
		Usuario auxAlta = facadeBD.getDatosUltimaAlta();
		assertTrue(this.testFalloDatosUltimaAlta_1, 
				auxAlta.getNombre().compareTo(this.NOMBRE_USUARIO_INICIAL) == 0);
		
		this.insertarUsuario(usuario1);
		facadeBD.actualizarDatosUltimaAlta(usuario1);
		auxAlta = facadeBD.getDatosUltimaAlta();
		assertTrue(this.testFalloDatosUltimaAlta_2 + usuario1.getNombre(), 
				auxAlta.getNombre().compareTo(usuario1.getNombre()) == 0);
		
		this.insertarUsuario(usuario2);
		facadeBD.actualizarDatosUltimaAlta(usuario2);
		auxAlta = facadeBD.getDatosUltimaAlta();
		assertTrue(this.testFalloDatosUltimaAlta_2 + usuario2.getNombre(), 
				auxAlta.getNombre().compareTo(usuario2.getNombre()) == 0);
		
	}
	
	public void testFallo() {
		assertFalse(this.testFalloNombre,facadeBD.insertarUsuario(fallo1));
		assertFalse(this.testFalloPassword, facadeBD.insertarUsuario(fallo2));
		assertFalse(this.testFalloMail, facadeBD.insertarUsuario(fallo3));
	}
	
	public void testVacio() {
		int numUsuarios = facadeBD.getNumeroUsuarios();
		assertTrue(this.testVacio + numUsuarios, 
				numUsuarios == 0);
	}
	
	public void testNoVacio() {
		insertarUsuario(usuario1);
		
		int numUsuarios = facadeBD.getNumeroUsuarios();
		assertTrue(this.testNoVacio + numUsuarios + "(" + contador + ")"
				,numUsuarios == contador);
		
		insertarUsuario(usuario2);
		
		numUsuarios = facadeBD.getNumeroUsuarios();
		assertTrue(this.testNoVacio + numUsuarios + "(" + contador + ")"
				,numUsuarios == contador);
	}

	public void testDuplicados() {
		insertarUsuario(usuario1);
		assertFalse(this.testDuplicados, 
				insertarUsuario(usuario1));
		facadeBD.borrarUsuario(usuario1);
		
		assertTrue(this.testDuplicados2, 
				insertarUsuario(usuario1));
	}
	
	private boolean insertarUsuario(Usuario user) {
		contador++;
		return this.facadeBD.insertarUsuario(user);
	}
	
	public static Test suite() {
		return new TestSuite(TestInsercionUsuarios.class);
	}
	
	public static void main(String []args) {
		junit.textui.TestRunner.run(suite());
	}
}
