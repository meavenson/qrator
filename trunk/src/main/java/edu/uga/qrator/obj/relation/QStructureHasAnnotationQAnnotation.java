package edu.uga.qrator.obj.relation;

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.Relation1M;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QAnnotation;

/**
 * A one-to-many relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QStructureHasAnnotationQAnnotation extends Relation1M<QStructure,QAnnotation> {

    Iterator<QAnnotation> getAnnotations(QStructure structure, Filter<QAnnotation> filter);

    Iterator<QAnnotation> getAnnotations(QStructure structure, Filter<QAnnotation> filter, int offset, int limit);

    QStructure getStructure(QAnnotation annotation);

    void setStructure(QStructure structure, QAnnotation annotation);

}