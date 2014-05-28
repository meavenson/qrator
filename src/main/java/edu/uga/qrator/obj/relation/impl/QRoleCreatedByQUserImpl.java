package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.relation.QRoleCreatedByQUser;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.RelationM1SQL;
import edu.uga.qrator.obj.entity.QRole;
import edu.uga.qrator.obj.entity.QUser;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QRoleCreatedByQUserImpl extends RelationM1SQL<QRole,QUser> implements QRoleCreatedByQUser {

    public QRoleCreatedByQUserImpl(Connection conn){
        super(conn, QRoleCreatedByQUser.class);
    }

    @Override
    public Iterator<QRole> getCreateds(QUser creator, Filter<QRole> filter){
        return getFrom(creator, filter);
    }

    @Override
    public Iterator<QRole> getCreateds(QUser creator, Filter<QRole> filter, int offset, int limit){
        return getFrom(creator, filter, offset, limit);
    }

    @Override
    public QUser getCreator(QRole created){
        return getTo(created);
    }

    @Override
    public void setCreator(QRole created, QUser creator){
        add(created, creator);
    }

}