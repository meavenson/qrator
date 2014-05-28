package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.relation.QAnnotationCreatedByQUser;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.RelationM1SQL;
import edu.uga.qrator.obj.entity.QAnnotation;
import edu.uga.qrator.obj.entity.QUser;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QAnnotationCreatedByQUserImpl extends RelationM1SQL<QAnnotation,QUser> implements QAnnotationCreatedByQUser {

    public QAnnotationCreatedByQUserImpl(Connection conn){
        super(conn, QAnnotationCreatedByQUser.class);
    }

    @Override
    public Iterator<QAnnotation> getCreateds(QUser creator, Filter<QAnnotation> filter){
        return getFrom(creator, filter);
    }

    @Override
    public Iterator<QAnnotation> getCreateds(QUser creator, Filter<QAnnotation> filter, int offset, int limit){
        return getFrom(creator, filter, offset, limit);
    }

    @Override
    public QUser getCreator(QAnnotation created){
        return getTo(created);
    }

    @Override
    public void setCreator(QAnnotation created, QUser creator){
        add(created, creator);
    }

}