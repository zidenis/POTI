/*
 * Created on 21.05.2005
 */
package converter;

import java.util.Hashtable;
import java.util.List;

import minicon.Rewriting;

import datalog.Constant;
import datalog.DatalogQuery;
import datalog.InterpretedPredicate;
import datalog.NumericalConstant;
import datalog.Predicate;
import datalog.PredicateElement;
import datalog.StringConstant;
import datalog.Variable;

/**
 * Class Converter contains methods to convert a Datalog query to an SQL query
 * and the other way around. Two crucial methods are convertToDalag and
 * convertToSQL. The former takes an SQL query and a database schema definition
 * as arguments and returns a DatalogQuery object. The latter takes a Rewriting
 * object created by the MiniCon algorithm and returns an object of type
 * SQLQuery.
 * 
 * @author Kevin Irmscher
 */
public class Converter {

	/**
	 * Converts an SQL query to a Datalog query. It takes as arguments a query
	 * name which will be the head of the resulting query. Also, a database
	 * schema has to be provided which contains of all relations used in the SQL
	 * query.
	 * 
	 * In order to perform the query conversion the method uses helper
	 * functions.
	 * 
	 * 1. head variables are added
	 * 
	 * 2. predicates are added to the body
	 * 
	 * 3. substitions of variables with constants and interpreted predicates are
	 * added
	 * 
	 * 4. rename head variables in order to simplify them
	 * 
	 * 5. name predicates equally that occur more than once in the SQL query 
	 * 
	 * @param queryName
	 *            name that appears in the head of the datalog query
	 * @param schema
	 *            definition of database relations
	 * @param sqlQuery
	 *            SQL query that is converted to a Datalog query
	 * @return DatalogQuery object
	 */
	public DatalogQuery convertToDatalog(String queryName,
			List<Predicate> schema, SQLQuery sqlQuery) {

		DatalogQuery query = new DatalogQuery(queryName);

		addHeadVariables(query, sqlQuery.getAttributes());

		addBodyPredicates(query, sqlQuery, schema);

		addConditions(query, sqlQuery);

		simplifyHeadVariables(query);

		equatePredicates(query, sqlQuery);

		return query;
	}

	/**
	 * The method coverts a Datalog query to an SQL statement. The query is given
	 * as a object of class Rewriting which is the output of the MiniCon
	 * algorithm. After instantiating an SQLQuery object three steps are
	 * performed:
	 * 
	 * 1. add attributes to the select clause using method addSelectClause
	 * 
	 * 2. add relations to the from clause using method addFromClause
	 * 
	 * 3. add conditions to the where clause using method addWhereClause
	 * 
	 * @param rw
	 *            Rewriting object
	 * @return SQLQuery object
	 */
	public SQLQuery convertToSQL(Rewriting rw) {
		SQLQuery sqlQuery = new SQLQuery();
		DatalogQuery datalogQuery = rw.getRewriting();

		addSelectClause(sqlQuery, datalogQuery);

		addFromClause(sqlQuery, datalogQuery);

		addWhereClause(sqlQuery, datalogQuery);

		return sqlQuery;
	}

	/**
	 * The methods adds head variables to the DatalogQuery object. It takes
	 * variables from the select clause of the SQL query which are renamed
	 * before they are added.
	 * 
	 * A head variable has the form: 'relationAliasName_variableName'.
	 * 
	 * @param query
	 *            DatalogQuery object
	 * @param headVars
	 *            variables that are added to Datalog query
	 */
	private void addHeadVariables(DatalogQuery query, List<Attribute> headVars) {
		for (Attribute attr : headVars) {
			String name = attr.alias + "_" + attr.variable;
			query.addHeadVariable(new Variable(name));
		}
	}

	/**
	 * The method adds body predicates to the Datalog query. Relations of the
	 * from clause of the SQL query are compared with the ones of the schema
	 * definition and added as Predicate objects to the body.
	 * 
	 * Using method findRelation it is determined whether the relation of the
	 * SQL query is defined in the schema. If that is not the case an error
	 * message will be printed out. Otherwise a new predicate will be added to
	 * the Datalog query it will be named with the relation alias.
	 * 
	 * Variables of the relation as defined in the schema are added to the
	 * Datalog predicate. Variable names have to format:
	 * 'relationAliasName_variableName'
	 * 
	 * @param query
	 *            DatalogQuery object
	 * @param sqlQuery
	 *            SQLQuery object
	 * @param schema
	 *            Schema definition
	 */
	private void addBodyPredicates(DatalogQuery query, SQLQuery sqlQuery,
			List<Predicate> schema) {

		// get relation of from clause of SQL query
		List<Relation> sqlRelations = sqlQuery.getRelations();

		// find relations according to schema and rename elements
		for (Relation queryRelation : sqlRelations) {

			String relName = queryRelation.relation.name;

			Predicate schemaRelation = findRelation(schema, relName);

			// return error message if relation is not defined in schema
			if (schemaRelation == null) {
				System.out.println("relation " + relName
						+ " not specified in schema");

				// otherwise add new predicate to query and add variabels
			} else {
				if (schemaRelation.name.equals(relName)) {
					String predicateName = queryRelation.alias;

					Predicate datalogPredicate = new Predicate(predicateName);

					// add variables
					for (Variable elem : schemaRelation.getVariables()) {

						// rename variables
						Variable element = new Variable(queryRelation.alias
								+ "_" + elem.name);
						datalogPredicate.addVariable(element);
					}
					query.addPredicate(datalogPredicate);
				}
			}
		}
	}

