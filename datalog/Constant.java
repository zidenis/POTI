package datalog;


/**
 * This class extends PredicateElement and represents a constant of a Datalog
 * predicate.
 * 
 * @author Kevin Irmscher
 */

public abstract class Constant extends PredicateElement {

	/**
	 * Constant constructor. Calls the constructor of class PredicateElement.
	 * 
	 * @param name
	 *            (value) of constant
	 */
	public Constant(String name) {
		super(name);
	}

}
