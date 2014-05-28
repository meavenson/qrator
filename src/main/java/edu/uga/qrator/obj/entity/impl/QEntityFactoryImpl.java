package edu.uga.qrator.obj.entity.impl;

import java.sql.Connection;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.entity.manage.EntityManager;
import persist.entity.manage.sql.EntityManagerSQL;
import persist.entity.PEntity;
import persist.relation.PRelation;

import edu.uga.qrator.obj.entity.*;
import edu.uga.qrator.obj.relation.*;
import edu.uga.qrator.obj.entity.QStructure.ReviewStatus;
import edu.uga.qrator.obj.entity.QProvenance.ProvenanceAction;

/**
 * A factory for creating entities.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QEntityFactoryImpl implements QEntityFactory {
    
    private final EntityManager manager;
    
    
    protected QEntityFactoryImpl(Connection conn){
        manager = new EntityManagerSQL(conn);
        
    }

    public static QEntityFactory getFactory(Connection conn){
        return new QEntityFactoryImpl(conn);
    }

    @Override
    public QSource createQSource(String name, String uri, Date createdOn, Date modifiedOn, QUser createdby){
        QSource qsource = new QSourceImpl(name, uri, createdOn, modifiedOn);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        keys.put(QSourceCreatedByQUser.class, createdby);
        manager.add(qsource, keys);
        qsource = manager.get(qsource.getId(), QSource.class);
        return qsource;
    }

    @Override
    public QSource createQSource(String name, String uri, QUser createdby){
        QSource qsource = new QSourceImpl(name, uri);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        keys.put(QSourceCreatedByQUser.class, createdby);
        manager.add(qsource, keys);
        qsource = manager.get(qsource.getId(), QSource.class);
        return qsource;
    }

    @Override
    public Iterator<QSource> findSources(Filter<QSource> filter){
        if(filter != null){
            return manager.get(filter);
        }else return manager.get(QSource.class);
    }

    @Override
    public Iterator<QSource> findSources(Filter<QSource> filter, int offset, int limit){
        if(filter != null){
            return manager.get(filter, offset, limit);
        }else return manager.get(QSource.class, offset, limit);
    }

    @Override
    public void updateQSource(QSource qsource){
        
        manager.update(qsource);
    }

    @Override
    public void removeSources(Filter<QSource> filter){
        
        manager.remove(filter);
    }

    @Override
    public void removeQSource(QSource qsource){
        
        manager.remove(qsource);
    }

    @Override
    public long countSources(Filter<QSource> filter){
        
        return manager.count(filter);
    }

    @Override
    public QProvenance createQProvenance(ProvenanceAction action, String uri, Date createdOn, Date modifiedOn, QUser createdby, QStructure hasprovenance){
        QProvenance qprovenance = new QProvenanceImpl(action, uri, createdOn, modifiedOn);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        keys.put(QProvenanceCreatedByQUser.class, createdby);
        keys.put(QStructureHasProvenanceQProvenance.class, hasprovenance);
        manager.add(qprovenance, keys);
        qprovenance = manager.get(qprovenance.getId(), QProvenance.class);
        return qprovenance;
    }

    @Override
    public QProvenance createQProvenance(ProvenanceAction action, QUser createdby, QStructure hasprovenance){
        QProvenance qprovenance = new QProvenanceImpl(action);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        keys.put(QProvenanceCreatedByQUser.class, createdby);
        keys.put(QStructureHasProvenanceQProvenance.class, hasprovenance);
        manager.add(qprovenance, keys);
        qprovenance = manager.get(qprovenance.getId(), QProvenance.class);
        return qprovenance;
    }

    @Override
    public Iterator<QProvenance> findProvenances(Filter<QProvenance> filter){
        if(filter != null){
            return manager.get(filter);
        }else return manager.get(QProvenance.class);
    }

    @Override
    public Iterator<QProvenance> findProvenances(Filter<QProvenance> filter, int offset, int limit){
        if(filter != null){
            return manager.get(filter, offset, limit);
        }else return manager.get(QProvenance.class, offset, limit);
    }

    @Override
    public void updateQProvenance(QProvenance qprovenance){
        
        manager.update(qprovenance);
    }

    @Override
    public void removeProvenances(Filter<QProvenance> filter){
        
        manager.remove(filter);
    }

    @Override
    public void removeQProvenance(QProvenance qprovenance){
        
        manager.remove(qprovenance);
    }

    @Override
    public long countProvenances(Filter<QProvenance> filter){
        
        return manager.count(filter);
    }

    @Override
    public QUser createQUser(String username, String password, String name, String email, boolean active, Date createdOn, Date lastLogin){
        QUser quser = new QUserImpl(username, password, name, email, active, createdOn, lastLogin);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        manager.add(quser, keys);
        quser = manager.get(quser.getId(), QUser.class);
        return quser;
    }

    @Override
    public QUser createQUser(String username, String password, String name, String email){
        QUser quser = new QUserImpl(username, password, name, email);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        manager.add(quser, keys);
        quser = manager.get(quser.getId(), QUser.class);
        return quser;
    }

    @Override
    public Iterator<QUser> findUsers(Filter<QUser> filter){
        if(filter != null){
            return manager.get(filter);
        }else return manager.get(QUser.class);
    }

    @Override
    public Iterator<QUser> findUsers(Filter<QUser> filter, int offset, int limit){
        if(filter != null){
            return manager.get(filter, offset, limit);
        }else return manager.get(QUser.class, offset, limit);
    }

    @Override
    public void updateQUser(QUser quser){
        
        manager.update(quser);
    }

    @Override
    public void removeUsers(Filter<QUser> filter){
        
        manager.remove(filter);
    }

    @Override
    public void removeQUser(QUser quser){
        
        manager.remove(quser);
    }

    @Override
    public long countUsers(Filter<QUser> filter){
        
        return manager.count(filter);
    }

    @Override
    public QStructureType createQStructureType(String name, String description, String glycoName, Date createdOn, Date modifiedOn, QUser createdby, QTree intree){
        QStructureType qstructuretype = new QStructureTypeImpl(name, description, glycoName, createdOn, modifiedOn);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        keys.put(QStructureTypeCreatedByQUser.class, createdby);
        keys.put(QStructureTypeInTreeQTree.class, intree);
        manager.add(qstructuretype, keys);
        qstructuretype = manager.get(qstructuretype.getId(), QStructureType.class);
        return qstructuretype;
    }

    @Override
    public QStructureType createQStructureType(String name, String glycoName, QUser createdby, QTree intree){
        QStructureType qstructuretype = new QStructureTypeImpl(name, glycoName);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        keys.put(QStructureTypeCreatedByQUser.class, createdby);
        keys.put(QStructureTypeInTreeQTree.class, intree);
        manager.add(qstructuretype, keys);
        qstructuretype = manager.get(qstructuretype.getId(), QStructureType.class);
        return qstructuretype;
    }

    @Override
    public Iterator<QStructureType> findStructureTypes(Filter<QStructureType> filter){
        if(filter != null){
            return manager.get(filter);
        }else return manager.get(QStructureType.class);
    }

    @Override
    public Iterator<QStructureType> findStructureTypes(Filter<QStructureType> filter, int offset, int limit){
        if(filter != null){
            return manager.get(filter, offset, limit);
        }else return manager.get(QStructureType.class, offset, limit);
    }

    @Override
    public void updateQStructureType(QStructureType qstructuretype){
        
        manager.update(qstructuretype);
    }

    @Override
    public void removeStructureTypes(Filter<QStructureType> filter){
        
        manager.remove(filter);
    }

    @Override
    public void removeQStructureType(QStructureType qstructuretype){
        
        manager.remove(qstructuretype);
    }

    @Override
    public long countStructureTypes(Filter<QStructureType> filter){
        
        return manager.count(filter);
    }

    @Override
    public QTree createQTree(String name, String description, String spec, Date createdOn, Date modifiedOn, QUser createdby){
        QTree qtree = new QTreeImpl(name, description, spec, createdOn, modifiedOn);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        keys.put(QTreeCreatedByQUser.class, createdby);
        manager.add(qtree, keys);
        qtree = manager.get(qtree.getId(), QTree.class);
        return qtree;
    }

    @Override
    public QTree createQTree(String name, String spec, QUser createdby){
        QTree qtree = new QTreeImpl(name, spec);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        keys.put(QTreeCreatedByQUser.class, createdby);
        manager.add(qtree, keys);
        qtree = manager.get(qtree.getId(), QTree.class);
        return qtree;
    }

    @Override
    public Iterator<QTree> findTrees(Filter<QTree> filter){
        if(filter != null){
            return manager.get(filter);
        }else return manager.get(QTree.class);
    }

    @Override
    public Iterator<QTree> findTrees(Filter<QTree> filter, int offset, int limit){
        if(filter != null){
            return manager.get(filter, offset, limit);
        }else return manager.get(QTree.class, offset, limit);
    }

    @Override
    public void updateQTree(QTree qtree){
        
        manager.update(qtree);
    }

    @Override
    public void removeTrees(Filter<QTree> filter){
        
        manager.remove(filter);
    }

    @Override
    public void removeQTree(QTree qtree){
        
        manager.remove(qtree);
    }

    @Override
    public long countTrees(Filter<QTree> filter){
        
        return manager.count(filter);
    }

    @Override
    public QStructure createQStructure(String filename, String hash, String spec, String contents, ReviewStatus status, String version, String uri, Date createdOn, Date modifiedOn, QUser createdby, QStructureType hastype){
        QStructure qstructure = new QStructureImpl(filename, hash, spec, contents, status, version, uri, createdOn, modifiedOn);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        keys.put(QStructureCreatedByQUser.class, createdby);
        keys.put(QStructureHasTypeQStructureType.class, hastype);
        manager.add(qstructure, keys);
        qstructure = manager.get(qstructure.getId(), QStructure.class);
        return qstructure;
    }

    @Override
    public QStructure createQStructure(String filename, String hash, String spec, String contents, ReviewStatus status, QUser createdby, QStructureType hastype){
        QStructure qstructure = new QStructureImpl(filename, hash, spec, contents, status);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        keys.put(QStructureCreatedByQUser.class, createdby);
        keys.put(QStructureHasTypeQStructureType.class, hastype);
        manager.add(qstructure, keys);
        qstructure = manager.get(qstructure.getId(), QStructure.class);
        return qstructure;
    }

    @Override
    public Iterator<QStructure> findStructures(Filter<QStructure> filter){
        if(filter != null){
            return manager.get(filter);
        }else return manager.get(QStructure.class);
    }

    @Override
    public Iterator<QStructure> findStructures(Filter<QStructure> filter, int offset, int limit){
        if(filter != null){
            return manager.get(filter, offset, limit);
        }else return manager.get(QStructure.class, offset, limit);
    }

    @Override
    public void updateQStructure(QStructure qstructure){
        
        manager.update(qstructure);
    }

    @Override
    public void removeStructures(Filter<QStructure> filter){
        
        manager.remove(filter);
    }

    @Override
    public void removeQStructure(QStructure qstructure){
        
        manager.remove(qstructure);
    }

    @Override
    public long countStructures(Filter<QStructure> filter){
        
        return manager.count(filter);
    }

    @Override
    public QAnnotation createQAnnotation(String comment, String uri, Date createdOn, Date modifiedOn, QUser createdby, QStructure hasannotation){
        QAnnotation qannotation = new QAnnotationImpl(comment, uri, createdOn, modifiedOn);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        keys.put(QAnnotationCreatedByQUser.class, createdby);
        keys.put(QStructureHasAnnotationQAnnotation.class, hasannotation);
        manager.add(qannotation, keys);
        qannotation = manager.get(qannotation.getId(), QAnnotation.class);
        return qannotation;
    }

    @Override
    public QAnnotation createQAnnotation(String comment, QUser createdby, QStructure hasannotation){
        QAnnotation qannotation = new QAnnotationImpl(comment);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        keys.put(QAnnotationCreatedByQUser.class, createdby);
        keys.put(QStructureHasAnnotationQAnnotation.class, hasannotation);
        manager.add(qannotation, keys);
        qannotation = manager.get(qannotation.getId(), QAnnotation.class);
        return qannotation;
    }

    @Override
    public Iterator<QAnnotation> findAnnotations(Filter<QAnnotation> filter){
        if(filter != null){
            return manager.get(filter);
        }else return manager.get(QAnnotation.class);
    }

    @Override
    public Iterator<QAnnotation> findAnnotations(Filter<QAnnotation> filter, int offset, int limit){
        if(filter != null){
            return manager.get(filter, offset, limit);
        }else return manager.get(QAnnotation.class, offset, limit);
    }

    @Override
    public void updateQAnnotation(QAnnotation qannotation){
        
        manager.update(qannotation);
    }

    @Override
    public void removeAnnotations(Filter<QAnnotation> filter){
        
        manager.remove(filter);
    }

    @Override
    public void removeQAnnotation(QAnnotation qannotation){
        
        manager.remove(qannotation);
    }

    @Override
    public long countAnnotations(Filter<QAnnotation> filter){
        
        return manager.count(filter);
    }

    @Override
    public QRole createQRole(String name, Date createdOn, Date modifiedOn, QUser createdby){
        QRole qrole = new QRoleImpl(name, createdOn, modifiedOn);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        keys.put(QRoleCreatedByQUser.class, createdby);
        manager.add(qrole, keys);
        qrole = manager.get(qrole.getId(), QRole.class);
        return qrole;
    }

    @Override
    public QRole createQRole(String name, QUser createdby){
        QRole qrole = new QRoleImpl(name);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        keys.put(QRoleCreatedByQUser.class, createdby);
        manager.add(qrole, keys);
        qrole = manager.get(qrole.getId(), QRole.class);
        return qrole;
    }

    @Override
    public Iterator<QRole> findRoles(Filter<QRole> filter){
        if(filter != null){
            return manager.get(filter);
        }else return manager.get(QRole.class);
    }

    @Override
    public Iterator<QRole> findRoles(Filter<QRole> filter, int offset, int limit){
        if(filter != null){
            return manager.get(filter, offset, limit);
        }else return manager.get(QRole.class, offset, limit);
    }

    @Override
    public void updateQRole(QRole qrole){
        
        manager.update(qrole);
    }

    @Override
    public void removeRoles(Filter<QRole> filter){
        
        manager.remove(filter);
    }

    @Override
    public void removeQRole(QRole qrole){
        
        manager.remove(qrole);
    }

    @Override
    public long countRoles(Filter<QRole> filter){
        
        return manager.count(filter);
    }

    @Override
    public QReference createQReference(String srcId, String uri, Date createdOn, Date modifiedOn, QUser createdby, QSource hassource, QStructure hasreference){
        QReference qreference = new QReferenceImpl(srcId, uri, createdOn, modifiedOn);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        keys.put(QReferenceCreatedByQUser.class, createdby);
        keys.put(QReferenceHasSourceQSource.class, hassource);
        keys.put(QStructureHasReferenceQReference.class, hasreference);
        manager.add(qreference, keys);
        qreference = manager.get(qreference.getId(), QReference.class);
        return qreference;
    }

    @Override
    public QReference createQReference(String srcId, QUser createdby, QSource hassource, QStructure hasreference){
        QReference qreference = new QReferenceImpl(srcId);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
        keys.put(QReferenceCreatedByQUser.class, createdby);
        keys.put(QReferenceHasSourceQSource.class, hassource);
        keys.put(QStructureHasReferenceQReference.class, hasreference);
        manager.add(qreference, keys);
        qreference = manager.get(qreference.getId(), QReference.class);
        return qreference;
    }

    @Override
    public Iterator<QReference> findReferences(Filter<QReference> filter){
        if(filter != null){
            return manager.get(filter);
        }else return manager.get(QReference.class);
    }

    @Override
    public Iterator<QReference> findReferences(Filter<QReference> filter, int offset, int limit){
        if(filter != null){
            return manager.get(filter, offset, limit);
        }else return manager.get(QReference.class, offset, limit);
    }

    @Override
    public void updateQReference(QReference qreference){
        
        manager.update(qreference);
    }

    @Override
    public void removeReferences(Filter<QReference> filter){
        
        manager.remove(filter);
    }

    @Override
    public void removeQReference(QReference qreference){
        
        manager.remove(qreference);
    }

    @Override
    public long countReferences(Filter<QReference> filter){
        
        return manager.count(filter);
    }

}