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

import edu.uga.glydeII.gom.AtomLink;
import edu.uga.glydeII.gom.BaseType;
import edu.uga.glydeII.gom.CompositeResidue;
import edu.uga.glydeII.gom.Residue;
import edu.uga.glydeII.gom.ResidueLink;
import edu.uga.glydeII.gom.ResidueType;
import edu.uga.glydeII.gom.SpecificType;
import edu.uga.glydeII.gom.Substituent;
import edu.uga.qrator.logic.QConfiguration;
import edu.uga.qrator.logic.manage.GlydeStructure;
import edu.uga.qrator.logic.manage.GlydeStructure.GlydeResidue;
import edu.uga.qrator.logic.manage.StructureManager;
import edu.uga.qrator.logic.manage.TypeManager;
import edu.uga.qrator.logic.match.matcher.GlycanFit;
import edu.uga.qrator.logic.match.matcher.MatchNode;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QStructure.ReviewStatus;
import edu.uga.qrator.obj.entity.QStructureType;
import edu.uga.qrator.obj.entity.QTree;
import edu.uga.qrator.util.QratorUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import persist.util.PersistenceUtil;

/**
 * Utility methods for using {@code Motif}s.  Specifically, methods to convert
 * objects into {@code MotifComparison}s for use in matching and searching. 
 * 
 * @author Matthew Eavenson (durandal@uga.edu)
 */
public class MotifManager {
    
    // classification motifs as specified in the motif configuration file
    public static final List<ClassificationMotif> MOTIFS;
    
    static{
        MOTIFS = new ArrayList<ClassificationMotif>();
        
        // load the motif file as a classpath resource
        ClassLoader loader = MotifManager.class.getClassLoader();
        if(loader==null)
            loader = ClassLoader.getSystemClassLoader();
        URL motifFile = (URL) loader.getResource(QConfiguration.MOTIFFILE);
        
        // parse the motif file
        try{
            parseMotifs(motifFile.openStream());
        }catch(IOException ioe){
            throw new RuntimeException(ioe);
        }
    }
    
    /**
     * Parses {@code ClassificationMotif}s from the motif configuration file.
     * 
     * @param stream  an {@code InputStream} of the contents of the motif configuration file
     */
    private static void parseMotifs(InputStream stream){
        Map<String,ClassificationMotif> motifMap = new HashMap<String,ClassificationMotif>();
        InputStreamReader isr = new InputStreamReader(stream);
        Connection conn = PersistenceUtil.connect();
        TypeManager manager = new TypeManager(conn);
        Object file = JSONValue.parse(isr);
        JSONArray arr = (JSONArray) file;
        
        for(Object o: arr){
            JSONObject obj = (JSONObject) o;
            String name = obj.get("name").toString();
            String type = obj.get("type").toString();
            QStructureType structType = manager.getType(name);
            @SuppressWarnings("unchecked")
            Map<String, Object> mMap = (Map<String,Object>) obj.get("spec");
            ClassificationMotif motif = motifMap.get(name);
            motif = motif == null? new ClassificationMotif(name, structType): motif;
            //System.out.println(name);
            if(type.equals("positive")){
                //System.out.println(" -- adding positive");
                motif.addPositive(mMap);
            }else{
                //System.out.println(" -- adding negative");
                motif.addNegative(mMap);
            }
            motifMap.put(name, motif);
            if(!MOTIFS.contains(motif)) MOTIFS.add(motif);
        }
        
        PersistenceUtil.close(conn);
    }
    
    
    /**
     * Get the {@code ClassificationMotif} that matches a {@code GlydeStructure}.
     * 
     * @param struct  the {@link GlydeStructure} to be matched
     * @return a {@link ClassificationMotif} that matches the provided {@code GlydeStructure}, or 
     * the Unknown motif if nothing matches
     */
    public static ClassificationMotif matchMotif(GlydeStructure struct){
        for(ClassificationMotif motif: MOTIFS){
            if(motif.isMatch(struct, true)){
                return motif;
            }
        }
        return ClassificationMotif.getMotif("Unknown");
    }
    
