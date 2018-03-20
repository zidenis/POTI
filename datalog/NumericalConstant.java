package datalog;


/**
 * This class extends class Constant and represents a numerical constant of a
 * Datalog predicate.
 * 
 * @author Kevin Irmscher
 */

public class NumericalConstant extends Constant {

	/**
	 * NumericalConstant constructor.
	 * 
	 * @param number
	 *            value of constant
	 */
	public NumericalConstant(String number) {
		super(number);
	}

}
