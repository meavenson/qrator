package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.relation.QProvenanceCreatedByQUser;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.RelationM1SQL;
import edu.uga.qrator.obj.entity.QProvenance;
import edu.uga.qrator.obj.entity.QUser;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QProvenanceCreatedByQUserImpl extends RelationM1SQL<QProvenance,QUser> implements QProvenanceCreatedByQUser {

    public QProvenanceCreatedByQUserImpl(Connection conn){
        super(conn, QProvenanceCreatedByQUser.class);
    }

    @Override
    public Iterator<QProvenance> getCreateds(QUser creator, Filter<QProvenance> filter){
        return getFrom(creator, filter);
    }

    @Override
    public Iterator<QProvenance> getCreateds(QUser creator, Filter<QProvenance> filter, int offset, int limit){
        return getFrom(creator, filter, offset, limit);
    }

    @Override
    public QUser getCreator(QProvenance created){
        return getTo(created);
    }

    @Override
    public void setCreator(QProvenance created, QUser creator){
        add(created, creator);
    }

}