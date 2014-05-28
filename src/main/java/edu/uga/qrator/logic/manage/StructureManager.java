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
package edu.uga.qrator.logic.manage;

import edu.uga.glydeII.gom.*;
import edu.uga.qrator.except.QException;
import edu.uga.qrator.except.StructureDuplicateException;
import edu.uga.qrator.except.StructureParsingException;
import edu.uga.qrator.logic.match.matcher.GlycanFit;
import edu.uga.qrator.logic.match.matcher.MatchNode;
import edu.uga.qrator.logic.match.motif.ClassificationMotif;
import edu.uga.qrator.logic.match.motif.MotifManager;
import edu.uga.qrator.obj.entity.*;
import edu.uga.qrator.obj.entity.QProvenance.ProvenanceAction;
import edu.uga.qrator.obj.entity.QStructure.ReviewStatus;
import edu.uga.qrator.obj.entity.impl.QEntityFactoryImpl;
import edu.uga.qrator.obj.relation.*;
import edu.uga.qrator.obj.relation.impl.QRelationFactoryImpl;
import edu.uga.qrator.util.FileUtils;
import edu.uga.qrator.util.QratorUtils;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;
import org.json.simple.JSONValue;
import persist.query.QueryBuilder;
import persist.query.Select;
import persist.query.filter.Filter;
import persist.query.sql.QueryBuilderSQL;
import persist.util.PersistenceUtil;

/**
 * Contains convenience methods for dealing with {@code QStructure}s.
 * 
 * @author Matthew Eavenson (durandal@uga.edu)
 */
public class StructureManager {
    
    // an entity factory object
    private final QEntityFactory efac;
    
    // relations from structures to other objects
    private final QStructureCreatedByQUser qsqu;
    //private final QUserTracksQStructure quqs;
    private final QStructureHasTypeQStructureType qsqt;
    private final QStructureTypeInTreeQTree qtqt;
    
    // a reference manager object
    private final ReferenceManager refManager;
    
    // a source manager object
    private final SourceManager srcManager;
    
    // a query builder (persistence module) -- allows the construction
    // of more complex queries
    private final QueryBuilder query;
    
    // this manager's database connection
    private final Connection conn;
        
    public StructureManager(Connection conn){
        this.conn = conn;
        efac = QEntityFactoryImpl.getFactory(conn);
        QRelationFactory afac = QRelationFactoryImpl.getFactory(conn);
        qsqu = afac.getQStructureCreatedByQUser();
        //quqs = afac.getQUserTracksQStructure();
        qsqt = afac.getQStructureHasTypeQStructureType();
        qtqt = afac.getQStructureTypeInTreeQTree();
        refManager = new ReferenceManager(conn);
        srcManager = new SourceManager(conn);
        query = new QueryBuilderSQL(conn);
    }
    
    /*
    public boolean checkout(QStructure structure){
        return StructureCheckout.checkout(structure);
    }
    
    public void checkin(QStructure structure){
        StructureCheckout.checkin(structure);
    }*/
    
    public void reviewed(QStructure structure, QUser user, String comment){
        transfer(structure, user, comment, ReviewStatus.reviewed, ProvenanceAction.toReviewed);
    }
    
    public void approve(QStructure structure, QUser user, String comment){
        transfer(structure, user, comment, ReviewStatus.approved, ProvenanceAction.toApproved);
    }

    public void reject(QStructure structure, QUser user, String comment){
        transfer(structure, user, comment, ReviewStatus.rejected, ProvenanceAction.toRejected);
    }
    
    public void defer(QStructure structure, QUser user, String comment){
        transfer(structure, user, comment, ReviewStatus.deferred, ProvenanceAction.toDeferred);
    }
    
    public void revert(QStructure structure, QUser user, String comment){
        transfer(structure, user, comment, ReviewStatus.pending, ProvenanceAction.toPending);
    }
    
