package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.relation.QUserHasRoleQRole;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.RelationMMSQL;
import edu.uga.qrator.obj.entity.QRole;
import edu.uga.qrator.obj.entity.QUser;

/**
 * A many-to-many relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QUserHasRoleQRoleImpl extends RelationMMSQL<QUser,QRole> implements QUserHasRoleQRole{

    public QUserHasRoleQRoleImpl(Connection conn){
        super(conn, QUserHasRoleQRole.class);
    }

    @Override
    public Iterator<QUser> getAccounts(QRole role, Filter<QUser> filter){
        return getFrom(role, filter);
    }

    @Override
    public Iterator<QRole> getRoles(QUser account, Filter<QRole> filter){
        return getTo(account, filter);
    }

    @Override
    public Iterator<QUser> getAccounts(QRole role, Filter<QUser> filter, int offset, int limit){
        return getFrom(role, filter, offset, limit);
    }

    @Override
    public Iterator<QRole> getRoles(QUser account, Filter<QRole> filter, int offset, int limit){
        return getTo(account, filter, offset, limit);
    }

}