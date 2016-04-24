package es.project.bd.abstractos;

import es.project.bd.objetos.Usuario;

/**
 * <p>Clase abstracta con las declaraciones de los métodos referidos a las operaciones
 * que se realizan con los datos sobre los logins y las altas en la base de datos</p>
 * @author Daniel Fernández Aller
 */
public abstract class DatosDAO {

	/**
	 * <p>Actualiza el nombre del último usuario que ha utilizado el servicio</p>
	 * @param usuario Objeto que representa al último usuario que ha utilizado
	 * el servicio
	 * @return Verdadero si la operación salió bien, falso en caso contrario
	 */
	public abstract boolean actualizarUltimoLogin(Usuario usuario);
	
	/**
	 * <p>Actualiza el nombre del último usuario que se ha dado de alta en el
	 * servicio</p>
	 * @param usuario Objeto que representa al último usuario dado de alta en el
	 * servicio
	 * @return Verdadero si la operación salió bien, falso en caso contrario
	 */
	public abstract boolean actualizarUltimaAlta(Usuario usuario);
	
	/**
	 * <p>Accede al nombre del último usuario en loguearse</p>
	 * @return Objeto usuario que representa el último login en el servicio
	 */
	public abstract Usuario getUltimoLogin();
	
	/**
	 * <p>Accede al nombre del último usuario en darse de alta</p>
	 * @return Objeto usuario que representa al último usuario que se ha dado
	 * de alta en el servicio
	 */
	public abstract Usuario getUltimaAlta();
}
