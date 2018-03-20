
/*
 *   Copyright 2015 Cheikh BA <cheikh.ba.sn@gmail.com>
 *
 *   This file is part of POTI.
 *
 *   POTI is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   POTI is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License,
 *   along with POTI.  If not, see <http://www.gnu.org/licenses/>.
*/


/*
 * Created on 20.01.2005
 * Programming project - Implementation of MiniCon algorithm
 * Modified by Cheikh
 */
package minicon;

import java.util.ArrayList;
import java.util.List;

import preference.PreferencesFileParser;

import datalog.DatalogQuery;
import datalog.Predicate;
import datalog.PredicateElement;
import java.lang.management.ManagementFactory;
import preference.BestFirst;
import preference.Index;

/**
 *
 * MiniCon is the main class of the implementation of the MiniCon algorithm. It
 * contains the main method to start the program. It uses class InputHandler to
 * obtain parsed user input in form of a MiniCon object. It contains the query
 * and a list of views. Basically, the algorithm consists of three steps: 1.
 * forming the MCDs, 2. combining the MCD, and 3. removing redundant subgoals
 * The last part is optional.
 *
 * @author Kevin Irmscher
 */
public class MiniConPref {

    private static int testID;

    /**
     * query Object used by algorithm
     */
    private DatalogQuery query;

    /**
     * list of views used by algorithm
     */
    private List<DatalogQuery> views;

    /**
     * list of MCDs created by algorithm
     */
    private List<MCD> mcds;

    /**
     * list of rewritings created by the algorithm
     */
    private List<Rewriting> rewritings;

    /**
     * MiniCon constructor
     *
     * @param query query obtained from the parser
     * @param views list of views obtained from the parser
     */
    public MiniConPref(DatalogQuery query, List<DatalogQuery> views) {
        this.query = query;
        this.views = views;
        this.mcds = new ArrayList<MCD>();
        this.rewritings = new ArrayList<Rewriting>();
    }

    /**
     * Main method will be called to start the algorithm. It uses class
     * InputHandler to handle the arguments provided by parameter args.
     * InputHandler will return a MiniCon object which contains the query and a
     * list of views.
     *
     * @param args -v : verbose mode (print MCDs);
     *
     * -f FILE.XML ID : read testcase with ID from file;
     *
     * -sql : SQL input mode;
     *
     * -r : remove redundancies
     */
    public static void main(String[] args) throws Exception {
        System.out.println("MiniConPref Algorithm - Poti");

        testID = Integer.parseInt(args[2]);

        MiniConPref mc = InputHandlerPref.handleArguments(args);

        if (mc != null) {
            mc.printQuery();  // comment for time evaluation
            mc.printViews();  // comment for time evaluation

            long start = ManagementFactory.getThreadMXBean().getCurrentThreadUserTime();
            mc.startMiniCon();
            long time = ManagementFactory.getThreadMXBean().getCurrentThreadUserTime() - start;

            mc.printRewritings(); // comment for time evaluation
            System.out.println("Done in: " + time + "ns");
        }

    }

    /**
     * The method will execute the actual algorithm. Three method calls will be
     * performed regarding to the three parts of the algorithm. 1. forming MCDs,
     * 2. combining MCDs, 3. remove redundancies; the last call depends on
     * whether argument -r is provided
     */
    public void startMiniCon() {
        formMCDs();

        /*C.BA*/
        // set the MCD preferences ...
        try {

            PreferencesFileParser.setMCDPreferences(mcds, "preferences.xml", testID);

            /* T.Abreu */
            // Initialize the Index and then the BestFirst iterator
            Index.initialize(mcds, query);
            BestFirst bf = new BestFirst(query);

            rewritings = bf.getRewritings(query);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("OUPSS !!! " + e);
            System.out.println("mcds.size() = " + mcds.size());
            System.out.println("rewritings.size() = " + rewritings.size());
        }

    }

    /**
     * The method will create the MCDs that are possible for the given query and
     * views. Every subgoal of the query will be considered separately. For each
     * subgoal the algorithm will create all possible mappings to each given
     * view. For every obtained valid mapping an MCD will be created. Using the
     * class MCD, it will be checked whether all properties are fulfilled and if
     * necessary, the MCD will be extended. If the MCD is valid, it will be
     * added to the list of MCDs. Finally duplicate MCDs will be removed from
     * the list.
     */
    private void formMCDs() {

        // subgoal of the query
        List<Predicate> subgoals = query.getPredicates();

        for (Predicate subgoal : subgoals) {
			// System.out.println("\n current subgoal " + subgoal);

            // for every view try to create mappings
            for (DatalogQuery view : views) {

                List<MCDMappings> mappings = createMapping(subgoal, view);

                // for every mapping created check whether properties are
                // fulfilled
                for (MCDMappings map : mappings) {

                    // create MCD
                    MCD mcd = new MCD(subgoal, query, view, map);

                    // MCD can be extend to fulfill properties
                    if (mcd.fulfillProperty())
                        mcds.add(mcd);
                }
            }
        }
        removeDuplicates();
    }

