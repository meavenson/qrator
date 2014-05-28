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

import edu.uga.qrator.logic.io.StructureExporter;
import edu.uga.qrator.logic.io.OntologyClient;
import edu.uga.glydeII.gom.CompositeResidue;
import edu.uga.qrator.except.QException;
import edu.uga.qrator.logic.QConfiguration;
import edu.uga.qrator.logic.manage.GlydeStructure.GlydeResidue;
import edu.uga.qrator.obj.entity.QEntityFactory;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QStructureType;
import edu.uga.qrator.obj.entity.QTree;
import edu.uga.qrator.obj.entity.QUser;
import edu.uga.qrator.obj.entity.impl.QEntityFactoryImpl;
import edu.uga.qrator.obj.relation.QRelationFactory;
import edu.uga.qrator.obj.relation.impl.QRelationFactoryImpl;
import edu.uga.qrator.util.FileUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import persist.query.filter.Filter;

/**
 *
 * @author Matthew
 */
public class TreeManager {
    
    private final QEntityFactory efac;
    private final QRelationFactory rfac;
    
    public TreeManager(Connection conn){
        efac = QEntityFactoryImpl.getFactory(conn);
        rfac = QRelationFactoryImpl.getFactory(conn);
    }
    
    // parses the motif file
    public static void loadTrees(Connection conn) throws IOException{
        
        TreeManager tManager = new TreeManager(conn);
        
        if(QConfiguration.APIENABLED){
            OntologyClient.loadTrees(tManager);
        }else{
            ClassLoader loader = TreeManager.class.getClassLoader();
            if(loader==null)
                loader = ClassLoader.getSystemClassLoader();
            
            URL treeFile = (URL) loader.getResource(QConfiguration.TREEFILE);
            InputStreamReader isr = new InputStreamReader(treeFile.openStream());
            JSONArray arr = (JSONArray) JSONValue.parse(isr);

            for(Object o: arr){
                JSONObject obj = (JSONObject) o;
                String name = obj.get("name").toString();
                String file = obj.get("file").toString();
                QTree tree = tManager.get(name);
                URL resource = loader.getResource(file);
                if(resource != null){
                    InputStream glyde = resource.openStream();
                    CompositeResidue root = FileUtils.parseGlydeRoot(glyde);
                    String spec = JSONValue.toJSONString(GlydeStructure.getGlydeStructure(root).toMap());
                    if(tree != null){
                        tree.setSpec(spec);
                        tManager.update(tree);
                    }
                }else{
                    System.err.println("WARNING: Could not load canonical tree - "+file);
                }
            }
        }
    }
    
    public static CompositeResidue getRoot(QTree tree){
        if(tree != null){
            return StructureExporter.getRoot(StructureExporter.convertTree(tree));
        }
        return null;
    }
    
    public int updateTree(QStructure struct){
        QStructureType type = rfac.getQStructureHasTypeQStructureType().getType(struct);
        QTree tree = rfac.getQStructureTypeInTreeQTree().getTree(type);
        Map<String,Object> structMap = (Map<String,Object>) JSONValue.parse(struct.getSpec());
        Map<String,Object> treeMap = (Map<String,Object>) JSONValue.parse(tree.getSpec());
        GlydeStructure structGlyde = GlydeStructure.getGlydeStructure(structMap);
        GlydeStructure treeGlyde = GlydeStructure.getGlydeStructure(treeMap);
        
        //System.out.println("TREE ("+tree.getName()+") BEFORE - "+treeGlyde.residueCount()+" residues.");
        boolean match = true;
        if(!structGlyde.root.id.equals(treeGlyde.root.id) ||
           !structGlyde.root.anomer.equals(treeGlyde.root.anomer) ||
           (structGlyde.root.linkNum != null && !structGlyde.root.linkNum.equals(treeGlyde.root.linkNum)) ||
           (structGlyde.root.from != null && !structGlyde.root.from.equals(treeGlyde.root.from)) ||
           (structGlyde.root.to != null && !structGlyde.root.to.equals(treeGlyde.root.to)) ) match = false;
        if(!match){
            throw new QException("Root residues don't match");
            //System.out.println("Root residues don't match");
        }else{
            int added = updateNode(structGlyde.root, treeGlyde.root);
            tree.setSpec(JSONValue.toJSONString(treeGlyde.toMap()));
            update(tree);
            return added;
        }
    }
    
    private int updateNode(GlydeResidue struct, GlydeResidue tree){
        List<GlydeResidue> treeChildren = tree.getChildren();
        int count = 0;
        for(GlydeResidue structChild : struct.getChildren()){
            GlydeResidue found = null;
            for(GlydeResidue treeChild : treeChildren){
                //System.out.println(structChild.id+" - "+structChild.anomer+" - "+structChild.linkNum+" - "+structChild.from+" - "+structChild.to);
                if(structChild.id.equals(treeChild.id)           &&
                   (structChild.anomer != null && structChild.anomer.equals(treeChild.anomer))   &&
                   structChild.linkNum.equals(treeChild.linkNum) &&
                   structChild.from.equals(treeChild.from)       &&
                   structChild.to.equals(treeChild.to)){
                    found = treeChild;
                    break;
                }
            }
            if(found == null){
                found = new GlydeResidue(structChild.id,
                                         structChild.anomer,
                                         structChild.linkNum,
                                         structChild.from,
                                         structChild.to);
                tree.addChild(found);
                //System.out.println("     added "+found.id);
                count++;
            }
            count += updateNode(structChild, found);
        }
        return count;
    }
    
    public CompositeResidue getRoot(String name){
        QTree tree = get(name);
        return getRoot(tree);
    }
    
    public QTree get(String name){
        Filter<QTree> filter = new Filter<QTree>(QTree.class).eq("name", name);
        Iterator<QTree> trees = efac.findTrees(filter);
        if(trees.hasNext()) return trees.next();
        return null;
    }
    
    public QTree create(String name, String spec, QUser creator){
        return efac.createQTree(name, spec, creator);
    }
    
    public void update(QTree tree){
        efac.updateQTree(tree);
    }
    
    public void remove(QTree tree){
        efac.removeQTree(tree);
    }
    
    public Iterator<QTree> list(int offset, int limit){
        return efac.findTrees(null, offset, limit);
    }
    
    public Iterator<QTree> list(Filter<QTree> filter){
        return efac.findTrees(filter);
    }
    
    public Iterator<QTree> list(){
        return efac.findTrees(null);
    }
}