    private void transfer(QStructure structure, QUser user, String comment, ReviewStatus status, ProvenanceAction action){
        StructureCheckout.checkin(structure);
        structure.setStatus(status);
        update(structure);
        ProvenanceAction reason = action;
        if(comment != null && !comment.isEmpty()) efac.createQAnnotation(comment, user, structure);
        efac.createQProvenance(reason, user, structure);
    }
    
    public int commitApproved(QUser user){
        TreeManager manager = new TreeManager(conn);
        int count = 0;
        for(Iterator<QStructure> iter = list(ReviewStatus.pending, null, null, null, -1, -1); iter.hasNext();){
            QStructure struct = iter.next();
            QTree tree = getTree(struct);
            if(tree.getSpec() != null){
                struct.setStatus(ReviewStatus.committed);
                update(struct);
                manager.updateTree(struct);
            }
            count++;
        }
        return count;
    }
    
    public QUser getCreator(QStructure structure){
        return qsqu.getCreator(structure);
    }
    
    public void setCreator(QStructure structure, QUser creator){
        qsqu.setCreator(structure, creator);
    }
    
    public QStructureType getType(QStructure structure){
        return qsqt.getType(structure);
    }
    
    public void setType(QStructure structure, QStructureType type){
        qsqt.setType(structure, type);
    }
    
    public QTree getTree(QStructure structure){
        QStructureType type = getType(structure);
        return qtqt.getTree(type);
    }
    
//    public Iterator<QStructure> listTrackedStructures(QUser user, Filter<QStructure> filter){
//        return quqs.getTrackeds(user, filter);
//    }
//    
//    public Iterator<QUser> listTrackers(QStructure structure, Filter<QUser> filter){
//        return quqs.getTrackers(structure, filter);
//    }
//    
//    public void addTracker(QStructure structure, QUser user){
//        quqs.add(user, structure);
//    }
//    
//    public void removeTracker(QStructure structure, QUser user){
//        quqs.remove(user, structure);
//    }
    
    public QStructure create(String name, byte[] contents, QStructureType type, QUser user){
        String glydeXML = new String(contents);
        CompositeResidue root = null;
        try{
            root = FileUtils.parseGlydeRoot(glydeXML);
        }catch(QException qe){
            throw new StructureParsingException(qe.getMessage());
        }
        GlydeStructure candidate = GlydeStructure.getGlydeStructure(root);
        String spec = getCandidateSpec(candidate);
        String hash = QratorUtils.hashSpec(spec);
        QStructure structure = getByHash(hash);
        if(structure != null){
            throw new StructureDuplicateException(name+" is a duplicate of "+structure.getFilename(), structure);
        }
        if(type == null){
            ClassificationMotif motif = MotifManager.matchMotif(candidate);
            type = motif.type;
        }
        
        structure = efac.createQStructure(name, hash, spec, glydeXML, ReviewStatus.pending, user, type);
        FileUtils.extractReferences(refManager, srcManager, user, structure);
        //addTracker(structure, user);
        efac.createQProvenance(ProvenanceAction.toPending, user, structure);
        return structure;
    }
    
    public long count(Filter<QStructure> filter){
        return efac.countStructures(filter);
    }
    
    public Iterator<QStructure> list(Filter<QStructure> filter){
        return efac.findStructures(filter);
    }
    
    public Iterator<QStructure> list(Filter<QStructure> filter, int offset, int limit){
        return efac.findStructures(filter, offset, limit);
    }
    