    /**
     * The second part of the algorithm will combine the MCDs in order to obtain
     * rewritings of the query. First, subsets of the list of MCDs will be
     * computed starting with subset size one. For each subset size, it will be
     * tested whether it is possible to combine the MCDs to a valid rewriting.
     * If this test suceeds, the rewriting will be added to the list of
     * rewritings.
     *
     */
    /* C.BA ==> replaced by another one !!
	 
     private void combineMCDs() {		
				
     for (int i = 1; i <= mcds.size(); i++) {

     // find subset of size i
     List<List<MCD>> subsetList = findMCDSubset(mcds, i);
						
     for (List<MCD> mcdList : subsetList) {
     if (isRewriting(mcdList)) {
     rewritings.add(new Rewriting(mcdList, query));
     }
     }
     }
     }
     */
    /* C.BA */
    private void combineMCDs() {

        List<List<MCD>> subsetList = findMCDSubsetPref(mcds);
        for (List<MCD> mcdList : subsetList) {
            if (isRewriting(mcdList))
                rewritings.add(new Rewriting(mcdList, query));
        }
    }

    /**
     * Redundant view from the rewriting will be removed using the method of
     * class Rewriting
     */
    private void removeRedundancies() {

        for (Rewriting rw : rewritings) {
            rw.removeRedundancies();
        }
    }

    /**
     * This method finds subsets of MCD list with a given size. If the size is
     * 1, it is a list that contains a list with single MCDs. If the size is
     * greater than 1, the method will be called recursivly with the size
     * reduced by 1. The first MCD of the given list will be taken out. Then it
     * will be added as first element to each MCD list which is returned by the
     * recursive call. The resulting list, containing a list of MCDs with the
     * same MCD as first element, will be returned by the method.
     *
     * @param list of MCDs from which the subsets will be computed
     * @param size of the subsets
     * @return list of subsets that are lists of MCDs
     */
	// C.BA:  findMCDSubset from minicon ==> does not always return all the subsets !
	/*
     private List<List<MCD>> findMCDSubset(List<MCD> list, int size) {

     List<MCD> mcdList = new ArrayList<MCD>(list);
     List<List<MCD>> returnList = new ArrayList<List<MCD>>();

     if (size == 1) {
     for (MCD mcd : mcdList) {
     List<MCD> tempList = new ArrayList<MCD>();
     tempList.add(mcd);
     returnList.add(tempList);
     }
     } else {
     for (int i = 0; i <= (mcdList.size() - size + 1); i++) {
											
     MCD mcd = mcdList.get(0);
     mcdList.remove(0);
     List<List<MCD>> tempList = findMCDSubset(mcdList, size - 1);
																
     addAsFirstElem(mcd, tempList);
     returnList.addAll(tempList);
     }
     }

     return returnList;
     }
     */
    // C.BA:  findMCDSubsetPref from C.BA ==> always return all the subsets !
    private List<List<MCD>> findMCDSubsetPref(List<MCD> list) {
        List<List<MCD>> result;

        if (list.size() == 0) {
            result = new ArrayList<List<MCD>>();
            result.add(new ArrayList<MCD>());
            return result;
        }

        List<MCD> newList = new ArrayList(list);
        MCD lastMCD = newList.remove(newList.size() - 1);

        return addMCDToSubsetList(lastMCD, findMCDSubsetPref(newList));
    }

    /* C.BA */
    private List<List<MCD>> addMCDToSubsetList(MCD mcd, List<List<MCD>> subsetList) {
        List<List<MCD>> resultat = clone(subsetList);
        List<List<MCD>> initialSubsetList = clone(subsetList);

        for (List<MCD> MCDList : initialSubsetList) {
            MCDList.add(mcd);
            resultat.add(MCDList);
        }
        return resultat;
    }

    /* C.BA */
    private static List<List<MCD>> clone(List<List<MCD>> listOfLists) {
        List<List<MCD>> result = new ArrayList<List<MCD>>();

        for (List<MCD> list : listOfLists) {
            List<MCD> newList = new ArrayList<MCD>();
            newList.addAll(list);
            result.add(newList);
        }
        return result;
    }

    /**
     * Called by findMCDSubset, it will add the given MCD to the front of each
     * element of the given list, i.e each list of MCDs will have the given MCD
     * as first element.
     *
     * @param elem MCD that is added as first element
     * @param list contains lists of MCDs
     */
    private void addAsFirstElem(MCD elem, List<List<MCD>> list) {
        for (List<MCD> currList : list) {
            currList.add(0, elem);

        }
    }