	/**
	 * The method incorporates conditions of the where clause of the SQL query
	 * into the Datalog query. It does the following:
	 * 
	 * 1. substitude predicate variables with constants: if right side of the
	 * condition is a constant replace variable with this constant
	 * 
	 * 2. equate variables: if condition contains two variables replace left
	 * variable with right variable or its representatives (method
	 * setRepresentatives is used)
	 * 
	 * 3. add interpreted predicates: condition is an unequation which is added
	 * to the Datalog query as interpreted predicate
	 * 
	 * @param query
	 *            DatalogQuery object
	 * @param sqlQuery
	 *            SQLQuery object
	 */
	private void addConditions(DatalogQuery query, SQLQuery sqlQuery) {

		// holds representatives of predicate variables
		Hashtable<String, String> table = new Hashtable<String, String>();

		// iterate through SQL query conditions
		for (Condition cond : sqlQuery.getConditions()) {

			// case 1: conditions is an equation
			if (cond.getComparator().equals("=")) {

				// iterate through predicates of Datalog query
				for (Predicate pred : query.getPredicates()) {

					// case 1.1: right side is constant
					// (rel1.variable1 = CONSTANT) ->
					// replace variable of relevant predicate with this constant
					if (cond.rightSideIsConstant()) {
						String extendedName = pred.name + "_"
								+ cond.leftElement();

						// rename variable
						for (int i = 0; i < pred.getVariables().size(); i++) {
							Variable var = pred.getVariables().get(i);
							if (cond.leftAlias().equals(pred.name)
									&& (var.name.equals(extendedName) || var.name
											.equals(cond.leftElement().name))) {

								// decide whether constant is numerical or a
								// string
								Constant cons;
								try {
									new Integer(cond.rightElement().name);
									cons = new NumericalConstant(cond
											.rightElement().name);
								} catch (NumberFormatException e) {
									cons = new StringConstant(cond
											.rightElement().name);
								}
								// remove variable from predicate
								pred.removeVariableAt(i);
								// add constant to predicate
								pred.addConstantAt(i, cons);
							}
						}

						// case 1.2: both sides are variables
						// (rel1.variable1 = rel2.variable2)
					} else {

						// iterate through variables of current predicate
						for (Variable var : pred.getVariables()) {
							String extendedName = pred.name + "_"
									+ cond.leftElement().name;
							if (cond.leftAlias().equals(pred.name)
									&& var.name.equals(extendedName)) {
								String newName = cond.rightAlias() + "_"
										+ cond.rightElement();

								// rename head variable set representative for
								// variable that appears on the left side of the
								// condition
								setHeadVariablesAndRepresentatives(query,
										table, extendedName, newName);
							}
						}
					}
				}

				// case 2: condition is an unequation
				// comparator is not '=' -> add interpreted predicate
			} else {
				String leftSide = cond.leftAlias() + "_"
						+ cond.leftElement().name;
				PredicateElement leftPred;
				if (table.containsKey(leftSide))
					leftPred = new Variable(table.get(leftSide));
				else {
					leftPred = new Variable(leftSide);
				}

				PredicateElement rightSide;
				try {
					new Integer(cond.rightElement().name);
					rightSide = new NumericalConstant(cond.rightElement().name);
				} catch (NumberFormatException e) {
					rightSide = new StringConstant(cond.rightElement().name);
				}

				InterpretedPredicate intPred = new InterpretedPredicate(
						leftPred, rightSide, cond.getComparator());
				query.addInterpretedPredicate(intPred);
			}
		}
		// substitute with representatives for variables
		for (Predicate queryPred : query.getPredicates()) {
			for (Variable var : queryPred.getVariables()) {
				String newName = table.get(var.name);
				if (newName != null) {
					var.name = newName;
				}
			}
		}
	}

