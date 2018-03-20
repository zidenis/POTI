// $ANTLR : "schema.g" -> "SchemaParser.java"$
 
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

public class SchemaParser extends antlr.LLkParser       implements SchemaParserTokenTypes
 {

	  private List<PredicateElement> tempPredElems = new ArrayList<PredicateElement>();

protected SchemaParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public SchemaParser(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected SchemaParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public SchemaParser(TokenStream lexer) {
  this(lexer,2);
}

public SchemaParser(ParserSharedInputState state) {
  super(state,2);
  tokenNames = _tokenNames;
}

	public final Predicate  predicate() throws RecognitionException, TokenStreamException {
		Predicate predicate = null;
		
		Token  n = null;
		
		n = LT(1);
		match(NAME);
		match(LPAREN);
		vars_or_cons();
		match(RPAREN);
		
		String name = n.getText().trim();
		Predicate pred = new Predicate(name);
		pred.addAllElements(tempPredElems);
		tempPredElems.clear();
		predicate = pred;
		
		return predicate;
	}
	
	public final void vars_or_cons() throws RecognitionException, TokenStreamException {
		
		
		{
		switch ( LA(1)) {
		case NAME:
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
		_loop1700:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				{
				switch ( LA(1)) {
				case NAME:
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
				break _loop1700;
			}
			
		} while (true);
		}
	}
	
	public final void variable() throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
		n = LT(1);
		match(NAME);
		
		String name = n.getText().trim();
		tempPredElems.add(new Variable(name));
		
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
		"NAME",
		"LPAREN",
		"RPAREN",
		"COMMA",
		"STRING_CONST",
		"NUMERICAL_CONST",
		"COLONDASH",
		"PERIOD",
		"LETTER",
		"QUOTE",
		"COMPARISON",
		"LOWERCASE",
		"UPPERCASE",
		"DIGIT",
		"NEWLINE",
		"WS"
	};
	
	
	}
