package minicon;

import java.util.ArrayList;
import java.util.List;

import datalog.Constant;
import datalog.DatalogQuery;
import datalog.InterpretedPredicate;
import datalog.Predicate;
import datalog.PredicateElement;
import datalog.Variable;

/**
 * The class MCD represents the MiniCon Descriptions used by the algorithm to
 * obtain rewritings of the query.
 * 
 * MCD contains methods to enforce the MiniCon property. Also part of the class
 * is the DatalogQuery object 'query' which represents to query provided by the
 * user and the DatalogQuery object 'view' to which query variables will be
 * mapped to. It also has a list of subgoals and a list of interpreted
 * predicates of the query that are covered by the MCD.
 * 
 * The object 'mappings' of class MCDMappings contains the mapping from query
 * variables or constants to variables of the view -> variable mapping; mapping
 * from query variable or constant to constants of the view -> constant mapping;
 * mapping from head variables of the view to variables or constants of the
 * query -> rewriting mapping
 */
public class MCD {

	/** variable, constant and rewriting mapping */
	//protected MCDMappings mappings;
	public MCDMappings mappings; /* C.BA */
	/** query object */
	protected DatalogQuery query;

	/** view object */
	protected DatalogQuery view;

	/** list of subgoals covered by the MCD */
	protected List<Predicate> coveredSubgoals;

	/** list of interpreted predicates (subgoals) covered by the MCD */
	protected List<InterpretedPredicate> coveredInterpretedPredicates;
	
	/** Enrichment with preferences !
	 * Rank of the view related to this MCD 
	 * Cheikh BA, 05/2014
	 * */
	protected double rank;
	
	/** Enrichment with preferences !
	 * Setting and getting the rank of the view related to this MCD 
	 * Cheikh BA, 05/2014
	 * */
	public void setRank(double rank){
		this.rank = rank;
	}
	
	public double getRank(){
		return rank;
	}
	
	public DatalogQuery getView(){
		return view;
	}

	/**
	 * MCD constructor
	 * 
	 * @param subgoal
	 *            that is currently considered by the time when creating the MCD
	 *            object
	 * @param query
	 *            provided by the user
	 * @param view
	 *            that is currently considered by the time when creating the MCD
	 *            object
	 * @param map
	 *            variables or constant mapping; rewriting mapping will be empty
	 *            (note that by the time the MCD is created the mapping is not
	 *            necessarily valid)
	 */
	public MCD(Predicate subgoal, DatalogQuery query, DatalogQuery view,
			MCDMappings map) {
		this.mappings = map;
		this.query = query;
		this.view = view;

		this.coveredSubgoals = new ArrayList<Predicate>();
		this.coveredInterpretedPredicates = new ArrayList<InterpretedPredicate>();
		this.coveredSubgoals.add(subgoal);
	}

	/**
	 * The method will enforce the MiniCon property. Four checks will be
	 * performed:
	 * 
	 * 1. Check query constants: a query constant must be mapped either to a
	 * distinguished variable or to a constant.
	 * 
	 * 2. Valid mapping of head variables: a distinguished variable of the query
	 * must be mapped to either a head variable or a constant
	 * 
	 * 3. Mappings to existential variables: if a query variable is mapped to an
	 * existential view variable, every query subgoal that contains this
	 * variable must be covered by the MCD
	 * 
	 * 4. Interpreted predicates of the query: check if interpreted predicates
	 * of the query can be satified by the relevant view.
	 * 
	 * @return true if Minicon property is fulfilled, false otherwise
	 */
	public boolean fulfillProperty() {

		if (!checkQueryConstants()) {
			return false;
		}

		if (!checkHeadVariables()) {
			return false;
		}

		if (!coverExistentialVariables()) {
			return false;
		}

		if (!checkInterpretedPredicates()) {
			return false;
		}

		return true;
	}