	/**
	 * Called by convertToDatalog. The method renames head variables and
	 * relevant variables in the body in order to increase legibility. The first
	 * variable is named x1, the second x2, the third x3 and so on. Head
	 * variables that also appear in the body get the same names.
	 * 
	 * @param query
	 *            Datalog query
	 */
	private void simplifyHeadVariables(DatalogQuery query) {

		int count = 1;
		// rename head variables
		for (Variable var : query.getHeadVariables()) {
			String newName = "x" + count;
			count++;
			changeBodyVariableNames(query, var.name, newName);
			var.name = newName;
		}
	}

	/**
	 * Called by convertToDatalog. The method equates predicate names of the
	 * Datalog query which belong to the same relation in the SQL query.
	 * 
	 * Example: select e1.x, e2.y from MyRelation e1, MyRelation e2
	 * 
	 * Resulting Datalog query before predicates have been equated:
	 * q(x1,x2):-e1(x1,e1_b), e2(e2_a,x2)
	 * 
	 * As e1 and e2 are alias names of the same relation, they should be equated:
	 * q(x1,x2):-e1(x1,e1_b), e1(e2_a,x2)
	 * 
	 * @param query
	 *            DatalogQuery object
	 * @param sqlQuery
	 *            SQLQuery object
	 */
	private void equatePredicates(DatalogQuery query, SQLQuery sqlQuery) {

		for (Relation rel : sqlQuery.getRelations()) {
			for (Relation rel2 : sqlQuery.getRelations()) {

				if (rel.relation.name.equals(rel2.relation.name)) {
					for (Predicate pred : query.getPredicates()) {
						if (pred.name.equals(rel.alias)
								|| (pred.name.equals(rel2.alias))) {
							pred.name = rel2.alias;
						}
					}

				}
			}

		}
	}

	/**
	 * Called by convertToSQL. The method adds attributes to the select clause
	 * of the SQL query. For each variable in the head of the Datalog query, it
	 * determines the relevant variable in the body and creates a new Attribute
	 * object. This object contains the variable name and the alias of the
	 * relevant relation. The alias name is determined by the position of the
	 * predicate in the Datalog query. That means, the first predicate gets
	 * alias e1, the predicate at the second position gets alias e2 and so on.
	 * 
	 * @param sql
	 *            SQLQuery object
	 * @param datalog
	 *            DatalogQuery object
	 */
	private void addSelectClause(SQLQuery sql, DatalogQuery datalog) {

		for (Variable var : datalog.getHeadVariables()) {
			Attribute attr = null;

			for (int i = 0; i < datalog.getPredicates().size(); i++) {
				Predicate pred = datalog.getPredicates().get(i);
				for (Variable var2 : pred.getVariables()) {
					if (var2.name.equals(var.name)) {
						int number = i + 1;
						String alias = "e" + number;
						attr = new Attribute(alias, var);
						i = datalog.getPredicates().size(); // stop loop
					}
				}
			}
			if (attr != null) {
				sql.addAttribute(attr);
			}
		}
	}

	/**
	 * Called by convertToSQL. The method adds relation to the from clause of
	 * the SQL query. It iterates through the list of predicates of the Datalog
	 * query and for each predicate, it creates an object of type Relation. The
	 * alias name for each relation is e1 for the first relaltion, e2 for the
	 * second on and so on.
	 * 
	 * @param sql
	 *            SQLQuery object
	 * @param datalog
	 *            DatalogQuery object
	 */
	private void addFromClause(SQLQuery sql, DatalogQuery datalog) {
		int count = 1;
		for (Predicate pred : datalog.getPredicates()) {
			String alias = "e" + count;
			count++;
			sql.addRelation(new Relation(pred, alias));
		}
	}

