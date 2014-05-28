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
 * This class represents a single residue assignment between 
 * a pattern (candidate) tree residue and a target (glyco) tree residue.
 *
 * @author Krys Kochut
 */
public class ResidueAssignment implements Comparable<ResidueAssignment>{

    private CompositeResidue  gtResidue;       // glyco tree residue
    private String            gtResidueBond;   // glyco tree bond
    private CompositeResidue  candResidue;     // candidate tree residue
    private String            candResidueBond; // candidate tree bond
    private ResidueAssignment parent;

    // this node score:  should we change it to the true edit distance?
    // i.e. with the score of 0 being what 5 is now (perfect agreement)
    // and 5 being what 0 is now.
    private int score = 0;

    // score up to this node with no edits from the root
    private boolean isPerfScore;

    // auxiliary array for computing permutations of Residue nodes
    //private List<CompositeResidue[]> permutations = null;
    
    private final String[] differences;

    /**
     * Create a ResidueAssignment object.
     *
     * @param parent the parent residue assignment, can be null
     * @param gtResidue the glyco tree residue
     * @param candResidue the candidate tree residue
     * @param gtResidueBond the glyco tree residue bond
     * @param candResidueBond the candidate tree residue bond
     * @param isParentPerf is the parent residue assignment perfect?
     */
    public ResidueAssignment( ResidueAssignment parent,
                              CompositeResidue gtResidue, 
                              CompositeResidue candResidue,
                              String  gtResidueBond,
                              String  candResidueBond,
                              boolean isParentPerf
                            )
    {
        String gtVal;
        String candVal;
        int    pos;
        
        this.gtResidue       = gtResidue;
        this.candResidue     = candResidue;
        this.gtResidueBond   = gtResidueBond;
        this.candResidueBond = candResidueBond;
        isPerfScore          = false;
        
        this.parent = parent;
            
        score = 0;

        // compute the score, but only of the assignment to glyco tree residue has been given
        //
        if( gtResidue != null ) {
            BaseType candBaseType = candResidue != null? candResidue.getBaseType() : null;
            BaseType canoBaseType = gtResidue.getBaseType();
            differences = new String[5];

            // compare the residue types
            gtVal = gtResidue.getName();
            pos = gtVal.lastIndexOf('-');
            if( pos != -1 )
                gtVal = gtVal.substring( pos+1 );

            candVal = candResidue != null? candResidue.getName() : "";
            pos = candVal.lastIndexOf('-');
            if( pos != -1 )
                candVal = candVal.substring( pos+1 );
            if( gtVal.equals( candVal ) )
                score = 1;
            else differences[0] = gtVal+"="+candVal;

            // compare the absolute configurations, if given in glyco tree
            gtVal = canoBaseType != null? canoBaseType.getAbsoluteConfiguration() : null;
            candVal = candBaseType != null? candBaseType.getAbsoluteConfiguration() : null;
            if( gtVal != null && candVal != null ) {
                if( gtVal.charAt( 0 ) == candVal.charAt( 0 ) )
                    score++;
                else differences[1] = gtVal+"="+candVal;
            }else score++;               // if not given, discount them
                

            // compare the anomeric configurations
            gtVal = canoBaseType != null? canoBaseType.getAnomericConfiguration() : null;
            candVal = candBaseType != null? candBaseType.getAnomericConfiguration() : null;
            if( gtVal != null && candVal != null) {
                if(gtVal.charAt( 0 ) == candVal.charAt( 0 ) )
                    score++;
                else differences[2] = gtVal+"="+candVal;
            }else score++;
            
            // compare the ring forms
            gtVal = canoBaseType != null? canoBaseType.getRingForm() : null;
            candVal = candBaseType != null? candBaseType.getRingForm() : null;
            if( gtVal != null && candVal != null) {
                if(gtVal.charAt( 0 ) == candVal.charAt( 0 ) )
                    score++;
                else differences[3] = gtVal.charAt( 0 )+"="+candVal.charAt( 0 );
            }else score++;

            if( gtResidueBond != null && candResidueBond != null ) {
                // compare the parent bond positions
                if( gtResidueBond.endsWith( candResidueBond ) )
                    score++;
                else differences[4] = gtResidueBond.charAt(gtResidueBond.length()-1) 
                        +"="+candResidueBond.charAt(candResidueBond.length()-1);
            }else score++;   // no bond position data, so must be the roots
                
        }else differences = null;

        if( isParentPerf && score == GlycoTreeMatcher.maxMatchNodeScore )
            isPerfScore = true;

    }

