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
package edu.uga.qrator.service;

import edu.uga.qrator.logic.manage.*;
import edu.uga.qrator.obj.entity.*;
import edu.uga.qrator.util.QratorUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import persist.query.filter.Filter;

/**
 *
 * @author Matthew
 */
public class ServiceUtil {
    
    public static final String INVALID_SESSION = "Invalid session.  Please login again.";
    public static final String SUBMIT = "submit";
    public static final String REVIEW = "review";
    public static final String CURATE = "curate";
    public static final String ADMIN  = "admin";
    
    public static String formatDate(Date date){
        return QratorUtils.formatDate(date);
    }
    
    public static Map<String, Object> userState(QUser user){
        Map<String, Object> state = new HashMap<String, Object>();
        state.put("id", user.getId()+"");
        state.put("name", user.getName());
        state.put("username", user.getUsername());
        state.put("email", user.getEmail());
        state.put("createdOn", formatDate(user.getCreatedOn()));
        state.put("lastLogin", formatDate(user.getLastLogin()));
        state.put("active", Boolean.toString(user.isActive()));
        return state;
    }
    
    public static Map<String, Object> structureState(QStructure structure, StructureManager manager){
        QUser user = manager.getCreator(structure);
        QStructureType type = manager.getType(structure);
        QTree tree = manager.getTree(structure);
        Map<String, Object> state = new HashMap<String, Object>();
        state.put("id", structure.getId()+"");
        state.put("spec", structure.getSpec());
        state.put("uri", structure.getUri());
        state.put("version", structure.getVersion());
        state.put("status", structure.getStatus().toString());
        state.put("type", type.getName());
        state.put("typeId", type.getId());
        state.put("tree", tree.getName());
        state.put("treeId", tree.getId());
        state.put("createdBy", user.getName());
        state.put("createdOn", formatDate(structure.getCreatedOn()));
        return state;
    }
    
    public static Map<String, Object> referenceState(QReference reference, ReferenceManager manager){
        QUser creator = manager.getCreator(reference);
        QSource source = manager.getSource(reference);
        Map<String, Object> state = new HashMap<String, Object>();
        state.put("id", reference.getId()+"");
        state.put("source", source.getName());
        state.put("sourceURI", source.getUri());
        state.put("refId", reference.getSrcId());
        state.put("uri", reference.getUri());
        state.put("createdBy", creator.getName());
        state.put("createdOn", formatDate(reference.getCreatedOn()));
        state.put("modifiedOn", formatDate(reference.getModifiedOn()));
        return state;
    }
    
    public static Map<String, Object> annotationState(QAnnotation annotation, AnnotationManager manager){
        QUser creator = manager.getCreator(annotation);
        Map<String, Object> state = new HashMap<String, Object>();
        state.put("id", annotation.getId()+"");
        state.put("comment", annotation.getComment());
        state.put("uri", annotation.getUri());
        state.put("createdBy", creator.getName());
        state.put("createdOn", formatDate(annotation.getCreatedOn()));
        state.put("modifiedOn", formatDate(annotation.getModifiedOn()));
        return state;
    }
    
    public static Map<String, Object> provenanceState(QProvenance provenance, ProvenanceManager manager){
        QUser creator = manager.getCreator(provenance);
        Map<String, Object> state = new HashMap<String, Object>();
        state.put("id", provenance.getId()+"");
        state.put("action", provenance.getAction().toString());
        state.put("uri", provenance.getUri());
        state.put("createdBy", creator.getName());
        state.put("createdOn", formatDate(provenance.getCreatedOn()));
        state.put("modifiedOn", formatDate(provenance.getModifiedOn()));
        return state;
    }
    
    public static Map<String, Object> treeState(QTree tree, TreeManager manager){
        Map<String, Object> state = new HashMap<String, Object>();
        state.put("id", tree.getId()+"");
        state.put("name", tree.getName());
        state.put("createdOn", formatDate(tree.getCreatedOn()));
        state.put("modifiedOn", formatDate(tree.getModifiedOn()));
        return state;
    }
    
    public static Map<String, Object> typeState(QStructureType type, TypeManager manager){
        Map<String, Object> state = new HashMap<String, Object>();
        state.put("id", type.getId()+"");
        state.put("name", type.getName());
        state.put("createdOn", formatDate(type.getCreatedOn()));
        state.put("modifiedOn", formatDate(type.getModifiedOn()));
        return state;
    }
    
    public static Map<String, Object> sourceState(QSource source, SourceManager manager){
        Map<String, Object> state = new HashMap<String, Object>();
        state.put("id", source.getId()+"");
        state.put("name", source.getName());
        state.put("uri", source.getUri());
        state.put("createdOn", formatDate(source.getCreatedOn()));
        state.put("modifiedOn", formatDate(source.getModifiedOn()));
        return state;
    }
    
    public static QUser getUser(long id, UserManager manager){
        Filter<QUser> filter = new Filter<QUser>(QUser.class).eq("sid", id+"");
        Iterator<QUser> users = manager.list(filter);
        if(users.hasNext()) return users.next();
        else return null;
    }
    
    public static QStructure getStructure(long id, StructureManager manager){
        Filter<QStructure> filter = new Filter<QStructure>(QStructure.class).eq("sid", id+"");
        Iterator<QStructure> structs = manager.list(filter);
        if(structs.hasNext()) return structs.next();
        else return null;
    }
    
    public static QTree getTree(long id, TreeManager manager){
        Filter<QTree> filter = new Filter<QTree>(QTree.class).eq("sid", id+"");
        Iterator<QTree> trees = manager.list(filter);
        if(trees.hasNext()) return trees.next();
        else return null;
    }
    
    public static QSource getSource(long id, SourceManager manager){
        Filter<QSource> filter = new Filter<QSource>(QSource.class).eq("sid", id+"");
        Iterator<QSource> sources = manager.list(filter);
        if(sources.hasNext()) return sources.next();
        else return null;
    }
    
    public static QReference getReference(long id, ReferenceManager manager){
        Filter<QReference> filter = new Filter<QReference>(QReference.class).eq("sid", id+"");
        Iterator<QReference> references = manager.list(filter);
        if(references.hasNext()) return references.next();
        else return null;
    }
    
    public static QAnnotation getAnnotation(long id, AnnotationManager manager){
        Filter<QAnnotation> filter = new Filter<QAnnotation>(QAnnotation.class).eq("sid", id+"");
        Iterator<QAnnotation> annotations = manager.list(filter);
        if(annotations.hasNext()) return annotations.next();
        else return null;
    }
    
    public static QStructureType getType(long id, TypeManager manager){
        Filter<QStructureType> filter = new Filter<QStructureType>(QStructureType.class).eq("sid", id+"");
        Iterator<QStructureType> types = manager.listTypes(filter);
        if(types.hasNext()) return types.next();
        else return null;
    }
    
}