    /**
     * Get the {@code ClassificationMotif} that matches a {@code GlycanFit.Candidate}.
     * 
     * @param candidate  the {@link GlycanFit.Candidate} to be matched
     * @return a {@link ClassificationMotif} that matches the provided {@code GlycanFit.Candidate}, or 
     * the Unknown motif if nothing matches
     */
    public static ClassificationMotif matchMotif(GlycanFit.Candidate candidate){
        for(ClassificationMotif motif: MOTIFS){
            if(motif.isMatch(candidate, true)){
                return motif;
            }
        }
        return ClassificationMotif.getMotif("Unknown");
    }
    
    /**
     * Annotate a {@code QStructure}'s JSON sequence with differences from a {@code QTree}.
     * Each residue that differs from the provided canonical tree will be annotated with a NOTATTEMPTED
     * code, which means that a residue that is not in the tree.  This only accounts for whether a
     * residue exists or not within a tree, as opposed to the {@code GlycanFit} matching algorithm,
     * which takes inexact matches into account as well.
     * 
     * @param structObj  the {@link QStructure} to be compared
     * @param treeObj  the {@link QTree} to compare the {@code QStructure} to
     * @return a {@code Map} that represents an annotated {@code QStructure}'s
     * JSON sequence information
     */
    public static Map<String,Object> compareToTree(QStructure structObj, QTree treeObj){
        GlydeStructure struct = getGlydeStructure(structObj);
        GlydeStructure tree = getGlydeStructure(treeObj);
        
        for(GlydeResidue child: struct.root.getChildren()){
            compare(child, tree.root);
        }
        
        return struct.toMap();
    }
    
    /**
     * Compare two {@code GlydeResidue}s' information.
     * <p>
     * The first residue is assumed to be from a structure, while the second is
     * assumed to be from a canonical tree.  If the two differ, the first residue
     * will be annotated with a NOTATTEMPTED code, which means the residue is not 
     * in the tree.
     * 
     * @param struct  the {@link GlydeResidue} from a structure
     * @param tree  the {@code GlydeResidue} from a canonical tree
     */
    private static void compare(GlydeResidue struct, GlydeResidue tree){
        
        GlydeResidue match = null;
        if(tree != null){
            for( GlydeResidue child : tree.getChildren() ) {
                if( child.id.equals(struct.id)                                   &&
                   (child.anomer != null && child.anomer.equals(struct.anomer))  &&
                    child.linkNum.equals(struct.linkNum)                         &&
                    child.from.equals(struct.from)                               &&
                    child.to.equals(struct.to)){
                    match = child;
                    break;
                }
            }
        }
        if(match != null) struct.setMatch(GlycanFit.MatchStatus.EXACT.toString());
        else              struct.setMatch(GlycanFit.MatchStatus.NOTATTEMPTED.toString());
        for(GlydeResidue child : struct.getChildren()){
            compare(child, match);
        }
    }
    
    /**
     * Parse a {@code MotifComparison} from a JSON object, represented as a {@code Map}.
     * <p>
     * This method returns the root {@link MotifComparison}, as a {@code MotifComparison}
     * is a recursive structure.
     * 
     * @param motif  a JSON object to be parsed
     * @return a {@link MotifComparison} containing the {@code Map}'s parsed information
     */
    public static MotifComparison parseMap(Map<String,Object> motif){
        return parseMap(motif, true);
    }
    
    /**
     * Recursively parse a {@code MotifComparison} from a JSON object, represented as a {@code Map}.
     * 
     * @param motif  a JSON object to be parsed
     * @param isRoot  a boolean specifying whether this {@code Map} represents the root residue
     * @return a {@link MotifComparison} containing the {@code Map}'s parsed information
     */
    private static MotifComparison parseMap(Map<String,Object> motif, boolean isRoot){
        String type = motif.containsKey("type")? motif.get("type").toString() : null;
        String anomer = motif.containsKey("anomer")? motif.get("anomer").toString() : null;
        String abconf = motif.containsKey("abconf")? motif.get("abconf").toString() : null;
        String link = motif.containsKey("link")? motif.get("link").toString() : isRoot? null : ".";
        String from = motif.containsKey("from")? motif.get("from").toString() : null;
        List<MotifComparison> children = new ArrayList<MotifComparison>();
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> childList = (List<Map<String,Object>>) motif.get("children");
        if(childList != null){
            for(Map<String,Object> child: childList){
                MotifComparison node = parseMap(child, false);
                children.add(node);
            }
        }
        return new MotifComparison(type, anomer, abconf, link, from, isRoot, children.toArray(new MotifComparison[0]));
    }
    
