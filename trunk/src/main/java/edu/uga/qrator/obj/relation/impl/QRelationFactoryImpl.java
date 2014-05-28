package edu.uga.qrator.obj.relation.impl;

import java.sql.Connection;
import edu.uga.qrator.obj.relation.*;

/**
 * A factory for creating relations.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QRelationFactoryImpl implements QRelationFactory {
    
    private final Connection conn;

    /**
    * Construct a QRelationFactoryImpl.
    */
    protected QRelationFactoryImpl(Connection conn){
        this.conn = conn;
    }

    public static QRelationFactory getFactory(Connection conn){
        return new QRelationFactoryImpl(conn);
    }

    @Override
    public QStructureTypeCreatedByQUser getQStructureTypeCreatedByQUser(){
        return new QStructureTypeCreatedByQUserImpl(conn);
    }

    @Override
    public QAnnotationCreatedByQUser getQAnnotationCreatedByQUser(){
        return new QAnnotationCreatedByQUserImpl(conn);
    }

    @Override
    public QReferenceCreatedByQUser getQReferenceCreatedByQUser(){
        return new QReferenceCreatedByQUserImpl(conn);
    }

    @Override
    public QTreeCreatedByQUser getQTreeCreatedByQUser(){
        return new QTreeCreatedByQUserImpl(conn);
    }

    @Override
    public QSourceCreatedByQUser getQSourceCreatedByQUser(){
        return new QSourceCreatedByQUserImpl(conn);
    }

    @Override
    public QRoleCreatedByQUser getQRoleCreatedByQUser(){
        return new QRoleCreatedByQUserImpl(conn);
    }

    @Override
    public QStructureCreatedByQUser getQStructureCreatedByQUser(){
        return new QStructureCreatedByQUserImpl(conn);
    }

    @Override
    public QProvenanceCreatedByQUser getQProvenanceCreatedByQUser(){
        return new QProvenanceCreatedByQUserImpl(conn);
    }

    @Override
    public QUserTracksQStructure getQUserTracksQStructure(){
        return new QUserTracksQStructureImpl(conn);
    }

    @Override
    public QStructureHasTypeQStructureType getQStructureHasTypeQStructureType(){
        return new QStructureHasTypeQStructureTypeImpl(conn);
    }

    @Override
    public QStructureTypeInTreeQTree getQStructureTypeInTreeQTree(){
        return new QStructureTypeInTreeQTreeImpl(conn);
    }

    @Override
    public QReferenceHasSourceQSource getQReferenceHasSourceQSource(){
        return new QReferenceHasSourceQSourceImpl(conn);
    }

    @Override
    public QStructureHasReferenceQReference getQStructureHasReferenceQReference(){
        return new QStructureHasReferenceQReferenceImpl(conn);
    }

    @Override
    public QStructureHasAnnotationQAnnotation getQStructureHasAnnotationQAnnotation(){
        return new QStructureHasAnnotationQAnnotationImpl(conn);
    }

    @Override
    public QStructureHasProvenanceQProvenance getQStructureHasProvenanceQProvenance(){
        return new QStructureHasProvenanceQProvenanceImpl(conn);
    }

    @Override
    public QUserHasRoleQRole getQUserHasRoleQRole(){
        return new QUserHasRoleQRoleImpl(conn);
    }

}