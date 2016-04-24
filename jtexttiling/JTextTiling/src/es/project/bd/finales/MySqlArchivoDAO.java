package es.project.bd.finales;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import es.project.bd.abstractos.ArchivoDAO;
import es.project.bd.objetos.Archivo;
import es.project.bd.objetos.Usuario;
import es.project.facade.ConectorBD;

/**
 * <p>Implementación de la clase ArchivoDAO particularizada para una base de datos MySQL</p>
 * @author Daniel Fernández Aller
 */
public class MySqlArchivoDAO extends ArchivoDAO{
	/**
	 * <p>Objeto que encapsula las operaciones que se realizan con los objetos de tipo
	 * Connection</p>
	 */
	private ConectorBD conectorBD = new ConectorBD();
	
	/**
	 * <p>Recorre la tabla que almacena la información sobre los archivos, guarda sus
	 * propiedades en una variable temporal, y la muestra por pantalla.</p>
	 */
	public void verArchivos() {
		Statement stat;
		ResultSet rs;
		Archivo archivo;
		
		try {
			stat = conectorBD.getStatement();
			rs = stat.executeQuery("select * from archivos");
			
			while (rs.next()){
				archivo = new Archivo();
				archivo.setNombreArchivo(rs.getString("nombreArchivo"));
				archivo.setNombrePropietario(rs.getString("nombrePropietario"));
				archivo.setRutaArchivo(rs.getString("rutaArchivo"));
				
				System.out.println(archivo);
			}
			
			rs.close();
			stat.close();
			
		} catch (SQLException sql) {
			System.err.println("MySqlArchivoDAO/Ver Archivos: " + sql.getMessage());
		}
	}
	
	/**
	 * <p>Calcula el número total de archivos mediante la operación "count", y lo
	 * muestra por pantalla</p>
	 * @return Número de archivos contenidos en la base de datos
	 */
	public int numeroArchivos()  {
		int numArchivos = 0;
		Statement stat; 
		ResultSet rs;
		
		try {
			stat = conectorBD.getStatement();
			rs = stat.executeQuery("select count(nombreArchivo) total from archivos");
			
			if (rs.next())
				numArchivos = rs.getInt("total");
			
			rs.close();
			stat.close();
			
		} catch (SQLException sql) {
			System.err.println("MySqlArchivoDAO/numeroArchivos: " + sql.getMessage());
		}
		return numArchivos;
	}
	
	/**
	 * <p>Borra toda la información de la tabla que contiene el nombre, el propietario y la ruta
	 * de los archivos</p>
	 * @return Verdadero si la operación fue bien
	 */
	public boolean borrarArchivos(){
		Statement stat;
		boolean borrados = true;
		
		try {
			stat = conectorBD.getStatement();
			stat.execute("delete from archivos");
			stat.close();
			
		} catch (SQLException sql) {
			System.err.println("MySqlArchivoDAO/borrarArchivos: " + sql.getMessage());
			borrados = false;
		}
		return borrados;
	}

	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para borrar
	 * un archivo concreto de la base de datos. Llama al método "borrarArchivo", privado, 
	 * con los parámetros necesarios</p>
	 * @param archivo Objeto de tipo Archivo que representa el archivo que se va a eliminar
	 * @return Verdadero si todo fue bien
	 */
	public boolean borrarArchivo(Archivo archivo) {
		return this.borrarArchivo(archivo.getNombreArchivo(),
				archivo.getNombrePropietario());
	}
	
