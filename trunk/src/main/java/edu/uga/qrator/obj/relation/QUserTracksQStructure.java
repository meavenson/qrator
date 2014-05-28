package edu.uga.qrator.obj.relation;

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.RelationMM;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QUser;

/**
 * A many-to-many relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QUserTracksQStructure extends RelationMM<QUser,QStructure> {

    Iterator<QUser> getTrackers(QStructure tracked, Filter<QUser> filter);

    Iterator<QStructure> getTrackeds(QUser tracker, Filter<QStructure> filter);

    Iterator<QUser> getTrackers(QStructure tracked, Filter<QUser> filter, int offset, int limit);

    Iterator<QStructure> getTrackeds(QUser tracker, Filter<QStructure> filter, int offset, int limit);

}