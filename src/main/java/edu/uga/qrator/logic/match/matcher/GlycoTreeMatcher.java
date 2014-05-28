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



import edu.uga.glydeII.gom.*;
import java.util.*;


/**
 * This class represents a Glyco Tree retrieved from the GlycO ontology.
 *
 * @author Krys Kochut
 */
public class GlycoTreeMatcher {

    /**
     * Provided match types
     */
    public enum MatchType { 

        /** 
         * Return a single best match, which internally represents all possible 
         * relevant alternative residue matches at different tree depths.
         */
        BESTONE,

        /** 
         * Return an ordered (by score) list of best, but only relevant matches.
         * Perfect residue matches are "fixed", and only non-perfect
         * residue matches will have alternatives that count as relevant.
         */
        BESTLIST
    };

    // perfect residuce match score
    public static final int maxMatchNodeScore = 5;
    
    private int             bestPossibleMatchScore;

    private int             noMatches;
    private int             noMatchesRequested;

    private List<MatchNode> alignments;

    // Jena's ontology model for GlycO
    //private OntModel        model;

    // 
    private Set<PartEntity> roots;

    /**
     * Create an "empty" GlycoTree object.
     */
    public GlycoTreeMatcher(){
        noMatches = 0;
        noMatchesRequested = 1000;
    }

    /**
     * Return a Set of roots of this GlycoTree.
     * @return set of roots of this GlycTree.
     */
    public Set<PartEntity> getRoots()
    {
        return roots;
    }

    /**
     * Recursively traverse and print this glyco tree.
     * @param residue the root for the traversal
     * @param depth current depth of the traversal
     * @return number of nodes in this GlycoTree
     */
    private int prefixTraversal( CompositeResidue residue, int depth ) 
    {
        int noNodes = 1;
        BaseType baseType = residue.getBaseType();
        Residue btResidue = baseType.getGeneralType();

//        if( compRes == null ) {
//            System.err.println( "GlycoTree.prefixTraversal: compRes is null" );
//            System.err.println( "GlycoTree.prefixTraversal: resType: " + resType );
//            System.err.flush();
//        }
//
//        for( int i = 0; i < depth; i++ )
//            System.out.print( "  " );
//
//        if( residue.getName() != null ) {
//            System.out.print( residue.getName() + " [" + residue.getPartId() + "]: " + compRes.getName() );
//            System.out.print( "; absConf: " + ((BaseType)resType).getAbsoluteConfiguration() );
//            System.out.print( "; anomConf: " + ((BaseType)resType).getAnomericConfiguration() );
//            System.out.println( "; ringForm: " + ((BaseType)resType).getRingForm() );
//        }
//        else {
//            System.out.print( compRes.getName() + " [" + residue.getPartId() + "] ");
//            System.out.print( "; absConf: " + ((BaseType)resType).getAbsoluteConfiguration() );
//            System.out.print( "; anomConf: " + ((BaseType)resType).getAnomericConfiguration() );
//            System.out.println( "; ringForm: " + ((BaseType)resType).getRingForm() );
//        }

        SortedSet<ResidueLink> children = btResidue.getLinkIn();

        for( ResidueLink resLink : children ) {
            Residue child = resLink.getFrom();
            ResidueType type = child.getSpecificType();
            
            // count substituents who are in their own composite residues
            if(type instanceof Substituent){
                Substituent sub = (Substituent) type;
                if(!sub.getCompositeResidue().equals(residue))
                    noNodes++;
            }else{
                BaseType bt = (BaseType) type;
//            if( resLink.getSubLinks().size() > 0 ) {
//                AtomLink atomLink = resLink.getSubLinks().first();
//                if( atomLink != null )
//                    System.out.print( "Parent link: " + 
//                                      atomLink.getFrom().getPartId() + 
//                                      " -> " + atomLink.getTo().getPartId() + "  " );
//                else
//                    System.out.print( "No Parent link  " );
//            }
                noNodes += prefixTraversal( bt.getCompositeResidue(), depth + 1 );
            }
        }
        return noNodes;

    }

