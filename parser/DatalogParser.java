// $ANTLR : "datalog.g" -> "DatalogParser.java"$
 
	  package parser;
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

public class DatalogParser extends antlr.LLkParser       implements DatalogParserTokenTypes
 {

	  private DatalogQuery query = new DatalogQuery();
	  private List<PredicateElement> tempPredElems = new ArrayList<PredicateElement>();

protected DatalogParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public DatalogParser(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected DatalogParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public DatalogParser(TokenStream lexer) {
  this(lexer,2);
}

public DatalogParser(ParserSharedInputState state) {
  super(state,2);
  tokenNames = _tokenNames;
}

	public final DatalogQuery  query() throws RecognitionException, TokenStreamException {
		DatalogQuery quer = null;
		
		
		head();
		match(COLONDASH);
		body();
		{
		switch ( LA(1)) {
		case PERIOD:
		{
			match(PERIOD);
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
	
	public final void head() throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
		n = LT(1);
		match(VARIABLE);
		match(LPAREN);
		head_variables();
		match(RPAREN);
		
		query.setName(n.getText().trim());
		
	}
	
	public final void body() throws RecognitionException, TokenStreamException {
		
		
		predicate();
		{
		_loop1498:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				predicate();
			}
			else {
				break _loop1498;
			}
			
		} while (true);
		}
	}
	
	public final void head_variables() throws RecognitionException, TokenStreamException {
		
		
		head_var();
		{
		_loop1494:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				head_var();
			}
			else {
				break _loop1494;
			}
			
		} while (true);
		}
	}
	
	public final void head_var() throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
		n = LT(1);
		match(VARIABLE);
		
		String name = n.getText().trim();
		query.addHeadVariable(new Variable(name));
		
	}
	
	public final void predicate() throws RecognitionException, TokenStreamException {
		
		
		{
		if ((LA(1)==VARIABLE) && (LA(2)==LPAREN)) {
			regular_pred();
		}
		else if ((LA(1)==VARIABLE||LA(1)==STRING_CONST||LA(1)==NUMERICAL_CONST) && (LA(2)==COMPARISON)) {
			interpreted_pred();
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
	}
	
	public final void regular_pred() throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
		n = LT(1);
		match(VARIABLE);
		match(LPAREN);
		vars_or_cons();
		match(RPAREN);
		
		String name = n.getText().trim();
		Predicate pred = new Predicate(name);
		pred.addAllElements(tempPredElems);
		tempPredElems.clear();
		query.addPredicate(pred);
		
	}
	
	public final void interpreted_pred() throws RecognitionException, TokenStreamException {
		
		
		{
		switch ( LA(1)) {
		case STRING_CONST:
		case NUMERICAL_CONST:
		{
			const_compar_var();
			break;
		}
		case VARIABLE:
		{
			var_compar_const();
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
	}
	
	public final void vars_or_cons() throws RecognitionException, TokenStreamException {
		
		
		{
		switch ( LA(1)) {
		case VARIABLE:
		{
			variable();
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
		{
		_loop1510:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				{
				switch ( LA(1)) {
				case VARIABLE:
				{
					variable();
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
			else {
				break _loop1510;
			}
			
		} while (true);
		}
	}
	
	public final void const_compar_var() throws RecognitionException, TokenStreamException {
		
		Token  c = null;
		
		constant();
		c = LT(1);
		match(COMPARISON);
		variable();
		
		PredicateElement left = tempPredElems.get(0);
		PredicateElement right = tempPredElems.get(1);
		String comparator = c.getText();
		InterpretedPredicate pred = new InterpretedPredicate(left,right,comparator);
		tempPredElems.clear();
		query.addInterpretedPredicate(pred);
		
	}
	
	public final void var_compar_const() throws RecognitionException, TokenStreamException {
		
		Token  c = null;
		
		variable();
		c = LT(1);
		match(COMPARISON);
		constant();
		
		PredicateElement left = tempPredElems.get(0);
		PredicateElement right = tempPredElems.get(1);
		String comparator = c.getText();
		InterpretedPredicate pred = new InterpretedPredicate(left,right,comparator);
		tempPredElems.clear();
		query.addInterpretedPredicate(pred);
		
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
	
	public final void variable() throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
		n = LT(1);
		match(VARIABLE);
		
		String name = n.getText().trim();
		tempPredElems.add(new Variable(name));
		
	}
	
	public final void string_constant() throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
		n = LT(1);
		match(STRING_CONST);
		
		String name = n.getText().trim();
		tempPredElems.add(new StringConstant(name));
		
	}
	
	public final void numerical_constant() throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
		n = LT(1);
		match(NUMERICAL_CONST);
		
		String name = n.getText().trim();
		tempPredElems.add(new NumericalConstant(name)); 
		
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"COLONDASH",
		"PERIOD",
		"NEWLINE",
		"VARIABLE",
		"LPAREN",
		"RPAREN",
		"COMMA",
		"COMPARISON",
		"STRING_CONST",
		"NUMERICAL_CONST",
		"LETTER",
		"QUOTE",
		"LOWERCASE",
		"UPPERCASE",
		"DIGIT",
		"WS"
	};
	
	
	}
