package minicon;

import java.util.ArrayList;
import java.util.List;

import datalog.Constant;
import datalog.Predicate;
import datalog.PredicateElement;
import datalog.Variable;

/**
 * Class MCDMappings comprises the three Mapping objects that are used by the
 * algorithm.
 * 
 * Object varMap of class Mapping is the mapping from variables or constants of
 * the query to variables of a view.
 * 
 * Object constMap is the mapping from variables or constants of the query to
 * constants of a view.
 * 
 * Object rewritingMap is the mapping from head variables of the view to
 * variables or constants in the query. It is formed and used when rewritings
 * are created.
 * 
 * This class also contains methods that access both variable and constant
 * mappings.
 * 
 * @author Kevin Irmscher
 */
public class MCDMappings {

	/** variable mapping - query variable or constant to view variable */
	Mapping varMap;

	/** constant mapping - query variable or constant to view constant */
	//Mapping constMap;
	public Mapping constMap; /* C.BA */

	/** rewriting mapping - view head variables to query variabe or constant */
	Mapping rewritingMap;

	/**
	 * MCDMappings constructor
	 * 
	 * @param querySubgoal
	 *            relevant subgoal of the quey
	 * @param viewPred
	 *            relevant subogoal of the view
	 */
	public MCDMappings(Predicate querySubgoal, Predicate viewPred) {
		varMap = new Mapping();
		constMap = new Mapping();
		rewritingMap = new Mapping();
		mapPredicates(querySubgoal, viewPred);
	}

	/**
	 * MCDMappings constructor
	 */
	public MCDMappings() {
		varMap = new Mapping();
		constMap = new Mapping();
		rewritingMap = new Mapping();
	}

	/**
	 * The method will map every element of the query subgoal to the respective
	 * element of the view subgoal. When the view element is of type Constant, the
	 * predicate elements will be added to the constant mapping. Otherwise, the element is
	 * of type Variable so the predicate elements will be added to the variable mapping.
	 * 
	 * @param querySubgoal
	 *            relevant subgoal of the query
	 * @param viewPred
	 *            relevant view predicate
	 */
	public void mapPredicates(Predicate querySubgoal, Predicate viewPred) {

		for (int i = 0; i < querySubgoal.numberOfElements(); i++) {
			PredicateElement queryElem = querySubgoal.getElement(i);
			PredicateElement viewElem = viewPred.getElement(i);

			if (viewElem instanceof Constant) {
				Constant constant = (Constant) viewElem;
				constMap.map(queryElem, constant);

			} else {
				Variable var = (Variable) viewElem;
				varMap.map(queryElem, var);
			}
		}
	}

	/**
	 * The method will return the ith argument of the variable mapping.
	 * 
	 * @param i
	 *            the ith position of the argument
	 * @return PredicateElement object - constant or variable of the query
	 */
	public PredicateElement getVarMapArgument(Integer i) {
		return varMap.arguments.get(i);
	}

	/**
	 * The method will return the ith value of the variable mapping.
	 * 
	 * @param i
	 *            the ith position of the value
	 * @return Variable object of the view
	 */
	public Variable getVarMapValue(int i) {
		PredicateElement elem = varMap.values.get(i);
		if (elem instanceof Variable) {
			return (Variable) elem;
		} else {
			return null;
		}

	}

	/**
	 * The method will return the size of the variable mapping, i.e. the number
	 * of elements mapped.
	 * 
	 * @return size of variable mapping
	 */
	public int varMapSize() {
		return varMap.size();
	}

	/**
	 * The method will return all variables values that of the variable mapping
	 * as a list of Variable objects.
	 * 
	 * @return list of variable mapping values
	 */
	public List<Variable> getAllVarMapValues() {
		List<Variable> varList = new ArrayList<Variable>();
		for (PredicateElement elem : varMap.values) {
			varList.add((Variable) elem);
		}
		return varList;
	}

	/**
	 * If argument value is a constant, the method will return all elements of
	 * the query (variables or constants) that are mapped in the constant
	 * mapping to this constant. Otherwise, if argument value is a variable, the
	 * arguments of the variable mapping for this value will be returned.
	 * 
	 * @param value
	 *            variable or constant of the view
	 * @return list of variables and/or constants
	 */
	public List<PredicateElement> getArguments(PredicateElement value) {
		if (value instanceof Constant) {
			return getConstMapArguments((Constant) value);
		} else {
			return getVarMapArguments((Variable) value);
		}
	}

	/**
	 * Called by getArguments. The method will return the respective arguments
	 * of variable 'value' contained in the variable mapping.
	 * 
	 * @param value
	 *            view variable
	 * @return list of variables and/or constants
	 */
	private List<PredicateElement> getVarMapArguments(PredicateElement value) {
		return varMap.getArguments(value);
	}

	/**
	 * Called by getArguments. The method will return the respective arguments
	 * of constant 'value' contained in the constant mapping.
	 * 
	 * @param cons
	 *            view constant
	 * @return list of variables and/or constants
	 */
	private List<PredicateElement> getConstMapArguments(Constant cons) {
		return constMap.getArguments(cons);
	}

	/**
	 * The method will return all values for the argument 'arg' contained in
	 * variable and constant mapping.
	 * 
	 * @param arg
	 *            constant or variable of the query
	 * @return list of variables and/or constants
	 */
	public List<PredicateElement> getValues(PredicateElement arg) {
		List<PredicateElement> vals = new ArrayList<PredicateElement>();

		vals.addAll(varMap.getValues(arg));
		vals.addAll(constMap.getValues(arg));

		return vals;
	}

	/**
	 * Overwrites Object method.
	 * 
	 * The objects of class MCDMappings are equal if
	 * 
	 * 1. the variable mapping is the same
	 * 
	 * 2. the constant mapping is the same
	 */
	public boolean equals(Object obj) {

		MCDMappings map = (MCDMappings) obj;
		Mapping compareVarMap = map.varMap;

		for (int i = 0; i < compareVarMap.size(); i++) {
			PredicateElement arg = compareVarMap.arguments.get(i);
			PredicateElement val = compareVarMap.values.get(i);

			if (!this.varMap.contains(arg, val)) {
				return false;
			}
		}

		Mapping constMap = map.constMap;
		for (int i = 0; i < constMap.size(); i++) {
			PredicateElement arg = constMap.arguments.get(i);
			PredicateElement val = constMap.values.get(i);

			if (!constMap.contains(arg, val)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Overwrites Object method.
	 * 
	 * The method will create a new MCDMapping object and set the references of
	 * variable and constant mappings to cloned objects of both.
	 */
	public MCDMappings clone() {

		MCDMappings newMapping = new MCDMappings();
		newMapping.varMap = varMap.clone();
		newMapping.constMap = constMap.clone();

		return newMapping;
	}

	/**
	 * Overwrites Object method.
	 * 
	 * It will return a String containing variable and constant mapping.
	 */
	public String toString() {
		return varMap.toString() + "\n" + constMap.toString();
	}
}