    /**
     * Traverse and print this GlycoTree, rooted at a given Residue.
     * @param root root of the glyco tree to traverse.
     * @return number of nodes in this glyco tree.
     */
    public int traverse( CompositeResidue root )
    {
        return prefixTraversal( root, 0 );
    }

    /**
     * Perform a match of a pattern (candidate) glycan 
     * against a glyco tree with the root gtRoot.  The two root residues must match;
     * their respective parent linkages do not exist, so they are assumed to match.
     * The performed match is a root-preserving tree path inclusion.
     * @param gtRoot is the root Residue of the glycao tree to use for matching
     * @param candRoot is the root Residue of the candidate structure
     * @param matchType is the MatchType to perform 
     * @param maxMatches is the maximum number of matches to return; use -1 to get all matches
     * @return a StructureMatch object representing the set of located matches
     */
    public StructureMatch match( CompositeResidue gtRoot, CompositeResidue candRoot, MatchType matchType, int maxMatches )
    {
        ArrayList<ResidueAssignment> startAssignment  = new ArrayList<ResidueAssignment>();
        MatchNode                    matchTreeRoot    = null;
        int                          noResidues       = 0;

        noMatchesRequested = maxMatches;

        // compute the maximum score for the candidate structure
        noResidues = traverse( candRoot );

        // create the top ResidueAssignment of the two roots;
        startAssignment.add( new ResidueAssignment( null,    // parent residue assignment
                                                    gtRoot,   // gt root residue
                                                    candRoot, // candidate root residue
                                                    null,     // no parent bond for gt root
                                                    null,     // no parent bond for candidate root
                                                    true      // yes, perfect score possible
                                                  ) );

        bestPossibleMatchScore = noResidues * maxMatchNodeScore;

        // allocate storage for the solutions
        alignments = new ArrayList<MatchNode>( noMatchesRequested + 1 );

        // create the starting MatchNode representing the match of two roots
        //
        matchTreeRoot = new MatchNode( startAssignment, 0, 0, 0 );

        // initialize the number of matches found
        //
        noMatches = 0;

        // create a StructureMatch (wrapper) object to represent the matching solution
        //
        StructureMatch structureMatch = new StructureMatch( bestPossibleMatchScore, noMatchesRequested );

        // Perform the actual match of the candidate tree into the glyco tree.
        // Once the match method returns, the matchTreeRoot is expanded to 
        // contain the best relevant residue assignments of the candidate tree within the
        // glyco tree.
        //
        match( matchTreeRoot, 0, matchType, structureMatch );

        // set the match tree of the result
        //
        structureMatch.setMatchRoot( matchTreeRoot );

        // Sort the matches
        Collections.sort( alignments, new MatchNodeComparator() );

        // All done, so return the matches (the StructureMatch wrapper object)
        return structureMatch;
    }

