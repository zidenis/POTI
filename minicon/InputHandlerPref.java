package minicon;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import converter.Converter;
import converter.SQLQuery;

import parser.DatalogParser;
import parser.DatalogScanner;
import parser.SQLParser;
import parser.SQLScanner;
import parser.SchemaParser;
import parser.SchemaScanner;

import datalog.DatalogQuery;
import datalog.InterpretedPredicate;
import datalog.Predicate;
import datalog.Variable;

import antlr.RecognitionException;
import antlr.TokenStreamException;

/**
 * Class InputHandler takes care of every kind of input provided by the user. It
 * can handle four types of input:
 * 
 * 1. Datalog command line input: user enters Datalog queries (using
 * DatalogParser)
 * 
 * 2. Datalog XML input: XML file with Datalog queries (using DatalogParser)
 * 
 * 3. SQL command line input: user enters DB schema definition and SQL
 * statements (using SchemaParser and SQLParser)
 * 
 * 4. SQL XML input: XML file with DB schema definition and SQL statements.
 * (using SchemaParser and SQLParser)
 * 
 * The main method returns a MiniCon object which contains DatalogQuery objects
 * for the query and the view. It is used by the algorithm to compute the
 * rewritings.
 * 
 * @author Kevin Irmscher
 */
public class InputHandlerPref {

	/** verbose mode, i.e. print out MCDs if true */
	public static boolean verbose = false;

	/** SQL command line or file input */
	public static boolean sqlInput = false;

	/** remove redundandies from rewriting */
	public static boolean removeRedundant = false;

	/** used to convert Datalog to SQL and v.v. */
	private static Converter convert = new Converter();

