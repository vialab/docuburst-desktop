package es.project.bd.finales;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import es.project.bd.abstractos.ArchivoDAO;
import es.project.bd.abstractos.DatosDAO;
import es.project.bd.abstractos.FactoriaDAO;
import es.project.bd.abstractos.UsuarioDAO;
import es.project.bd.configuracion.ConfigBD;

/**
 * <p>Clase que crea la conexión con una base de datos MySQL. Implementa el patrón Singleton.</p>
 * @author Daniel Fernández Aller
 */
public class MySqlFactoriaDAO extends FactoriaDAO {
	
	/**
	 * <p>Objeto que gestiona la conexión con la base de datos</p>
	 */
	private static Connection connection = null;
	
	/**
	 * <p>Variable que indica si la conexión ha sido creada previamente</p>
	 */
	private static boolean creado = false;
	
	/**
	 * <p>Registra el driver y crea la conexión con la base de datos según los valores obtenidos del
	 * fichero de configuración. Esta operación sólo se realizará una vez. A partir de la primera llamada, 
	 * se devuelve siempre el objeto de tipo Connection creado la primera vez.</p>
	 * <p>Es un constructor privado para asegurar el patrón Singleton</p>
	 */
	private MySqlFactoriaDAO () {
		if (!creado) {
			try {
		      Class.forName("com.mysql.jdbc.Driver");
		    }
		    catch (ClassNotFoundException cnfe) {
		      System.err.println("\nDriver no encontrado: " + cnfe.getMessage());
		    }

		    try {
		    	connection = DriverManager.getConnection(ConfigBD.getBDUrl() + ConfigBD.getEsquema(),
		    		ConfigBD.getUser(), ConfigBD.getPassword());
		    }
		      
		    catch (SQLException sql) {
		        System.err.println("\nImposible conectarse a la base de datos: " + sql.getMessage());
		    }

		      creado = true;
		    }
	}
	
	/**
	 * <p>Método estático que se ocupa de crear la conexión con la base de datos, sólo si esta
	 * no ha sido creada previamente, para lo cual hace una llamada al constructor de la clase.</p>
	 * @return Devuelve una única instancia de la clase
	 */
	public static MySqlFactoriaDAO getInstance() {
		return new MySqlFactoriaDAO();
	}
	
	/**
	 * <p>Crea un objeto de tipo UsuarioDAO, particularizado para una base de datos MySQL</p>
	 * @return Devuelve un objeto de tipo MySqlUsuarioDAO (que hereda de UsuarioDAO)
	 */
	public UsuarioDAO getUsuario() {
		return new MySqlUsuarioDAO();
	}
	
	/**
	 * <p>Crea un objeto de tipo ArchivoDAO, particularizado para un base de datos MySQL</p>
	 * @return Devuelve un objeto de tipo MySqlArchivoDAO (que hereda de ArchivoDAO)
	 */
	public ArchivoDAO getArchivo() {
		return new MySqlArchivoDAO();
	}
	
	/**
	 * <p>Crea un objeto de tipo DatosDAO, particularizado para un base de datos MySQL</p>
	 * @return Devuelve un objeto de tipo MySqlDatosDAO (que hereda de DatosDAO)
	 */
	public DatosDAO getDatosDAO() {
		return new MySqlDatosDAO();
	}
	
	/**
	 * <p>Crea el objeto que gestiona los datos de la conexión (sólo la primera vez) y lo
	 * devuelve</p>
	 * @return Objeto de tipo Connection con los datos de la conexión con la base de datos
	 * MySQL
	 */
	public static Connection getConexion() {
		if (!creado) 
			new MySqlFactoriaDAO();
		return connection;
		
	}

}