    /**
     * Convert a {@code GlydeResidue} to a {@code MotifComparison}.
     * 
     * @param residue  a {@link GlydeResidue} to be converted
     * @return a {@link MotifComparison} containing the {@code GlydeResidue}'s information
     */
    public static MotifComparison convertGlydeResidue(GlydeResidue residue){
        return convertGlydeResidue(residue, true);
    }
    
    /**
     * Recursively convert a {@code GlydeResidue} into a {@code MotifComparison}.
     * 
     * @param residue  a {@link GlydeResidue} to be parsed
     * @param isRoot  a boolean specifying whether this {@code GlydeResidue} represents the root residue
     * @return a {@link MotifComparison} containing the {@code GlydeResidue}'s information
     */
    private static MotifComparison convertGlydeResidue(GlydeResidue residue, boolean isRoot){
        // extract the residue type
        String type = residue.id;
        int pos = type.lastIndexOf( '-' );
        if( pos != -1 )
            type = type.substring( pos+1 );
        
        String abconf = QratorUtils.getMatch(residue.id, "[A-Z]\\-");
        if(abconf != null) abconf = abconf.replaceAll("\\-", "");
        
        List<MotifComparison> children = new ArrayList<MotifComparison>();
        List<GlydeResidue> childList = residue.getChildren();
        if(!childList.isEmpty()){
            for(GlydeResidue child: childList){
                MotifComparison node = convertGlydeResidue(child, false);
                children.add(node);
            }
        }
        return new MotifComparison(type, residue.anomer, abconf, residue.linkNum, residue.from, isRoot, children.toArray(new MotifComparison[0]));
    }
    
     /**
     * Convert a {@code CompositeResidue} to a {@code MotifComparison}.
     * 
     * @param residue  a {@link CompositeResidue} to be converted
     * @return a {@link MotifComparison} containing the {@code CompositeResidue}'s information
     */
    public static MotifComparison convertCompositeResidue(CompositeResidue residue){
        return convertCompositeResidue(residue, true);
    }
    
    /**
     * Recursively convert a {@code CompositeResidue} into a {@code MotifComparison}.
     * 
     * @param residue  a {@link CompositeResidue} to be parsed
     * @param isRoot  a boolean specifying whether this {@code CompositeResidue} represents the root residue
     * @return a {@link MotifComparison} containing the {@code CompositeResidue}'s information
     */
    private static MotifComparison convertCompositeResidue(CompositeResidue residue, boolean isRoot){
        
        // extract the residue type
        String type = residue.getName();
        
        int pos = type.lastIndexOf( '-' );
        if( pos != -1 )
            type = type.substring( pos+1 );

        BaseType baseType = residue.getBaseType();
        String absConfig = "",
               anomer = "",
               link = getLinkPosition(residue),
               from = getFrom(residue);
        
        SortedSet<ResidueLink> linkSet = null; 
        if(baseType != null){
            linkSet = baseType.getGeneralType().getLinkIn();
            
            // extract the absolute configuration
            absConfig = baseType.getAbsoluteConfiguration();
            //abs = abs.charAt( 0 )+"";
            
            // extract the anomeric configuration
            anomer = baseType.getAnomericConfiguration();
            //ano = ano.charAt( 0 )+"";
        }
        
        List<MotifComparison> children = new ArrayList<MotifComparison>();
        if(linkSet != null){
            for(ResidueLink resLink: linkSet){
                Residue child = resLink.getFrom();
                ResidueType resType = child.getSpecificType();
                MotifComparison node = null;
                // count substituents who are in their own composite residues
                if(resType instanceof Substituent){
                    Substituent sub = (Substituent) resType;
                    CompositeResidue parent = sub.getCompositeResidue();
                    if(!parent.equals(residue))
                        node = convertCompositeResidue(parent, false);
                }else{
                    BaseType bt = (BaseType) resType;
                    node = convertCompositeResidue(bt.getCompositeResidue(), false);
                }
                if(node != null)
                    children.add(node);
            }
        }
        return new MotifComparison(type, anomer, absConfig, link, from, isRoot, children.toArray(new MotifComparison[0]));
    }
    
