package minicon;

import java.util.ArrayList;
import java.util.List;

import datalog.PredicateElement;

/**
 * Class Mapping exhibits the functionality to map elements of type
 * PredicateElement to elements of the same type. The former are called
 * arguments and the latter values.
 * 
 * Both arguments and values are stored in a list. The ith element of the
 * argument belongs to the ith element of the value list.
 * 
 * @author Kevin Irmscher
 */
public class Mapping {

	/** list of arguments */
	//protected List<PredicateElement> arguments;
	public List<PredicateElement> arguments; /* C.BA */

	/** list of values */
	protected List<PredicateElement> values;

	/**
	 * Mapping Constructor
	 */
	public Mapping() {
		arguments = new ArrayList<PredicateElement>();
		values = new ArrayList<PredicateElement>();

	}

	/**
	 * The method will map an object of class PredicateElement (arg) to an
	 * object of the same class (value) by adding them to the their respective
	 * list. Both elements will be added at the same position of the lists.
	 * 
	 * @param arg
	 *            mapping argument
	 * @param value
	 *            mapping value
	 */
	public void map(PredicateElement arg, PredicateElement value) {
		if (!contains(arg, value)) {
			arguments.add(arg);
			values.add(value);
		}
	}

	/**
	 * The method will return all arguments for the given value
	 * 
	 * @param value
	 * @return list of mapping arguments
	 */
	public List<PredicateElement> getArguments(PredicateElement value) {

		List<PredicateElement> args = new ArrayList<PredicateElement>();

		for (int i = 0; i < values.size(); i++) {
			PredicateElement current = values.get(i);
			if (current.equals(value)) {
				PredicateElement arg = arguments.get(i);
				if (!args.contains(arg)) { // don't add a duplicate
					args.add(arg);
				}
			}
		}
		return args;
	}

	/**
	 * This method will return all values for the given argument.
	 * 
	 * @param argument
	 * @return list of mapping values
	 */
	public List<PredicateElement> getValues(PredicateElement argument) {
		List<PredicateElement> vals = new ArrayList<PredicateElement>();

		for (int i = 0; i < arguments.size(); i++) {
			PredicateElement current = arguments.get(i);
			if (current.equals(argument)) {
				PredicateElement value = values.get(i);
				if (!vals.contains(value)) { // don't add a duplicate
					vals.add(value);
				}
			}
		}
		return vals;
	}

	/**
	 * The method will test whether the mapping contains both argument and value
	 * 
	 * @param argument
	 *            mapping argument
	 * @param value
	 *            mapping value
	 * @return true, if mapping contains argument and value, false otherwise
	 */
	public boolean contains(PredicateElement argument, PredicateElement value) {

		for (int i = 0; i < arguments.size(); i++) {
			PredicateElement arg = arguments.get(i);
			PredicateElement val = values.get(i);
			if ((arg.equals(argument)) && (val.equals(value))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The method will test wether mapping contains argument (arg)
	 * 
	 * @param arg
	 *            mapping argument
	 * @return true, if arg is contained in the mapping, false otherwise
	 */
	public boolean containsArgument(PredicateElement arg) {
		for (PredicateElement elem : arguments) {
			if (arg.equals(elem)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method will return first value that is found for given argument
	 * (arg)
	 * 
	 * @param arg
	 *            mapping argument
	 * @return first value for the given argument, null if no value found
	 */
	public PredicateElement getFirstMatchingValue(PredicateElement arg) {
		for (int i = 0; i < arguments.size(); i++) {
			if (arguments.get(i).equals(arg)) {
				return values.get(i);
			}
		}
		return null;
	}

	/**
	 * The method will return the size of the mapping, i.e. number of arguments.
	 * 
	 * @return size of mapping
	 */
	public int size() {
		return arguments.size();
	}

	/**
	 * Overwrites Object method.
	 * 
	 * The method will create a new Mapping object and will map every
	 * argument-value pair to the new mapping that will be returned.
	 */
	public Mapping clone() {
		Mapping newMapping = new Mapping();
		for (int i = 0; i < arguments.size(); i++) {
			newMapping.map(arguments.get(i), values.get(i));
		}
		return newMapping;
	}

	/**
	 * Overwrites Object method.
	 * 
	 * Return String that every argument-value pair.
	 */
	public String toString() {
		String output = "";
		for (int i = 0; i < arguments.size(); i++) {
			output += arguments.get(i) + " -> " + values.get(i) + "; ";
		}
		return output;
	}

}