    /**
     * Called by combineMCDs, it will test whether the given MCDs can be
     * combined to a valid rewriting.
     *
     * A rewriting is valid if the combination of the view predicates result in
     * the set of query subgoals and when the predicates are pairwise disjoint.
     * First, the total number of predicates of the given MCDs will be computed.
     * If the number doesn't equal to the number of subgoals in the query, false
     * will be returned (interpreted predicates are not considered here).
     * Second, every MCD is compared with every other MCD to test whether they
     * are disjoint. Finally, mappings to constants will be checked for
     * validity. If there is a variable that the exists in at least two MCDs and
     * that is mapped to two different constants, the combination of these MCDs
     * is not possible.
     *
     * @param mcds that will be test whether they can be combined
     * @return true if mcds can be combined to a valid rewriting, false
     * otherwise
     */
    private boolean isRewriting(List<MCD> mcds) {
        int countPredicates = 0;

        for (MCD mcd : mcds) {
            countPredicates += mcd.numberOfSubgoals();
        }

        // compare total number of predicates with number of query subgoals
        if (countPredicates != query.numberOfPredicates())
            return false;

        // test pairwise disjoint
        for (int i = 0; i < mcds.size(); i++) {
            for (int j = 0; j < mcds.size(); j++) {
                if (i != j) {
                    MCD mcd1 = mcds.get(i);
                    MCD mcd2 = mcds.get(j);
                    if (!mcd1.isDisjoint(mcd2))
                        return false;
                }
            }
        }

        // x exists in C1 and C2 ==> it must be mapped to the same constant
        for (int i = 0; i < mcds.size(); i++) {
            MCD mcd1 = mcds.get(i);
            Mapping constMap1 = mcd1.mappings.constMap;
            for (int j = 0; j < mcds.size(); j++) {
                if (i != j) {
                    MCD mcd2 = mcds.get(j);
                    Mapping constMap2 = mcd2.mappings.constMap;
                    for (PredicateElement elem : constMap1.arguments) {
                        if ((constMap2.containsArgument(elem) && !(constMap1
                                .getFirstMatchingValue(elem).equals(constMap2
                                        .getFirstMatchingValue(elem)))))
                            return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Called by formMCDs. The given query subgoal is tested if it can be mapped
     * to every predicate of the view. If a mapping is possible, a new mapping
     * object is added to the list of mappings.
     *
     * @param subgoal current query subgoal
     * @param view current view
     * @return list of possible mappings
     */
    private List<MCDMappings> createMapping(Predicate subgoal, DatalogQuery view) {
        List<Predicate> viewPredicates = view.getPredicates();
        List<MCDMappings> mappings = new ArrayList<MCDMappings>();

        for (Predicate viewPred : viewPredicates) {

            if (subgoal.canBeMapped(viewPred))
                mappings.add(new MCDMappings(subgoal, viewPred));
        }
        return mappings;
    }

    /**
     * Called by formMCDs. The method will remove duplicate MCDs. First the
     * empty list 'noDuplicates' will be created. By iterating through the
     * member list 'mcds', each MCD will be added to the noDuplicates list only
     * if there is no duplicate already contained in the list. The reference of
     * the member list mcds will finally be linked to the list noDuplicates. The
     * equality of the MCDs is determined by method 'equals' in class MCD.
     */
    private void removeDuplicates() {

        List<MCD> noDuplicates = new ArrayList<MCD>();

        for (MCD mcd : mcds) {
            boolean contains = false;

            for (MCD noDup : noDuplicates) {
                if (mcd.equals(noDup))
                    contains = true;
            }
            if (!contains)
                noDuplicates.add(mcd);
        }
        mcds = noDuplicates;
    }

    /**
     * Print rewritings
     */
    private void printRewritings() {
        if (!rewritings.isEmpty()) {
            System.out.println("\nRewriting(s):");
            for (Rewriting rw : rewritings) {
                System.out.println(rw);
            }
        }
    }

    /**
     * C. BA Print rewritings
     */
    private void printPrefRewritings(List<Rewriting> listRewritings) {
        if (!listRewritings.isEmpty()) {
            System.out.println("\n### My Rewriting(s): ### ");
            for (Rewriting rw : listRewritings) {
                System.out.println(rw);
            }
        }
    }

    /**
     * Print MCDs
     */
    private void printMCDs() {
        // System.out.println("\n");
        if (mcds.isEmpty())
            System.out.println("\nNo MCDs created");
        else
            for (MCD mcd : mcds) {
                System.out.println(mcd.toString());
            }
    }

    /**
     * Print query provided by user
     */
    private void printQuery() {
        System.out.println("\nQuery: " + query);
    }

    /**
     * Print views provided by user
     */
    private void printViews() {
        for (DatalogQuery view : views) {
            System.out.println("View: " + view);

        }
    }

    /**
     * Returns list of Rewriting objects created by the algorithm.
     *
     * @return list of Rewriting objects
     */
    public List<Rewriting> getRewritings() {
        return rewritings;
    }

}
