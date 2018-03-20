package datalog;


/**
 * Class InterpretedPredicate represents a predicate of the form x < y, x <= y,
 * x > y or x >= y. It must be either the case that x is a variable and y is a
 * constant or vice versa.
 * 
 * @author Kevin Irmscher
 */

public class InterpretedPredicate {

	/** left element of the comparision */
	private PredicateElement leftSide;

	/** right element of the comparision */
	private PredicateElement rightSide;

	/** comparision symbol */
	private String comparator;

	/**
	 * InterpretedPredicate constructor
	 * 
	 * @param left
	 *            element
	 * @param right
	 *            element
	 * @param comparator
	 *            comparision symbol
	 */
	public InterpretedPredicate(PredicateElement left, PredicateElement right,
			String comparator) {
		this.leftSide = left;
		this.rightSide = right;
		this.comparator = comparator;
	}

	/**
	 * Returns left hand side of the comparision
	 * 
	 * @return left element
	 */
	public PredicateElement getLeft() {
		return leftSide;
	}

	/**
	 * Returns right hand side of the comparision
	 * 
	 * @return right element
	 */
	public PredicateElement getRight() {
		return rightSide;
	}

	/**
	 * Returns variable of the comparision.
	 * 
	 * @return variable
	 */
	public Variable getVariable() {
		if (leftSide instanceof Variable) {
			return (Variable) leftSide;
		} else {
			return (Variable) rightSide;
		}
	}

	/**
	 * Returns comparision symbol.
	 * 
	 * @return comparision symbol
	 */
	public String getComparator() {
		return comparator;
	}

	/**
	 * Overwrites Object method. Returns String representation of an interpreted
	 * predicate.
	 */
	public String toString() {
		return leftSide + " " + comparator + " " + rightSide;
	}
}
