// $ANTLR : "schema.g" -> "SchemaScanner.java"$
 
	  package parser;
	  import datalog.*;
  import java.util.ArrayList;
  import java.util.List;

public interface SchemaParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int NAME = 4;
	int LPAREN = 5;
	int RPAREN = 6;
	int COMMA = 7;
	int STRING_CONST = 8;
	int NUMERICAL_CONST = 9;
	int COLONDASH = 10;
	int PERIOD = 11;
	int LETTER = 12;
	int QUOTE = 13;
	int COMPARISON = 14;
	int LOWERCASE = 15;
	int UPPERCASE = 16;
	int DIGIT = 17;
	int NEWLINE = 18;
	int WS = 19;
}
