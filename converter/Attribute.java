package converter;

import datalog.Variable;

/**
 * Class Attribute represents elements of the SQL statement's select clause. It
 * is composed of the alias name of a relation and a variable of this relation.
 * The string representation of an Attribute object is: aliasName.variable
 * 
 * @author Kevin Irmscher
 */
public class Attribute {

	/** alias name of a relation of the relevant variable */
	String alias;

	/** variable in the select clause */
	Variable variable;

	/**
	 * Attribute constructor
	 * 
	 * @param alias
	 *            alias name of relation
	 * @param variable
	 *            variable in select clause
	 */
	public Attribute(String alias, Variable variable) {
		this.alias = alias;
		this.variable = variable;
	}

	/**
	 * Overwrites object method
	 */
	public String toString() {
		return alias + "." + variable.name;
	}
}
