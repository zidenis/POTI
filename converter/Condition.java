package converter;

import datalog.PredicateElement;
import datalog.Variable;

/**
 * Class Conditon represents the WHERE clause of an SQL query. It is composed of
 * three parts. The left side consists of a relation alias and the relevant
 * variable. The right side also contains a relation alias and a
 * PredicateElement object which can be a variable or a constant.
 * 
 * @author Kevin Irmscher
 */
public class Condition {

	/** alias name of left relation */
	private String leftAlias;

	/** variable of the left side */
	private Variable leftElement;

	/** alias name of right relation */
	private String rightAlias;

	/** right element - either variable or constant */
	private PredicateElement rightElement;

	/** comparator */
	private String comparator;

	/**
	 * Condition constructor
	 * 
	 * @param leftAlias
	 *            alias of left relation
	 * @param leftElement
	 *            variabel
	 * @param rightAlias
	 *            alias of right relation
	 * @param rightElement
	 *            constant or variable
	 * @param comparator
	 *            comparator symbol
	 */
	public Condition(String leftAlias, Variable leftElement, String rightAlias,
			PredicateElement rightElement, String comparator) {
		this.leftAlias = leftAlias;
		this.leftElement = leftElement;
		this.rightAlias = rightAlias;
		this.rightElement = rightElement;
		this.comparator = comparator;
	}

	/**
	 * Returns comparator of condition
	 * 
	 * @return comparator symbol
	 */
	public String getComparator() {
		return comparator;
	}

	/**
	 * Returns true if right side of condition is a constant.
	 * 
	 * @return true, if right side is constant, false otherwise
	 */
	public boolean rightSideIsConstant() {
		return rightAlias.length() == 0;
	}

	/**
	 * Returns the variable of the left side of the condition
	 * 
	 * @return variable of left side
	 */
	public Variable leftElement() {
		return leftElement;
	}

	/**
	 * Returns alias name of left relation.
	 * 
	 * @return alias of left relation
	 */
	public String leftAlias() {
		return leftAlias;
	}

	/**
	 * Returns right element of the condition which can either be a variable or
	 * a constant.
	 * 
	 * @return right element of condition
	 */
	public PredicateElement rightElement() {
		return rightElement;
	}

	/**
	 * Returns alias name of right relation.
	 * 
	 * @return alias of right relation
	 */
	public String rightAlias() {
		return rightAlias;
	}

	/**
	 * Overwrites object method.
	 */
	public String toString() {
		String s;
		if (rightAlias.length() > 0) {
			s = leftAlias + "." + leftElement + " " + comparator + " "
					+ rightAlias + "." + rightElement;
		} else {
			s = leftAlias + "." + leftElement + " " + comparator + " "
					+ rightElement;
		}
		return s;
	}

}
