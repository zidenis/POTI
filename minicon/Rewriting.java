package minicon;

import java.util.ArrayList;
import java.util.List;

import datalog.DatalogQuery;
import datalog.InterpretedPredicate;
import datalog.Predicate;
import datalog.PredicateElement;
import datalog.Variable;

/**
 * Class Rewriting represents the union of one or more MCDs which all together
 * form a valid rewriting of the query.
 * 
 * A rewriting consists of a list of MCDs with the relevant predicates. They do
 * not contain interpreted predicates that consists of a variable that maps to a
 * distinguished view variable. Therefore, these interpreted predicates are
 * stored explicitely in the list 'interpretedPred'.
 */

public class Rewriting {

	/** list of MCDs that form the rewriting */
	private List<MCD> mcds; 

	/** starting query that will be expressed by the rewriting */
	private DatalogQuery query;

	/** interpreted predicates that contain variable mapped to view head variable */
	private List<InterpretedPredicate> interpretedPreds;

	/** final rewriting of the query */
	private DatalogQuery rewriting;

	/**
	 * Rewriting constructor
	 * 
	 * When creating a new Rewriting object the method setRewritingMapping will
	 * be called. This method will determine a mapping from head variables of
	 * the views, contained in the rewriting, to variables of the query.
	 * 
	 * Then, the method addInterpretedPredicates will add necessary interpreted
	 * predicates to the list interpredPreds.
	 * 
	 * Finally, setRewritingQuery will be called in order to create an object of
	 * type DatalogQuery that represents the final rewriting of the query.
	 * 
	 * @param mcds
	 *            MCDs that form the rewriting
	 * @param query
	 *            represented by the rewriting
	 */
	public Rewriting(List<MCD> mcds, DatalogQuery query) {
		this.mcds = new ArrayList<MCD>();
		this.interpretedPreds = new ArrayList<InterpretedPredicate>();
		this.mcds.addAll(mcds);
		this.query = query;

		setRewritingMapping();
		addInterpretedPredicates();
		setRewritingQuery();
	}

	/**
	 * Called by the contructor. This method will map head variables of the
	 * views contained in the rewriting to variables or constants of the query.
	 * 
	 * A temporary mapping will be maintained (named 'represents'). It is a
	 * mapping from a query variable or constant to its representativ. In most
	 * cases the variable or constant is mapped to itself.
	 * 
	 * For each MCD part of the rewriting, the following will be performed:
	 * 
	 * For every "query element - view variable" pair, test if view variable was
	 * already mapped in the current MCD.
	 * 
	 * 1. case - view variable has not been mapped before: if representative
	 * mapping does not contain the current query element, add the mapping
	 * "query element -> query element". Add "view variable -> query elem" to
	 * the rewriting mapping. On the other hand, if there is already a
	 * representative for the current query variable, add "view variable ->
	 * representative" to the rewriting mapping.
	 * 
	 * 2. case - view variable has been mapped before: Obtain the mapping value
	 * (represent) to that the view variable has been mapped before from the
	 * rewriting mapping. Add to representative mapping "query elem ->
	 * represent". Also add "view variable -> represent" to rewriting mapping.
	 */
	private void setRewritingMapping() {

		// temporary mapping to have a representatives for each query variable
		Mapping represents = new Mapping();

		for (MCD mcd : mcds) {

			// get reference for rewritingMapping from mappings object of class
			// MCD
			Mapping rewritingMap = mcd.mappings.rewritingMap;

			// list of variables that are already mapped
			List<Variable> alreadyMapped = new ArrayList<Variable>();

			for (int i = 0; i < mcd.mappings.varMapSize(); i++) {

				PredicateElement queryElem = mcd.mappings.getVarMapArgument(i);
				Variable viewVar = mcd.mappings.getVarMapValue(i);

				// view variable has not been mapped before
				if (!alreadyMapped.contains(viewVar)) {
					alreadyMapped.add(viewVar);

					// there is no yet a representative for the query variable
					if (!represents.containsArgument(queryElem)) {

						represents.map(queryElem, queryElem);

						// add mapping from view variable to query element
						rewritingMap.map(viewVar, queryElem);

						// there is already a representative for the query
						// variable
					} else {
						PredicateElement represent = represents
								.getFirstMatchingValue(queryElem);

						// add mapping from view variable to reprentative of
						// query variable
						rewritingMap.map(viewVar, represent);
					}

					// same view variable has been mapped before
				} else {
					PredicateElement represent = rewritingMap
							.getFirstMatchingValue(viewVar);
					// this query element gets same represantative as
					// the one that was relevant when the view variable
					// was mapped before
					represents.map(queryElem, represent);

					// add mapping from view variable to reprentative of
					// query variable
					rewritingMap.map(viewVar, represent);
				}

			}
		}
	}

