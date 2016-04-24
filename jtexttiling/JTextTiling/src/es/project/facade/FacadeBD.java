package es.project.facade;

import java.sql.SQLException;
import java.util.List;

import es.project.bd.abstractos.ArchivoDAO;
import es.project.bd.abstractos.DatosDAO;
import es.project.bd.abstractos.FactoriaDAO;
import es.project.bd.abstractos.UsuarioDAO;
import es.project.bd.objetos.Archivo;
import es.project.bd.objetos.Usuario;

/**
 * <p>Clase que actúa de fachada entre las operaciones con la base de datos MySQL y el resto
 * de la lógica de la aplicación</p>
 * @author Daniel Fernández Aller
 */
public class FacadeBD {
	private final String NOMBRE = "nombre";
	private final String UUID = "uuid";
	private final String PASS = "password";
	private final String MAIL = "email";
	/**
	 * <p>Objeto que maneja la conexión con la base de datos MySQL</p>
	 */
	private FactoriaDAO factoriaDAO;
	
	/**
	 * <p>Objeto que maneja las operaciones de la base de datos referidas a usuarios</p>
	 */
	private UsuarioDAO usuarioDAO;
	
	/**
	 * <p>Objeto que maneja las operaciones de la base de datos referidas a archivos</p>
	 */
	private ArchivoDAO archivoDAO;
	
	private DatosDAO datosDAO;
	
	/**
	 * <p>Constructor de la clase: llama al método que inicializa el valor de los objetos</p>
	 */
	public FacadeBD() {
		setUp();
	}
	
	/**
	 * <p>Crea el objeto FactoriaDAO, que inicia la conexión con la base de datos y a su vez 
	 * permite crear los objetos que manejan el resto de operaciones con la base de datos</p>
	 */
	public void setUp() {
		factoriaDAO = FactoriaDAO.getFactoriaDAO(FactoriaDAO.mySql);
		usuarioDAO = factoriaDAO.getUsuario();
		archivoDAO = factoriaDAO.getArchivo();
		datosDAO = factoriaDAO.getDatosDAO();
	}
	
	/**
	 * <p>Destruye los objetos</p>
	 */
	public void tearDown() {
		usuarioDAO = null;
		archivoDAO = null;
		factoriaDAO = null;
		datosDAO = null;
	}
	
	public void setTipoConexion(boolean esTest) {
		factoriaDAO.setEsTest(esTest);
	}

	/* <USUARIOS> */
	/**
	 * <p>Recorre la tabla que almacena la información sobre los usuarios, guarda sus
	 * propiedades en una variable temporal, y la muestra por pantalla.</p>
	 */
	public void verUsuarios() {
		usuarioDAO.verUsuarios();
	}
	