	/**
	 * Called by convertToSQL. The method adds conditions to the where clause of
	 * the SQL query. In order to access the head variables of the views
	 * (predicates in rewriting) the following schema is assumed:
	 * ViewName(x1,x2,x3,...). The method is split up in tree parts. Part one
	 * and two are both performed in the first loop. Each parts add conditions
	 * in form of objects of class Condition to the SQL query.
	 * 
	 * 1. Constants in the views are added as condition
	 * 
	 * Example: Rewriting: Q'(x) :- V(x,23)
	 * 
	 * SELECT e1.x from V e1 where e1.x2 = 23
	 * 
	 * 2. Variables in the views are renamed. If variable i in the rewriting is
	 * named xi, nothing will be done. Otherwise a condition will be added to
	 * rename the variable in the view.
	 * 
	 * Example: Rewriting :Q'(x1,x2) :- V1(x1,e1_b), V2(x1,x2)
	 * 
	 * SELECT e1.x1, e2.x2 FROM V1 e1, V2 e2 WHERE e1.x2 = e1_b
	 * 
	 * 3. For each interpreted predicate of the Datalog query it adds a
	 * condition to the SQL query.
	 * 
	 * Example: Rewriting :Q'(x1) :- V1(x1,x2), V2(x1,x2), x2 > 23
	 * 
	 * SELECT e1.x1, e2.x2 FROM V1 e1, V2 e2 WHERE e1.x2 > 23
	 * 
	 * @param sql
	 *            SQLQuery object
	 * @param datalog
	 *            DatalogQuery object
	 */
	private void addWhereClause(SQLQuery sql, DatalogQuery datalog) {

		// set variable names and add constants
		for (int i = 0; i < datalog.getPredicates().size(); i++) {
			Predicate pred = datalog.getPredicates().get(i);
			for (int j = 0; j < pred.numberOfElements(); j++) {
				PredicateElement elem = pred.getElement(j);
				int aliasNumber = i + 1;
				String alias = "e" + aliasNumber;
				int number = j + 1;
				Variable var = new Variable("x" + number);
				if (elem instanceof Constant) {
					sql.addCondition(new Condition(alias, var, "", elem, "="));
				} else if (!elem.name.equals("x" + number) && !elem.name.equals("_")) {
					sql.addCondition(new Condition(alias, var, "", elem, "="));
				}
			}
		}

		// add interpreted predicates
		for (InterpretedPredicate intPred : datalog.getInterpretedPredicates()) {
			Variable var = intPred.getVariable();

			for (int i = 0; i < datalog.getPredicates().size(); i++) {
				Predicate pred = datalog.getPredicates().get(i);

				for (Variable var2 : pred.getVariables()) {
					if (var.name.equals(var2.name)) {
						int number = i + 1;
						String leftRelation = "e" + number;

						sql.addCondition(new Condition(leftRelation, var, "",
								intPred.getRight(), intPred.getComparator()));

						i = datalog.getPredicates().size(); // stop loop
					}
				}
			}
		}
	}

	/**
	 * This method changes variable names of predicates and interpreted
	 * predicates in the Datalog query. The variable name is given as argument
	 * 'oldName' and the variable will be changed to 'newName'.
	 * 
	 * @param query
	 *            DatalogQuery
	 * @param oldName
	 *            current name of the variable
	 * @param newName
	 *            new name of the variable
	 */
	private void changeBodyVariableNames(DatalogQuery query, String oldName,
			String newName) {
		for (Predicate pred : query.getPredicates()) {
			for (Variable var : pred.getVariables()) {
				if (var.name.equals(oldName)) {
					var.name = newName;
				}
			}
		}

		for (InterpretedPredicate pred : query.getInterpretedPredicates()) {
			Variable var = pred.getVariable();
			if (var.name.equals(oldName)) {
				var.name = newName;
			}
		}
	}

	/**
	 * Called by method addConditions, it allocates representatives to variables.
	 * Given a condition 'var1 = var2' the argument oldName represents var1 and
	 * newName represents var2. The hash table contains variables as keys and
	 * their values which are repreprentative of the variables.
	 * 
	 * The method first replaces head variables that equal var1 (oldName) with
	 * var2 (newName). The hash table is checked whether newName is already
	 * contained as key. If it is the case newName itself has a representative
	 * and oldName will be allocated the newName's representative. Otherwise a
	 * new 'key-value' pair is put into the hash table.
	 * 
	 * The for-loop iterates through the set of hash table keys. If oldName is
	 * already contained as value in the table the current 'key-oldName' pair
	 * will be replaced by a new pair 'key-newName'.
	 * 
	 * @param query
	 *            DatalogQuery
	 * @param table
	 *            hash table
	 * @param oldName
	 *            variable name of left side of condition
	 * @param newName
	 *            variable name of right side of condition
	 */
	private void setHeadVariablesAndRepresentatives(DatalogQuery query,
			Hashtable<String, String> table, String oldName, String newName) {

		for (Variable var : query.getHeadVariables()) {
			if (var.name.equals(oldName)) {
				var.name = newName;
			}
		}
		if (table.containsKey(newName)) {
			table.put(oldName, table.get(newName));
		} else {
			table.put(oldName, newName);
		}

		for (String key : table.keySet()) {
			if (table.get(key).equals(oldName)) {
				table.put(key, newName);
			}
		}
	}

	/**
	 * Called by method addCondition. It takes a database schema and a relation
	 * name as arguments and returns the predicate of the schema that fits to
	 * the relation name.
	 * 
	 * @param schema
	 *            database schema
	 * @param relName
	 *            relation name which is to be found in the schema
	 * @return Predicate object of schema that is equal to relation name, if
	 *         there is no such predicate null is returned
	 */
	private Predicate findRelation(List<Predicate> schema, String relName) {

		for (Predicate schemaRelation : schema) {
			if (schemaRelation.name.equals(relName))
				return schemaRelation;
		}
		return null;
	}
}