	/**
	 * <p>Busca en la base de datos el archivo que concuerde con los parámetros especificados,
	 * y lo elimina.</p>
	 * @param nombreArchivo Nombre del archivo a eliminar
	 * @param nombrePropietario Nombre del propietario del archivo a eliminar
	 * @return Verdadero si todo fue bien
	 */
	private boolean borrarArchivo(String nombreArchivo, String nombrePropietario) {
		PreparedStatement ps;
		boolean borrado = true;
		
		try {
			ps = conectorBD.getPreparedStatement("delete from archivos where nombreArchivo = ? and nombrePropietario = ?");
			ps.setString(1, nombreArchivo);
			ps.setString(2, nombrePropietario);
			ps.executeUpdate();
			
			ps.close();
			
		} catch (SQLException sql) {
			borrado = false;
			System.err.println("MySqlArchivoDAO/borrarArchivo: " + sql.getMessage());
		}
		
		return borrado;
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para
	 * insertar un archivo. Llama al método "insertarArchivo", privado, con los parámetros
	 * necesarios.</p>
	 * @param archivo Objeto de tipo Archivo que representa el archivo a insertar
	 * @return Verdadero si todo fue bien
	 */
	public boolean insertarArchivo (Archivo archivo) {
		return this.insertarArchivo(archivo.getNombreArchivo(), 
				archivo.getNombrePropietario(), archivo.getRutaArchivo());
	}
	
	/**
	 * <p>Inserta en la tabla "archivos" una fila con el nombre del archivo, el nombre de su
	 * propietario y la ruta en la que se encuentra</p>
	 * @param nombreArchivo Nombre del archivo a insertar
	 * @param nombrePropietario Nombre del propietario del archivo a insertar
	 * @param rutaArchivo Ruta del archivo a insertar
	 * @return Verdadero si todo fue bien
	 */
	private boolean insertarArchivo(String nombreArchivo, String nombrePropietario, String rutaArchivo) {
		boolean insertado = true;
		PreparedStatement ps;
		
		try {
			ps = 
				conectorBD.getPreparedStatement("insert into archivos(nombreArchivo,nombrePropietario,rutaArchivo) values(?,?,?)");
			ps.setString(1, nombreArchivo);
			ps.setString(2, nombrePropietario);
			ps.setString(3, rutaArchivo);
			
			ps.executeUpdate();
			ps.close();
			
		} catch (SQLException sql) {
			System.err.println("MySqlArchivoDAO/insertarArchivo: " + sql.getMessage());
			insertado = false;
		}
		return insertado;
	}
	
	/**
	 * <p>Método que hace de interfaz con el resto de la aplicación para buscar un archivo. Llama
	 * al método "buscarArchivo", privado, con los parámetros necesarios.</p>
	 * @param archivo Objeto de tipo archivo que representa el archivo a buscar
	 * @return Verdadero si el archivo se encuentra en la tabla
	 */
	public boolean buscarArchivo(Archivo archivo) {
		return this.buscarArchivo(archivo.getNombreArchivo(),
				archivo.getNombrePropietario());
	}
	
	/**
	 * <p>Busca en la tabla "archivos", y comprueba si alguna fila contiene el mismo nombre
	 * de archivo y de propietario que los que se pasan como parámetro. </p>
	 * @param nombreArchivo Nombre del archivo a buscar
	 * @param nombrePropietario Nombre del propietario del archivo a buscar
	 * @return Verdadero si el archivo se encuentra en la tabla
	 */
	private boolean buscarArchivo (String nombreArchivo, String nombrePropietario) {
		boolean almacenado = true;
		PreparedStatement ps;
		ResultSet rs;
		
		try {
			ps = conectorBD.getPreparedStatement("select * from archivos where nombreArchivo = ? and nombrePropietario = ?");
			ps.setString(1, nombreArchivo);
			ps.setString(2, nombrePropietario);
			rs = ps.executeQuery();
			
			if (!rs.next()) 
				almacenado = false;
				
		} catch (SQLException sql) {
			System.err.println("MySqlArchivoDAO/buscarArchivo: " + sql.getMessage());
			almacenado = false;
		}
		return almacenado;
	}
	
	/**
	 * <p>Busca el archivo que coincida con los parámetros especificados y
	 * devuelve un objeto con esta información.</p>
	 * @param nombreArchivo Nombre del archivo a buscar
	 * @param nombrePropietario Nombre del propietario del archivo a buscar
	 * @return Un objeto de tipo Archivo si hay alguna fila de la tabla que coincida
	 * con los parámetros especificados. Si no, devuelve null.
	 */
	public Archivo getArchivo(String nombreArchivo, String nombrePropietario) {
		Archivo retorno = null;
		PreparedStatement ps;
		ResultSet rs;
		
		try {
			ps = conectorBD.getPreparedStatement("select * from archivos where nombreArchivo = ? and nombrePropietario = ?");
			ps.setString(1, nombreArchivo);
			ps.setString(2, nombrePropietario);
			rs = ps.executeQuery();
			
			if (rs.next()) {
				retorno = new Archivo();
				retorno.setNombreArchivo(rs.getString("nombreArchivo"));
				retorno.setNombrePropietario(rs.getString("nombrePropietario"));
				retorno.setRutaArchivo(rs.getString("rutaArchivo"));
			}
			
			rs.close();
			ps.close();
			
		} catch (SQLException sql) {
			System.err.println("MySqlArchivoDAO/getArchivo: " + sql.getMessage());
		}
		return retorno;
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para
	 * la operación de actualizar el nombre de un archivo. Llama al método "actualizarArchivo",
	 * privado, con los parámetros necesarios</p>
	 * @param archivo Objeto de tipo archivo que representa el archivo a modificar
	 * @param valor Nuevo valor que recibirá el nombre del archivo
	 * @return Verdadero si la operación resultó bien
	 */
	public boolean actualizarNombreArchivo(Archivo archivo, String valor) {
		return this.actualizarArchivo(archivo.getNombreArchivo(), 
				archivo.getNombrePropietario(), "nombreArchivo", valor);
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para
	 * la operación de actualizar la ruta de un archivo. Llama al método "actualizarArchivo",
	 * privado, con los parámetros necesarios</p>
	 * @param archivo Objeto de tipo archivo que representa el archivo a modificar
	 * @param valor Nuevo valor que recibirá el nombre del archivo
	 * @return Verdadero si la operación resultó bien
	 */
	public boolean actualizarRutaArchivo(Archivo archivo, String valor) {
		return this.actualizarArchivo(archivo.getNombreArchivo(), 
				archivo.getNombrePropietario(), "rutaArchivo", valor);
	}
	
	/**
	 * <p>Busca si alguna fila coincide con el nombre del archivo y del propietario especificados.
	 * En caso de que así sea, modifica el campo requerido con el valor especificado. Se utiliza tanto
	 * para actualizar el nombre como la ruta del archivo.</p>
	 * @param nombreArchivo Nombre del archivo a modificar
	 * @param nombrePropietario Nombre del propietario del archivo a modificar
	 * @param campo Campo a modificar, puede ser el nombre del archivo o su ruta
	 * @param valor Nuevo valor que va a tener el campo modificado
	 * @return Verdadero si se actualizó algún archivo, falso en caso contrario
	 */
	private boolean actualizarArchivo(String nombreArchivo, String nombrePropietario, String campo,
			String valor) {
		
		boolean modificado = false;
		PreparedStatement ps;
		
		try {
			ps = conectorBD.getPreparedStatement("update archivos set " + campo + " = ? where nombreArchivo = ? and nombrePropietario = ?");
			ps.setString(1, valor);
			ps.setString(2, nombreArchivo);
			ps.setString(3, nombrePropietario);
			
			if ((ps.executeUpdate()) > 0) 
				modificado = true;
			
			ps.close();
			
		} catch (SQLException sql) {
			modificado = false;
			System.err.println("MySqlArchivoDAO/actualizarArchivo: " + sql.getMessage());
		}
		return modificado;
	}
	
	/**
	 * <p>Recorre la tabla que almacena la información referente a los archivos y la copia
	 * en una lista enlazada. Si se produce algún problema durante la ejecución, el método
	 * devuelve una lista que apunta a null</p>
	 * @return Si todo fue bien, una lista enlazada con la información de todos los archivos;
	 * si se produjo algún error se devuelve una lista que apunta
	 * a null
	 */
	public List<Archivo> getTodosArchivos() {
		List<Archivo> listaArchivos = new LinkedList<Archivo>();
		Statement stat;
		ResultSet rs;
		Archivo aux;
		
		try {
			stat = conectorBD.getStatement();
			rs = stat.executeQuery("select * from archivos");
				
			while (rs.next()) {
				aux = new Archivo(rs.getString("nombreArchivo"),
						rs.getString("nombrePropietario"),rs.getString("rutaArchivo"));
				listaArchivos.add(aux);
			}
				
			rs.close();
			stat.close();
				
		} catch (SQLException sql) {
			System.err.println("MySqlArchivoDAO/getTodosArchivos " + sql.getMessage());
			listaArchivos = null;
		}
		
		return listaArchivos;
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para
	 * la operación de averiguar el usuario propietario de un archivo. Llama al método 
	 * "getPropietario", privado, con los parámetros necesarios</p>
	 * @param archivo Objeto de tipo archivo que representa el archivo del cual queremos
	 * averiguar su propietario
	 * @return Objeto de tipo usuario que representa al propietario del archivo
	 */
	public Usuario getPropietario(Archivo archivo) {
		return this.getPropietario(archivo.getRutaArchivo());
	}
	
	/**
	 * <p>Busca el usuario propietario del archivo en la tabla que almacena la información
	 * sobre los archivos. Para conocer al propietario de un archivo nos basta con saber la
	 * ruta de éste</p>
	 * @param archivo Objeto de tipo archivo que representa el archivo del cual queremos
	 * averiguar su propietario
	 * @return Objeto de tipo usuario que representa al propietario del archivo
	 */
	private Usuario getPropietario(String rutaArchivo) {
		Usuario propietario = null;
		PreparedStatement ps;
		ResultSet rs;
		
		try {
			ps = conectorBD.getPreparedStatement("select nombrePropietario from archivos where rutaArchivo = ?");
			ps.setString(1, rutaArchivo);
			rs = ps.executeQuery();
			
			if (rs.next()) 
				propietario = new Usuario(rs.getString("nombrePropietario"));
			
			rs.close();
			ps.close();
				
		} catch (SQLException sql) {
			System.err.println("MySqlArchivoDAO/getPropietario " + sql.getMessage());
			propietario = null;
		}
		return propietario;
	}
	
	/**
	 * <p>Obtiene el nombre del archivo a partir de su ruta</p>
	 * @param ruta Ruta del archivo del cual queremos obtener el nombre
	 * @return Devuelve una cadena con el nombre del archivo
	 */
	public String getNombreArchivo(String ruta) {
		String nombre = "";
		PreparedStatement ps;
		ResultSet rs;
		
		try {
			ps = conectorBD.getPreparedStatement("select nombreArchivo from archivos where rutaArchivo = ?");
			ps.setString(1, ruta);
			rs = ps.executeQuery();
			
			if (rs.next())
				nombre = rs.getString("nombreArchivo");
			
			rs.close();
			ps.close();
			
		} catch (SQLException sql) {
			System.err.println("MySqlArchivoDAO/getPropietario " + sql.getMessage());
			nombre = "";
		}
		return nombre;
	}
 }
