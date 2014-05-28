package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.relation.QTreeCreatedByQUser;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.RelationM1SQL;
import edu.uga.qrator.obj.entity.QTree;
import edu.uga.qrator.obj.entity.QUser;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QTreeCreatedByQUserImpl extends RelationM1SQL<QTree,QUser> implements QTreeCreatedByQUser {

    public QTreeCreatedByQUserImpl(Connection conn){
        super(conn, QTreeCreatedByQUser.class);
    }

    @Override
    public Iterator<QTree> getCreateds(QUser creator, Filter<QTree> filter){
        return getFrom(creator, filter);
    }

    @Override
    public Iterator<QTree> getCreateds(QUser creator, Filter<QTree> filter, int offset, int limit){
        return getFrom(creator, filter, offset, limit);
    }

    @Override
    public QUser getCreator(QTree created){
        return getTo(created);
    }

    @Override
    public void setCreator(QTree created, QUser creator){
        add(created, creator);
    }

}