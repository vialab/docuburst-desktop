package ca.utoronto.cs.wordnetexplorer.data;

import java.util.EventObject;

public class LoadEvent<T> extends EventObject {

	private static final long serialVersionUID = 1L;

	private T item;
		
	public T getItem() {
		return item;
	}
		
	public LoadEvent(Object source, T item) {
		super(source);
		this.item = item;
	}
}
