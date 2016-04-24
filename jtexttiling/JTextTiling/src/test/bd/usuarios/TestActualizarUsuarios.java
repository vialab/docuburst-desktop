package test.bd.usuarios;

import es.project.bd.objetos.Archivo;
import es.project.bd.objetos.Usuario;
import es.project.facade.FacadeBD;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestActualizarUsuarios extends TestCase{
	private FacadeBD facadeBD;
	
	private final String testActualizarNombreCorrecto = "No actualiza el nombre del usuario";
	private final String testActualizarNombre_2 = "No encuentra al usuario al que hemos modificado el nombre";
	private final String testActualizarNombreIncorrecto = "No debería permitir actualizar el nombre del usuario";
	private final String testActualizarPass = "No actualiza el password";
	private final String testActualizarPass_2 = "No encuentra al usuario al que hemos modificado el password";
	private final String testActualizarMail = "No actualiza el mail";
	private final String testActualizarMail_2 = "No encuentra al usuario al que hemos modificado el mail";
	private final String testActualizarNombreArchivo_1 = 
		"No almacena correctamente la información sobre los archivos";
	private final String testActualizarNombreArchivo_2 = "No actualiza el nombre del propietario del archivo";
	private final String testFalloDatosUltimoLogin = "No actualiza el nombre del último usuario logueado";
	
	private Usuario usuario1 = new Usuario("nombre1","pass1","mail1");
	private Usuario usuario2 = new Usuario("nombre2", "pass2","mail2");
	private Archivo archivo1 = new Archivo("archivo1",usuario1.getNombre(),"c:\\ruta");
	
	public void setUp() {
		facadeBD = new FacadeBD();
		this.insertarUsuarios();
		this.insertarArchivos();
	}
	
	public void tearDown() {
		Usuario root = new Usuario("root");
		borrarUsuarios();
		facadeBD.actualizarDatosUltimoLogin(root);
		facadeBD.tearDown();
		facadeBD = null;
	}
	
	public void testActualizarNombre() {
		String nombreNuevo = "nombreNuevo";
		assertTrue(this.testActualizarNombreCorrecto, 
				facadeBD.actualizarNombre(usuario1, nombreNuevo));
		Usuario aux = facadeBD.getUsuario(facadeBD.getNOMBRE(),"nombreNuevo");
		assertFalse(this.testActualizarNombre_2, aux == null);
		
		nombreNuevo = usuario2.getNombre();
		assertFalse(this.testActualizarNombreIncorrecto, 
				facadeBD.actualizarNombre(usuario1, nombreNuevo));
	}
	
	public void testActualizarDatosLogin() {
		/* sabemos que el último usuario insertado es usuario 2 */
		Usuario aux = facadeBD.getDatosUltimoLogin();
		assertTrue(this.testFalloDatosUltimoLogin,
				aux.getNombre().compareTo(usuario2.getNombre()) == 0);
		
		String nombreNuevo = "nombreNuevo";
		facadeBD.actualizarNombre(usuario2, nombreNuevo);
		usuario2.setNombre(nombreNuevo);
		facadeBD.actualizarDatosUltimoLogin(usuario2);
		aux = facadeBD.getDatosUltimoLogin();
		assertTrue(this.testFalloDatosUltimoLogin,
				aux.getNombre().compareTo(nombreNuevo) == 0);
	}
	
	public void testActualizarMail() {
		String nuevoMail = "mail@mail";
		assertTrue(this.testActualizarMail,
				facadeBD.actualizarMail(usuario1, nuevoMail));
		
		Usuario aux = facadeBD.getUsuario(facadeBD.getNOMBRE(),usuario1.getNombre());
		assertTrue(this.testActualizarMail_2, 
				aux.getEmail().compareTo(nuevoMail) == 0);
	}
	
	public void testActualizarPassword() {
		String passNuevo = "passNuevo";
		assertTrue(this.testActualizarPass, 
				facadeBD.actualizarPassword(usuario1, passNuevo));
		
		Usuario aux = facadeBD.getUsuario(facadeBD.getNOMBRE(), usuario1.getNombre());
		assertTrue(this.testActualizarPass_2,
				aux.getPassword().compareTo(passNuevo) == 0);
	}
	
	public void testActualizarArchivos() {
		Archivo auxiliar = facadeBD.getArchivo(archivo1.getNombreArchivo(), archivo1.getNombrePropietario());
		assertTrue(this.testActualizarNombreArchivo_1, 
				auxiliar.getNombrePropietario().compareToIgnoreCase(usuario1.getNombre()) == 0);
		
		String nombreNuevo = "nombreNuevo";
		facadeBD.actualizarNombre(usuario1, nombreNuevo);
		auxiliar = facadeBD.getArchivo(archivo1.getNombreArchivo(), nombreNuevo);
		assertTrue(this.testActualizarNombreArchivo_2,
				auxiliar.getNombrePropietario().compareToIgnoreCase(nombreNuevo) == 0);
	}
	
	private void borrarUsuarios() {
		facadeBD.borrarUsuarios();
	}
	
	private void insertarUsuarios() {
		facadeBD.insertarUsuario(usuario1);
		facadeBD.actualizarDatosUltimoLogin(usuario1);
		facadeBD.insertarUsuario(usuario2);
		facadeBD.actualizarDatosUltimoLogin(usuario2);
	}
	
	private void insertarArchivos() {
		facadeBD.insertarArchivo(archivo1);
	}
	
	public static Test suite() {
		return new TestSuite(TestActualizarUsuarios.class);
	}
	
	public static void main(String []args) {
		junit.textui.TestRunner.run(suite());
	}
}
