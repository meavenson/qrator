package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.relation.QSourceCreatedByQUser;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.RelationM1SQL;
import edu.uga.qrator.obj.entity.QUser;
import edu.uga.qrator.obj.entity.QSource;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QSourceCreatedByQUserImpl extends RelationM1SQL<QSource,QUser> implements QSourceCreatedByQUser {

    public QSourceCreatedByQUserImpl(Connection conn){
        super(conn, QSourceCreatedByQUser.class);
    }

    @Override
    public Iterator<QSource> getCreateds(QUser creator, Filter<QSource> filter){
        return getFrom(creator, filter);
    }

    @Override
    public Iterator<QSource> getCreateds(QUser creator, Filter<QSource> filter, int offset, int limit){
        return getFrom(creator, filter, offset, limit);
    }

    @Override
    public QUser getCreator(QSource created){
        return getTo(created);
    }

    @Override
    public void setCreator(QSource created, QUser creator){
        add(created, creator);
    }

}