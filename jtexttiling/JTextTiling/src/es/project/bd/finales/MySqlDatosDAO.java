package es.project.bd.finales;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.project.bd.abstractos.DatosDAO;
import es.project.bd.objetos.Usuario;
import es.project.facade.ConectorBD;

/**
 * <p>Implementación de la clase DatosDAO particularizada para una base de datos MySQL</p>
 * @author Daniel Fernández Aller
 */
public class MySqlDatosDAO extends DatosDAO {
	/**
	 * <p>Objeto que encapsula las operaciones que se realizan con los objetos de tipo
	 * Connection</p>
	 */
	private ConectorBD conectorBD = new ConectorBD();
	private final String ULTIMO_LOGIN = "ultimoLogin";
	private final String ULTIMA_ALTA = "ultimaAlta";
	
	/**
	 * <p>Método que hace de interfaz con el resto de la aplicación para la operación
	 * que actualiza el nombre del último usuario que ha utilizado el servicio. Llama
	 * al método "actualizarDatos", privado, con los parámetros necesarios</p>
	 * @param usuario Objeto que representa al último usuario que ha utilizado
	 * el servicio
	 * @return Verdadero si la operación salió bien, falso en caso contrario
	 */
	public boolean actualizarUltimoLogin(Usuario usuario) {
		return this.actualizarDatos(usuario.getNombre(), this.ULTIMO_LOGIN);
	}
	
	/**
	 * <p>Método que hace de interfaz con el resto de la aplicación para la operación
	 * que actualiza el nombre del último usuario que se ha dado de alta en el servicio. 
	 * Llama al método "actualizarDatos", privado, con los parámetros necesarios</p>
	 * @param usuario Objeto que representa al último usuario que se ha dado de
	 * alta en el servicio
	 * @return Verdadero si la operación salió bien, falso en caso contrario
	 */
	public  boolean actualizarUltimaAlta(Usuario usuario) {
		return this.actualizarDatos(usuario.getNombre(), this.ULTIMA_ALTA);
	}
	
	/**
	 * <p>Actualiza la columna indicada con el valor pasado como parámetro. Se utiliza
	 * para actualizar el nombre del último usuario logueado y el último usuario que se
	 * ha dado de alta</p>
	 * @param nombre Nombre del usuario
	 * @param columna Columna de la base de datos a actualizar. Los nombres posibles para 
	 * este parámetro vienen dados por las variables finales ULTIMO_LOGIN Y ULTIMA_ALTA
	 * @return Verdadero si hubo alguna columna actualizada, falso en caso contrario
	 */
	private boolean actualizarDatos (String nombre, String columna) {
		boolean actualizado = false;
		PreparedStatement ps;
		
		try {
			ps = conectorBD.getPreparedStatement("update datosestadisticos set " + columna + " = ?");
			ps.setString(1, nombre);
			
			actualizado = (ps.executeUpdate() > 0);
			ps.close();
			
		} catch (SQLException sql) {
			System.err.println("MySqlDatosDAO/actualizarDatos " + sql.getMessage());
		}
		return actualizado;
	}
	
	/**
	 * <p>Método que hace de interfaz con el resto de la aplicación para la operación
	 * que nos permite obtener al último usuario que se ha logueado. Llama al método
	 * "getDatos", privado, con los parámetros necesarios.</p>
	 * @return Objeto usuario que representa el último login en el servicio
	 */
	public Usuario getUltimoLogin() {
		return this.getDatos(this.ULTIMO_LOGIN);
	}
	
	/**
	 * <p>Método que hace de interfaz con el resto de la aplicación para la operación
	 * que nos permite obtener al último usuario que se ha dado de alta en el servicio. 
	 * Llama al método "getDatos", privado, con los parámetros necesarios.</p>
	 * @return Objeto usuario que representa el último usuario dado de alta en el servicio
	 */
	public Usuario getUltimaAlta() {
		return this.getDatos(this.ULTIMA_ALTA);
	}
	
	/**
	 * <p>Obtiene el nombre del último usuario que se ha logueado o que se ha dado de alta, 
	 * según el valor de la columna que se recibe como parámetro.</p>
	 * @param columna Columna de la base de datos a obtener. Los nombres posibles para 
	 * este parámetro vienen dados por las variables finales ULTIMO_LOGIN Y ULTIMA_ALTA
	 * @return Objeto usuario que representa al último usuario logueado o al último usuario
	 * dado de alta
	 */
	private Usuario getDatos(String columna) {
		Usuario aux = null;
		PreparedStatement ps;
		ResultSet rs;
		
		try {
			ps = conectorBD.getPreparedStatement("select " + columna + " from datosestadisticos");
			rs = ps.executeQuery();
			
			if (rs.next())
				aux = new Usuario(rs.getString(1));
			
			rs.close();
			ps.close();
			
		} catch (SQLException sql) {
			System.err.println("MySqlDatosDAO/getDatos " + sql.getMessage());
		}
		return aux;
	}
}