	/**
	 * The method will add necessary interpreted predicates the rewriting. Every
	 * interpreted predicate of the query that has a variable that is mapped to
	 * a distinguished view variable must be added to the rewriting.
	 */
	private void addInterpretedPredicates() {

		// iterate through interpreted predicates of the query
		for (InterpretedPredicate pred : query.getInterpretedPredicates()) {
			Variable var = pred.getVariable();

			boolean containsExistentVar = false;
			// if one of the mcd contains a mapping from Variable var
			// to an existential variable set boolean value to true
			for (MCD mcd : mcds) {
				List existentVars = mcd.findExistentialMappings();
				if (existentVars.contains(var)) {
					containsExistentVar = true;
				}
			}

			// only add interpreted predicate if its variable is not mapped to
			// an existential variable and if not already included in the list
			if (!containsExistentVar && !interpretedPreds.contains(pred)) {
				interpretedPreds.add(pred);
			}
		}
	}

	/**
	 * Called by the constructor. This method will create an object of type
	 * DatalogQuery that represents the actual rewriting of the query.
	 * 
	 * 1. Use the same head variables
	 * 
	 * 2. Add views contained in the MCDs to the rewriting. Use rewriting
	 * mapping to obtain a mapping from view head variables to variables or
	 * constants of the query. If a head variable of the view is not covered by
	 * the rewriting mapping, use "_" instead.
	 * 
	 * 3. Add interpreted predicates to the rewriting.
	 * 
	 */
	private void setRewritingQuery() {

		rewriting = new DatalogQuery(query.getName());

		for (Variable headVar : query.getHeadVariables()) {
			rewriting.addHeadVariable(headVar);
		}

		for (MCD mcd : mcds) {

			Predicate view = new Predicate(mcd.view.getName());
			rewriting.addPredicate(view);
			Mapping rewritingMap = mcd.mappings.rewritingMap;
			for (Variable var : mcd.view.getHeadVariables()) {

				PredicateElement rwVar = rewritingMap
						.getFirstMatchingValue(var);

				if (rwVar != null) {
					view.addElement(rwVar);
				} else {
					view.addVariable(new Variable("_"));
				}
			}

		}

		for (InterpretedPredicate pred : interpretedPreds) {
			rewriting.addInterpretedPredicate(pred);
		}

	}

	/**
	 * This is one approach to remove reduntant views (subgoals) from a
	 * rewriting. The method iterates through the list of subgoals of the
	 * rewriting and uses method insertNonRedundant(List <Predicate) to add non
	 * redundant subgoals to a list. In the end, the list will not contain
	 * redundant subgoals. Note, the subgoals are not redundant regarding to the
	 * approach of reducing redundancies used here, however, further reductions
	 * may still be possible.
	 */
	public void removeRedundancies() {

		List<Predicate> preds = rewriting.getPredicates();
		List<Predicate> noRedundant = new ArrayList<Predicate>();

		for (Predicate pred : preds) {
			noRedundant = insertNonRedundant(pred, noRedundant);
		}
		rewriting.setPredicates(noRedundant);
	}

