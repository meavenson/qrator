/* 
 * Copyright (C) 2014 Matthew Eavenson <matthew.eavenson at gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package edu.uga.qrator.logic.match.matcher;


import java.util.Comparator;


/**
 * A comparator class for the MatchNode class
 *
 * @author Krys Kochut
 */
public class MatchNodeComparator implements Comparator<MatchNode>
{
    /**
     * Compare two MatchNodes.
     * @param mn1 the left operand
     * @param mn2 the right operand
     * @return a value < 0, 0, or > 0 depending on the relative comparison of the MatchNodes
     */
    @Override
    public int compare(MatchNode mn1, MatchNode mn2)
    {
        int totScore = mn1.getTotalScore() - mn2.getTotalScore();

        if( totScore == 0 )
            return mn2.getPerfectScore() - mn1.getPerfectScore();
        else
            return totScore;
    }
}