     /**
     * Convert a motif JSON, represented as a {@code Map}, into a {@code QStructure} sequence, also represented as a {@code Map}.
     * 
     * @param motif  a motif's JSON representation
     * @return a {@code Map} containing equivalent information, formatted for use as a {@code QStructure}'s sequence
     */
    public static Map<String, Object> motifToSpec(Map<String, Object> motif){
        MotifComparison comp = parseMap(motif);
        return motifToSpec(comp);
    }
    
    /**
     * Convert a {@code MotifComparison} into a {@code QStructure} sequence, represented as a {@code Map}.
     * 
     * @param motif  a {@link MotifComparison} to be converted
     * @return a {@code Map} containing equivalent information, formatted for use as a {@code QStructure}'s sequence
     */
    public static Map<String, Object> motifToSpec(MotifComparison motif){
        Map<String, Object> json = new HashMap<String, Object>();
        if(motif.abconf != null) json.put("id", motif.anomer+"-"+motif.abconf+"-"+motif.type);
        else json.put("id", motif.type);
        String from;
        if(motif.anomer != null){
            from = "C"+motif.from;
            json.put("anomer", motif.anomer);
        }else{
            from = motif.from;
        }
        json.put("from", from);
        json.put("to", "O"+motif.link);
        json.put("link", motif.link);
        
        if(motif.children.length > 0){
            List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
            for(MotifComparison child : motif.children){
                children.add(motifToSpec(child));
            }
            json.put("children", children);
        }
        return json;
    }
    
    /**
     * Convert a {@code QTree}'s sequence information into a {@code GlydeStructure}.
     * 
     * @param tree  a {@link QTree}, whose sequence information is to be converted
     * @return a {@link GlydeStructure} containing the tree's sequence information
     */
    public static GlydeStructure getGlydeStructure(QTree tree){
        Map<String,Object> json = (Map<String,Object>) JSONValue.parse(tree.getSpec());
        return GlydeStructure.getGlydeStructure(json);
    }
    
    /**
     * Convert a {@code QStructure}'s sequence information into a {@code GlydeStructure}.
     * 
     * @param struct  a {@link QStructure}, whose sequence information is to be converted
     * @return a {@code GlydeStructure} containing the structure's sequence information
     */
    public static GlydeStructure getGlydeStructure(QStructure struct){
        Map<String,Object> json = (Map<String,Object>) JSONValue.parse(struct.getSpec());
        return GlydeStructure.getGlydeStructure(json);
    }
    
    /**
     * Searches through an {@code Iterator} of {@code QStructure}s and returns an 
     * {@code Iterator} of matching structures.
     * 
     * @param iter  an {@code Iterator} of {@code QStructure}s to be searched
     * @param motif  a {@link SearchMotif} to match structures against
     * @return an {@code Iterator} of matching {@code QStructure}s
     */
    public static Iterator<QStructure> search(Iterator<QStructure> iter, SearchMotif motif){
        List<QStructure> structs = new ArrayList<QStructure>();
        while(iter.hasNext()){
            QStructure struct = iter.next();
            GlydeStructure glydeStruct = getGlydeStructure(struct);
            if(motif.isMatch(glydeStruct, false)){
                structs.add(struct);
            }
        }
        return structs.iterator();
    }
    
