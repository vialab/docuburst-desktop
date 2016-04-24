package es.project.facade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import es.project.bd.finales.MySqlFactoriaDAO;

/**
 * <p>Clase que maneja la conexión con la base de datos MySQL</p>
 * @author Daniel Fernández Aller
 */
public class ConectorBD {
	
	private Connection conn;
	
	/**
	 * <p>Constructor de la clase: inicializa el objeto de la conexión según las operaciones
	 * realizadas en la FactoriaDAO de MySQL</p>
	 */
	public ConectorBD() {
		conn = MySqlFactoriaDAO.getConexion();
	}
	
	/**
	 * <p>Crea un objeto de tipo Statement a partir del objeto Connection</p>
	 * @return Devuelve el Statement creado
	 * @throws SQLException Se lanza debido a posibles problemas en el acceso a la base de datos
	 */
	public Statement getStatement() throws SQLException {
		return conn.createStatement();
	}
	
	/**
	 * <p>Crea un objeto de tipo PreparedStatement a partir del objeto Connection y la consulta
	 * recibida como parámetro</p>
	 * @param query Consulta con la cual se va a crear el PreparedStatement
	 * @return Devuelve el PreparedStatement creado 
	 * @throws SQLException Se lanza debido a posibles problemas en el acceso a la base de datos
	 */
	public PreparedStatement getPreparedStatement(String query) throws SQLException {
		return conn.prepareStatement(query);
	}
	
}
