package datalog;


/**
 * This class extends PredicateElement and represents a variable of a Datalog
 * query.
 */

public class Variable extends PredicateElement {

	/**
	 * Variable constructor. Calls the PredicateElement constructor.
	 * 
	 * @param name
	 *            (value) of variable
	 */
	public Variable(String name) {
		super(name);
	}

}