    /**
     * Get a list of {@code Map}s which represent sequence information of canonical tree matches.
     * 
     * @param  manager      a {@link StructureManager} used to search for duplicates
     * @param  structures   the {@code List} of matches to be converted
     * @return a list of {@code Map}s which represent canonical tree matches, formatted as structure sequences.
     */
    public static List<Map<String,Object>> getMatchSpecs(StructureManager manager, List<MatchNode> structures){

        List<Map<String,Object>> structList = new ArrayList<Map<String,Object>>();
        for(MatchNode structure : structures){
            GlycanFit.Candidate candidate = GlycanFit.convertMatch(structure);
            GlydeStructure gStruct = GlydeStructure.getGlydeStructure(candidate.assignment.getCandidateResidue());
            int[] score = new int[]{structure.getTotalScore(), structure.getPerfectScore(), gStruct.residueCount()*5};
            Map<String, Object> specMap = getMatchSpec( candidate );
            String spec = JSONValue.toJSONString(specMap);
            String hash = QratorUtils.hashSpec(spec);

            Map<String, Object> struct = new HashMap<String, Object>();
            struct.put("score", score[0]+"/"+score[1]+"/"+score[2]);
            //System.out.println("SCORE: "+score[0]+"/"+score[1]+"/"+score[2]);
            ClassificationMotif motif = matchMotif( candidate );
            struct.put("type", motif.name);
            struct.put("typeId", motif.type.getId());

            // test to see whether this structure already exists
            QStructure match = manager.getByHash(hash);
            if(match != null){
                if(match.getStatus() == ReviewStatus.reviewed ||
                   match.getStatus() == ReviewStatus.approved)
                struct.put("exists", match.getId()+"");
            }
            struct.put("spec", spec);
            structList.add(struct);
        }
        return structList;
    }

    /**
     * Produce a {@code Map} representing a single canonical match specification.
     * 
     * @param  candidateStructure  the canonical tree match
     * @return a {@code Map} representing the sequence information of the match
     */
    public static Map<String, Object> getMatchSpec( GlycanFit.Candidate candidateStructure ){

        Map<String, Object> spec = new HashMap<String, Object>();

        CompositeResidue candidateResidue = candidateStructure.assignment.getCandidateResidue();
        CompositeResidue canonicalResidue = candidateStructure.assignment.getCanonicalResidue();
        
        BaseType candBaseType = candidateResidue != null? candidateResidue.getBaseType() : null;
        Set<Substituent> candSubs = candidateResidue != null? candidateResidue.getSubstituents() : null;
        
        BaseType canoBaseType = canonicalResidue != null? canonicalResidue.getBaseType() : null;
        Set<Substituent> canoSubs = canonicalResidue != null? canonicalResidue.getSubstituents() : null;
        
        // take the substituent only if the basetype is null and there is a substituent
        Residue candBase = candBaseType == null ? 
                                (candSubs != null && !candSubs.isEmpty() ?
                                    candSubs.iterator().next().getGeneralType() : null) :
                                candBaseType.getGeneralType();
        
        Residue canoBase = canoBaseType == null ?
                                (canoSubs != null && !canoSubs.isEmpty() ?
                                    canoSubs.iterator().next().getGeneralType() : null) :
                                canoBaseType.getGeneralType();
        
        GlycanFit.MatchStatus status = canonicalResidue == null? 
                                GlycanFit.MatchStatus.NOTATTEMPTED: 
                                candidateStructure.assignment.getScore() < 5? 
                                    GlycanFit.MatchStatus.INEXACT: 
                                    GlycanFit.MatchStatus.EXACT;
        String nodeId,
               anomer = null,
               linkNum = null,
               from = null,
               to = null;
        List<String> diff = null;

        
        if(status == GlycanFit.MatchStatus.EXACT || status == GlycanFit.MatchStatus.INEXACT){
            SpecificType type = canoBase.getSpecificType();
            if(type instanceof BaseType){
                BaseType bt = (BaseType) type;
                nodeId = bt.getCompositeResidue().getName();
                anomer = bt.getAnomericConfiguration();    
            }else{
                Substituent sub = (Substituent) type;
                nodeId = sub.getCompositeResidue().getPartId();
            }
            
            ResidueLink resLink = canoBase.getLinkOut();
            if(resLink != null){
                AtomLink atomLink = resLink.getSubLinks().first();
                from = atomLink.getFrom().getPartId();
                to = atomLink.getTo().getPartId();
            }
            
            if(candidateStructure.assignment.getParent() != null){
                String bond = candidateStructure.assignment.getCanonicalResidueBond();
                linkNum = bond.charAt(bond.length()-1)+"";
            }
            if(status == GlycanFit.MatchStatus.INEXACT){
                String[] differences = candidateStructure.assignment.getDifferences();
                diff = Arrays.asList(differences);
            }
        }else{
            SpecificType type = candBase.getSpecificType();
            if(type instanceof BaseType){
                BaseType bt = (BaseType)type;
                nodeId = bt.getCompositeResidue().getName();
                anomer = bt.getAnomericConfiguration();
            }else{
                Substituent sub = (Substituent) type;
                nodeId = sub.getCompositeResidue().getName();
            }
            
            ResidueLink resLink = candBase.getLinkOut();
            if(resLink != null){
                AtomLink atomLink = resLink.getSubLinks().first();
                from = atomLink.getFrom().getPartId();
                to = atomLink.getTo().getPartId();
            }
            
            if(candidateStructure.assignment.getParent() != null){
                String bond = candidateStructure.assignment.getCandidateResidueBond();
                linkNum = bond.charAt(bond.length()-1)+"";
            }
        }
        
        spec.put("id", nodeId);
        if(linkNum != null) spec.put("link", linkNum);
        spec.put("match", status.toString());
        if(anomer != null) spec.put("anomer", anomer);
        if(from != null) spec.put("from", from);
        if(to != null) spec.put("to", to);
        if(diff != null) spec.put("diff", diff);

        List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
        for (GlycanFit.Candidate each : candidateStructure.getChildren()) {
            children.add( getMatchSpec( each ) );
        }
        if(!children.isEmpty())
            spec.put("children", children);
        
        return spec;
    }

