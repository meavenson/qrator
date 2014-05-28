package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.relation.QUserTracksQStructure;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.RelationMMSQL;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QUser;

/**
 * A many-to-many relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QUserTracksQStructureImpl extends RelationMMSQL<QUser,QStructure> implements QUserTracksQStructure{

    public QUserTracksQStructureImpl(Connection conn){
        super(conn, QUserTracksQStructure.class);
    }

    @Override
    public Iterator<QUser> getTrackers(QStructure tracked, Filter<QUser> filter){
        return getFrom(tracked, filter);
    }

    @Override
    public Iterator<QStructure> getTrackeds(QUser tracker, Filter<QStructure> filter){
        return getTo(tracker, filter);
    }

    @Override
    public Iterator<QUser> getTrackers(QStructure tracked, Filter<QUser> filter, int offset, int limit){
        return getFrom(tracked, filter, offset, limit);
    }

    @Override
    public Iterator<QStructure> getTrackeds(QUser tracker, Filter<QStructure> filter, int offset, int limit){
        return getTo(tracker, filter, offset, limit);
    }

}