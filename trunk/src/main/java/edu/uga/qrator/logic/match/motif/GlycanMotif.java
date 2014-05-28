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
package edu.uga.qrator.logic.match.motif;

import edu.uga.qrator.logic.manage.GlydeStructure;
import edu.uga.qrator.logic.match.matcher.CombinationGenerator;
import edu.uga.qrator.logic.match.matcher.GlycanFit.Candidate;
import java.util.*;

/**
 *
 * @author Matthew
 */
public abstract class GlycanMotif {
    
    private final Set<MotifComparison> positives;
    private final Set<MotifComparison> negatives;
    
    public GlycanMotif(){
        positives = new HashSet<MotifComparison>();
        negatives = new HashSet<MotifComparison>();
    }
    
    public void addPositive(Map<String, Object> motif){
        MotifComparison root = MotifManager.parseMap(motif);
        positives.add(root);
    }
    
    public void addNegative(Map<String, Object> motif){
        MotifComparison root = MotifManager.parseMap(motif);
        negatives.add(root);
    }
    
    private boolean isMatch(MotifComparison root, boolean classifying){
//        System.out.println("------MATCHING AGAINST: "+name);
        boolean positive = false;
        for(MotifComparison node: positives){
//            System.out.println("matching positive");
            if(isMatch(root, node, classifying))
                positive = true;
        }
        boolean negative = false;
        for(MotifComparison node: negatives){
//            System.out.println("matching negative");
            if(isMatch(root, node, classifying))
                negative = true;
        }
//        System.out.println("POSITIVE: "+positive);
//        System.out.println("NEGATIVE: "+negative);
//        System.out.println("RESULT: "+(positive&&!negative));
//        System.out.println("------------------------------");
        return positive && !negative;
    }
    
    public boolean isMatch(GlydeStructure struct, boolean classifying){
        return isMatch(MotifManager.convertGlydeResidue(struct.root), classifying);
    }
    
    public boolean isMatch(Candidate candidate, boolean classifying){
        return isMatch(MotifManager.convertCompositeResidue(candidate.assignment.getCandidateResidue()), classifying);
    }
    
    private boolean isMatch(MotifComparison source, MotifComparison motif, boolean classifying){
        if(motif.type == null) return false;
        
        String type = classifying? source.type.replaceAll("5Gc", "~")
                                               .replaceAll("5Ac", "!")
                                               .replaceAll("\\d[A-Za-z]+", "")
                                               .replaceAll("~", "5Gc")
                                               .replaceAll("!", "5Ac")
                                 : source.type;
        
//        System.out.println("    COMPARING: "+type+" matches? "+motif.type);
//        System.out.println("               "+residue.anomer+" equals? "+motif.anomer);
//        System.out.println("               "+residue.type+" contains? "+motif.abconf);
        
        int pos = type.lastIndexOf( '-' );
        if( pos != -1 )
            type = type.substring( pos+1 );

        if(!type.matches(motif.type)) return false;
        if(source.anomer != null && !source.anomer.matches(motif.anomer)) return false;
        if(source.abconf != null && !source.abconf.matches(motif.abconf)) return false;
        MotifComparison[] sourceChildren = source.children;
        MotifComparison[] motifChildren = motif.children;
        int scLength = sourceChildren.length;
        int mcLength = motifChildren.length;
        
        if( mcLength > 0 ){
            if( mcLength <= scLength ) {
                List<MotifComparison[]> permutations = CombinationGenerator.permute( motifChildren );

                for( MotifComparison[] permutation : permutations ) {
                    boolean match = true;
                    Set<MotifComparison> used = new HashSet<MotifComparison>();
                    for( MotifComparison mc : permutation ) {
                        boolean found = false;
                        for( MotifComparison sc : sourceChildren ) {
                            if(!used.contains(sc)){
                                if(sc.isRoot || sc.link.matches(mc.link)){
                                    found = isMatch(sc, mc, classifying);
                                    if(found){
                                        used.add(sc);
                                        break;
                                    }
                                }
                            }
                        }
                        match = match && found;
                    }
                    if(match) return true;
                }
            }
            return false;
        }
        return true;
    }
    
}