	/**
	 * Called by fulfillPropery. The method will check whether a query constant,
	 * that is contained in the mapping, is mapped either to the same constant
	 * in the view or to a distinguished variable in the view.
	 * 
	 * The method will only consider mappings to view variables, i.e it will
	 * make sure that constants are mapped to distinguished variables. The other
	 * case is already covered in the constant mapping, i.e if a constant of the
	 * query is mapped to a constant of the view, this mapping will be part of
	 * the constant mapping that is contained in MCDMappings. The equality of
	 * both constants has already been checked when they were mapped.
	 * 
	 * @return true, if all constants of the query are mapped to distinguished
	 *         variables of the view, false otherwise
	 */
	private boolean checkQueryConstants() {

		for (int i = 0; i < mappings.varMapSize(); i++) {
			PredicateElement elem = mappings.getVarMapArgument(i);

			if (elem instanceof Constant) {
				Variable value = mappings.getVarMapValue(i);
				if (!view.containsHeadVariable(value)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * PRE: subGoal and viewPred can be mapped
	 * 
	 * Called by fulfillProperty. This method will check the first part MiniCon
	 * property: every distinguished query variable contained in the variable
	 * mapping must also be in the head of the respective view.
	 * 
	 * Head variables that are mapped to constants are part of the constant
	 * mapping and not considered by the method. They already fulfill the
	 * property.
	 * 
	 * @return return false if query variable is in the head but asociated view
	 *         variable is not, return true otherwise
	 */
	private boolean checkHeadVariables() {

		for (int i = 0; i < mappings.varMapSize(); i++) {
			PredicateElement queryElem = mappings.getVarMapArgument(i);
			Variable viewVar = mappings.getVarMapValue(i);

			// if variable of query subgoal is in the head then variable of view
			// predicate must also be in the head of the view
			// i.e. query variable distinguished => view variable distinguished
			// implication is expressed as NOT ... OR ... : NOT query var OR
			// view var
			// statement is negated because it will return false when statement
			// is NOT true
			if (!(!(query.containsHeadVariable(queryElem)) || (view
					.containsHeadVariable(viewVar)))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Called by fulfillProperty. The method will observe the crucial property
	 * of the MiniCon Algorithm. It will return false if it is not possible to
	 * fulfill to following property:
	 * 
	 * Suppose the mapping contains query variable 'x' mapped to 'a' (x->a) and
	 * 'a' is existential, then the MCD must cover every subgoal that contains
	 * 'x'.
	 * 
	 * First of all, the method will find all predicates that contain a variable
	 * that is mapped to a existential view variable. Then it will use
	 * extendMapping in order to recursively extend the mapping to fulfill the
	 * property.
	 * 
	 * @return true if mapping has been extended in order to fulfill property,
	 *         false if it is not possible to extend mapping
	 */
	private boolean coverExistentialVariables() {

		// find all predicates that contain these variables and which are not
		// coverd yet
		List<Predicate> predicates = findPredicates();
		// System.out.println(predicates);
		return extendMapping(predicates);

	}

	/**
	 * Called by fulfillProperty and recursively by itself. The method will try
	 * to extend the existing mapping by adding the predicates provided as
	 * arguments and then testing whether the MCD is still valid.
	 * 
	 * If the list of subgoals is empty, no extension is necessary. Otherwise,
	 * iterate through the 'preds' list. First, it will find the mapping
	 * partners for the current predicate by calling findMappingPartners. If no
	 * mapping partner will be found, it is not possible to extend the current
	 * mapping and thus you will not fulfill the property. False will be
	 * returned immediately.
	 * 
	 * In the other case, iterate through all possible mapping partners. A
	 * temporary mapping 'oldMap' will be created in order to be able to restore
	 * the existing mapping varMap. The current mapping partner 'mapPartner'
	 * will be added to the existing mapping. Then, 'varMap' will be tested for
	 * following 4 conditions:
	 * 
	 * 1. query constants must be mapped to the same constants or to
	 * distinguished variables.
	 * 
	 * 2. head variables in the query must be also head variables in the view
	 * (first part of the MiniCon property)
	 * 
	 * 3. there are no variables that cannot be equated, i.e. if query variable
	 * is mapped to an existential view variable, it cannot be mapped to any
	 * other variable of the view.
	 * 
	 * 4. subgoal 'pred' has not been covered yet, i.e. it is not in the list
	 * coveredSubgoals.
	 * 
	 * If one of these 4 conditions is not fulfilled, restore the old mapping .
	 * 
	 * Finally if one or more subgoals have been added to coveredSubgoals call
	 * findPredicates to get possible new subgoals that have to be covered after
	 * extending the list of covered subgoal. It will return the result of the
	 * recursive call of extendMapping.
	 * 
	 * @param subgoals
	 *            list of subgoal that have to be covered
	 * @return return false, if it is not possible to cover subgoals
	 */
	private boolean extendMapping(List<Predicate> subgoals) {

		// no predicates that have to be mapped
		if (subgoals.isEmpty()) {
			return true;

		} else {
			for (Predicate subgoal : subgoals) {

				List<Predicate> mapPartners = findMappingPartners(subgoal);

				if (mapPartners.isEmpty()) {
					return false;
				} else {

					boolean newCoveredSubgoal = false;

					for (Predicate mapPartner : mapPartners) {

						// remember old mapping in order to be able to restore
						// it in case mapping was not valid
						MCDMappings oldMap = mappings.clone();

						// add to existing mapping
						mappings.mapPredicates(subgoal, mapPartner);

						// 1. check if query constants are mapped to
						// distinguished view vars
						// 2. check if mapped head variables of the query are
						// also
						// head variables in the view
						// 3. check if there are variables which can't be
						// equated
						// 4. check if subgoal is already covered
						if (checkQueryConstants() && checkHeadVariables()
								&& !cannotEquateVariables()
								&& !coveredSubgoals.contains(subgoal)) {
							coveredSubgoals.add(subgoal);
							newCoveredSubgoal = true;

							// one of the conditions was not fulfilled
							// -> restore old mapping
						} else {
							mappings = oldMap;
						}
					}

					// in this iteration it was not possible to find a
					// predicate in the view to cover subgoal "pred"
					// i.e. it not possible to fulfill MCD property
					if (!newCoveredSubgoal) {
						return false;
					}
				}
			}
			List<Predicate> newPredicates = findPredicates();
			return extendMapping(newPredicates);
		}
	}

	/**
	 * Called by coverExitentialMapping and extendMapping. The method will find
	 * every query subgoal that has not been covered yet but that is necessary
	 * to fulfill property.
	 * 
	 * It will call findExistentialMappings to obtain the query variables that
	 * are mapped to an existential view variable. Then it will determine the
	 * query subgoals containing these variables and add them to the list
	 * 'predicates'. Subgoals that have already been covered are also included
	 * in the list. That is why the final step will remove these subgoals from
	 * the predicate list.
	 * 
	 * @return list of predicate that have to be covered
	 */
	private List<Predicate> findPredicates() {

		// find elements in the query that are mapped to existential variable
		// in the view
		List<PredicateElement> existentMaps = findExistentialMappings();

		List<Predicate> predicates = new ArrayList<Predicate>();

		// all predicates that contain these elements except the subgoal
		for (Predicate pred : query.getPredicates()) {
			for (PredicateElement elem : existentMaps) {
				if (pred.contains(elem) && !predicates.contains(pred)) {
					predicates.add(pred);
				}
			}
		}
		predicates.removeAll(coveredSubgoals);
		return predicates;
	}

	/**
	 * Called by findPredicates. It will find the query variables that are
	 * mapped to existential view variables.
	 * 
	 * The first loop will iterate through the variables of the mapping. A
	 * variable will be added to the list 'existentMappings' if it is also
	 * contained in the list of existential varibles of the view. The second
	 * loop will iterate through the obtained list existentMappings to find
	 * corresponding arguments in the query and will add them the list
	 * 'mappedToExist' that will be returned.
	 * 
	 * @return list of query variables that are mapped to existential view
	 *         variables
	 */
	public List<PredicateElement> findExistentialMappings() {

		List<PredicateElement> mappedToExistent = new ArrayList<PredicateElement>();

		List<Variable> existentMappings = getExistentialMappingValues();

		// for every existential variable in the view predicate find the
		// corresponding argument
		for (Variable var : existentMappings) {
			List<PredicateElement> tempList = mappings.getArguments(var);
			for (PredicateElement elem : tempList) {
				if (elem instanceof Variable) {
					mappedToExistent.add((Variable) elem);
				}
			}
		}
		return mappedToExistent;
	}

	/**
	 * Method returns the value elements that are part of an existential mapping
	 * of the MCD.
	 * 
	 * @return list of existential variables that are part of the MCD mapping.
	 */
	public List<Variable> getExistentialMappingValues() {

		List<Variable> existentMappings = new ArrayList<Variable>();
		List<Variable> values = mappings.getAllVarMapValues();
		List<Variable> existentVars = view.getExistentialVariables();

		// for every value contained in the mapping find out if it is an
		// existential variable
		for (Variable var : values) {
			if (existentVars.contains(var) && !existentMappings.contains(var)) {
				existentMappings.add(var);
			}
		}

		return existentMappings;

	}

	/**
	 * Called by extendMapping. Given a query subgoal, the method will find all
	 * view predicates that can possibly be mapped to the subgoal.
	 * 
	 * @param subgoal
	 *            query subgoal that will be used to find all mapping partners
	 *            of the view
	 * @return list of all possible predicates of the view that can be mapped to
	 *         the subgoal
	 */
	private List<Predicate> findMappingPartners(Predicate subgoal) {
		List<Predicate> partners = new ArrayList<Predicate>();

		for (Predicate viewPred : view.getPredicates()) {
			if (subgoal.canBeMapped(viewPred)) {
				partners.add(viewPred);
			}
		}
		return partners;
	}

	/**
	 * Called by extendMapping. Returns true if there exists a variable in query
	 * that is mapped to an existential variable of the view and moreover, it is
	 * mapped to another view variable or constant. That means it ist not
	 * possible to equate this query variable.
	 * 
	 * @return true if there exists a query variable in the current mapping that
	 *         cannot be equated
	 */
	private boolean cannotEquateVariables() {
		List<PredicateElement> existentMap = findExistentialMappings();

		// arg is mapped to existential mapping, thus it cannot be mapped to any
		// other variable or constant
		for (PredicateElement arg : existentMap) {

			List<PredicateElement> values = mappings.getValues(arg);
			if (values.size() != 1) {
				return true;
			}

		}
		return false;
	}

	/**
	 * Called by fulfillPropery. The method will only consider interpreted
	 * predicates of the query that contain a variable which is mapped to an
	 * existential variable of the view. If that is the case, the method
	 * checkComparisionAndCover will be called.
	 * 
	 * @return true, if all interpreted predicates of the query can be satisfied
	 *         by the respective view
	 */
	private boolean checkInterpretedPredicates() {

		boolean valid = true;

		for (InterpretedPredicate interpPred : query.getInterpretedPredicates()) {

			List<PredicateElement> existMaps = findExistentialMappings();

			PredicateElement left = interpPred.getLeft();
			PredicateElement right = interpPred.getRight();

			for (PredicateElement queryVar : existMaps) {

				if (left.equals(queryVar) || right.equals(queryVar)) {
					valid = checkComparisonAndCover(interpPred);

				}
			}
		}
		return valid;
	}

	/**
	 * Called by checkInterpretedPredicates. Only interpreted predicates of the
	 * query with a variable mapped to a existential variable are considered
	 * here. The method will use findViewInterpretedPredicate to obtain an
	 * interpreted predicate from the view that can be mapped to one of the
	 * query.
	 * 
	 * If no predicate will be found, it means there is no possibility to satify
	 * the query predicate, thus null well be returned immediately.
	 * 
	 * Otherwise, if there is a view predicate, it must logically entail the
	 * query predicate. Four possibilites have to be distiguished:
	 * 
	 * 1. x < N OR N > x
	 * 
	 * where x is a variable of the query which is mapped to existential
	 * variable and N is a constant; if a is existential variable in the
	 * interpreted predicate of the view and M is the relevant constant,
	 * following constraints have to be satisfied in order to obtain a valid
	 * mapping of the interpreted predicates:
	 * 
	 * if a < M then M <= N; if a <= M --> M < N; if M > a --> N >= M; if M >= a
	 * --> N > M
	 * 
	 * The other 3 cases can be found in the relevant parts of the source code.
	 * 
	 * If the interpreted view predicate fulfills the constraints, the relevant
	 * query predicate will be added to the list of interpreted predicates.
	 * 
	 * @param queryPred
	 *            interpreted predicate of the query that contains a variable
	 *            that is mapped to an existential view variable
	 * @return true, if there is an interpreted predicate in the view that
	 *         logically entails an interpreted query predicate
	 */
	private boolean checkComparisonAndCover(InterpretedPredicate queryPred) {

		PredicateElement qLeft = queryPred.getLeft();
		PredicateElement qRight = queryPred.getRight();

		InterpretedPredicate viewPred = findViewInterpretedPredicate(queryPred);

		boolean isValid = false;

		if (viewPred == null) {
			return false;
		} else {
			PredicateElement vLeft = viewPred.getLeft();
			PredicateElement vRight = viewPred.getRight();

			// x < N OR N > x
			if (((qLeft instanceof Variable) && queryPred.getComparator()
					.equals("<"))
					|| ((qRight instanceof Variable) && queryPred
							.getComparator().equals(">"))) {
				int N;
				if (qLeft instanceof Variable) {
					N = new Integer(qRight.toString()).intValue();
				} else {
					N = new Integer(qLeft.toString()).intValue();
				}

				// 1.case: a < M --> M <= N
				// 2.case a <= M --> M < N
				// 3.case M > a --> N >= M
				// 4.case M >= a --> N > M

				// 1.case ((a < M) && M <= N))
				if (vLeft instanceof Variable
						&& viewPred.getComparator().equals("<")) {
					int M = new Integer(vRight.toString()).intValue();
					if (M <= N)
						isValid = true;
					// 2. case ((a <= M) && M<N)
				} else if (vLeft instanceof Variable
						&& viewPred.getComparator().equals("<=")) {
					int M = new Integer(vRight.toString()).intValue();
					if (M < N)
						isValid = true;
					// 3. case M > a AND N >= M
				} else if (vRight instanceof Variable
						&& viewPred.getComparator().equals(">")) {
					int M = new Integer(vLeft.toString()).intValue();
					if (M >= N)
						isValid = true;
					// M >= a AND N > M
				} else if (vRight instanceof Variable
						&& viewPred.getComparator().equals(">=")) {
					int M = new Integer(vLeft.toString()).intValue();
					if (M > N)
						isValid = true;
				}
			}
			// x <= N OR N >= x
			else if (((qLeft instanceof Variable) && queryPred.getComparator()
					.equals("<="))
					|| ((qRight instanceof Variable) && queryPred
							.getComparator().equals(">="))) {

				int N;
				if (qLeft instanceof Variable) {
					N = new Integer(qRight.toString()).intValue();
				} else {
					N = new Integer(qLeft.toString()).intValue();
				}

				// 1.case: a < M --> M <= N
				// 2.case a <= M --> M <= N
				// 3.case M > a --> N >= M
				// 4.case M >= a --> N >= M

				// 1.case ((a < M) && M <= N))
				if (vLeft instanceof Variable
						&& viewPred.getComparator().equals("<")) {
					int M = new Integer(vRight.toString()).intValue();
					if (M <= N)
						isValid = true;
					// 2. case ((a <= M) && M<=N)
				} else if (vLeft instanceof Variable
						&& viewPred.getComparator().equals("<=")) {
					int M = new Integer(vRight.toString()).intValue();
					if (M <= N)
						isValid = true;
					// 3.case M > a => N >= M
				} else if (vRight instanceof Variable
						&& viewPred.getComparator().equals(">")) {
					int M = new Integer(vLeft.toString()).intValue();
					if (N >= M)
						isValid = true;
					// 4.case M >= a => N >= M
				} else if (vRight instanceof Variable
						&& viewPred.getComparator().equals(">=")) {
					int M = new Integer(vLeft.toString()).intValue();
					if (N >= M)
						isValid = true;
				}
			}
			// x > N OR N < x
			else if (((qLeft instanceof Variable) && queryPred.getComparator()
					.equals(">"))
					|| ((qRight instanceof Variable) && queryPred
							.getComparator().equals("<"))) {

				int N;
				if (qLeft instanceof Variable) {
					N = new Integer(qRight.toString()).intValue();
				} else {
					N = new Integer(qLeft.toString()).intValue();
				}

				// 1.case: a > M --> M >= N
				// 2.case a >= M --> M > N
				// 3.case M < a --> N <= M
				// 4. case M <= a --> N < M

				// 1.case: a > M --> M >= N
				if (vLeft instanceof Variable
						&& viewPred.getComparator().equals(">")) {
					int M = new Integer(vRight.toString()).intValue();
					if (M >= N)
						isValid = true;
					// 2.case a >= M --> M > N
				} else if (vLeft instanceof Variable
						&& viewPred.getComparator().equals(">=")) {
					int M = new Integer(vRight.toString()).intValue();
					if (M > N)
						isValid = true;
					// 3.case M < a --> N <= M
				} else if (vRight instanceof Variable
						&& viewPred.getComparator().equals("<")) {
					int M = new Integer(vLeft.toString()).intValue();
					if (N <= M)
						isValid = true;
					// 4. case M <= a --> N < M
				} else if (vRight instanceof Variable
						&& viewPred.getComparator().equals("<=")) {
					int M = new Integer(vLeft.toString()).intValue();
					if (N < M)
						isValid = true;
				}
			}
			// x >= N OR N <= x
			else if (((qLeft instanceof Variable) && queryPred.getComparator()
					.equals(">="))
					|| ((qRight instanceof Variable) && queryPred
							.getComparator().equals("<="))) {

				int N;
				if (qLeft instanceof Variable) {
					N = new Integer(qRight.toString()).intValue();
				} else {
					N = new Integer(qLeft.toString()).intValue();
				}

				// 1.case: a > M --> M >= N
				// 2.case a >= M --> M >= N
				// 3.case M < a --> N <= M
				// 4. case M <= a --> N <= M

				// 1.case: a > M --> M >= N
				if (vLeft instanceof Variable
						&& viewPred.getComparator().equals(">")) {
					int M = new Integer(vRight.toString()).intValue();
					if (M >= N)
						isValid = true;
					// 2.case a >= M --> M >= N
				} else if (vLeft instanceof Variable
						&& viewPred.getComparator().equals(">=")) {
					int M = new Integer(vRight.toString()).intValue();
					if (M >= N)
						isValid = true;
					// 3.case M < a --> N <= M
				} else if (vRight instanceof Variable
						&& viewPred.getComparator().equals("<")) {
					int M = new Integer(vLeft.toString()).intValue();
					if (N <= M)
						isValid = true;
					// // 4. case M <= a --> N <= M
				} else if (vRight instanceof Variable
						&& viewPred.getComparator().equals("<=")) {
					int M = new Integer(vLeft.toString()).intValue();
					if (N <= M)
						isValid = true;
				}
			}
		}
		if (isValid) {
			coveredInterpretedPredicates.add(queryPred);
			return true;
		}
		return false;
	}

	/**
	 * Called by checkComparisonAndCover. The method will try to find an
	 * interpreted predicate in the view that contains an existential variable
	 * that is contained in the variable mapping. The first predicate found will
	 * be returned.
	 * 
	 * In the first loop every interpreted predicate in the view will be tested
	 * only with the left hand side of the interpreted predicate of the query.
	 * If the element of the query matches with either left or right side of
	 * predicate, it will be returned.
	 * 
	 * The second loop does the same with the right hand side of the interpreted
	 * query predicate.
	 * 
	 * @param queryPred
	 *            interpreted predicate of the query that contains a variable
	 *            that is mapped to an existential view variable
	 * @return InterpretedPredicate object that contains an existential variable
	 *         'y' which is part of the variable mapping from a variable of an
	 *         interpreted predicate of the query to view variable 'y'.
	 */
	private InterpretedPredicate findViewInterpretedPredicate(
			InterpretedPredicate queryPred) {

		for (InterpretedPredicate viewPred : view.getInterpretedPredicates()) {
			PredicateElement viewVal = mappings.varMap
					.getFirstMatchingValue(queryPred.getLeft());
			if (viewVal != null
					&& ((viewPred.getLeft().equals(viewVal)) || viewPred
							.getRight().equals(viewVal))) {
				return viewPred;
			}
		}

		for (InterpretedPredicate viewPred : view.getInterpretedPredicates()) {
			PredicateElement viewVal = mappings.varMap
					.getFirstMatchingValue(queryPred.getRight());
			if (viewVal != null
					&& ((viewPred.getRight().equals(viewVal)) || viewPred
							.getLeft().equals(viewVal))) {
				return viewPred;
			}
		}

		return null;
	}

	/**
	 * Returns the list of subgoals contained in the MCD object
	 * 
	 * @return covered subgoals
	 */
	public List<Predicate> getSubgoals() {
		return coveredSubgoals;
	}

	/**
	 * Overwrites equals method of class Object.
	 * 
	 * Two mcd Object are equal if
	 * 
	 * 1. the relevant view is the same
	 * 
	 * 2. number of covered subgoals is the same
	 * 
	 * 3. their variable and constant mapping is equal.
	 * 
	 * 4. all above properties are fulfilled but coverd subgoals are different
	 * 
	 * Example:
	 * 
	 * Query: Q(x,y) :- e1(x,y), e2(x,y); View: V(a,b) :- e1(a,b),e2(a,b))
	 * 
	 */
	public boolean equals(Object mcdObj) {
		MCD mcd = (MCD) mcdObj;

		if (!(mcd.view.getName().equals(this.view.getName()))) {
			return false;
		}
		boolean sameCoveredSubgoals = true;

		// number of subgoals is the same
		if (mcd.coveredSubgoals.size() != this.coveredSubgoals.size()) {
			return false;
		}

		// compare mappings
		if (!this.mappings.equals(mcd.mappings)) {
			return false;
		}

		// same view, same number of subgoals, same mappings but different
		// covered subgoals
		boolean hasSubgoal = false;
		for (Predicate subgoal : this.coveredSubgoals) {
			for (Predicate compare : mcd.coveredSubgoals) {
				if (subgoal.name.equals(compare.name)) {
					hasSubgoal = true;
				}
			}
		}
		if (!hasSubgoal) {
			return false;
		}

		return true;
	}

	/**
	 * The method will test if two MCDs are disjoint. Two MCDs are disjount if
	 * they have not both covered the same query subgoal.
	 * 
	 * @param mcd
	 *            object to be tested with this.mcd
	 * @return true if MCDs are disjoint, false otherwise
	 */
	public boolean isDisjoint(MCD mcd) {
		for (Predicate pred : mcd.coveredSubgoals) {
			if (this.hasSubgoal(pred)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if the list of covered subgoals contains the predicate
	 * provided as argument.
	 * 
	 * @param pred
	 *            Predicate to be tested whether it is contained in the list of
	 *            covered subgoals
	 * @return true, if argument is contained in covered subgoals, false
	 *         otherwise
	 */
	public boolean hasSubgoal(Predicate pred) {
		return coveredSubgoals.contains(pred);
	}

	/**
	 * Returns the number of covered subgoals (without interpreted predicates).
	 * 
	 * @return size of MCD (number of covered subgoals)
	 */
	public int numberOfSubgoals() {
		return coveredSubgoals.size();
	}

	/**
	 * Overwrites toString method of Object.
	 */
	public String toString() {
		/*C.BA*/
		//String str = "\nMCD " + view.getName() + " - Rank[" + rank + "] \n";
		String str = view.getName();
		//str += "mapped to variables: " + mappings.varMap.toString();
		//str += "\nmapped to constants: " + mappings.constMap.toString();

		//str += "\ncovered subgoals: ";
		for (Predicate subgoal : coveredSubgoals) {
			//str += subgoal + ", ";
		}

		for (InterpretedPredicate intPred : coveredInterpretedPredicates) {
			//str += intPred + ", ";
		}
		// remove last comma
		//str = str.substring(0, str.length() - 2);
		return str;
	}

	// //////////////////////////////////////////////////////////////////
	// provide access to private methods in order to perfom JUnit tests
	// //////////////////////////////////////////////////////////////////

	public boolean testCheckQueryConstants() {
		return checkQueryConstants();
	}

	public boolean testCheckHeadVariables() {
		return checkHeadVariables();
	}

	public boolean testCoverExistentialVariables() {
		return coverExistentialVariables();
	}

	public List<Predicate> testFindMappingPartners(Predicate pred) {
		return findMappingPartners(pred);
	}

	public List<PredicateElement> testFindExistentialMappings() {
		return findExistentialMappings();
	}

	public boolean testCannotEquateVariables() {
		return cannotEquateVariables();
	}

	public boolean testcheckInterpretedPredicates() {
		return checkInterpretedPredicates();
	}
}
