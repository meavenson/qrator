package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.relation.QStructureTypeCreatedByQUser;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.RelationM1SQL;
import edu.uga.qrator.obj.entity.QUser;
import edu.uga.qrator.obj.entity.QStructureType;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:17 PM
 */
public class QStructureTypeCreatedByQUserImpl extends RelationM1SQL<QStructureType,QUser> implements QStructureTypeCreatedByQUser {

    public QStructureTypeCreatedByQUserImpl(Connection conn){
        super(conn, QStructureTypeCreatedByQUser.class);
    }

    @Override
    public Iterator<QStructureType> getCreateds(QUser creator, Filter<QStructureType> filter){
        return getFrom(creator, filter);
    }

    @Override
    public Iterator<QStructureType> getCreateds(QUser creator, Filter<QStructureType> filter, int offset, int limit){
        return getFrom(creator, filter, offset, limit);
    }

    @Override
    public QUser getCreator(QStructureType created){
        return getTo(created);
    }

    @Override
    public void setCreator(QStructureType created, QUser creator){
        add(created, creator);
    }

}