	/**
	 * The method gets a predicate and a list of predicates as arguments. It
	 * iterates through the list and checks whether it is possible to substitute
	 * an element of the list with the given predicate. This is tested by using
	 * method canSubstitutePred. If that is the case, method findUnifer will be
	 * called to obtain the unifer between the new predicate and the predicate
	 * in the list. In the other case, if predicate cannot subsititute one of
	 * the elements in the list, it will be added as new element to the list.
	 * 
	 * @param pred
	 *            Predicate to be added to the list as substitude or new element
	 * @param list
	 *            list of non redundant subgoals
	 * @return list of non redundant subgoals
	 */
	private List<Predicate> insertNonRedundant(Predicate pred,
			List<Predicate> list) {

		List<Predicate> newList = new ArrayList<Predicate>();

		// make copy of list
		newList.addAll(list);

		// first time method is called
		if (newList.isEmpty()) {
			newList.add(pred);

		} else {
			// iterate through list with non redundant subgoals
			for (Predicate oldPred : list) {

				// predicate can substitude on of the elements of the list
				if (canSubstitutePred(pred, oldPred)) {
					newList.remove(oldPred);

					// get unifer
					Predicate newPred = findUnifer(pred, oldPred);

					// add unifer to list
					newList.add(newPred);
					return newList; // don't add any other predicate and return
				}
			}
			// no subsitute has been found -> add predicate to list
			newList.add(pred);
		}
		return newList;
	}

	/**
	 * This method determines equality of two Rewriting subgoals.
	 * 
	 * 
	 * It gets two Predicate objects as arguments which are compared with each
	 * other. If the predicates' names don't equal and the number of predicate
	 * elements is not the same, false will be returned immediately. Otherwise,
	 * the elements of each predicate are tested. False will be return if the
	 * predicate elements don't equal and the element of the second predicate is
	 * not '_'.
	 * 
	 * 1. Example: pred1(x,y,z) - pred2(x,y,_)
	 * 
	 * x = x; y = y; z = _ -> true
	 * 
	 * 2. Example: pred1(x,_,z) - pred2(x,_,y)
	 * 
	 * x = x; _ = _; z != y -> false
	 * 
	 * @param pred
	 *            first predicate
	 * @param oldPred
	 *            second predicate
	 * @return true if second predicate can be substituded by the second
	 *         predicate
	 */
	private boolean canSubstitutePred(Predicate pred, Predicate oldPred) {

		if (!(pred.name.equals(oldPred.name) && (pred.numberOfElements() == oldPred
				.numberOfElements()))) {
			return false;
		} else {

			for (int i = 0; i < pred.numberOfElements(); i++) {

				String name1 = pred.getElement(i).name;
				String name2 = oldPred.getElement(i).name;

				if (!name1.equals(name2) && !name2.equals("_") && !name1.equals("_")) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * PRE: Predicate oldPred can be substituted by Predicate pred. The method
	 * finds a unifer for the predicates provided as arguments. A new Predicate
	 * object is created and elements of either pred or oldPred are added to it.
	 * If element of oldPred is an underscore ('_') then the relevant element of
	 * pred is added to the new Predicate object. Otherwise the elements of pred
	 * are chosen to be added.
	 * 
	 * @param pred
	 *            first predicate
	 * @param oldPred
	 *            second predicate
	 * @return unifer of first and second predicate
	 */
	private Predicate findUnifer(Predicate pred, Predicate oldPred) {

		Predicate newPred = new Predicate(oldPred.name);

		for (int i = 0; i < oldPred.numberOfElements(); i++) {

			String name1 = pred.getElement(i).name;
			String name2 = oldPred.getElement(i).name;

			if (name2.equals("_")) {
				newPred.addElement(pred.getElement(i));
			} else {
				newPred.addElement(oldPred.getElement(i));
			}
		}
		return newPred;
	}

	/**
	 * Returns DatalogQuery object which represents a rewriting.
	 * 
	 * @return rewriting
	 */
	public DatalogQuery getRewriting() {
		return rewriting;
	}

	/**
	 * Overwrites Object method. Prints out the DatalogQuery object rewriting.
	 */
	public String toString() {
		return rewriting.toString();
	}

}