    public long count(ReviewStatus status, QTree tree, QStructureType type, QUser owner){
        Filter<QStructure> sf = new Filter<QStructure>(QStructure.class);
        if(status != null) sf.eq("status", status.toString());
        
        // create a filter that EXCLUDES a certain owner
        Filter<QUser> uf = null;
        if(owner != null){
            uf = new Filter<QUser>(QUser.class).ne("sid", owner.getId()+"");
        }
        
        Select<QStructure> select = query.select(QStructure.class);
        if(tree != null && type == null){
            select.where(sf);
            Filter<QTree> tf = new Filter<QTree>(QTree.class).eq("sid", tree.getId()+"");
            select.traverse(QStructureHasTypeQStructureType.class)
                  .traverse(QStructureTypeInTreeQTree.class)
                  .where(tf);
            if(owner != null){
                select.traverse(QStructureCreatedByQUser.class).where(uf);
            }
            return select.count();
        }else if(type != null){
            if(owner != null){
                select.where(sf);
                Filter<QStructureType> tf = new Filter<QStructureType>(QStructureType.class).eq("sid", type.getId()+"");
                select.traverse(QStructureHasTypeQStructureType.class)
                      .traverse(QStructureCreatedByQUser.class)
                      .where(tf).where(uf);
                return select.count();
            }else return qsqt.countFrom(type, sf);
        }else if(owner != null){
            select.traverse(QStructureCreatedByQUser.class).where(uf).where(sf);
            return select.count();
        }
        return count(sf);
    }
    
    public Iterator<QStructure> list(ReviewStatus status, QTree tree, QStructureType type, QUser owner, int offset, int limit){
        Filter<QStructure> sf = new Filter<QStructure>(QStructure.class);
        if(status != null) sf.eq("status", status.toString());
        
        // create a filter that EXCLUDES a certain owner
        Filter<QUser> uf = null;
        if(owner != null){
            uf = new Filter<QUser>(QUser.class).ne("sid", owner.getId()+"");
        }
        
        Select<QStructure> select = query.select(QStructure.class);
        if(type != null){
            if(owner != null){
                select.where(sf);
                Filter<QStructureType> tf = new Filter<QStructureType>(QStructureType.class).eq("sid", type.getId()+"");
                select.traverse(QStructureHasTypeQStructureType.class)
                      .traverse(QStructureCreatedByQUser.class)
                      .where(tf).where(uf);
                
                return select.listResults(offset, limit);
            }else return qsqt.getStructures(type, sf, offset, limit);
        }else if(tree != null){
            select.where(sf);
            Filter<QTree> tf = new Filter<QTree>(QTree.class).eq("sid", tree.getId()+"");
            select.traverse(QStructureHasTypeQStructureType.class)
                  .traverse(QStructureTypeInTreeQTree.class)
                  .where(tf);
            if(owner != null){
                select.traverse(QStructureCreatedByQUser.class).where(uf);
            }
            return select.listResults(offset, limit);
        }else if(owner != null){
            select.traverse(QStructureCreatedByQUser.class).where(uf).where(sf);
            return select.listResults(offset, limit);
        }
        return list(sf, offset, limit);
    }
    
    public void remove(QStructure structure){
        efac.removeQStructure(structure);
    }
    
    public void update(QStructure structure){
        efac.updateQStructure(structure);
    }
    
    public Iterator<QStructure> listByOwner(QUser user){
        return listByOwner(user, null, -1, -1);
    }
    
    public Iterator<QStructure> listByOwner(QUser user, int offset, int limit){
        return listByOwner(user, null, offset, limit);
    }
    
    public Iterator<QStructure> listByOwner(QUser owner, Filter<QStructure> filter, int offset, int limit){
        return qsqu.getCreateds(owner, filter, offset, limit);
    }
    
    public QStructure getByHash(String hash){
        Filter<QStructure> filter = new Filter<QStructure>(QStructure.class).eq("hash", hash);
        Iterator<QStructure> structures = list(filter);
        if(structures.hasNext()) return structures.next();
        return null;
    }
    
    public static String getCandidateSpec( CompositeResidue cr ){
        return getCandidateSpec(GlydeStructure.getGlydeStructure(cr));
    }
    
    public static String getCandidateSpec( GlydeStructure struct ){
        return JSONValue.toJSONString(struct.toMap());
    }
    
    public static String getCandidateSpec( String glydeXML ){
        CompositeResidue root = FileUtils.parseGlydeRoot(glydeXML);
        return getCandidateSpec(root);
    }
    
