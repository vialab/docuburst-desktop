package es.project.bd.objetos;

/**
 * <p>Clase que encapsula las propiedades que se van a manejar de los archivos</p>
 * @author Daniel Fernández Aller
 */
public class Archivo {
	
	private static boolean accesoRoot = false;
	/**
	 * <p>Atributos del objeto Archivo</p>
	 */
	private String nombreArchivo, nombrePropietario, rutaArchivo;
	
	/**
	 * <p>Constructor por defecto</p>
	 */
	public Archivo() {}
	
	/**
	 * <p>Crea un archivo y le asigna un nombre</p>
	 * @param nombre Nombre del archivo en su creación
	 */
	public Archivo(String nombre) {
		this.nombreArchivo = nombre;
	}
	
	/**
	 * <p>Crea un archivo y le asigna un nombre, el nombre de su propietario y su ruta</p>
	 * @param nombreArchivo Nombre del archivo
	 * @param nombrePropietario Nombre del propietario del archivo
	 * @param ruta Ruta del archivo
	 */
	public Archivo(String nombreArchivo, String nombrePropietario, String ruta) {
		this(nombreArchivo);
		this.nombrePropietario = nombrePropietario;
		this.rutaArchivo = ruta;
	}
	
	/**
	 * <p>Crea un archivo y le asigna un nombre, un usuario propietario y su ruta</p>
	 * @param nombreArchivo Nombre del archivo
	 * @param propietario Usuario propietario del archivo
	 * @param ruta Ruta del archivo
	 */
	public Archivo(String nombreArchivo, Usuario propietario, String ruta) {
		this(nombreArchivo, propietario.getNombre(), ruta);
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve el nombre del archivo
	 */
	public String getNombreArchivo() {
		return nombreArchivo;
	}

	/**
	 * <p>Asigna un nombre al archivo</p>
	 * @param nombreArchivo Nombre que se va a asignar
	 */
	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve el nombre del propietario
	 */
	public String getNombrePropietario() {
		return nombrePropietario;
	}

	/**
	 * <p>Asigna un propietario al archivo</p>
	 * @param nombrePropietario Nombre del propietario a asignar
	 */
	public void setNombrePropietario(String nombrePropietario) {
		this.nombrePropietario = nombrePropietario;
	}

	/**
	 * <p>Accede al atributo</p>
	 * @return Devuelve la ruta del archivo
	 */
	public String getRutaArchivo() {
		return rutaArchivo;
	}

	/**
	 * <p>Asigna una ruta al archivo</p>
	 * @param rutaArchivo Ruta que se va a asignar
	 */
	public void setRutaArchivo(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
	}
	
	/**
	 * <p>Método toString de la clase: muestra el nombre del archivo o su ruta dependiendo
	 * de que el usuario de la web sea el administrador o no. Es necesario, puesto que el
	 * root necesita la ruta completa para poder borrar un archivo, mientras que el usuario
	 * propietario sólo necesita el nombre</p>
	 * @return Cadena con el nombre del archivo
	 */
	public String toString() {
		if (this.isAccesoRoot())
			return this.getRutaArchivo();
		else return this.getNombreArchivo();
	}
	
	/**
	 * <p>Encadena de manera ordenada el nombre del archivo, su propietario y su ruta</p>
	 * @return Cadena con la información del archivo formateada de cierta manera
	 */
	public String verInformacionArchivo() {
		return this.getNombreArchivo() + "(" + this.getNombrePropietario() + ")   " + this.getRutaArchivo();
	}

	/**
	 * <p>Accede al atributo de la clase</p>
	 * @return Verdadero si el usuario en sesión es el root, falso si es un usuario normal
	 */
	public static boolean isAccesoRoot() {
		return accesoRoot;
	}

	/**
	 * <p>Establece el valor del atributo de la clase</p>
	 * @param accesoRoot Verdadero si el usuario en sesión es el root, falso en caso contrario
	 */
	public static void setAccesoRoot(boolean accesoRoot) {
		Archivo.accesoRoot = accesoRoot;
	}
}
