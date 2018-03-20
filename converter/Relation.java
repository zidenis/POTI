/*
 * Created on 30.05.2005
 */
package converter;

import datalog.Predicate;

/**
 * Class Relation represents elements of the FROM clause of an SQL statement.
 * Member variable 'relation' holds the actual relation and variable 'alias'
 * denotes an alias name that is used in the SQL statement.
 * 
 * @author Kevin Irmscher
 */
public class Relation {

	/** relation in the from clause */
	Predicate relation;

	/** alias name of the relation */
	String alias;

	/**
	 * Relation constructor
	 * 
	 * @param relation
	 *            relation in the from clause
	 * @param alias
	 *            alias name of relation
	 */
	public Relation(Predicate relation, String alias) {
		this.relation = relation;
		this.alias = alias;
	}

	/**
	 * Overwrites object method
	 */
	public String toString() {
		return relation.name + " AS " + alias;
	}

}
