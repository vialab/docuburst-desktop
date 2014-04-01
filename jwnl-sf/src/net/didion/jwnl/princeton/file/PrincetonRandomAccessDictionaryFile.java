/**
 * Java WordNet Library (JWNL)
 * See the documentation for copyright information.
 */
package net.didion.jwnl.princeton.file;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLRuntimeException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.file.DictionaryFile;
import net.didion.jwnl.dictionary.file.DictionaryFileType;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import com.imagero.uio.RandomAccessBufferRO;
import com.imagero.uio.RandomAccessFactory;

/**
 * A <code>RandomAccessDictionaryFile</code> that accesses files named with
 * Princeton's dictionary file naming convention.
 */
public class PrincetonRandomAccessDictionaryFile extends
		AbstractPrincetonRandomAccessDictionaryFile {
	/** Read-only file permission. */
	public static final String READ_ONLY = "r";
	/** Read-write file permission. */
	public static final String READ_WRITE = "rw";

	/** The random-access file. */
	private RandomAccessBufferRO _file = null; // use over the web and uses
												// buffered IO
	// private RandomAccessFile _file = null; // doesn't use buffered IO; very
	// slow

	private String filename;
	/** The file permissions to use when opening a file. */
	protected String _permissions;
	
	private long length;
	
	public DictionaryFile newInstance(String path, POS pos,
			DictionaryFileType fileType) {
		return new PrincetonRandomAccessDictionaryFile(path, pos, fileType);
	}

	public PrincetonRandomAccessDictionaryFile() {
	}

	public PrincetonRandomAccessDictionaryFile(String path, POS pos,
			DictionaryFileType fileType) {
		this(path, pos, fileType, READ_ONLY);
	}

	public PrincetonRandomAccessDictionaryFile(String path, POS pos,
			DictionaryFileType fileType, String permissions) {
		super(path, pos, fileType);
		_permissions = permissions;
	}

	public String readLine() throws IOException {
		if (isOpen()) {
			return _file.readLine();
		} else {
			throw new JWNLRuntimeException("PRINCETON_EXCEPTION_001");
		}
	}

	public void seek(long pos) throws IOException {
		_file.seek(pos);
	}

	public long getFilePointer() throws IOException {
		return _file.getFilePointer();
	}

	public boolean isOpen() {
		return _file != null;
	}

	public void close() {
		try {
			_file.close();
		} catch (Exception ex) {
		} finally {
			_file = null;
		}
	}

	protected void openFile(File path) throws IOException {

		DataInputStream is = new DataInputStream(
				streamFromFile(path.toString()));
		_file = RandomAccessFactory.createBufferedRO(is);
		filename = path.getName();
		try { 
			length = FileLengths.get(filename+JWNL.getVersion().getNumber());
		} catch (NullPointerException e) {
			throw new IOException("Unknown file: " + filename + ". Check FileLengths map.");
		}
	}

	/**
	 * Return the total length of this file -- in Buffer version, need to return
	 * total max length buffer (although what's been read so far may be less).
	 */
	public long length() throws IOException {
		return length;
		// return _file.length();
	}

	public int read() throws IOException {
		return _file.read();
	}

	public static InputStream streamFromFile(String location)
			throws IOException {
		InputStream is = null;

		// try to get a working url from the string
		URL url = PrincetonRandomAccessDictionaryFile.class
				.getResource(location);
		if (url == null && !location.startsWith("/"))
			url = PrincetonRandomAccessDictionaryFile.class.getResource("/"
					+ location);

		if (url != null) {
			is = url.openStream();
		} else {
			// if that failed, try the file system
			File f = new File(location);
			if (f.exists())
				is = new FileInputStream(f);
		}

		if (is == null) {
			return null; // couldn't find it
		}
		return is;
	}

}