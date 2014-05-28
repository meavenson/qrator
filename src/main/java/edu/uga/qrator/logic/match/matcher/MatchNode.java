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

import java.util.ArrayList;
import java.util.List;




/**
 * This class represents a single node in the overall match tree of possible matches
 * of a glycan pattern (candidate) structure against a target (glyco) tree.
 * It includes a list of individual node assignments (ResidueAssignment) and a list of
 * descendent (children) match nodes.
 *
 * @author Krys Kochut
 */
public class MatchNode {

    // residue assignments at this node
    private List<ResidueAssignment>      assignments = null;
    
    // parent node
    private MatchNode                    parent     = null;

    // children nodes
    private List<MatchNode>              children   = null;

    // alternatives of this node;
    private List<MatchNode>              alts       = null;

    // the level of this node
    private int                          level;

    // total score up to this node
    private int                          totalScore = 0;

    // total score up to this node
    private int                          nodeScore = 0;

    // perfect score up to this node (no edits from the root)
    private int                          perfScore  = 0;


    /**
     * Create a MatchNode object.
     * @param assgn is a list of ResidueAssignment elements, which this MatchNode represents
     * @param lev is the depth of the node in the match tree
     * @param ts is the total score of this node
     * @param ps is the perfect score of this node
     */
    public MatchNode( List<ResidueAssignment> assgn, int lev, int ts, int ps )
    {
        assignments = assgn;
        level      = lev;
        parent     = null;
        children   = null;
        totalScore = ts;
        perfScore  = ps;

        nodeScore = 0;
        for( ResidueAssignment resAssgn : assignments ) {
            // compute the total score
            totalScore += resAssgn.getScore();
            nodeScore += resAssgn.getScore();
            // compute the perfect score, if the parent permits it
            if( resAssgn.isPerfScore() )
                perfScore += resAssgn.getScore();
        }
            
    }

    /**
     * Create a MatchNode object.
     * @param assignments is a list of ResidueAssignment elements, which this MatchNode represents
     * @param parent is the parent of this MatchNode
     * @param lev is the depth of the node in the match tree
     * @param totalScore is the total score of this node
     * @param perfScore is the perfect score of this node
     */
    public MatchNode( List<ResidueAssignment> assignments, MatchNode parent, int level, int totalScore, int perfScore )
    {
        this.assignments = assignments;
        this.level       = level;
        this.parent      = parent;
        children         = null;
        this.totalScore  = totalScore;
        this.perfScore   = perfScore;

        nodeScore = 0;
        for( ResidueAssignment resAssgn : assignments ) {
            // compute the total score
            this.totalScore += resAssgn.getScore();
            nodeScore += resAssgn.getScore();
            // compute the perfect score, if the parent permits it
            if( resAssgn.isPerfScore() )
                this.perfScore += resAssgn.getScore();
        }

    }

    public List<ResidueAssignment> getAssignments()
    {
        return assignments;
    }

    public MatchNode getParent()
    {
        return parent;
    }

    public int getLevel()
    {
        return level;
    }
    
    public int getPerfectScore()
    {
        return perfScore;
    }

    public int getTotalScore()
    {
        return totalScore;
    }

    public int getNodeScore()
    {
        return nodeScore;
    }

    public void addChild( MatchNode childNode )
    {
        if( children == null )
            children = new ArrayList<MatchNode>();
        children.add( childNode );
    }

    public int getNumberOfChildren()
    {
        if( children == null )
            return 0;
        return children.size();
    }

    public List<MatchNode> getChildren()
    {
        return children;
    }

    public void addAlternative( MatchNode altNode )
    {
        if( alts == null )
            alts = new ArrayList<MatchNode>();
        alts.add( altNode );
    }

    public int getNumberOfAlternatives()
    {
        if( alts == null )
            return 0;
        return alts.size();
    }

    public List<MatchNode> getAlternatives()
    {
        return alts;
    }

}
