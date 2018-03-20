package converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class SQLQuery represents an SQL statement and consists of three parts:
 * 
 * 1. SELECT clause
 * 
 * 2. FROM clause
 * 
 * 3. WHERE clause
 * 
 * @author Kevin Irmscher
 */
public class SQLQuery {

	private List<Attribute> select;

	private List<Relation> from;

	private List<Condition> where;

	/**
	 * SQLQuery constructor
	 * 
	 */
	public SQLQuery() {
		select = new ArrayList<Attribute>();
		from = new ArrayList<Relation>();
		where = new ArrayList<Condition>();
	};

	/**
	 * SQLQuery constructor
	 * 
	 * @param select
	 *            List of attributes of the select clause
	 * @param from
	 *            List of relations of the from clause
	 * @param where
	 *            List of conditions of the where clause
	 */
	public SQLQuery(List<Attribute> select, List<Relation> from,
			List<Condition> where) {
		this.select = select;
		this.from = from;
		this.where = where;
	}

	/**
	 * Adds attribute to the select clause.
	 * 
	 * @param var
	 *            attribute
	 */
	public void addAttribute(Attribute attr) {
		select.add(attr);
	}

	/**
	 * Returns list of all attributes contained in the select clause.
	 * 
	 * @return list of attributes
	 */
	public List<Attribute> getAttributes() {
		return select;
	}

	/**
	 * Adds relation to the from clause.
	 * 
	 * @param pred
	 *            relation
	 */
	public void addRelation(Relation pred) {
		from.add(pred);
	}

	/**
	 * Returns all relations contained in the from clause.
	 * 
	 * @return list of relations
	 */
	public List<Relation> getRelations() {
		return from;
	}

	/**
	 * Adds condition to the where clause.
	 * 
	 * @param cond
	 *            condition
	 */
	public void addCondition(Condition cond) {
		where.add(cond);
	}

	/**
	 * Returns all conditions contained in the where clause.
	 * 
	 * @return list of conditions
	 */
	public List<Condition> getConditions() {
		return where;
	}

	/**
	 * Overwrites object method. Returns string representation of the sql query.
	 */
	public String toString() {
		String s;
		if (where.isEmpty()) {
			s = "\nSELECT" + select + "\nFROM " + from;
		} else {
			s = "\nSELECT" + select + "\nFROM  " + from + "\nWHERE " + where;
		}
		s = s.replace('[', ' ');
		s = s.replace(']', ' ');
		return s;
	}

}
