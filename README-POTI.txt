
@author Thiago CERQUEIRA - (thiagoa7@gmail.com)

======= MiniCon with Preference: POTI =========

* Based on original MiniCon
	* PCDs are computed in a first preliminary step

* Takes into account domains and preferences
	* Pareto semantics is used: there is no order of importance between domains.
	* Returns the N first best rewritings according to the user preferences


== Requirements ==

		Needs 2 input files, named testcases.xml (for test cases) and 
    preferences.xml (for user preferences for concrete services)

== How to run it ==
	In the base directory call

		java minicon.MiniConPref  TEST_ID  [ NUMBER_OF_REQUIRED_REWRITINGS ]

=> The first argument (TEST_ID) is MANDATORY, and must correspond to a 
   present test Id in files testcases.xml and preferences.xml

=> The second argument (NUMBER_OF_REQUIRED_REWRITINGS) is OPTIONAL. 
   If it is not provided, ALL the rewritings will be returned

=================================================
