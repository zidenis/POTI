// $ANTLR : "sqlQuery.g" -> "SQLParser.java"$
 
	  package parser;
	  import converter.*;
	  import datalog.*;
  import java.util.ArrayList;
  import java.util.List;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;

public class SQLParser extends antlr.LLkParser       implements SQLParserTokenTypes
 {

	  private SQLQuery query = new SQLQuery();
	  //private List<PredicateElement> tempPredElems = new ArrayList<PredicateElement>();
	  private String relationLeft = "";
	  private Variable variableLeft = null;
	  private String relationRight = "";
	  private PredicateElement elementRight = null;

protected SQLParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public SQLParser(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected SQLParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public SQLParser(TokenStream lexer) {
  this(lexer,2);
}

public SQLParser(ParserSharedInputState state) {
  super(state,2);
  tokenNames = _tokenNames;
}

	public final SQLQuery  query() throws RecognitionException, TokenStreamException {
		SQLQuery quer = null;
		
		
		match(SELECT);
		attributes();
		match(FROM);
		relations();
		{
		switch ( LA(1)) {
		case WHERE:
		{
			match(WHERE);
			conditions();
			break;
		}
		case EOF:
		case SEMICOLON:
		case NEWLINE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case SEMICOLON:
		{
			match(SEMICOLON);
			break;
		}
		case EOF:
		case NEWLINE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case NEWLINE:
		{
			match(NEWLINE);
			break;
		}
		case EOF:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		
					  quer = query;
		
		return quer;
	}
	
	public final void attributes() throws RecognitionException, TokenStreamException {
		
		
		attr();
		{
		_loop7:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				attr();
			}
			else {
				break _loop7;
			}
			
		} while (true);
		}
	}
	
	public final void relations() throws RecognitionException, TokenStreamException {
		
		
		relation();
		{
		_loop11:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				relation();
			}
			else {
				break _loop11;
			}
			
		} while (true);
		}
	}
	
	public final void conditions() throws RecognitionException, TokenStreamException {
		
		
		condition();
		{
		_loop15:
		do {
			if ((LA(1)==AND)) {
				match(AND);
				condition();
			}
			else {
				break _loop15;
			}
			
		} while (true);
		}
	}
	
	public final void attr() throws RecognitionException, TokenStreamException {
		
		Token  r = null;
		Token  v = null;
		
		r = LT(1);
		match(NAME);
		match(PERIOD);
		v = LT(1);
		match(NAME);
		
		String relation = r.getText().trim();
		String variable = v.getText().trim();
		query.addAttribute(new Attribute(relation,new Variable(variable)));
		
	}
	
	public final void relation() throws RecognitionException, TokenStreamException {
		
		Token  r = null;
		Token  n = null;
		
		r = LT(1);
		match(NAME);
		match(AS);
		n = LT(1);
		match(NAME);
		
				  String relation = r.getText().trim();
		String name = n.getText().trim();
		Relation rel = new Relation(new Predicate(relation), name);
		query.addRelation(rel);
		
	}
	
	public final void condition() throws RecognitionException, TokenStreamException {
		
		
		comparison();
	}
	
	public final void comparison() throws RecognitionException, TokenStreamException {
		
		Token  c = null;
		
		left();
		c = LT(1);
		match(COMPARISON);
		right();
		
		//Variable left = (Variable) tempPredElems.get(0);
		//PredicateElement right = tempPredElems.get(1);
		String comparator = c.getText();
		Condition cond = new Condition(relationLeft,variableLeft, relationRight, elementRight, comparator);
		//tempPredElems.clear();
		query.addCondition(cond);
		
	}
	
	public final void left() throws RecognitionException, TokenStreamException {
		
		Token  r = null;
		Token  v = null;
		
		r = LT(1);
		match(NAME);
		match(PERIOD);
		v = LT(1);
		match(NAME);
		
		relationLeft = r.getText().trim();
		variableLeft = new Variable(v.getText().trim());
		
		
	}
	
	public final void right() throws RecognitionException, TokenStreamException {
		
		
		{
		switch ( LA(1)) {
		case NAME:
		{
			rel_var();
			break;
		}
		case STRING_CONST:
		case NUMERICAL_CONST:
		{
			constant();
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
	}
	
	public final void rel_var() throws RecognitionException, TokenStreamException {
		
		Token  r = null;
		Token  v = null;
		
		r = LT(1);
		match(NAME);
		match(PERIOD);
		v = LT(1);
		match(NAME);
		
		relationRight = r.getText().trim();
		elementRight = new Variable(v.getText().trim());
		
	}
	
	public final void constant() throws RecognitionException, TokenStreamException {
		
		
		{
		switch ( LA(1)) {
		case STRING_CONST:
		{
			string_constant();
			break;
		}
		case NUMERICAL_CONST:
		{
			numerical_constant();
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
	}
	
	public final void string_constant() throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
		n = LT(1);
		match(STRING_CONST);
		
		relationRight = "";
		elementRight = new StringConstant(n.getText().trim());
		
	}
	
	public final void numerical_constant() throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
		n = LT(1);
		match(NUMERICAL_CONST);
		
		relationRight = "";
		elementRight = new NumericalConstant(n.getText().trim());
		
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"SELECT",
		"FROM",
		"WHERE",
		"SEMICOLON",
		"NEWLINE",
		"COMMA",
		"NAME",
		"PERIOD",
		"AS",
		"AND",
		"COMPARISON",
		"STRING_CONST",
		"NUMERICAL_CONST",
		"COLONDASH",
		"LPAREN",
		"RPAREN",
		"LETTER",
		"QUOTE",
		"LOWERCASE",
		"UPPERCASE",
		"DIGIT",
		"WS"
	};
	
	
	}