	/**
	 * Read arguments, set boolean values for verbose and SQL, call handling
	 * methods according to the parameters.
	 * 
	 * @param args
	 *            input arguments
	 * @return MiniCon object
	 */
	public static MiniConPref handleArguments(String[] args) {

		MiniConPref mc;
		boolean fileInput = false;
		String fileName = "";
		String id = "";

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-v")) {
				verbose = true;
			}
			// file input
			if (args[i].equals("-f")) {
				fileInput = true;
				if (args.length <= i + 2) {
					System.out
							.println("\nwrong arguments! \n\nOptions:\n -v                 : verbose mode \n -sql               : SQL input \n -f FILENAME.XML ID : input file mode");
					return null;
				} else {
					fileName = args[i + 1];
					id = args[i + 2];
				}
			}
			// sql input
			if (args[i].equals("-sql")) {
				sqlInput = true;
			}
			// remove redundancies
			if ((args[i].equals(("-r")) || (args[i].equals("remove")))) {
				removeRedundant = true;
			}
		}

		if (verbose) {
			System.out.println("[verbose mode]\n");
		}

		if (fileInput && !sqlInput) {
			return readFile(fileName, id);
		} else if (sqlInput && !fileInput) {
			return sqlInput();
		} else if (sqlInput && fileInput) {
			return readSQLFile(fileName, id);
		} else {
			return readInput();
		}
	}

	/**
	 * Calls method parse() in order to parse query and view String object which will
	 * result in DatalogQuery objects.
	 * 
	 * @param queryStr
	 *            query string provided by the user
	 * @param viewsStr
	 *            list of view strings provided by the user
	 * @return MiniCon object that comprises DatalogQuery objects for the query
	 *         and the views.
	 */
	public static MiniConPref parseInput(String queryStr, List<String> viewsStr) {
		DatalogQuery query = null;
		List<DatalogQuery> views = new ArrayList<DatalogQuery>();
		query = parse(queryStr);
		for (String viewStr : viewsStr) {
			DatalogQuery view = parse(viewStr);
			views.add(view);
		}
		return new MiniConPref(query, views);
	}

	/**
	 * Method parses String objects representing Datalog queries using class
	 * DatalogParser. The parser returns objects of class DatalogQuery.
	 * 
	 * @param queryStr
	 *            string that is parsed
	 * @return DatalogQuery object
	 */
	public static DatalogQuery parse(String queryStr) {
		DatalogQuery query = null;

		try {
			StringReader in = new StringReader(queryStr);
			DatalogScanner scanner = new DatalogScanner(in);
			DatalogParser parser = new DatalogParser(scanner);
			query = parser.query();

		} catch (RecognitionException e) {
			System.out.println("Parsing error");
		} catch (TokenStreamException e) {
			System.out.println("Parsing error");
		}
		return query;
	}

	/**
	 * Method handles command line SQL input.
	 * 
	 * 1. reading and parsing the schema definition using SchemaParser
	 * 
	 * 2. reading and parsing SQL query using SQLParser
	 * 
	 * 3. reading and parsing set of SQL views using SQLParser
	 * 
	 * 4. converting parsed SQL query and list of SQL views to Datalog queries
	 * 
	 * 5. creating and return MiniCon object that contains DatalogQuery objects
	 * for query and views.
	 * 
	 * @return MiniCon object that contains
	 */
	private static MiniConPref sqlInput() {
		System.out.println("[sql input]");

		BufferedReader buffread = new BufferedReader(new InputStreamReader(
				System.in));
		boolean readAgain = true;
		SQLQuery query = null;

		// read schema
		int count = 1;
		readAgain = true;
		String s = new String();

		List<Predicate> schema = new ArrayList<Predicate>();
		System.out.println("\ndatabase schema: name(attr1,attr2,attr3,....)");
		while (readAgain) {
			System.out.print("enter relation " + count + ": ");

			try {
				s = buffread.readLine();
				if (s.length() > 0) {
					StringReader in = new StringReader(s);
					SchemaScanner scanner = new SchemaScanner(in);
					SchemaParser parser = new SchemaParser(scanner);
					schema.add(parser.predicate());
					count++;
				} else {
					readAgain = false;
				}
			} catch (IOException ioe) {
				System.out.println("unexpected IO error");
				readAgain = true;
			} catch (RecognitionException re) {
				System.out.println("Parsing error");
				readAgain = true;
			} catch (TokenStreamException e) {
				System.out.println("Parsing error");
				readAgain = true;
			}
		}

		// read sql query
		count = 1;
		readAgain = true;
		System.out
				.println("\nSQL query: SELECT attr1, attr2 FROM rel1, rel2 WHERE rel1.attr1 = rel2.attr2");
		while (readAgain) {
			System.out.print("enter sql query: ");

			try {
				s = buffread.readLine();
				StringReader in = new StringReader(s);
				SQLScanner scanner = new SQLScanner(in);
				SQLParser parser = new SQLParser(scanner);
				query = parser.query();
				readAgain = false;
			} catch (IOException ioe) {
				System.out.println("unexpected IO error");
				readAgain = true;
			} catch (RecognitionException re) {
				System.out.println("Parsing error");
				readAgain = true;
			} catch (TokenStreamException e) {
				System.out.println("Parsing error");
				readAgain = true;
			}
		}

		// read sql views
		List<SQLQuery> sqlViews = new ArrayList<SQLQuery>();
		count = 1;
		readAgain = true;
		while (readAgain) {
			System.out.print("enter sql view " + count + ": ");

			try {
				s = buffread.readLine();
				if (s.length() > 0) {
					StringReader in = new StringReader(s);
					SQLScanner scanner = new SQLScanner(in);
					SQLParser parser = new SQLParser(scanner);
					sqlViews.add(parser.query());
					count++;
				} else {
					readAgain = false;
				}
			} catch (IOException ioe) {
				System.out.println("unexpected IO error");
				readAgain = true;
			} catch (RecognitionException re) {
				System.out.println("Parsing error");
				readAgain = true;
			} catch (TokenStreamException e) {
				System.out.println("Parsing error");
				readAgain = true;
			}
		}

		DatalogQuery datalogQuery = convert
				.convertToDatalog("Q", schema, query);
		System.out.println(datalogQuery);
		List<DatalogQuery> views = new ArrayList<DatalogQuery>();

		count = 1;
		for (SQLQuery view : sqlViews) {
			String name = "V" + count;
			count++;
			views.add(convert.convertToDatalog(name, schema, view));
		}

		// print out view and rename view variables
		for (DatalogQuery view : views) {
			System.out.println(view);
			renameVariables(view);
		}
		return new MiniConPref(datalogQuery, views);
	}

	/**
	 * Parses a single relation of a database schema definition using
	 * SchemaParser.
	 * 
	 * @param relationStr
	 *            string representing database relation
	 * @return Predicate object
	 */
	public static Predicate parseSchema(String relationStr) {
		Predicate relation = null;

		try {
			StringReader in = new StringReader(relationStr);
			SchemaScanner scanner = new SchemaScanner(in);
			SchemaParser parser = new SchemaParser(scanner);
			relation = parser.predicate();

		} catch (RecognitionException e) {
			System.out.println("Parsing error");
		} catch (TokenStreamException e) {
			System.out.println("Parsing error");
		}
		return relation;
	}

	/**
	 * Parses a single SQL query string using SQLParser.
	 * 
	 * @param queryStr
	 *            string representing SQL query
	 * @return SQLQuery object
	 */
	public static SQLQuery parseSQL(String queryStr) {
		SQLQuery query = null;

		try {
			StringReader in = new StringReader(queryStr);
			SQLScanner scanner = new SQLScanner(in);
			SQLParser parser = new SQLParser(scanner);
			query = parser.query();

		} catch (RecognitionException e) {
			System.out.println("Parsing error");
		} catch (TokenStreamException e) {
			System.out.println("Parsing error");
		}
		return query;
	}

	/**
	 * The method reads in Datalog command line input. It parses query and views
	 * using DatalogParser and returns a MiniCon object which contains objects
	 * of class DatalogQuery representing query and views.
	 * 
	 * @return MiniCon object
	 */
	private static MiniConPref readInput() {

		List<DatalogQuery> views = new ArrayList<DatalogQuery>();
		DatalogQuery query = null;

		BufferedReader buffread = new BufferedReader(new InputStreamReader(
				System.in));
		boolean readAgain = true;

		// read query
		while (readAgain) {
			System.out.print("enter query: ");

			try {
				String s = buffread.readLine();
				StringReader in = new StringReader(s);
				DatalogScanner scanner = new DatalogScanner(in);
				DatalogParser parser = new DatalogParser(scanner);
				query = parser.query();
				readAgain = false;
			} catch (IOException ioe) {
				System.out.println("unexpected IO error");
				readAgain = true;
			} catch (RecognitionException re) {
				System.out.println("Parsing error");
				readAgain = true;
			} catch (TokenStreamException e) {
				System.out.println("Parsing error");
				readAgain = true;
			}
		}

		// read views
		int count = 1;
		readAgain = true;
		String s = new String();

		while (readAgain) {
			System.out.print("enter view " + count + ": ");

			try {
				s = buffread.readLine();
				if (s.length() > 0) {
					StringReader in = new StringReader(s);
					DatalogScanner scanner = new DatalogScanner(in);
					DatalogParser parser = new DatalogParser(scanner);
					DatalogQuery view = parser.query();
					views.add(view);
					count++;
				} else {
					readAgain = false;
				}
			} catch (IOException ioe) {
				System.out.println("unexpected IO error");
				readAgain = true;
			} catch (RecognitionException re) {
				System.out.println("Parsing error");
				readAgain = true;
			} catch (TokenStreamException e) {
				System.out.println("Parsing error");
				readAgain = true;
			}
		}
		return new MiniConPref(query, views);
	}

	/**
	 * Handles Datalog XML file input. It reads in file and parser the XML
	 * document to obtain the test case with the id provided as argument.
	 * 
	 * The query String object and the list of views is then parsed by using method
	 * parseInput which results in DatalogQuery objects. Using these objects a
	 * MiniCon objects is instatiated and returned.
	 * 
	 * @param testcaseFile
	 *            XML file with Datalog test cases
	 * @param testID
	 *            id of relevant test case
	 * @return MiniCon object
	 */
	public static MiniConPref readFile(String testcaseFile, String testID) {

		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new File(testcaseFile));

			// normalize text representation
			doc.getDocumentElement().normalize();

			NodeList testcases = doc.getDocumentElement().getElementsByTagName(
					"testcase");

			for (int i = 0; i < testcases.getLength(); i++) {

				Node testcaseNode = testcases.item(i);

				if (testcaseNode.getNodeType() == Node.ELEMENT_NODE) {

					Element content = (Element) testcaseNode;
					Node idNode = content.getElementsByTagName("id").item(0);
					if (idNode.getTextContent().equals(testID)) {

						Node queryElem = content.getElementsByTagName("query")
								.item(0);
						String queryStr = queryElem.getTextContent();

						NodeList viewNodes = content
								.getElementsByTagName("view");

						List<String> viewsStr = new ArrayList<String>();
						for (int j = 0; j < viewNodes.getLength(); j++) {
							viewsStr.add(viewNodes.item(j).getTextContent());
						}
						return parseInput(queryStr, viewsStr);
					}
				}
			}

		} catch (SAXParseException err) {
			System.out.println("** Parsing error" + ", line "
					+ err.getLineNumber() + ", uri " + err.getSystemId());
			System.out.println(" " + err.getMessage());

		} catch (SAXException e) {
			Exception x = e.getException();
			((x == null) ? e : x).printStackTrace();

		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	/**
	 * Handles schema definitions and SQL queries in an XML file. Reads in and
	 * parses XML file to obtain a SQL test case that belongs to testID given as
	 * argument.
	 * 
	 * SQL input is parsed by using method handleSQLFileInput. This helper
	 * method also converts SQL queries to Datalog queries and returns a MiniCon
	 * object.
	 * 
	 * @param testcaseFile
	 *            XML file with SQL test cases
	 * @param testID
	 *            id of relevant test case
	 * @return MiniCon object
	 */
	public static MiniConPref readSQLFile(String testcaseFile, String testID) {

		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new File(testcaseFile));

			// normalize text representation
			doc.getDocumentElement().normalize();

			NodeList testcases = doc.getDocumentElement().getElementsByTagName(
					"testcase");

			for (int i = 0; i < testcases.getLength(); i++) {

				Node testcaseNode = testcases.item(i);

				if (testcaseNode.getNodeType() == Node.ELEMENT_NODE) {

					Element content = (Element) testcaseNode;
					Node idNode = content.getElementsByTagName("id").item(0);
					if (idNode.getTextContent().equals(testID)) {

						Node schemaElem = content.getElementsByTagName(
								"DBschema").item(0);
						NodeList relations = schemaElem.getChildNodes();

						List<String> relationsStr = new ArrayList<String>();
						for (int j = 0; j < relations.getLength(); j++) {

							if (relations.item(j).getNodeType() == Node.ELEMENT_NODE) {
								relationsStr.add(relations.item(j)
										.getTextContent());
							}
						}

						Node queryElem = content.getElementsByTagName(
								"SQLquery").item(0);
						String queryStr = queryElem.getTextContent();

						NodeList viewNodes = content
								.getElementsByTagName("SQLview");

						List<String> viewsStr = new ArrayList<String>();
						for (int j = 0; j < viewNodes.getLength(); j++) {
							viewsStr.add(viewNodes.item(j).getTextContent());
						}
						return handleSQLFileInput(relationsStr, queryStr,
								viewsStr);
					}
				}
			}

		} catch (SAXParseException err) {
			System.out.println("** Parsing error" + ", line "
					+ err.getLineNumber() + ", uri " + err.getSystemId());
			System.out.println(" " + err.getMessage());

		} catch (SAXException e) {
			Exception x = e.getException();
			((x == null) ? e : x).printStackTrace();

		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	/**
	 * Called by readSQLInput. The method does the following:
	 * 
	 * 1. parse schema definition using method parseSchema
	 * 
	 * 2. parse SQL query using method parseSQL
	 * 
	 * 3. parse SQL views using method parseSQL
	 * 
	 * 4. convert SQL query to Datalog query
	 * 
	 * 5. convert SQL views to Datalog views (queries)
	 * 
	 * 6. rename view variables to descriminate them from query variables using
	 * method renameVariables
	 * 
	 * 7. return MiniCon object containing Datalog query and views
	 * 
	 * @param schema
	 *            database schema definition
	 * @param queryStr
	 *            string representing SQL query
	 * @param viewsStr
	 *            list of strings representing SQL views
	 * @return MiniCon object
	 */
	public static MiniConPref handleSQLFileInput(List<String> schema,
			String queryStr, List<String> viewsStr) {
		List<Predicate> relations = new ArrayList<Predicate>();

		// parse schema
		for (String relationStr : schema) {
			Predicate relation = parseSchema(relationStr);
			relations.add(relation);
		}

		SQLQuery sqlQuery = null;
		List<SQLQuery> sqlViews = new ArrayList<SQLQuery>();
		sqlQuery = parseSQL(queryStr); // parse SQL query

		// parse SQL views
		for (String viewStr : viewsStr) {
			SQLQuery view = parseSQL(viewStr);
			sqlViews.add(view);
		}

		// convert SQL query to Datalog
		DatalogQuery query = convert.convertToDatalog("Q", relations, sqlQuery);

		// convert SQL views to Datalog
		List<DatalogQuery> views = new ArrayList<DatalogQuery>();
		int count = 1;
		for (SQLQuery view : sqlViews) {
			String name = "V" + count;
			count++;
			views.add(convert.convertToDatalog(name, relations, view));
		}

		// rename view variables
		for (DatalogQuery view : views) {
			renameVariables(view);
		}
		return new MiniConPref(query, views);
	}

	/**
	 * Called by method sqlInput. It renames head variables and variables of
	 * predicates and interpreted predicates. The variable names are kept and a '
	 * is added.
	 * 
	 * This method will only be called to rename variable names of views in
	 * order to discrimenate them from query variables. However, renaming
	 * doesn't influence the behavior of the MiniCon algorithm.
	 * 
	 * @param query
	 *            DatalogQuery object
	 * @return DatalogQuery object with variable renamed
	 */
	private static DatalogQuery renameVariables(DatalogQuery query) {

		for (Variable var : query.getHeadVariables()) {
			var.name = var.name + "'";
		}

		for (Predicate pred : query.getPredicates()) {
			for (Variable var : pred.getVariables()) {
				var.name = var.name + "'";
			}
		}

		for (InterpretedPredicate pred : query.getInterpretedPredicates()) {
			pred.getVariable().name = pred.getVariable().name + "'";
		}
		return query;
	}

	/**
	 * Method takes a list of rewritings as argument. It converts the rewritings to
	 * SQL statements and prints out the result.
	 * 
	 * @param rewritings
	 *            list of rewritings
	 */
	public static void printSQLStmts(List<Rewriting> rewritings) {
		System.out.print("\nPrint SQL statement(s)? (Y/N): ");

		BufferedReader buffread = new BufferedReader(new InputStreamReader(
				System.in));
		try {
			String s = buffread.readLine();

			if (s.toLowerCase().equals("y")) {
				for (Rewriting rw : rewritings) {
					System.out.println(convert.convertToSQL(rw));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
