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


import edu.uga.glydeII.gom.Residue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;




/**
 * This class represents all possible matches of
 * a single pattern glycan (candidate) structure to a target glyco tree.
 *
 * @author Krys Kochut
 */
public class StructureMatch {

    // the root of the match tree, representing all topological matches
    private MatchNode             matchTreeRoot = null;

    // the best match
    private MatchNode             bestMatch = null;

    private int                   perfectScore = 0;

    private int                   noMatches = 0;

    private int                   noMatchesRequested = 0;

    // a vector of the first noMatchesRequested matches
    //
    private List<MatchNode>     alignments = null;

    /**
     * Create a new match of the candidate structure against a glyco tree of the specified linkage type.
     * @param candRoot the pattern (candidate) glycan
     * @param gtRoot the root of the target glyco tree
     * @param bestMatchNode the best MatchNode for this StructureMatch
     * @param reqMatchNo the requested number of matches to save
     */
    public StructureMatch( int perfScore, int reqMatchNo )
    {
        perfectScore       = perfScore;
        bestMatch          = null;
        noMatches          = 0;
        noMatchesRequested = reqMatchNo;

        // allocate storage for the solutions; make sure there is room for best match,
        // which is located always
        //
        alignments = new ArrayList<MatchNode>( noMatchesRequested + 1 );
    }

    /**
     * Set the root MatchNode for this StructureMatch.
     * @param matchNode the new root MatchNode.
     */
    public void setMatchRoot( MatchNode matchNode )
    {
        matchTreeRoot = matchNode;
    }

    /**
     * Set the best match for this StructureMatch node.
     * @param bestNode the new best match node.
     */
    public void setBestMatchNode( MatchNode bestNode )
    {
        bestMatch = bestNode;
    }
    
    public MatchNode getBestMatchNode( )
    {
        return bestMatch;
    }

    /**
     * Return the number of matches in this StructureMatch.
     * @return the number of matches in this StructureMatch.
     */ 
    public int getNoMatches()
    {
        return noMatches;
    }

    /**
     * Perform a count of matches in this StructureMatch.
     */
    public void countMatches()
    {
        noMatches = 0;
        countMatches( matchTreeRoot );
    }

    /**
     * Add another match to the collection of matches.
     * @param matchNode the match to be added to the collection of matches.
     */
    public void addMatch( MatchNode matchNode )
    {
        if( noMatches < noMatchesRequested + 1) {
            alignments.add( matchNode );
	    noMatches++;
	}
    }
    
    public List<MatchNode> getAlignments(){
        return alignments;
    }

    /**
     * Print a specific number of matches.
     * @param noToPrint the number of matches to print.
     */
    public void printAlignments( int noToPrint )
    {
        Collections.sort( alignments, new MatchNodeComparator() );

        for( int i = 0; i < noMatchesRequested && i < noMatches && i < noToPrint; i++ ) {
            System.out.println( "=======================================" );
            System.out.println( "== Match " + (i + 1) + ":" );
            System.out.println( "==");
            System.out.println();
            printMatch( alignments.get( i ) );
        }
            
    }

    /**
     * Print this StructureMatch.  Be careful, the StructureMatch may represent a large number of matches.
     */
    public void print()
    {
        noMatches = 0;
        print( matchTreeRoot, 0 );
        System.out.println( "StructureMatch.print: number of matches printed: " + noMatches );
    }

    /**
     * Print the best match included in this set of matches.
     */
    public void printBestMatch()
    {
        System.out.println( "============== Best match follows: =================" );
        printMatch( bestMatch );
        System.out.println( "============== End of best match   =================" );
    }

