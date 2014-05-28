package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.relation.QStructureCreatedByQUser;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.RelationM1SQL;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QUser;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QStructureCreatedByQUserImpl extends RelationM1SQL<QStructure,QUser> implements QStructureCreatedByQUser {

    public QStructureCreatedByQUserImpl(Connection conn){
        super(conn, QStructureCreatedByQUser.class);
    }

    @Override
    public Iterator<QStructure> getCreateds(QUser creator, Filter<QStructure> filter){
        return getFrom(creator, filter);
    }

    @Override
    public Iterator<QStructure> getCreateds(QUser creator, Filter<QStructure> filter, int offset, int limit){
        return getFrom(creator, filter, offset, limit);
    }

    @Override
    public QUser getCreator(QStructure created){
        return getTo(created);
    }

    @Override
    public void setCreator(QStructure created, QUser creator){
        add(created, creator);
    }

}