    public CompositeResidue getCanonicalResidue()
    {
        return gtResidue;
    }

    public String getCanonicalResidueBond()
    {
        return gtResidueBond;
    }

    public CompositeResidue getCandidateResidue()
    {
        return candResidue;
    }

    public String getCandidateResidueBond()
    {
        return candResidueBond;
    }

    public int getScore()
    {
        return score;
    }

    public boolean isPerfScore()
    {
        return isPerfScore;
    }
    
    public String[] getDifferences(){
        return differences;
    }
    
    public ResidueAssignment getParent(){
        return parent;
    }

    /**
     * Create and return a list of all assignments for the children of this ResidueAssignment.
     *
     * @return the listof all assignments for the children of this ResidueAssignment.
     */
    public List<List<ResidueAssignment>> createAllChildrenAssignments()
    {
        List<List<ResidueAssignment>> assignments = new ArrayList<List<ResidueAssignment>>();
        int noCandChildren;
        int noGTChildren;

        CompositeResidue[] gtChildren = null;
        CompositeResidue[] candChildren = null;

        //permutations = new ArrayList<CompositeResidue[]>();

        if( gtResidue != null ) {
            gtChildren = getChildren( gtResidue );
            noGTChildren = gtChildren.length;
        }
        else
            noGTChildren = 0;

        if( candResidue != null ) {
            candChildren = getChildren( candResidue );
            noCandChildren = candChildren.length;
        }
        else
            noCandChildren = 0;

        // if the candidate structure has no mode children, stop and return the empty list of assignments
        if( noCandChildren == 0 )
            return assignments;

        if( noCandChildren <= noGTChildren ) {

            List<CompositeResidue[]> permutations = CombinationGenerator.permute( candChildren );

            CombinationGenerator<CompositeResidue> cg = 
                new CombinationGenerator<CompositeResidue>( Arrays.asList( gtChildren ), noCandChildren);

            for( List<CompositeResidue> combination : cg ) {
                for( CompositeResidue[] a : permutations ) {
                    int i = 0;
                    List<ResidueAssignment> assignment = new ArrayList<ResidueAssignment>();
                    for( CompositeResidue c : combination ) {
                        ResidueAssignment resAssignment = 
                            new ResidueAssignment( this, c, a[i],
                                                   getBondData( c ), getBondData( a[i] ),
                                                   isPerfScore );
                        /*
                        resAssignment.setGTResidueBond( getBondData( c ) );
                        resAssignment.setCandResidueBond( getBondData( a[i] ) );
                        */
                        assignment.add( resAssignment );
                        i++;
                    }
                    assignments.add( assignment );
                }
            }
            
            return assignments;

        }
        else if( noGTChildren > 0 ) {

            // we have fewer GTChildren than candChildren;
            // some candChildren will be left unassigned

            // generate all permutations of the glyco tree residue's children;
            // the result is stored in the variable permutations
            //permute( gtChildren );
            List<CompositeResidue[]> permutations = CombinationGenerator.permute( gtChildren );

            // create a combination generator for the candidate structure residue's children;
            // each time, select noGTChildren elements
            CombinationGenerator<CompositeResidue> cg = 
                new CombinationGenerator<CompositeResidue>( Arrays.asList( candChildren ), noGTChildren);

            // for each combination of candidate structure's residues and each permutation of glyco tree residues
            // combine them into assignments, pairwise
            for( List<CompositeResidue> combination : cg ) {
                for( CompositeResidue[] a : permutations ) {
                    int i = 0;
                    List<ResidueAssignment> assignment = new ArrayList<ResidueAssignment>();
                    for( CompositeResidue c : combination ) {
                        ResidueAssignment resAssignment = 
                            new ResidueAssignment( this, a[i], c,
                                                   getBondData( a[i] ), getBondData( c ),
                                                   isPerfScore );
                        assignment.add( resAssignment );
                        i++;
                    }

                    // now, add the unassigned candChildren children
                    for( CompositeResidue c : difference( candChildren, combination ) ) {
                        ResidueAssignment resAssignment =
                            new ResidueAssignment( this, null, c,
                                                   null, getBondData( c ),
                                                   isPerfScore );
                        assignment.add( resAssignment );
                    }

                    assignments.add( assignment );
                }
            }
            return assignments;

        }
        else { 
            
            // we have no GTChildren residues;
            // all candChildren residues will be left unassigned
            List<ResidueAssignment> assignment = new ArrayList<ResidueAssignment>();

            // create NULL residue assignments for all candidate structure residues
            for( int i = 0; i < candChildren.length; i++ ) {
                ResidueAssignment resAssignment = 
                    new ResidueAssignment( this, null, candChildren[i],
                                           null, getBondData( candChildren[i] ),
                                           isPerfScore );
                assignment.add( resAssignment );
            }

            assignments.add( assignment );
            
            return assignments;

        }

    }
    
