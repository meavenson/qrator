package edu.uga.qrator.obj.entity;

import persist.query.filter.Filter;
import java.util.Date;
import java.util.Iterator;
import edu.uga.qrator.obj.entity.QStructure.ReviewStatus;
import edu.uga.qrator.obj.entity.QProvenance.ProvenanceAction;

/**
 * A factory for creating entities.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QEntityFactory {

    QSource createQSource(String name, String uri, Date createdOn, Date modifiedOn, QUser createdby);

    QSource createQSource(String name, String uri, QUser createdby);

    Iterator<QSource> findSources(Filter<QSource> filter);

    Iterator<QSource> findSources(Filter<QSource> filter, int offset, int limit);

    void updateQSource(QSource qsource);

    void removeSources(Filter<QSource> filter);

    void removeQSource(QSource qsource);

    long countSources(Filter<QSource> filter);


    QProvenance createQProvenance(ProvenanceAction action, String uri, Date createdOn, Date modifiedOn, QUser createdby, QStructure hasprovenance);

    QProvenance createQProvenance(ProvenanceAction action, QUser createdby, QStructure hasprovenance);

    Iterator<QProvenance> findProvenances(Filter<QProvenance> filter);

    Iterator<QProvenance> findProvenances(Filter<QProvenance> filter, int offset, int limit);

    void updateQProvenance(QProvenance qprovenance);

    void removeProvenances(Filter<QProvenance> filter);

    void removeQProvenance(QProvenance qprovenance);

    long countProvenances(Filter<QProvenance> filter);


    QUser createQUser(String username, String password, String name, String email, boolean active, Date createdOn, Date lastLogin);

    QUser createQUser(String username, String password, String name, String email);

    Iterator<QUser> findUsers(Filter<QUser> filter);

    Iterator<QUser> findUsers(Filter<QUser> filter, int offset, int limit);

    void updateQUser(QUser quser);

    void removeUsers(Filter<QUser> filter);

    void removeQUser(QUser quser);

    long countUsers(Filter<QUser> filter);


    QStructureType createQStructureType(String name, String description, String glycoName, Date createdOn, Date modifiedOn, QUser createdby, QTree intree);

    QStructureType createQStructureType(String name, String glycoName, QUser createdby, QTree intree);

    Iterator<QStructureType> findStructureTypes(Filter<QStructureType> filter);

    Iterator<QStructureType> findStructureTypes(Filter<QStructureType> filter, int offset, int limit);

    void updateQStructureType(QStructureType qstructuretype);

    void removeStructureTypes(Filter<QStructureType> filter);

    void removeQStructureType(QStructureType qstructuretype);

    long countStructureTypes(Filter<QStructureType> filter);


    QTree createQTree(String name, String description, String spec, Date createdOn, Date modifiedOn, QUser createdby);

    QTree createQTree(String name, String spec, QUser createdby);

    Iterator<QTree> findTrees(Filter<QTree> filter);

    Iterator<QTree> findTrees(Filter<QTree> filter, int offset, int limit);

    void updateQTree(QTree qtree);

    void removeTrees(Filter<QTree> filter);

    void removeQTree(QTree qtree);

    long countTrees(Filter<QTree> filter);


    QStructure createQStructure(String filename, String hash, String spec, String contents, ReviewStatus status, String version, String uri, Date createdOn, Date modifiedOn, QUser createdby, QStructureType hastype);

    QStructure createQStructure(String filename, String hash, String spec, String contents, ReviewStatus status, QUser createdby, QStructureType hastype);

    Iterator<QStructure> findStructures(Filter<QStructure> filter);

    Iterator<QStructure> findStructures(Filter<QStructure> filter, int offset, int limit);

    void updateQStructure(QStructure qstructure);

    void removeStructures(Filter<QStructure> filter);

    void removeQStructure(QStructure qstructure);

    long countStructures(Filter<QStructure> filter);


    QAnnotation createQAnnotation(String comment, String uri, Date createdOn, Date modifiedOn, QUser createdby, QStructure hasannotation);

    QAnnotation createQAnnotation(String comment, QUser createdby, QStructure hasannotation);

    Iterator<QAnnotation> findAnnotations(Filter<QAnnotation> filter);

    Iterator<QAnnotation> findAnnotations(Filter<QAnnotation> filter, int offset, int limit);

    void updateQAnnotation(QAnnotation qannotation);

    void removeAnnotations(Filter<QAnnotation> filter);

    void removeQAnnotation(QAnnotation qannotation);

    long countAnnotations(Filter<QAnnotation> filter);


    QRole createQRole(String name, Date createdOn, Date modifiedOn, QUser createdby);

    QRole createQRole(String name, QUser createdby);

    Iterator<QRole> findRoles(Filter<QRole> filter);

    Iterator<QRole> findRoles(Filter<QRole> filter, int offset, int limit);

    void updateQRole(QRole qrole);

    void removeRoles(Filter<QRole> filter);

    void removeQRole(QRole qrole);

    long countRoles(Filter<QRole> filter);


    QReference createQReference(String srcId, String uri, Date createdOn, Date modifiedOn, QUser createdby, QSource hassource, QStructure hasreference);

    QReference createQReference(String srcId, QUser createdby, QSource hassource, QStructure hasreference);

    Iterator<QReference> findReferences(Filter<QReference> filter);

    Iterator<QReference> findReferences(Filter<QReference> filter, int offset, int limit);

    void updateQReference(QReference qreference);

    void removeReferences(Filter<QReference> filter);

    void removeQReference(QReference qreference);

    long countReferences(Filter<QReference> filter);

}