    /**
     * Get the residue replacement information of the outgoing link of 
     * a given {@code CompositeResidue} as a {@code String}.
     * 
     * @param  res  a {@link CompositeResidue}
     * @return the label of the {@code CompositeResidue}'s outgoing link
     */
    public static String getFrom(CompositeResidue res) {
        String link = null;
        Residue r;
        if(res.hasBaseType()) r = res.getBaseType().getGeneralType();
        else r = res.getSubstituents().iterator().next().getGeneralType();
        
        if( r.getLinkOut() != null ) {
            link = r.getLinkOut().getSubLinks().first().getFrom().getPartId();
        }
        return link;
    }
    
    /**
     * Get the residue replacement information of the outgoing link of 
     * a given {@code CompositeResidue} as a {@code String}.
     * 
     * @param  res  a {@link CompositeResidue}
     * @return the label of the {@code CompositeResidue}'s outgoing link
     */
    public static String getTo(CompositeResidue res) {
        String link = null;
        Residue r;
        if(res.hasBaseType()) r = res.getBaseType().getGeneralType();
        else r = res.getSubstituents().iterator().next().getGeneralType();
        
        if( r.getLinkOut() != null ) {
            link = r.getLinkOut().getSubLinks().first().getTo().getPartId();
        }
        return link;
    }
    
    /**
     * Get the position of the outgoing link of a given {@code CompositeResidue}
     * as a {@code String}.
     * 
     * @param  res  the {@code CompositeResidue}
     * @return the position of the {@code CompositeResidue}'s outgoing link, as a {@code String}
     */
    public static String getLinkPosition(CompositeResidue res) {
        String link = null;
        Residue r;
        if(res.hasBaseType()) r = res.getBaseType().getGeneralType();
        else r = res.getSubstituents().iterator().next().getGeneralType();
        
        if( r.getLinkOut() != null ) {
            String partId = r.getLinkOut().getSubLinks().first().getTo().getPartId();
            link = partId.charAt(1)+"";
        }
        return link;
    }
    
}
