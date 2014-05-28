package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.relation.QReferenceHasSourceQSource;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.RelationM1SQL;
import edu.uga.qrator.obj.entity.QReference;
import edu.uga.qrator.obj.entity.QSource;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QReferenceHasSourceQSourceImpl extends RelationM1SQL<QReference,QSource> implements QReferenceHasSourceQSource {

    public QReferenceHasSourceQSourceImpl(Connection conn){
        super(conn, QReferenceHasSourceQSource.class);
    }

    @Override
    public Iterator<QReference> getReferences(QSource source, Filter<QReference> filter){
        return getFrom(source, filter);
    }

    @Override
    public Iterator<QReference> getReferences(QSource source, Filter<QReference> filter, int offset, int limit){
        return getFrom(source, filter, offset, limit);
    }

    @Override
    public QSource getSource(QReference reference){
        return getTo(reference);
    }

    @Override
    public void setSource(QReference reference, QSource source){
        add(reference, source);
    }

}