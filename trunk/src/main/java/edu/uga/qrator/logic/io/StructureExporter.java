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
package edu.uga.qrator.logic.io;

import edu.uga.glydeII.gom.*;
import edu.uga.glydeII.io.GOMWriter;
import edu.uga.glydeII.io.MonosaccharideMap;
import edu.uga.glydeII.io.MonosaccharideMap.MonoNode;
import edu.uga.glydeII.io.MonosaccharideMap.SubNode;
import edu.uga.qrator.except.StructureParsingException;
import static edu.uga.qrator.logic.QConfiguration.MONOMAP;
import edu.uga.qrator.obj.entity.*;
import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


/**
 * Exports {@code QStructure}s to GLYDE-II XML and contains other utility methods.
 * 
 * @author Matthew Eavenson (durandal@uga.edu)
 */
public class StructureExporter {
    
    
    /**
     * Obtains the root {@code CompositeResidue} of a {@code Glycan} object.
     * 
     * @param glycan  the {@link Glycan} object whose root we are attempting to acquire
     * @return the root {@link CompositeResidue} object, or null if this is a cyclical structure
     */
    public static CompositeResidue getRoot(Glycan glycan){
        Molecule m = glycan.getMolecule();
        
        // iterate through the molecule's parts
        for(PartEntity pe: m.getParts()){
            // only consider residues
            if(pe instanceof Residue){
                Residue r = (Residue)pe;
                // if there is no link out, this should be the root
                if(r.getLinkOut() == null){
                    Object subType = r.getSpecificType();
                    if(subType instanceof BaseType){
                        CompositeResidue root = ((BaseType)subType).getCompositeResidue();
                        return root;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Write a {@code Glycan} to GLYDE-II XML.
     * 
     * @param glycan  the {@link Glycan} object to be written
     * @return a stringified GLYDE XML representation of the {@code Glycan}
     */
    public static String writeGlycan(Glycan glycan){
        Set<WholeEntity> set = new HashSet<WholeEntity>();
        set.add(glycan.getMolecule());
        return GOMWriter.toXML(set);
    }
    
    /**
     * Write a {@code QStructure} to GLYDE XML.
     * 
     * @param structure  the {@link QStructure} to be written
     * @return a GLYDE XML representation of the {@code QStructure}
     */
    public static String writeStructure(QStructure structure){
        Glycan glycan = convertStructure(structure);
        return writeGlycan(glycan);
    }
    
    /**
     * Converts a {@code QTree} into a {@code Glycan}.
     * 
     * @param tree  the {@link QTree} object to be converted
     * @return a {@link Glycan} object
     */
    public static Glycan convertTree(QTree tree){

        String spec = tree.getSpec();
        JSONObject parsed = (JSONObject) JSONValue.parse(spec);
        
        Glycan glycan = null;
        try{
            Molecule molecule = new Molecule();
            glycan = new Glycan(molecule);
            convertNode(parsed, null, molecule, new Counter(1));
        }catch(GlydeException ge){
            ge.printStackTrace();
        }
        
        return glycan;
    }

    /***************************************************************
     * Converts a {@code QStructure} into a {@code Glycan}.
     * 
     * @param  structure  the {@link QStructure} object to be converted
     * @return the equivalent {@link Glycan} structure
     */
    public static Glycan convertStructure(QStructure structure){
        String spec = structure.getSpec();
        return convertStructure(spec);
    }
    
    /***************************************************************
     * Converts a {@code QStructure} spec into a {@code Glycan}.
     * 
     * @param spec  the {@link QStructure} spec to be converted
     * @return the equivalent {@link Glycan} structure
     */
    public static Glycan convertStructure(String spec){
        JSONObject parsed = (JSONObject) JSONValue.parse(spec);
        
        Glycan glycan = null;
        try{
            Molecule molecule = new Molecule();
            glycan = new Glycan(molecule);
            convertNode(parsed, null, molecule, new Counter(1));
        }catch(GlydeException ge){
            ge.printStackTrace();
        }
        
        return glycan;
    }
    
    /***************************************************************
     * Converts a node in a {@code QStructure}'s JSON spec into a {@code Residue}.
     * 
     * @param node  the {@code JSONObject} to be converted
     * @param parent  the parent {@link Residue}, if it exists -- null otherwise
     * @param molecule  the {@link Molecule} to add the new {@code Residue} to
     * @param partId  a running {@link Counter} of part ids in this structure
     * @throws {@link GlydeException} if the residue cannot be assembled properly
     * @throws {@link StructureParsingException} if the JSONObject cannot be parsed properly
     */
    private static void convertNode(JSONObject node, Residue parent, Molecule molecule, Counter partId) throws GlydeException{
        
        String prefix = MonosaccharideMap.MDBPREFIX;
        
        String id = (String) node.get("id");
        String from = (String) node.get("from");
        String to = (String) node.get("to");
        
        // attempt to get a monosaccharide by the residue id
        MonoNode mn = MONOMAP.getByFullName(id);
        Residue residue;
        // if we found a monosaccharide, process it
        if(mn != null){
            
            // create a new archetype Molecule from the monosaccharide
            Molecule archetype = new Molecule( prefix+mn.baseType, true );
            // create a new residue with the monosaccharide as its archetype
            residue = new Residue(archetype);
            
            // create a base type out of the residue
            BaseType bt = new BaseType(residue);
            
            // set anomer, absolute config, and ring form
            bt.setAnomericConfiguration(mn.anomeric.charAt(0)+"");
            bt.setAbsoluteConfiguration(mn.absolute);
            bt.setRingForm(mn.ringForm);
            
            // create a composite residue out of the base type
            CompositeResidue cr = new CompositeResidue(bt);
            cr.setName(mn.monoName);
            
            // process the substituents
            Set<SubNode> subs = mn.getSubstituents();
            for(SubNode sub: subs){
                
                // create an archetype for the substituent
                archetype = new Molecule(prefix+sub.name, true);
                
                // create a new residue for the substituent out of the archetype
                Residue subResidue = new Residue(archetype);
                
                // set the part id and increment
                subResidue.setPartId(partId.count+"");
                partId.count++;
                
                // create a new substituent out of the residue
                Substituent substituent = new Substituent(subResidue);
                
                // add the substituent to the composite residue
                cr.addSubstituent(substituent);
                
                // create bound atoms for the linkage and set part ids
                BoundAtom fromAtom = new BoundAtom();
                BoundAtom toAtom = new BoundAtom();
                fromAtom.setPartId(sub.from);
                toAtom.setPartId(sub.to);

                // create a new atom link with the bound atoms
                AtomLink aLink = new AtomLink(fromAtom, toAtom);
                
                // set to_replaces and from_replaces
                if(sub.from.contains("C")){
                    aLink.setToReplaces(sub.from.replace("C", "O"));
                }else if(sub.to.contains("C")){
                    aLink.setFromReplaces(sub.to.replace("C", "O"));
                }else aLink.setToReplaces("O1");  // set to O1 for inorganic substituents

                // bond order always 1?
                aLink.setBondOrder("1");
                
                // create a residue link out of the base type and subsituent
                // and add it to the molecule
                ResidueLink rLink = new ResidueLink(subResidue, residue);
                rLink.addSublink(aLink);
                molecule.addPart(subResidue);
            }
            residue.setPartId(partId.count+"");
            partId.count++;
        }else{  // this is probably an inorganic substituent
            SubNode sub = MONOMAP.getInorganicSubstituent(id);
            
            // if we still didn't find an entry in the monosaccharide map, give up
            if(sub == null) throw new StructureParsingException("Unknown residue detected - "+id);
            
            // create the molecular archetype
            Molecule archetype = new Molecule(prefix+sub.name, true);
            
            // create a residue out of the archetype
            residue = new Residue(archetype);
            residue.setPartId(partId.count+"");
            partId.count++;
            
            // create a new substituent out of the residue and add it to
            // the composite residue
            Substituent substituent = new Substituent(residue);
            CompositeResidue cr = new CompositeResidue(substituent);
            cr.setName(sub.name);
        }
        
        // if there is a parent residue, we need to create linkages
        if(parent != null){
            
            // if we don't have one of these, fail
            if(from == null || to == null)
                throw new StructureParsingException("Incomplete structure information provided");
            
            // create bound atoms for the linkage and set part ids
            BoundAtom fromAtom = new BoundAtom();
            BoundAtom toAtom = new BoundAtom();
            fromAtom.setPartId(from);
            toAtom.setPartId(to);

            // create a new atom link with the bound atoms
            AtomLink aLink = new AtomLink(fromAtom, toAtom);
            
            // set to_replaces and from_replaces
            if(from.contains("C")) aLink.setToReplaces(from.replace("C", "O"));
            if(to.contains("C")) aLink.setFromReplaces(to.replace("C", "O"));
            aLink.setBondOrder("1");

            // create a residue link out of the residue and its parent
            ResidueLink rLink = new ResidueLink(residue, parent);
            rLink.addSublink(aLink);
            
            // add substituents or set parent residues as needed
            SpecificType fromType = residue.getSpecificType();
            SpecificType toType = parent.getSpecificType();

            if( fromType instanceof Substituent && toType instanceof BaseType ){
                CompositeResidue cr = ((BaseType)toType).getCompositeResidue();
                cr.addSubstituent((Substituent)fromType);
            }else if( fromType instanceof BaseType && toType instanceof BaseType ){
                CompositeResidue fromComp = ((BaseType)fromType).getCompositeResidue();
                fromComp.setParentResidue();
            }
        }
        
        // add this residue to the molecule
        molecule.addPart(residue);
        
        // recurse to children
        JSONArray children = (JSONArray) node.get("children");
        if(children != null){
            for(Object childObj: children){
                JSONObject child = (JSONObject) childObj;
                convertNode(child, residue, molecule, partId);
            }
        }
    }
    
    /**
    * Keeps track of Glycan part numbers.
    * 
    */
    private static class Counter{
        private int count;
        private Counter(int count){
            this.count = count;
        }
    }
    
}