	/**
	 * <p>Calcula el número total de usuarios mediante la operación "count", y lo
	 * muestra por pantalla. No se cuenta el root ni los usuarios que están sin activar.</p>
	 * @return Número de usuarios contenidos en la base de datos
	 */
	public int getNumeroUsuarios() {
		return usuarioDAO.numeroUsuarios();
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para realizar
	 * la comprobación de un usuario concreto en la base de datos. Llama al método "comprobarUsuario", 
	 * privado, con los parámetros necesarios</p>
	 * @param usuario Objeto de tipo Usuario que representa el usuario que se va a comprobar
	 * @return Verdadero si todo fue bien
	 */
	public boolean comprobarUsuario(Usuario usuario) {
		return usuarioDAO.comprobarUsuario(usuario);
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para insertar 
	 * un usuario en la base de datos. Llama al método "insertarUsuario", privado, con los parámetros 
	 * necesarios</p>
	 * @param usuario Objeto de tipo Usuario que representa el usuario que se va a insertar
	 * @return Verdadero si todo fue bien
	 */
	public boolean insertarUsuario(Usuario usuario) {
		return usuarioDAO.insertarUsuario(usuario);
	}
	
	/**
	 * <p>Borra toda la información de la tabla que contiene el nombre y el password de los
	 * usuarios</p>
	 * @return Verdadero si la operación fue bien
	 */
	public void borrarUsuarios() {
		usuarioDAO.borrarUsuarios();
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para borrar
	 * un usuario concreto de la base de datos. Llama al método "borrarUsuario", privado, 
	 * con los parámetros necesarios</p>
	 * @param usuario Objeto de tipo Usuario que representa el usuario que se va a eliminar
	 * @return Verdadero si todo fue bien
	 */
	public boolean borrarUsuario(Usuario usuario) {
		return usuarioDAO.borrarUsuario(usuario);
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para buscar
	 * un usuario concreto de la base de datos. Llama al método "buscarUsuario", privado, 
	 * con los parámetros necesarios</p>
	 * @param usuario Objeto de tipo Usuario que representa el usuario que se va a buscar
	 * @return Verdadero si el usuario está en la base de datos
	 */
	public boolean buscarUsuario(Usuario usuario) {
		return usuarioDAO.buscarUsuario(usuario);
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para calcular
	 * el número de archivos que tiene un usuario concreto. Llama al método "numeroArchivos", privado, 
	 * con los parámetros necesarios</p>
	 * @param usuario Usuario del que se va a contar el número de archivos
	 * @return Número de archivos del usuario
	 */
	public int numeroArchivosPorUsuario(Usuario usuario) {
		return usuarioDAO.numeroArchivos(usuario);
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para
	 * la operación de actualizar el nombre de un usuario. Llama al método "actualizarUsuario",
	 * privado, con los parámetros necesarios</p>
	 * @param usuario Objeto de tipo usuario que representa el usuario a modificar
	 * @param valor Nuevo valor que recibirá el nombre del usuario
	 * @return Verdadero si la operación resultó bien
	 */
	public boolean actualizarNombre(Usuario usuario, String valor) {
		return usuarioDAO.actualizarNombre(usuario, valor);
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para
	 * la operación de actualizar el password de un usuario. Llama al método "actualizarUsuario",
	 * privado, con los parámetros necesarios</p>
	 * @param usuario Objeto de tipo usuario que representa el usuario a modificar
	 * @param valor Nuevo valor que recibirá el password del usuario
	 * @return Verdadero si la operación resultó bien
	 */
	public boolean actualizarPassword(Usuario usuario, String valor) {
		return usuarioDAO.actualizarPassword(usuario, valor);
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para
	 * la operación de actualizar el mail de un usuario. Llama al método "actualizarUsuario",
	 * privado, con los parámetros necesarios</p>
	 * @param usuario Objeto de tipo usuario que representa el usuario a modificar
	 * @param valor Nuevo valor que recibirá el email del usuario
	 * @return Verdadero si la operación resultó bien
	 */
	public boolean actualizarMail(Usuario usuario, String valor) {
		return usuarioDAO.actualizarMail(usuario, valor);
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para la
	 * operación que actualiza la fecha del último login de un usuario. Llama al método 
	 * "actualizarUsuario", privado, con los parámetros necesarios</p>
	 * @param usuario Objeto de tipo usuario que representa el usuario a modificar
	 * @return Verdadero si la operación resultó bien
	 */
	public boolean actualizarUltimoLogin(Usuario usuario) {
		return usuarioDAO.actualizarUltimoLogin(usuario);
	}
	
	/**
	 * <p>Busca el usuario que coincida con el nombre especificado y
	 * devuelve un objeto con esta información.</p>
	 * @param campo Campo en base al cual se va a buscar el usuario (nombre o uuid)
	 * @param valor Valor del campo 
 	 * @return Un objeto de tipo Usuario si hay alguna fila de la tabla que coincida
	 * el parámetro especificado. Si no, devuelve null.
	 */
	public Usuario getUsuario(String campo, String valor) {
		return usuarioDAO.getUsuario(campo, valor);
	}
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para
	 * la operación de obtener la lista de archivos de un usuario. Llama al método "getArchivos",
	 * privado, con los parámetros necesarios</p>
	 * @param usuario Objeto de tipo usuario que representa el usuario a modificar
	 * @return Lista con los archivos del usuario
	 */
	public List<Archivo> getArchivosPorUsuario(Usuario usuario) {
		return usuarioDAO.getArchivos(usuario);
	}
	
	/**
	 * <p>Método interfaz de la clase con el resto de la aplicación para averiguar la fecha de
	 * alta de un usuario que se recibe como parámetro. Llama al método "getFecha(?,?)", privado,
	 * con los parámetros necesarios</p>
	 * @param usuario Objeto de tipo usuario que representa al usuario del que se quiere averiguar
	 * la fecha de alta
	 * @return Devuelve un String con la fecha en formato yyyy-mm-dd
	 */
	public String getFechaAlta(Usuario usuario) throws SQLException{
		return usuarioDAO.getFechaAlta(usuario);
	}
	
	/**
	 * <p>Método interfaz de la clase con el resto de la aplicación para averiguar la fecha del último
	 * login de un usuario que se recibe como parámetro. Llama al método "getFecha(?,?)", privado, con los
	 * parámetros necesarios</p>
	 * @param usuario Objeto de tipo usuario que representa al usuario del que se quiere averiguar la
	 * fecha del último login 
	 * @return Devuelve un String con la fecha en formato yyyy-mm-dd
	 */
	public String getUltimoLogin(Usuario usuario) throws SQLException{
		return usuarioDAO.getUltimoLogin(usuario);
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para
	 * la operación de obtener el identificar universal de un usuario. Llama al método "getUuid",
	 * privado, con los parámetros necesarios</p>
	 * @param usuario Objeto de tipo Usuario que representa al usuario del que se quiere
	 * averiguar el uuid
	 * @return Devuelve una cadena con el uuid
	 */
	public String getUuid(Usuario usuario) {
		return usuarioDAO.getUuid(usuario);
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para
	 * la operación de obtener el email de un usuario. Llama al método "getEmail",
	 * privado, con los parámetros necesarios</p>
	 * @param usuario Objeto de tipo Usuario que representa al usuario del que se quiere
	 * averiguar el email
	 * @return Devuelve una cadena con el email
	 */
	public String getEmail(Usuario usuario) {
		return usuarioDAO.getEmail(usuario);
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para
	 * la operación de finalizar la activación de la cuenta del usuario. Llama al método 
	 * "activarUsuario", privado, con los parámetros necesarios</p>
	 * @param usuario Objeto de tipo Usuario que representa al usuario que se quiere
	 * activar
	 * @return Verdadero si la operación se realizó correctamente
	 */
	public boolean activarUsuario(Usuario usuario) {
		return usuarioDAO.activarUsuario(usuario);
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para
	 * la operación de comprobar el estado de la cuenta del usuario. Llama al método 
	 * "estaActivado", privado, con los parámetros necesarios</p>
	 * @param usuario Objeto de tipo Usuario que representa al usuario del que se quiere
	 * saber si está activado
	 * @return Verdadero si el usuario está activado en el servicio, falso en cualquier
	 * caso contrario
	 */
	public boolean estaActivado(Usuario usuario) {
		return usuarioDAO.estaActivado(usuario);
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para
	 * la operación de comprobar si el usuario que se está autenticando es el 
	 * administrador. Llama al método "esRoot", privado, con los parámetros apropiados.</p>
	 * @param usuario Objeto de tipo Usuario que representa al administrador
	 * @return Verdadero si el usuario es el administrador
	 */
	public boolean esRoot(Usuario usuario) {
		return usuarioDAO.esRoot(usuario);
	}
	
	/**
	 * <p>Copia en una lista la información de todos los usuarios almacenados en la base de datos</p>
	 * @return Lista enlazada con las propiedades de los usuarios
	 */
	public List<Usuario> getTodosUsuarios() {
		return usuarioDAO.getTodosUsuarios();
	}
	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve la cadena que se necesita pasar como parámetro a un método cuando queramos
	 * referirnos al campo nombre de un usuario
	 */
	public String getNOMBRE() {
		return NOMBRE;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve la cadena que se necesita pasar como parámetro a un método cuando queramos
	 * referirnos al campo uuid de un usuario
	 */
	public String getUUID() {
		return UUID;
	}
	
	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve la cadena que se necesita pasar como parámetro a un método cuando queramos
	 * referirnos al campo password de un usuario
	 */
	public String getPASS() {
		return PASS;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve la cadena que se necesita pasar como parámetro a un método cuando queramos
	 * referirnos al campo email de un usuario
	 */
	public String getMAIL() {
		return MAIL;
	}
	/* </USUARIOS>*/
	
	
	/* <ARCHIVOS>*/
	/**
	 * <p>Recorre la tabla que almacena la información sobre los archivos, guarda sus
	 * propiedades en una variable temporal, y la muestra por pantalla.</p>
	 */
	public void verArchivos() {
		archivoDAO.verArchivos();
	}
	
	/**
	 * <p>Calcula el número total de archivos mediante la operación "count", y lo
	 * muestra por pantalla</p>
	 * @return Número de archivos contenidos en la base de datos
	 */
	public int getNumeroArchivos() {
		return archivoDAO.numeroArchivos();
	}
	
	/**
	 * <p>Borra toda la información de la tabla que contiene el nombre, el propietario y la ruta
	 * de los archivos</p>
	 * @return Verdadero si la operación fue bien
	 */
	public void borrarArchivos() {
		archivoDAO.borrarArchivos();
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para borrar
	 * un archivo concreto de la base de datos. Llama al método "borrarArchivo", privado, 
	 * con los parámetros necesarios</p>
	 * @param archivo Objeto de tipo Archivo que representa el archivo que se va a eliminar
	 * @return Verdadero si todo fue bien
	 */
	public boolean borrarArchivo(Archivo archivo) {
		return archivoDAO.borrarArchivo(archivo);
	}
	
	/**
	 * <p>Método que hace de interfaz de la clase con el resto de la aplicación para
	 * insertar un archivo. Llama al método "insertarArchivo", privado, con los parámetros
	 * necesarios.</p>
	 * @param archivo Objeto de tipo Archivo que representa el archivo a insertar
	 * @return Verdadero si todo fue bien
	 */
	public boolean insertarArchivo (Archivo archivo) {
		return archivoDAO.insertarArchivo(archivo);
	}
	
	/**
	 * <p>Método que hace de interfaz con el resto de la aplicación para buscar un archivo. Llama
	 * al método "buscarArchivo", privado, con los parámetros necesarios.</p>
	 * @param archivo Objeto de tipo archivo que representa el archivo a buscar
	 * @return Verdadero si el archivo se encuentra en la tabla
	 */
	public boolean buscarArchivo (Archivo archivo) {
		return archivoDAO.buscarArchivo(archivo);
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
		return archivoDAO.getArchivo(nombreArchivo, nombrePropietario);
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
		return archivoDAO.actualizarRutaArchivo(archivo, valor);
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
		return archivoDAO.actualizarNombreArchivo(archivo, valor);
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
		return archivoDAO.getTodosArchivos();
	}
	
	/**
	 * <p>Busca el usuario propietario del archivo en la tabla que almacena la información
	 * sobre los archivos. Para conocer al propietario de un archivo nos basta con saber la
	 * ruta de éste</p>
	 * @param archivo Objeto de tipo archivo que representa el archivo del cual queremos
	 * averiguar su propietario
	 * @return Objeto de tipo usuario que representa al propietario del archivo
	 */
	public Usuario getPropietario(Archivo archivo) {
		return archivoDAO.getPropietario(archivo);
	}
	
	/**
	 * <p>Obtiene el nombre del archivo a partir de su ruta</p>
	 * @param ruta Ruta del archivo del cual queremos obtener el nombre
	 * @return Devuelve una cadena con el nombre del archivo
	 */
	public String getNombreArchivo(String ruta) {
		return archivoDAO.getNombreArchivo(ruta);
	}
	/* </ARCHIVOS>*/
	
	/* <DATOS> */
	
	/**
	 * <p>Actualiza la base de datos con el nombre del usuario que ha sido
	 * el último en realizar un login en el servicio</p>
	 * @param usuario Usuario que acaba de entrar en el servicio
	 * @return Verdadero si la operación fue bien, falso en caso contrario
	 */
	public boolean actualizarDatosUltimoLogin(Usuario usuario) {
		return datosDAO.actualizarUltimoLogin(usuario);
	}
	
	/**
	 * <p>Actualiza la base de datos con el nombre del usuario que ha sido
	 * el último en darse de alta en el servicio</p>
	 * @param usuario Usuario que se acaba de registrar
	 * @return Verdadero si la operación fue bien, falso en caso contrario
	 */
	public boolean actualizarDatosUltimaAlta(Usuario usuario) {
		return datosDAO.actualizarUltimaAlta(usuario);
	}
	
	/**
	 * <p>Obtiene los datos del último usuario en entrar en el servicio</p>
	 * @return Objeto que representa al último usuario que ha utilizado
	 * el servicio
	 */
	public Usuario getDatosUltimoLogin() {
		return datosDAO.getUltimoLogin();
	}
	
	/**
	 * <p>Obtiene los datos del último usuario que se ha dado de alta en el servicio</p>
	 * @return Objeto que representa al último usuario en darse de alta
	 */
	public Usuario getDatosUltimaAlta() {
		return datosDAO.getUltimaAlta();
	}
	/* </DATOS>*/
}
