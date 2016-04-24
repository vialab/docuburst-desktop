package test.suite;

import java.util.Enumeration;

import test.bd.archivos.TestActualizarArchivos;
import test.bd.archivos.TestBorradoArchivos;
import test.bd.archivos.TestInsercionArchivos;
import test.bd.usuarios.TestActualizarUsuarios;
import test.bd.usuarios.TestBorradoUsuarios;
import test.bd.usuarios.TestComprobarUsuario;
import test.bd.usuarios.TestInsercionUsuarios;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class TestGeneral {
	
	private static TestSuite suite;
	private static Enumeration<TestFailure> failures;
	private static Enumeration<TestFailure> errors;
	private static int runCount;
	
	public static Test suite() {
		suite = new TestSuite("Test General");
		
		TestSuite suiteUsuarios = new TestSuite("Test Usuarios");
		TestSuite suiteArchivos = new TestSuite("Test Archivos");
		
		/* <USUARIOS> */
		TestSuite suiteA1 = new TestSuite("ComprobarUsuarios");
		suiteA1.addTest(new TestSuite(TestComprobarUsuario.class));
		TestSuite suiteB1 = new TestSuite("BorradoUsuarios");
		suiteB1.addTest(new TestSuite(TestBorradoUsuarios.class));
		TestSuite suiteC1 = new TestSuite("InserciónUsuarios");
		suiteC1.addTest(new TestSuite(TestInsercionUsuarios.class));
		TestSuite suiteD1 = new TestSuite("ActualizacionUsuarios");
		suiteD1.addTest(new TestSuite(TestActualizarUsuarios.class));
		
		suiteUsuarios.addTest(suiteA1);
		suiteUsuarios.addTest(suiteB1);
		suiteUsuarios.addTest(suiteC1);
		suiteUsuarios.addTest(suiteD1);
		/* </USUARIOS> */
		
		/* <ARCHIVOS> */
		TestSuite suiteA2 = new TestSuite("InserciónArchivos");
		suiteA2.addTest(new TestSuite(TestInsercionArchivos.class));
		TestSuite suiteB2 = new TestSuite("BorradoArchivos");
		suiteB2.addTest(new TestSuite(TestBorradoArchivos.class));
		TestSuite suiteC2 = new TestSuite("ActualizarArchivos");
		suiteC2.addTest(new TestSuite(TestActualizarArchivos.class));
		
		suiteArchivos.addTest(suiteA2);
		suiteArchivos.addTest(suiteB2);
		suiteArchivos.addTest(suiteC2);
		/* </ARCHIVOS> */
		
		suite.addTest(suiteUsuarios);
		suite.addTest(suiteArchivos);
		
		return suite;
	}
	
	public static void main(String[] args) {
		Test test = suite();
		TestResult tr = new TestResult();
		test.run(tr);
		
		failures = tr.failures();
		errors = tr.errors();
		runCount = tr.runCount();
	}
	
	public static Enumeration<TestFailure> getFailures() {
		return failures;
	}
	
	public static Enumeration<TestFailure> getErrors() {
		return errors;
	}
	
	public static int getRunCount() {
		return runCount;
	}

}