    /**
     * Extend the match tree represented by a MatchNode.  This is a recursive method
     * implementing the main matching process.
     * @param matchNode a MatchNode to expand further.
     * @param level current level (rank) of the match tree.
     * @return the argument MatchNode which was extended (if possible to do so)
     */
    private MatchNode match( MatchNode matchNode, int level, MatchType matchType, StructureMatch structureMatch )
    {
        List<MatchNode>                       nextLevelMatchNodes = null;
        List<ResidueAssignment>               assignment = null;
	List<List<List<ResidueAssignment>>>   childrenBranches = null;
	List<List<ResidueAssignment>>         childrenBranch = null;
	List<List<ResidueAssignment>>         childrenAssignments = null;

        // enough matches already?
        //
        if( noMatches >= noMatchesRequested )
            return matchNode;

        nextLevelMatchNodes = new ArrayList<MatchNode>();

	// NOTE:
	//   If this matchNode has multiple residue assignments:
	//         assignment:         ( [res-1-1 -> res-1-2], ..., [res-k-1 -> res-k-2] )
	//   the next level will result in a list of lists of children residue assignments.
	//   Therefore, each new child residue assignment list must be composed by
	//   selecting one element from each of the lists.  Such child residue assignments
	//   will be composed for all possibile cross combinations (merging) of the lists.

        assignment = matchNode.getAssignments();

	childrenBranches = new ArrayList<List<List<ResidueAssignment>>>();

	// iterate over the list of all residue assignments and
	// and for each assignment, create its children list including only
	// the relevant children (as in the case of assignment.size() == 1 above)
	// 
	for( ResidueAssignment resAssign : assignment ) {

	    // create all possible assignments of the children of the two residues in this resAssign;
	    // this is the next branch in the match tree being expanded
	    childrenBranch = resAssign.createAllChildrenAssignments();

	    if( childrenBranch.isEmpty() )
		continue;

	    // get the best children nodes to explore in this branch; 
	    // lower scoring children will be discarded as not relevant;
	    // this will effectively "filter out" irrelevant nodes
	    childrenBranch = getChildrenToExplore( childrenBranch, false );

	    // add this branch
	    childrenBranches.add( childrenBranch );
	    
	}

	childrenAssignments = null;

	// merge the children branches
	childrenAssignments = mergeChildrenBranches( childrenBranches );

	// now, construct the next level of the match tree
	if( childrenAssignments != null && childrenAssignments.size() > 0 ) {

	    childrenAssignments = getChildrenToExplore( childrenAssignments, true );

	    MatchNode childMatchNode = null;

	    nextLevelMatchNodes.clear();
		
	    // create the MatchNodes and place them in the nextLevelMatchNodes Vector
	    //
	    for( List<ResidueAssignment> childAssignment : childrenAssignments ) {
		    
		// make a MatchNode of each of this assignments and save it as an alternative MatchNode
		childMatchNode = new MatchNode( childAssignment, matchNode, 
						level + 1,
						matchNode.getTotalScore(),
						matchNode.getPerfectScore() );
		nextLevelMatchNodes.add( childMatchNode );
	    }

	    // sort the nextLevelMatchNodes Vector
	    Collections.sort( nextLevelMatchNodes, new MatchNodeComparator() );

	    if( matchType == MatchType.BESTONE ) {
		
		// get the highest score MatchNode and set it as the "main" child
		//
		childMatchNode = nextLevelMatchNodes.remove( 0 );
		matchNode.addChild( childMatchNode );

		for( MatchNode nextLevelMatchNode : nextLevelMatchNodes)
		    childMatchNode.addAlternative( nextLevelMatchNode );
	    }
	    else if( matchType == MatchType.BESTLIST ) {

		// place all the matchNodes from the next level list as chldren of
		// this matchNode, preserving their ordering;  the leftmost is the best node
		for( MatchNode nextLevelMatchNode : nextLevelMatchNodes)
                    matchNode.addChild( nextLevelMatchNode );

	    }

	}

	// now, expand recursively, if there are any children matchNodes
        if( matchNode.getChildren() != null ) {
            // recursively match the children nodes
            for( MatchNode childMatchNode : matchNode.getChildren() ) 
                match( childMatchNode, level + 1, matchType, structureMatch );
        }
        else { // found the next match

            noMatches++;

	    // save this match; need to save this leaf matchNode
            if( noMatches < alignments.size() )
                alignments.add( matchNode );

            structureMatch.addMatch( matchNode );
            
            MatchNode bestNode = structureMatch.getBestMatchNode();
            // check if this is the best alignment so far
            if( bestNode == null ) {
                structureMatch.setBestMatchNode(matchNode);
            }
            else if( matchNode.getPerfectScore() > bestNode.getPerfectScore() ) {
                structureMatch.setBestMatchNode(matchNode);
            }
            else if( matchNode.getPerfectScore() == bestNode.getPerfectScore() &&
                     matchNode.getTotalScore() > bestNode.getTotalScore() ) {
                structureMatch.setBestMatchNode(matchNode);
            }

        }

        return matchNode;
    }

