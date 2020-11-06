/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.backend;

/**
 * Exception thrown by a theorem prover backend when something
 * backend-specific goes wrong.
 * 
 * @author Max Schaefer
 *
 */

public class ProverException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a prover exception from a given error message.
	 * 
	 * @param msg the error message
	 */
	public ProverException(String msg) {
		super(msg);
	}
	
	/**
	 * Constructs a prover exception from a given error message
	 * and another exception (presumably the cause of this
	 * exception).
	 * 
	 * @param msg the error message
	 * @param cause the underlying exception causing this one
	 */
	public ProverException(String msg, Exception cause) {
		super(msg, cause);
	}

}
