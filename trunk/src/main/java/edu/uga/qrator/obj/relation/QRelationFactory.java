package edu.uga.qrator.obj.relation;

/**
 * A factory for creating relations.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QRelationFactory {

    QStructureTypeCreatedByQUser getQStructureTypeCreatedByQUser();

    QAnnotationCreatedByQUser getQAnnotationCreatedByQUser();

    QReferenceCreatedByQUser getQReferenceCreatedByQUser();

    QTreeCreatedByQUser getQTreeCreatedByQUser();

    QSourceCreatedByQUser getQSourceCreatedByQUser();

    QRoleCreatedByQUser getQRoleCreatedByQUser();

    QStructureCreatedByQUser getQStructureCreatedByQUser();

    QProvenanceCreatedByQUser getQProvenanceCreatedByQUser();

    QUserTracksQStructure getQUserTracksQStructure();

    QStructureHasTypeQStructureType getQStructureHasTypeQStructureType();

    QStructureTypeInTreeQTree getQStructureTypeInTreeQTree();

    QReferenceHasSourceQSource getQReferenceHasSourceQSource();

    QStructureHasReferenceQReference getQStructureHasReferenceQReference();

    QStructureHasAnnotationQAnnotation getQStructureHasAnnotationQAnnotation();

    QStructureHasProvenanceQProvenance getQStructureHasProvenanceQProvenance();

    QUserHasRoleQRole getQUserHasRoleQRole();
	
}