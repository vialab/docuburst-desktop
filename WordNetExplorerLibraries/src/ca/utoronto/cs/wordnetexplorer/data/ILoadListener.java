package ca.utoronto.cs.wordnetexplorer.data;

import java.util.EventListener;

public interface ILoadListener<T> extends EventListener {

	public abstract void dataLoaded(LoadEvent<T> e);
	
}