    private List<List<ResidueAssignment>> 
    getChildrenToExplore( List<List<ResidueAssignment>> childrenAssignments, boolean afterMerge )
    {
        List<List<ResidueAssignment>> childrenToExplore   = null;
        int bestAssignmentScore = 0;
        int thisAssignmentScore = 0;
	int assignmentScoreMax  = 0;

	if( childrenAssignments == null || childrenAssignments.get( 0 ) == null )
	    return null;

	// establish the maximum score for this assignment (just the residue count)
	assignmentScoreMax = childrenAssignments.get( 0 ).size();

        childrenToExplore = new ArrayList<List<ResidueAssignment>>();

        for( List<ResidueAssignment> childAssignment : childrenAssignments ) {

            // compute this assignment score
            thisAssignmentScore = 0;
            for( ResidueAssignment newResAssign : childAssignment ) {
                if( newResAssign.isPerfScore() ) {
                    thisAssignmentScore++;
                }
            }
            
            // check if it is better than the best so far
            if( thisAssignmentScore > bestAssignmentScore ) {

                bestAssignmentScore = thisAssignmentScore;

                // start collecting only nodes with this score
                childrenToExplore.clear();
                if( afterMerge && thisAssignmentScore < assignmentScoreMax ) {
                    for( List<ResidueAssignment> residueAssignments : generateGlycoTreeExtensionMatches( childAssignment ) ) {
                        Collections.sort( residueAssignments );
                        if( !isMember( residueAssignments, childrenToExplore ) )
                           childrenToExplore.add( residueAssignments );
                    }
                }
                else if( afterMerge ) {
                    Collections.sort( childAssignment );
                    if( !isMember( childAssignment, childrenToExplore ) )
                        childrenToExplore.add( childAssignment );
                } 
                else 
                    childrenToExplore.add( childAssignment );

                // if this is a perfect node, quit the loop since there can be
                // only one max score node
                if( thisAssignmentScore == assignmentScoreMax ) {
                    break;
                }

            }
            else if( thisAssignmentScore == bestAssignmentScore ) {
                // this score is equal to the best so far, so save this one too
                if( afterMerge && thisAssignmentScore < assignmentScoreMax ) {
                    for( List<ResidueAssignment> residueAssignments : generateGlycoTreeExtensionMatches( childAssignment ) ) {
                        Collections.sort( residueAssignments );
                        if( !isMember( residueAssignments, childrenToExplore ) )
                            childrenToExplore.add( residueAssignments );
                    }
                }
                else if( afterMerge ) {
                    Collections.sort( childAssignment );
                    if( !isMember( childAssignment, childrenToExplore ) )
                        childrenToExplore.add( childAssignment );
                }
                else
                    childrenToExplore.add( childAssignment );
            }
            
        }
        return childrenToExplore;
    }
    
    // Check if a given ResidueAssignment list is already present on the list of alternative assignment lists.
    //
    // This check is necessary to avoid creating duplicate matches, where two different
    // imperfect residueAssignments for the same candidate residue are both converted into
    // null assignments (GlycoTree extensions).
    // 
    // ResidueAssignment lists must be sorted (simply by the object reference to the candidate Residue object
    // (just to establish consistent ordering).  Then, the residues on the two lists are compared pairwise
    // for equality.
    //
    // The ResidueAssignment class has been modified to include the compareTo and equals methods.
    // compareTo is needed for sorting, and equals for the comparisons.
    //
    private boolean isMember( List<ResidueAssignment> residueAssignments, List<List<ResidueAssignment>> childrenToExplore )
    {
        boolean same;
        
        for( List<ResidueAssignment> nextResidueAssignments : childrenToExplore ) {
            same = true;
            for( int i = 0; i < nextResidueAssignments.size(); i++ )
                if( ! residueAssignments.get(  i  ).equals( nextResidueAssignments.get(  i  ) ) ) {
                    same = false;
                    break;
                }
            if( same )
                return true;
        }
        return false;
    }