    /**
     * Print a match represented by a given MatchNode.
     * @param aMatch a MatchNode representing a match of the pattern (candidate) glycan.
     */
    public void printMatch( MatchNode aMatch )
    {
        MatchNode branchNode = aMatch;
        int       rank;
        int       i;
        int       k;

        List<MatchNode> matchNodes = new ArrayList<MatchNode>();

        // collect the MatchNodes on this branch, all the way to the root
        //
        while( branchNode != null ) {
            matchNodes.add( 0, branchNode );
            branchNode = branchNode.getParent();
        }

        // now, print the branch starting from the root
        //
        System.out.println( "Match score: "
                            + aMatch.getTotalScore() + "/" +
                            + aMatch.getPerfectScore() + "/" +
                            perfectScore );
        System.out.println( "Individual residue assignments by rank: " );
        System.out.println( "Rank AS Total-AS" );
        System.out.println( "-----------------" );
        rank = 1;
        for( MatchNode mNode : matchNodes ) {
            System.out.print( "[" + rank + "]  " + mNode.getNodeScore() + "  " );
            System.out.println( mNode.getTotalScore() + "/" + mNode.getPerfectScore() + ": " );
            i = 0;
            for( ResidueAssignment resAssignment : mNode.getAssignments() ) {
                System.out.print( "\t" );
                for( int j = 0; j < i; j++ )
                    System.out.print( " " );
                System.out.println( resAssignment.toString() + "  " );
                i++;
            }
            if( mNode.getNumberOfAlternatives() > 0 ) {

                System.out.println( "     Alternatives:" );

                for( MatchNode altMatchNode : mNode.getAlternatives() ) {

                    System.out.print( "\t\t" + altMatchNode.getNodeScore() + "  " );
                    System.out.println( altMatchNode.getTotalScore() + "/" + altMatchNode.getPerfectScore() + ": " );
                    k = 0;
                    for(  ResidueAssignment altResAssignment : altMatchNode.getAssignments() ) {
                        System.out.print( "\t\t\t" );
                        for( int l = 0; l < k; l++ )
                            System.out.print( " " );
                        System.out.println( altResAssignment.toString() + "  " );
                        k++;
                    }
                }
            }
            rank++;
        }
    }

    // Private methods
    //
    private void countMatches( MatchNode matchNode )
    {
        if( matchNode.getChildren() != null ) {
            for( MatchNode childMatchNode : matchNode.getChildren() ) {
                countMatches( childMatchNode );
            }
        }
        else
            noMatches++;
    }

    private void print( MatchNode matchNode, int level )
    {
        int indent;

        for( indent = 0; indent < level; indent++ )
            System.out.print( "  " );
        System.out.print( "Match level " + level + ": " );
        
        for( ResidueAssignment resAssignment : matchNode.getAssignments() ) {
            System.out.print( resAssignment.toString() + "  " );
        }
        
        /*
        if( matchNode.getChildren() != null )
            System.out.println( "; " + matchNode.getChildren().size() + " descendants" );
        else {
            noMatches++;
            System.out.println( "; 0 descendants" );
        }
        */

        if( matchNode.getChildren() != null ) {
            System.out.println( "; " + matchNode.getChildren().size() + " descendants" );
            for( MatchNode childMatchNode : matchNode.getChildren() ) {
                print( childMatchNode, level + 1 );
            }
        }
        else {
            noMatches++;
            System.out.println( "; 0 descendants" );
            MatchNode branchNode = matchNode;
            System.out.println( ">>>> Match: " + noMatches + " start; score: "
                                + matchNode.getTotalScore() + "/" +
                                + matchNode.getPerfectScore() + "/" +
                                perfectScore );
            while( branchNode != null ) {
                for( int i = 0; i < branchNode.getLevel(); i++ )
                    System.out.print( "  " );
                System.out.print( "(" + branchNode.getNodeScore() + ") " );
                System.out.print( branchNode.getTotalScore() + "/" + branchNode.getPerfectScore() + ": " );
                for( ResidueAssignment resAssignment : branchNode.getAssignments() ) {
                    System.out.print( resAssignment.toString() + "  " );
                }
                branchNode = branchNode.getParent();
                System.out.println( "" );
            }
            System.out.println( ">>>> Match: stop" );
        }
    }

}
