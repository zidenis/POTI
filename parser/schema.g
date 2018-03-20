header 
{ 
	  package parser;
	  import datalog.*;
  import java.util.ArrayList;
  import java.util.List;
}

class SchemaParser extends Parser;
options {defaultErrorHandler=false;k=2;}

{
	  private List<PredicateElement> tempPredElems = new ArrayList<PredicateElement>();
}

predicate returns [Predicate predicate = null]: n:NAME LPAREN vars_or_cons RPAREN
{
  String name = n.getText().trim();
  Predicate pred = new Predicate(name);
  pred.addAllElements(tempPredElems);
  tempPredElems.clear();
  predicate = pred;
};

vars_or_cons : (variable | constant) (COMMA (variable | constant))*;

variable : n:NAME
{
  String name = n.getText().trim();
  tempPredElems.add(new Variable(name));
};

constant : (string_constant | numerical_constant);

string_constant : n:STRING_CONST
{
  String name = n.getText().trim();
  tempPredElems.add(new StringConstant(name));
};

numerical_constant : n:NUMERICAL_CONST
{
  String name = n.getText().trim();
  tempPredElems.add(new NumericalConstant(name)); 
};

class SchemaScanner extends Lexer;
options {defaultErrorHandler=false;k=2;}

COLONDASH: ":-";
LPAREN: '(';
RPAREN: ')';
COMMA : ',';
PERIOD: '.';

NUMERICAL_CONST : (DIGIT)+;

NAME:   ( LETTER | DIGIT)+ ;
LETTER: (LOWERCASE | UPPERCASE);

QUOTE : '"' | "'";    
STRING_CONST : QUOTE NAME QUOTE;

COMPARISON : ('<' | '>' | "<=" | ">=") ;
    
LOWERCASE : 'a'..'z';
UPPERCASE : 'A'..'Z';
DIGIT : '0'..'9';

NEWLINE
    :   '\r' '\n'   // DOS
    |   '\n'        // UNIX
    ;
    
WS : (' ' | '\t' | '\n' | '\r')
{ 
	  _ttype = Token.SKIP; 
	};
	