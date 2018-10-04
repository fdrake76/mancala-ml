package com.freddrake.mancala.mancalaml;

/**
 * Application-level exception if something goes wrong.
 */
public class MancalaException extends RuntimeException {

	public MancalaException(String string) {
		super(string);
	}
	
	public MancalaException(Throwable t) {
		super(t);
	}

}