    /**
     * Compare this ResidueAssignment with another one.
     * @param ra the other ResidueAssignment
     * @return a value < 0, 0, or > 0 depending on the relative comparison of the ResidueAssignments
     */
    @Override
    public int compareTo( ResidueAssignment ra )
    {
            return getCandidateResidue().toString().compareTo( ra.getCandidateResidue().toString() );
    }
    
    public boolean equals( ResidueAssignment ra )
    {
        if( getCandidateResidue().equals( ra.getCandidateResidue() ) &&
            getCandidateResidueBond().equals( ra.getCandidateResidueBond() ) ) {
            if( getCanonicalResidue() != null &&
                ra.getCanonicalResidue() != null &&
                getCanonicalResidueBond() != null &&
                ra.getCanonicalResidueBond() != null &&
                getCanonicalResidue().equals( ra.getCanonicalResidue() ) &&
                getCanonicalResidueBond().equals(  ra.getCanonicalResidueBond() ) )
                    return true;
            else if( getCanonicalResidue() == null && ra.getCanonicalResidue() == null &&
                    getCanonicalResidueBond() == null && ra.getCanonicalResidueBond() == null )
                    return true;
            else
                return false;
        }
        return false;
       
    }

    /**
     * Return this ResidueAssignment object as a String.
     */
    @Override
    public String toString()
    {
        BaseType crt, grt;
        String   notPerfS = "";
        String   perfS    = "*";
        String   perfPrefix;

        if( gtResidue != null && candResidue != null ) {

            crt = candResidue.getBaseType();
            grt = gtResidue.getBaseType();
            
            perfPrefix = notPerfS;
            if( isPerfScore() )
                perfPrefix = perfS;

            if( gtResidueBond != null && candResidueBond != null )
                return 
                    perfPrefix +
                    crt.getCompositeResidue().getName() + " "
                    + crt.getRingForm().charAt( 0 ) + "_"
                    + candResidueBond.charAt( candResidueBond.length() - 1 )
                    + " ["
                   // + candResidue.getId()
                    + "] --> "
                    + gtResidue.getName() + " "
                    + grt.getRingForm().charAt( 0 ) + "_"
                    + gtResidueBond.charAt( gtResidueBond.length() - 1 );
            else
                return 
                    perfPrefix +
                    crt.getCompositeResidue().getName() + " "
                    + crt.getRingForm().charAt( 0 ) + "_"
                    + "X"
                    + " ["
                  //  + candResidue.getId()
                    + "] --> "
                    + gtResidue.getName() + " "
                    + grt.getRingForm().charAt( 0 ) + "_"
                    + "X";
        } 
        else if( candResidue != null ) {

            crt = candResidue.getBaseType();

            if( candResidueBond != null )
                return 
                    crt.getCompositeResidue().getName() + " "
                    + candResidue.getBaseType().getRingForm().charAt( 0 ) + "_"
                    + candResidueBond.charAt( candResidueBond.length() - 1 )
                    + " ["
                   // + candResidue.getId()
                    + "] --> ";
            else
                return 
                    crt.getCompositeResidue().getName() + " "
                    + candResidue.getBaseType().getRingForm().charAt( 0 ) + "_"
                    + "X"
                    + " ["
                   // + candResidue.getId()
                    + "] --> ";
        }
        else
            return "??? --> ???";
    }


