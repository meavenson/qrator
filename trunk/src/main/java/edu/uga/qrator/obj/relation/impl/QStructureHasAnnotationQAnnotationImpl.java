package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.relation.QStructureHasAnnotationQAnnotation;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.Relation1MSQL;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QAnnotation;

/**
 * A one-to-many relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QStructureHasAnnotationQAnnotationImpl extends Relation1MSQL<QStructure,QAnnotation> implements QStructureHasAnnotationQAnnotation {

    public QStructureHasAnnotationQAnnotationImpl(Connection conn){
        super(conn, QStructureHasAnnotationQAnnotation.class);
    }

    @Override
    public Iterator<QAnnotation> getAnnotations(QStructure structure, Filter<QAnnotation> filter){
        return getTo(structure, filter);
    }

    @Override
    public Iterator<QAnnotation> getAnnotations(QStructure structure, Filter<QAnnotation> filter, int offset, int limit){
        return getTo(structure, filter, offset, limit);
    }

    @Override
    public QStructure getStructure(QAnnotation annotation){
        return getFrom(annotation);
    }

    @Override
    public void setStructure(QStructure structure, QAnnotation annotation){
        add(structure, annotation);
    }

}