header 
{ 
	  package parser;
	  import converter.*;
	  import datalog.*;
  import java.util.ArrayList;
  import java.util.List;
}

class SQLParser extends Parser;
options {defaultErrorHandler=false;k=2;}

{
	  private SQLQuery query = new SQLQuery();
	  //private List<PredicateElement> tempPredElems = new ArrayList<PredicateElement>();
	  private String relationLeft = "";
	  private Variable variableLeft = null;
	  private String relationRight = "";
	  private PredicateElement elementRight = null;
}

query returns [SQLQuery quer = null] : SELECT attributes FROM relations (WHERE conditions)? (SEMICOLON)? (NEWLINE)? 
{
			  quer = query;
};

attributes : attr (COMMA attr)*	;
	
attr : r:NAME PERIOD v:NAME
{
  String relation = r.getText().trim();
  String variable = v.getText().trim();
  query.addAttribute(new Attribute(relation,new Variable(variable)));
};

relations : relation (COMMA relation)*;

relation : r:NAME AS n:NAME
{
		  String relation = r.getText().trim();
  String name = n.getText().trim();
  Relation rel = new Relation(new Predicate(relation), name);
  query.addRelation(rel);
};

conditions: condition (AND condition)*;

condition :  comparison;

comparison : left c:COMPARISON right
{
  //Variable left = (Variable) tempPredElems.get(0);
  //PredicateElement right = tempPredElems.get(1);
  String comparator = c.getText();
  Condition cond = new Condition(relationLeft,variableLeft, relationRight, elementRight, comparator);
  //tempPredElems.clear();
  query.addCondition(cond);
};


left : r:NAME PERIOD v:NAME
{
  relationLeft = r.getText().trim();
  variableLeft = new Variable(v.getText().trim());

};

right : (rel_var | constant);

rel_var :  r:NAME PERIOD v:NAME
{
  relationRight = r.getText().trim();
  elementRight = new Variable(v.getText().trim());
};

constant : (string_constant | numerical_constant);

string_constant : n:STRING_CONST
{
  relationRight = "";
  elementRight = new StringConstant(n.getText().trim());
};

numerical_constant : n:NUMERICAL_CONST
{
  relationRight = "";
  elementRight = new NumericalConstant(n.getText().trim());
};

class SQLScanner extends Lexer;
options {defaultErrorHandler=false;k=2;}

SELECT : ("select" | "Select" | "SELECT") ("distinct" | "Distinct" | "DISTINCT")?;
FROM : ("from" | "From" | "FROM");
WHERE : ("where" | "Where" | "WHERE");


COLONDASH: ":-";
LPAREN: '(';
RPAREN: ')';
COMMA : ',';
PERIOD: '.';
SEMICOLON: ';';

AS: ("AS" | "as");

AND: ("AND" | "and" | "And");

NUMERICAL_CONST : (DIGIT)+;

NAME:   ( LETTER | DIGIT)+ ;

LETTER: (LOWERCASE | UPPERCASE);

QUOTE : '"' | "'";    
STRING_CONST : QUOTE NAME QUOTE;

COMPARISON : ('<' | '>' | "<=" | ">=" | '=') ;
    
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