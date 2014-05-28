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

public class GlycanFit {
    
    public enum MatchStatus{
        EXACT,
        INEXACT,
        NOTATTEMPTED
    }

    // matching type
    //private static final GlycoTreeMatcher.MatchType matchType = GlycoTreeMatcher.MatchType.BESTONE;
    private static final GlycoTreeMatcher.MatchType matchType = GlycoTreeMatcher.MatchType.BESTLIST;

    // maximum number of matches
    private static final int MATCHLIMITMAX = 1000;

    // maximum number of matches
    private static final int MATCHREQUEST  = 100;

    public List<MatchNode> match(WholeEntity we, CompositeResidue gtRoot) {

        // this will be the glyco tree root used for matching
        //
        Set<Residue> candroots = null;  // roots of the structures in the parsed Glyde-II file

        // initialize the GlycoTreeMatcher object and read the GlycO ontology
        //
        GlycoTreeMatcher gtMatcher = new GlycoTreeMatcher();

        // match
        GlydeRootPartEntityIdentifier gri = new GlydeRootPartEntityIdentifier();
        List<MatchNode> alignments = new ArrayList<MatchNode>();

        // check if it's a molecule
        //
        if( we instanceof Molecule ) {

            // cast to a Molecule
            Molecule mol = (Molecule) we;

            // output the candidate structure
            //
            mol.traversePartonomy( gri, 0 );

            // find roots of the candidate structure(s)
            //
            candroots = gri.getRootPartEntities();

            for( Residue candroot : candroots ) {
                ResidueType type = candroot.getSpecificType();
                if(type instanceof BaseType){
                    BaseType baseType = (BaseType) type;
                    // match the structure into the selected glyco tree root and return at most MATCHREQUEST matches
                    //
                    StructureMatch match = gtMatcher.match( gtRoot, baseType.getCompositeResidue(), matchType, MATCHREQUEST );
                    // count the matches
                    //
                    match.countMatches();
                    alignments.addAll(match.getAlignments());
                }
            }
        }
        return alignments;

    }
    
    public static Candidate convertMatch( MatchNode aMatch )
    {        
        Map<ResidueAssignment, Candidate> resMap = new HashMap<ResidueAssignment, Candidate>();
        
        MatchNode branch = aMatch;
        while(branch != null){
            for(ResidueAssignment ra: branch.getAssignments()){
                Candidate res = new Candidate(ra);
                resMap.put(ra, res);
            }
            branch = branch.getParent();
        }
        branch = aMatch;
        Candidate root = null;
        while(branch != null){
            for(ResidueAssignment ra: branch.getAssignments()){
                root = assembleStructure(ra, resMap);
            }
            branch = branch.getParent();
        }
        
        return root;
    }
    
    
    private static Candidate assembleStructure(ResidueAssignment ra, Map<ResidueAssignment, Candidate> resMap){
        ResidueAssignment parentAssignment = ra.getParent();
        Candidate child = resMap.get(ra);
        if(parentAssignment != null){
            Candidate parent = resMap.get(parentAssignment);
            parent.addChild(child);
        }
        return child;
    }
    
    
    public static class Candidate{
        
        public final ResidueAssignment assignment;
        private final List<Candidate> children;

        private Candidate(ResidueAssignment assignment){
            this.assignment = assignment;
            children = new ArrayList<Candidate>();
        }

        public void addChild(Candidate residue){
            children.add(residue);
        }

        public List<Candidate> getChildren(){
            return children;
        }
        
    }
    

}