    public static String generateHash(String glydeXML){
        CompositeResidue root = FileUtils.parseGlydeRoot(glydeXML);
        GlydeStructure candidate = GlydeStructure.getGlydeStructure(root);
        Map<String, Object> candidateMap = candidate.toMap();
        String hash = QratorUtils.hashSpec(candidateMap);
        return hash;
    }
    
    /***************************************************************
     * Produce a list of MatchNodes, which are the root nodes of
     * structures that are possible matches against the GlycoTree
     * @param  treeRoot     String prefix of the GlycoTree to match against
     *                  e.g. N-glycan, O-glycan
     * @param  root     a CompositeResidue to match against the GlycoTree
     * @return List     a List of MatchNodes that represent the root nodes of
     *                  structures which were matched against the GlycoTree.
     */
    public static List<MatchNode> listMatches(CompositeResidue treeRoot, QStructure structure){
        List<MatchNode> structs = new ArrayList<MatchNode>();
        try{
            Set<WholeEntity> struct = FileUtils.parseGlyde(structure.getContents());
            GlycanFit fit = new GlycanFit();
            WholeEntity we = struct.iterator().next();
            structs = fit.match(we, treeRoot);
        }catch(NullPointerException ne){
            //Found an unknown CompositeResidue
            ne.printStackTrace();
            //throw new QratorException("Encountered an unknown composite residue.");
        }
        return structs;
    }
    
    public Map<String,Object> differenceFromTree(QStructure struct){
        QTree tree = getTree(struct);
        return MotifManager.compareToTree(struct, tree);
    }
    
    public int recalculateHashes(){
        int collisions = 0;
        Set<String> hashes = new HashSet<String>();
        for(Iterator<QStructure> structs = list(null); structs.hasNext();){
            QStructure struct = structs.next();
            String spec = struct.getSpec();
            String newHash = QratorUtils.hashSpec(spec);
            //System.out.println("\n"+struct.getHash());
            //System.out.println(newHash);
            //if(!struct.getHash().equals(newHash)) collisions++;
            
            if(!hashes.add(newHash)) collisions++;
            struct.setHash(newHash);
            update(struct);
        }
        return collisions;
    }
    
