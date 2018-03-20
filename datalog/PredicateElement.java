package datalog;

/**
 * Class PredicateElement represents an element of a Datalog predicate. An
 * element can either be a variable or a constant which in turn can be a
 * numerical constant of string constant.
 * 
 * @author Kevin Irmscher
 */
public class PredicateElement {

	/** name (value) of predicate element */
	public String name;

	/**
	 * PredicateElement constructor
	 * 
	 * @param name
	 *            (value) of predicate element
	 */
	public PredicateElement(String name) {
		this.name = name;
	}

	/**
	 * Overwrites Object method. Returns name of predicate element.
	 */
	public String toString() {
		return name;
	}

	/**
	 * Overwrites Object method. Returns true if names are equal.
	 */
	public boolean equals(Object elem) {
		return this.name.equals(((PredicateElement) elem).name);
	}

}
