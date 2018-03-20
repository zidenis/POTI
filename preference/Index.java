
/*
 *   Copyright 2015 Thiago CERQUEIRA <thiagoa7@gmail.com>
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
 * Created on 09.09.2014
 * Index: to store PCD for concrete services according to the user's preferences.
 * 
 * @author Thiago Abreu
 */
package preference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import datalog.DatalogQuery;
import datalog.Predicate;
import datalog.PredicateElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import minicon.MCD;
import minicon.Mapping;

public class Index {

    private static DatalogQuery query;

    private static List<List<MCD>> index;

    private static Map<SubAndDesc, Boolean> blackList;

    private static Map<MCD, boolean[]> coverList;

    public static void initialize(List<MCD> mcds, DatalogQuery _query) {
        RankComparator comp = new RankComparator();

        query = _query;
        index = new ArrayList<List<MCD>>(query.getPredicates().size());
        blackList = new HashMap<SubAndDesc, Boolean>();
        coverList = new HashMap<MCD, boolean[]>();

        List<MCD> coverageDomain;

        for (int i = 0; i < query.getPredicates().size(); i++) {
            Predicate subGoal = query.getPredicates().get(i);
            coverageDomain = getCoverageDomain(subGoal, mcds);
            ArrayList<MCD> abstractService = new ArrayList<MCD>(coverageDomain);
            Collections.sort(abstractService, comp);

            setBlackLists(abstractService);

            index.add(abstractService);
        }

        for (MCD mcd : mcds) {
            int n = query.getPredicates().size();
            boolean[] covering = new boolean[n];
            for (int i = 0; i < n; i++) {
                covering[i] = false;
            }

            for (Predicate predicate : mcd.getSubgoals()) {
                int m = query.getPredicates().indexOf(predicate);
                covering[m] = true;
            }
            coverList.put(mcd, covering);
        }
    }

    public static MCD getMCDfromPos(int subGoal, int pos) {
        return index.get(subGoal).get(pos);
    }

    public static boolean[] getCoverList(MCD mcd) {
        return coverList.get(mcd);
    }
    
    public static boolean[] getCoverList(int i, int j) {
        return getCoverList(index.get(i).get(j));
    }

    private static void setBlackLists(List<MCD> mcds) {
        for (int i = 0; i < mcds.size(); i++) {
            MCD mcd = mcds.get(i);
            for (int j = 0; j < mcd.getSubgoals().size(); j++) {
                Predicate pred = mcd.getSubgoals().get(j);
                int dom = query.getPredicates().indexOf(pred);

                blackList.put(new SubAndDesc(dom, i), j != 0);
            }
        }
    }

    public static boolean isBlackListed(int i, int j) {
        return blackList.get(new SubAndDesc(i, j));
    }

    private static List<MCD> getCoverageDomain(Predicate abstractService, List<MCD> mcds) {
        List<MCD> coverageDomain = new LinkedList<MCD>();

        for (int i = 0; i < mcds.size(); i++) {
            MCD mcd = mcds.get(i);
            List<Predicate> coveredSubGoals = mcd.getSubgoals();
            for (int j = 0; j < coveredSubGoals.size(); j++) {

                if (coveredSubGoals.get(j).equals(abstractService)) {
                    coverageDomain.add(mcd);
                    j = coveredSubGoals.size();
                }
            }
        }
        return coverageDomain;
    }
    
    public static List<MCD> getSubdomain(int i) {
        return index.get(i);
    }

    private static boolean isRewriting(List<MCD> mcds, DatalogQuery query) {
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

}

class RankComparator implements Comparator<MCD> {

    @Override
    public int compare(MCD r1, MCD r2) {
        if (r1.getRank() == r2.getRank())
            return r1.getView().getName().compareTo(r2.getView().getName());
        else if (r1.getRank() > r2.getRank())
            return -1;
        else
            return 1;

    }
}

class SubAndDesc {

    private Integer first;
    private Integer second;

    public SubAndDesc(Integer first, Integer second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int hashCode() {
        int hashFirst = first != null ? first.hashCode() : 0;
        int hashSecond = second != null ? second.hashCode() : 0;

        return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SubAndDesc) {
            SubAndDesc otherPair = (SubAndDesc) other;
            return ((this.first == otherPair.first
                    || (this.first != null && otherPair.first != null
                    && this.first.equals(otherPair.first)))
                    && (this.second == otherPair.second
                    || (this.second != null && otherPair.second != null
                    && this.second.equals(otherPair.second))));
        }

        return false;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