    public static void main(String[] args) throws IOException{
        
        Connection conn = PersistenceUtil.connect();
        UserManager uManager = new UserManager(conn);
        StructureManager sManager = new StructureManager(conn);
        ProvenanceManager pManager = new ProvenanceManager(conn);
        ReferenceManager rManager = new ReferenceManager(conn);
        SourceManager srcManager = new SourceManager(conn);
        AnnotationManager aManager = new AnnotationManager(conn);
        //QUser admin = uManager.getUserByUsername("admin");
        QUser glycomedb = uManager.getUserByUsername("glycomedb");
        
        //sManager.commitApproved(admin);
        
        StructureManager manager = new StructureManager(conn);
        //QStructure structure = getStructure(2, manager);
        
        //System.out.println("COLLISIONS DETECTED: "+manager.recalculateHashes());
        
        /*for(Iterator<QStructure> iter = sManager.list(null); iter.hasNext();){
            QStructure struct = iter.next();
            String filename = struct.getFilename();
            boolean hasReviewed = false;
            boolean hasApproved = false;
            for(Iterator<QProvenance> provs = pManager.list(struct, -1, -1); provs.hasNext();){
                QProvenance prov = provs.next();
                ProvenanceAction action = prov.getAction();
                if(action.equals(ProvenanceAction.toReviewed)) hasReviewed = true;
                if(action.equals(ProvenanceAction.toApproved)) hasApproved = true;
            }
            if(!hasReviewed && !hasApproved && struct.getStatus().equals(ReviewStatus.approved)){
                System.out.println(filename);
                struct.setStatus(ReviewStatus.pending);
                sManager.update(struct);
            }
        }*/
        
        for(Iterator<QStructure> iter = sManager.list(null); iter.hasNext();){
            QStructure struct = iter.next();
            String filename = struct.getFilename();
            /*for(Iterator<QProvenance> provs = pManager.list(struct, -1, -1); provs.hasNext();){
                QProvenance prov = provs.next();
                ProvenanceAction action = prov.getAction();
                if(action.equals(ProvenanceAction.toReviewed)) hasReviewed = true;
                if(action.equals(ProvenanceAction.toApproved)) hasApproved = true;
            }*/
            for(Iterator<QAnnotation> annos = aManager.list(struct, -1, -1); annos.hasNext();){
                QAnnotation anno = annos.next();
                String comment = anno.getComment();
                if(comment.contains("Modified from GlycomeDB")){
                    String[] tokens = comment.split(" ");
                    System.out.println(tokens[tokens.length-1]);
                    aManager.remove(anno);
                    //FileUtils.extractReferences(rManager, srcManager, glycomedb, struct);
                }
            }
            
        }
        
        
        // fix structures
        /*String glyde = Utils.fileToString(new File("/Users/durandal/Development/Qrator/Files/Glycosphingolipid/fully-defined/06047.glyde.xml"));
        String spec = StructureManager.getCandidateSpec(glyde);
        String hash = StructureManager.generateHash(glyde);
        QStructure struct = sManager.getByHash(hash);
        System.out.println(hash+" -- "+struct.getFilename());
        struct.setSpec(spec);
        sManager.update(struct);
        
        glyde = Utils.fileToString(new File("/Users/durandal/Development/Qrator/Files/Glycosphingolipid/fully-defined/09753.glyde.xml"));
        spec = StructureManager.getCandidateSpec(glyde);
        hash = StructureManager.generateHash(glyde);
        struct = sManager.getByHash(hash);
        System.out.println(hash+" -- "+struct.getFilename());
        struct.setSpec(spec);
        sManager.update(struct);*/
        
        // remove admin test annotations
        /*for(Iterator<QAnnotation> iter = aManager.list(admin, -1, -1); iter.hasNext();){
            QAnnotation anno = iter.next();
            QStructure struct = aManager.getStructure(anno);
            System.out.println(struct.getFilename()+" - "+anno.getComment());
            aManager.remove(anno);
        }*/
        
        // remove admin test provenance
        /*for(Iterator<QProvenance> iter = pManager.list(admin, null, -1, -1); iter.hasNext();){
            QProvenance prov = iter.next();
            QStructure struct = pManager.getStructure(prov);
            if(struct.getUri() == null && !prov.getAction().equals(ProvenanceAction.toOntology)){
                System.out.println(prov.getAction()+" - "+struct.getFilename());
                pManager.remove(prov);
                struct.setStatus(ReviewStatus.pending);
                sManager.update(struct);
                //if(prov.getAction().equals(ProvenanceAction.toPending)){
                //    pManager.setCreator(prov, glycomedb);
                //}
            }
        }*/
        
        // change structure creator from admin to glycomedb
        /*for(Iterator<QStructure> iter = sManager.listByOwner(admin); iter.hasNext();){
            QStructure struct = iter.next();
            String filename = struct.getFilename();
            System.out.println(filename);
            sManager.setCreator(struct, glycomedb);
        }*/
        
        
        //TypeManager tmanager = new TypeManager(conn);
        //QStructureType type = tmanager.listTypes(new Filter(QStructureType.class).eq("sid", "2")).next();
        
        /*for(Iterator<QStructure> iter = manager.list(ReviewStatus.deferred, null, type, null, null, -1, -1); iter.hasNext();){
            QStructure struct = iter.next();
            System.out.println(struct.getName());
        }*/
        
        
        //CompositeResidue root = FileUtils.parseGlydeRoot(Utils.fileToString(new File("/Users/durandal/Development/Qrator/Files/OFuc/fully-defined/05243.glyde.xml")));
        
        //System.out.println(root);
        
        //System.out.println(StructureService.listStructures("rejected", 6l, null, null, null, 0, 10));
        
    }
    
    
}