    ///////////////////////////////////////////////////////////////
    //
    // Auxuliary methods for combination and permutation generation
    //

    // compute the set difference between the candidate children nodes and glyco tree nodes
    // used in a selected combination;
    //
    // this method is used only if there are some unassigned candidate children nodes, in case
    // the number of glyco tree children nodes is less than the number of candidate childrennodes
    //
    private List<CompositeResidue> difference( CompositeResidue[] candChildren, List<CompositeResidue> combination )
    {
        List<CompositeResidue> diff = new ArrayList<CompositeResidue>();
        int candNo = candChildren.length;

        for( int i = 0; i < candNo; i++ ) {

            boolean found = false;

            for( CompositeResidue res : combination ) {

                //System.out.println( "ResidueAssignment.difference: combination res: " + res );
                if( res == candChildren[ i ] ) {
                    found = true;
                    break;
                }
            }
            if( !found )
                diff.add( candChildren[ i ] );
        }

        return diff;
    }

    // return an array with children of the argument residue
    private CompositeResidue[] getChildren( CompositeResidue residue )
    {
//        if( residue == null ) {
//            System.out.println( "ResidueAssignment.getChildren: residue is null" );
//            System.out.flush();
//        }
        
        BaseType baseType = residue.getBaseType();
        Residue res = null;
        if(baseType == null){
            Substituent sub = residue.getSubstituents().iterator().next();
            res = sub.getGeneralType();
        }else{
            res = baseType.getGeneralType();
        }

        SortedSet<ResidueLink> incoming = res.getLinkIn();
        //Residue[] children = new Residue[ incoming.size() ];
        //int i = 0;
        List<CompositeResidue> children = new ArrayList<CompositeResidue>();
        
        for( ResidueLink resLink : incoming ) {
            ResidueType from = resLink.getFrom().getSpecificType();
            if( from instanceof Substituent ){
                Substituent sub = (Substituent)from;
                CompositeResidue comp = sub.getCompositeResidue();
                if(!residue.equals(comp)) children.add(comp);
            }else{
                BaseType bt = (BaseType)from;
                children.add(bt.getCompositeResidue());
            }
            //children[ i ] = resLink.getFrom();
            //i++;
        }

        //children = Arrays.copyOf( children, i  );

        return children.toArray(new CompositeResidue[0]);
    }

    // return String representation of the residue bond data
    private String getBondData( CompositeResidue composite )
    {
        Residue residue;
        BaseType bt = composite.getBaseType();
        if(bt == null){
            Substituent sub = composite.getSubstituents().iterator().next();
            residue = sub.getGeneralType();
        }else{
            residue = bt.getGeneralType();
        }
        
        ResidueLink resLink  = residue.getLinkOut();
        String      bondData = null;

        if( resLink != null ) {
            if( resLink.getSubLinks().size() > 0 ) {
                AtomLink atomLink = resLink.getSubLinks().first();
                if( atomLink != null ) {
                    bondData = atomLink.getTo().getPartId();
                }
            }

        }

        return bondData;

    }

}
