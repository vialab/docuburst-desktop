package es.project.bd.objetos;

import java.util.List;

/**
 * <p>Clase que encapsula las propiedades que se van a manejar de los usuarios</p>
 * @author Daniel Fernández Aller
 */
public class Usuario {
	/**
	 * <p>Atributos del objeto Usuario</p>
	 */
	private String nombre, password, fecha_alta, ultimo_login, email, uuid;
	
	/**
	 * <p>Verdadero si el usuario ya recibió el mail de verificación y
	 * terminó el alta en el servicio</p>
	 */
	private boolean activado;
	/**
	 * <p>Lista de archivos del objeto Usuario</p>
	 */
	private List<Archivo> listaArchivos;
	
	/**
	 * <p>Constructor por defecto</p>
	 */
	public Usuario() {}
	
	/**
	 * <p>Crea un usuario a partir del nombre. Se utiliza para eliminar los usuarios, en las tareas
	 * de root</p>
	 * @param nombre Nombre del usuario
	 */
	public Usuario(String nombre) {
		this.nombre = nombre;
	}
	 
	/**
	 * <p>Crea un usuario y le asigna un nombre, un password, una dirección de email y el estado
	 * de activación de su cuenta</p>
	 * @param nombre Nombre del usuario
	 * @param password Password del usuario
	 * @param email Dirección de email del usuario
	 * @param activado Estado de alta de su cuenta
	 */
	public Usuario(String nombre, String password, String email, boolean activado) {
		this(nombre,password,email);
		this.activado = activado;
	}
	
	/**
	 * <p>Crea un usuario y le asigna un nombre, un password y una dirección de email</p>
	 * @param nombre Nombre del usuario
	 * @param password Password del usuario
	 * @param email Dirección de email del usuario
	 */
	public Usuario(String nombre, String password, String email) {
		this(nombre, password);
		this.email = email;
	}
	
	/**
	 * <p>Crea un usuario y le asigna un nombre y un password</p>
	 * @param nombre Nombre del usuario
	 * @param password Password del usuario
	 */
	public Usuario(String nombre, String password) {
		this.nombre = nombre;
		this.password = password;
	}
	
	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve el nombre del usuario
	 */
	public String getNombre() {
		return nombre;
	}
	
	/**
	 * <p>Asigna un nombre al usuario</p>
	 * @param nombre Nombre a asignar
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve el password del usuario
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * <p>Asigna un password al usuario</p>
	 * @param password Password a asignar
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * <p>Método toString de la clase: muestra el nombre del usuario</p>
	 * @return Cadena con el nombre del usuario
	 */
	public String toString() {
		return this.getNombre(); 
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve la lista de archivos del usuario
	 */
	public List<Archivo> getListaArchivos() {
		return listaArchivos;
	}

	/**
	 * <p>Asigna una lista de archivos al usuario</p>
	 * @param listaArchivos Lista de archivos a asignar
	 */
	public void setListaArchivos(List<Archivo> listaArchivos) {
		this.listaArchivos = listaArchivos;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve una cadena con la fecha en la que el usuario se dio de alta en 
	 * el servicio, con el formato yyyy-mm-dd
	 */
	public String getFecha_alta() {
		return fecha_alta;
	}

	/**
	 * <p>Modifica el atributo</p>
	 * @param fecha_alta Fecha a asignar como alta de usuario
	 */
	public void setFecha_alta(String fecha_alta) {
		this.fecha_alta = fecha_alta;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve una cadena con la fecha del último login del usuario con el formato
	 * yyyy-mm-dd
	 */
	public String getUltimo_login() {
		return ultimo_login;
	}

	/**
	 * <p>Modifica el atributo</p>
	 * @param ultimo_login Fecha a asignar como último login del usuario
	 */
	public void setUltimo_login(String ultimo_login) {
		this.ultimo_login = ultimo_login;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve una cadena con la dirección de email del usuario
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * <p>Asigna un mail al usuario</p>
	 * @param email Dirección de email a asignar
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Verdadero si el usuario completó el alta en el servicio
	 */
	public boolean isActivado() {
		return activado;
	}

	/**
	 * <p>Modifica el valor del atributo</p>
	 * @param activado Valor que va a tomar el atributo
	 */
	public void setActivado(boolean activado) {
		this.activado = activado;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Cadena con un identificador universal que diferencia a cada usuario
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * <p>Modifica el valor del atributo</p>
	 * @param uuid Valor que va a identificar el atributo
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}
