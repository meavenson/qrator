package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.relation.QStructureHasReferenceQReference;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.Relation1MSQL;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QReference;

/**
 * A one-to-many relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QStructureHasReferenceQReferenceImpl extends Relation1MSQL<QStructure,QReference> implements QStructureHasReferenceQReference {

    public QStructureHasReferenceQReferenceImpl(Connection conn){
        super(conn, QStructureHasReferenceQReference.class);
    }

    @Override
    public Iterator<QReference> getReferences(QStructure structure, Filter<QReference> filter){
        return getTo(structure, filter);
    }

    @Override
    public Iterator<QReference> getReferences(QStructure structure, Filter<QReference> filter, int offset, int limit){
        return getTo(structure, filter, offset, limit);
    }

    @Override
    public QStructure getStructure(QReference reference){
        return getFrom(reference);
    }

    @Override
    public void setStructure(QStructure structure, QReference reference){
        add(structure, reference);
    }

}