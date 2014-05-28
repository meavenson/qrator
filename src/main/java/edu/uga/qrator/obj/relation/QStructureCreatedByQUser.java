package edu.uga.qrator.obj.relation;

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.RelationM1;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QUser;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QStructureCreatedByQUser extends RelationM1<QStructure,QUser> {

    Iterator<QStructure> getCreateds(QUser creator, Filter<QStructure> filter);

    Iterator<QStructure> getCreateds(QUser creator, Filter<QStructure> filter, int offset, int limit);

    QUser getCreator(QStructure created);

    void setCreator(QStructure created, QUser creator);

}