package datalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Class DatalogQuery is used to represent the query as well as one or more
 * views that are provided as user input.
 * 
 * A DatalogQuery object consists of the head and the body. The head contains
 * head variables and the body consists of predicates and/or interpreted
 * predicates.
 * 
 * @author Kevin
 */

public class DatalogQuery {

	/** name of the Datalog query, i.e. name of head predicate */
	private String name;

	/** head variables */
	private List<Variable> headVariables;

	/** predicates of the body */
	private List<Predicate> predicates;

	/** interpreted predicates of the body */
	private List<InterpretedPredicate> interpretedPredicates;

	/**
	 * DatalogQuery constructor
	 */
	public DatalogQuery() {
		headVariables = new ArrayList<Variable>();
		predicates = new ArrayList<Predicate>();
		interpretedPredicates = new ArrayList<InterpretedPredicate>();
	}

	/**
	 * DatalogQuery constructor
	 * 
	 * @param name
	 *            of the Datalog query
	 */
	public DatalogQuery(String name) {
		this.name = name;
		headVariables = new ArrayList<Variable>();
		predicates = new ArrayList<Predicate>();
		interpretedPredicates = new ArrayList<InterpretedPredicate>();
	}

	/**
	 * Set query name
	 * 
	 * @param name
	 *            of query
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns Datalog query name.
	 * 
	 * @return name of Datalog query
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Adds a head variable to Datalog query.
	 * 
	 * @param var
	 *            head variable
	 */
	public void addHeadVariable(Variable var) {
		headVariables.add(var);
	}

	/**
	 * Returns a head variables of Datalog query
	 * 
	 * @return list of head variables
	 */
	public List<Variable> getHeadVariables() {
		return headVariables;
	}

	/**
	 * Finds a predicate with the given argument (name)
	 * 
	 * @param name
	 *            of the predicate that is to be found
	 * @return Datalog query predicate, null if no matching predicate will be
	 *         found
	 */
	public Predicate getPredicate(String name) {

		for (Predicate pred : predicates) {
			if (pred.name.equals(name)) {
				return pred;
			}
		}
		return null;
	}

	/**
	 * Returns predicates of Datalog query
	 * 
	 * @return list of predicates
	 */
	public List<Predicate> getPredicates() {
		return predicates;
	}

	/**
	 * Returns interpreted predicates of Datalog query
	 * 
	 * @return list of interpreted predicates
	 */
	public List<InterpretedPredicate> getInterpretedPredicates() {
		return interpretedPredicates;
	}

	/**
	 * Sets the reference of predicates to the list of predicates (preds) given
	 * as argument.
	 * 
	 * @param preds
	 *            list of predicates
	 */
	public void setPredicates(List<Predicate> preds) {
		this.predicates = preds;
	}

	/**
	 * Returns true if Datalog query contains the head variable that is provided
	 * as argument. If argument elem is not of type Variable, false will be
	 * returned.
	 * 
	 * @param var
	 *            head variable that is tested to be contained
	 * @return true, if variable is in the head, false otherwise
	 */
	public boolean containsHeadVariable(PredicateElement elem) {
		if (elem instanceof Variable) {
			return headVariables.contains(elem);
		} else
			return false;
	}

	/**
	 * Adds a predicate to the Datalog query.
	 * 
	 * @param predicate
	 *            Predicate to be added
	 */
	public void addPredicate(Predicate predicate) {
		predicates.add(predicate);
	}

	/**
	 * Adds an interpreted predicate to the Datalog query.
	 * 
	 * @param predicate
	 *            interpreted predicate to be added
	 */
	public void addInterpretedPredicate(InterpretedPredicate predicate) {
		interpretedPredicates.add(predicate);
	}

	/**
	 * The method will return list of variables that are existential in the
	 * Datalog query, i.e. that are not in the head.
	 * 
	 * @return list of existential variables
	 */
	public List<Variable> getExistentialVariables() {
		List<Variable> vars = new ArrayList<Variable>();
		List<Variable> existentVars = new ArrayList<Variable>();

		// collect all variables
		for (Predicate pred : predicates) {
			vars.addAll(pred.getVariables());
		}

		for (Variable var : vars) {
			// variable is not contained in the head and not already
			// in the list of existential variables
			if (!headVariables.contains(var) && (!existentVars.contains(var))) {
				existentVars.add(var);
			}
		}
		return existentVars;
	}

	/**
	 * The method will return the the number of predicates of the Datalog query
	 * (without interpreted predicates)
	 * 
	 * @return number of predicates
	 */
	public int numberOfPredicates() {
		return predicates.size();
	}

	/**
	 * Overwrites object method. Returns a String representation of the Datalog
	 * query.
	 */
	public String toString() {
		String preds = printCollection(predicates);
		String interpretedPreds = "";
		if (printCollection(interpretedPredicates).length() > 0) {
			interpretedPreds = "," + printCollection(interpretedPredicates);
		}
		String val = name + "(" + printCollection(headVariables) + ") :- "
				+ preds + interpretedPreds;
		return val;
	}

	/**
	 * String representation of either a list of head variables or a list of
	 * predicates.
	 * 
	 * @param coll
	 *            list of head variables of predicates
	 * @return String of the form "(list1Elem1,...)"
	 */
	private String printCollection(Collection collect) {
		String val = "";
		for (Object obj : collect) {
			val = val + "," + obj.toString();
		}
		val = val.replaceFirst(",", "");
		return val;
	}
	
	
}
