package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.relation.QReferenceCreatedByQUser;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.RelationM1SQL;
import edu.uga.qrator.obj.entity.QReference;
import edu.uga.qrator.obj.entity.QUser;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QReferenceCreatedByQUserImpl extends RelationM1SQL<QReference,QUser> implements QReferenceCreatedByQUser {

    public QReferenceCreatedByQUserImpl(Connection conn){
        super(conn, QReferenceCreatedByQUser.class);
    }

    @Override
    public Iterator<QReference> getCreateds(QUser creator, Filter<QReference> filter){
        return getFrom(creator, filter);
    }

    @Override
    public Iterator<QReference> getCreateds(QUser creator, Filter<QReference> filter, int offset, int limit){
        return getFrom(creator, filter, offset, limit);
    }

    @Override
    public QUser getCreator(QReference created){
        return getTo(created);
    }

    @Override
    public void setCreator(QReference created, QUser creator){
        add(created, creator);
    }

}