    private List<List<ResidueAssignment>>
    mergeChildrenBranches( List<List<List<ResidueAssignment>>> childrenBranches )
    {
        int listToAppend = 0;
        //int childrenCnt = 0;
        
        int branchNo = childrenBranches.size();

	if( childrenBranches.isEmpty() )
	    return null;
	else if( childrenBranches.size() == 1 )
	    return childrenBranches.get( 0 );

        List<List<ResidueAssignment>> childrenAssignments = 
                    new ArrayList<List<ResidueAssignment>>();

        List<Iterator<List<ResidueAssignment>>>  childrenBranchesIters =
            new ArrayList<Iterator<List<ResidueAssignment>>>( branchNo );
        for( int k = 0; k < branchNo; k++ )
            childrenBranchesIters.add( null );

        List<List<ResidueAssignment>> toAppend = new ArrayList<List<ResidueAssignment>>( branchNo );
        for( int k = 0; k < branchNo; k++ )
            toAppend.add( null );

        while( listToAppend >= 0 ) {
                    
            if( listToAppend < branchNo ) { // move forward

                // create a new iterator
                childrenBranchesIters.set( listToAppend, 
                                           childrenBranches.get( listToAppend ).iterator() );

                if( childrenBranchesIters.get( listToAppend ).hasNext() ) {

                    toAppend.set( listToAppend, childrenBranchesIters.get( listToAppend ).next() );
                    listToAppend++;

                } else {
                    // strange; shouldn't happen!
                    System.out.println( "Error combining alternative lists" );
                }
            }
            else {          // have all sublists; now, combine them

                List<ResidueAssignment> assignmentList = new ArrayList<ResidueAssignment>();
                for( List<ResidueAssignment> nextAssignment : toAppend )
                    assignmentList.addAll( nextAssignment );

               // childrenCnt++;

                childrenAssignments.add( assignmentList );

                listToAppend--;

                // find the most recent non-empty iterator and advance in it
                while( listToAppend >= 0 ) {

                    // and check if there are more lists
                    if( childrenBranchesIters.get( listToAppend ).hasNext() ) {
                        
                        toAppend.set( listToAppend, childrenBranchesIters.get( listToAppend ).next() );
                        listToAppend++;
                        break;
                    }
                    listToAppend--;

                }

            }

        }
        return childrenAssignments;
    }
    
    // this method adds the necessary ResidueAssignment lists, where the imperfect ResidueAssignments are
    // supplemented by their null assignments, indicating GlycoTree extensions
    //
    @SuppressWarnings("unchecked")
    private List<List<ResidueAssignment>>
    generateGlycoTreeExtensionMatches( List<ResidueAssignment> childAssignments )
    {
        List<List<ResidueAssignment>> expandedChildAssignments = null;
        List<List<ResidueAssignment>> remainingChildAssignments = null;
        List<ResidueAssignment>       newChildAssignments = null;
        ResidueAssignment             thisResidueAssignment = null;
        ResidueAssignment             nullResidueAssignment = null;
       
        if( childAssignments.isEmpty() ) {
            newChildAssignments = new ArrayList<ResidueAssignment>();
            expandedChildAssignments = new ArrayList<List<ResidueAssignment>>();
            expandedChildAssignments.add( newChildAssignments );
            return expandedChildAssignments;
        }
               
        thisResidueAssignment = childAssignments.get( 0 );
        nullResidueAssignment = null;
                
        if( thisResidueAssignment.getCanonicalResidue() != null && ! thisResidueAssignment.isPerfScore() ) {
            nullResidueAssignment = new ResidueAssignment( thisResidueAssignment.getParent(), null, 
                                                           thisResidueAssignment.getCandidateResidue(), null,
                                                          thisResidueAssignment.getCandidateResidueBond(),
                                                          false );
        }
        
        remainingChildAssignments = 
                generateGlycoTreeExtensionMatches( childAssignments.subList( 1, childAssignments.size() ) );

        for( List<ResidueAssignment> nextChildAssignments : remainingChildAssignments ) {
            if( nullResidueAssignment != null ) {
                newChildAssignments = new ArrayList<ResidueAssignment>();
                newChildAssignments.addAll(nextChildAssignments);
                newChildAssignments.add( 0, nullResidueAssignment );
                if( expandedChildAssignments == null )
                    expandedChildAssignments = new ArrayList<List<ResidueAssignment>>();
            }
            if( newChildAssignments != null )
                expandedChildAssignments.add( newChildAssignments );
            nextChildAssignments.add( 0, thisResidueAssignment );
        }
        if( expandedChildAssignments != null ) {
            for( List<ResidueAssignment> eca : expandedChildAssignments ) {
                remainingChildAssignments.add( eca );              
            }
        }

        return remainingChildAssignments;
    }

}
