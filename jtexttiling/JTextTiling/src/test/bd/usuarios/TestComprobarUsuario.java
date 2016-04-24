package test.bd.usuarios;

import es.project.bd.objetos.Usuario;
import es.project.facade.FacadeBD;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestComprobarUsuario extends TestCase {
	private FacadeBD facadeBD;
	
	private final String comprobarCorrecto = ": no realiza la validación de un usuario de la BD";
	private final String buscarCorrecto = ": no encuentra al usuario en la BD";
	private final String comprobarFallo = ": valida a un usuario con password incorrecto";
	private final String comprobarFallo2 = ": valida a un usuario que no existe";
	private final String comprobarActivado = ": el usuario está activado, pero el sistema no lo reconoce";
	private final String comprobarActivadoFallo = ": no debería comprobar al usuario como activado";
	
	Usuario usuario1 = new Usuario("nombre1", "password1","mail1",true);
	Usuario usuario2 = new Usuario("nombre2", "password2","mail2",true);
	
	Usuario usuario00 = new Usuario("nombre00", "pass00","mail00",true);
	
	public void setUp() {
		facadeBD = new FacadeBD();
		insertarUsuarios();
	}
	
	private void insertarUsuarios() {
		facadeBD.insertarUsuario(usuario1);
		facadeBD.insertarUsuario(usuario2);
	}
	
	public void tearDown() {
		facadeBD.borrarUsuarios();
		facadeBD.tearDown();
		facadeBD = null;
	}
	
	public void testComprobarActivadoCorrecto() {
		assertTrue(usuario1.getNombre() + this.comprobarActivado,
				usuario1.isActivado());
		assertTrue(usuario2.getNombre() + this.comprobarActivado,
				usuario2.isActivado());
		assertTrue(usuario00.getNombre() + this.comprobarActivado,
				usuario00.isActivado());
	}
	
	public void testComprobarActivadoIncorrecto() {
		Usuario aux = new Usuario("Usuario aux","aux","aux",false);
		facadeBD.insertarUsuario(aux);
		assertFalse(aux.getNombre() + this.comprobarActivadoFallo,
				aux.isActivado());
	}
	
	public void testComprobarCorrecto() {
		assertTrue(usuario1.getNombre() + this.comprobarCorrecto, 
				facadeBD.comprobarUsuario(usuario1));

		assertTrue(usuario2.getNombre() + this.comprobarCorrecto, 
				facadeBD.comprobarUsuario(usuario2));
		
	}
	
	public void testBuscarUsuario() {
		assertTrue(usuario1.getNombre() + this.buscarCorrecto, 
				facadeBD.buscarUsuario(usuario1));
		assertTrue(usuario2.getNombre() + this.buscarCorrecto, 
				facadeBD.buscarUsuario(usuario2));
	}
	
	public void testComprobarFallo () {
		usuario1.setPassword("passincorrecto");
		assertFalse(usuario1.getNombre() + this.comprobarFallo, 
				facadeBD.comprobarUsuario(usuario1));
		
		usuario2.setPassword("passincorrecto");
		assertFalse(usuario2.getNombre()+ this.comprobarFallo, 
				facadeBD.comprobarUsuario(usuario2));
		
		assertFalse(usuario00.getNombre() + this.comprobarFallo2,
				facadeBD.comprobarUsuario(usuario00));
	}
	
	public static Test suite() {
		return new TestSuite(TestComprobarUsuario.class);
	}
	
	public static void main(String []args) {
		junit.textui.TestRunner.run(suite());
	}
	
}

