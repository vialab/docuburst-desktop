package ca.utoronto.cs.prefuseextensions.swing;

import java.util.EventObject;

public class ValueChangedEvent<T> extends EventObject { 

	T value;
	
	public ValueChangedEvent(Object source, T newValue) {
		super(source);
		value = newValue;
	}

	public T getValue() {
		return value;
	}
	
}

