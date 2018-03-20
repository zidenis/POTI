// $ANTLR : "sqlQuery.g" -> "SQLScanner.java"$
 
	  package parser;
	  import converter.*;
	  import datalog.*;
  import java.util.ArrayList;
  import java.util.List;

public interface SQLParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int SELECT = 4;
	int FROM = 5;
	int WHERE = 6;
	int SEMICOLON = 7;
	int NEWLINE = 8;
	int COMMA = 9;
	int NAME = 10;
	int PERIOD = 11;
	int AS = 12;
	int AND = 13;
	int COMPARISON = 14;
	int STRING_CONST = 15;
	int NUMERICAL_CONST = 16;
	int COLONDASH = 17;
	int LPAREN = 18;
	int RPAREN = 19;
	int LETTER = 20;
	int QUOTE = 21;
	int LOWERCASE = 22;
	int UPPERCASE = 23;
	int DIGIT = 24;
	int WS = 25;
}
