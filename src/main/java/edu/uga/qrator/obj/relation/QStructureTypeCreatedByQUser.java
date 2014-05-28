package edu.uga.qrator.obj.relation;

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.RelationM1;
import edu.uga.qrator.obj.entity.QUser;
import edu.uga.qrator.obj.entity.QStructureType;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:17 PM
 */
public interface QStructureTypeCreatedByQUser extends RelationM1<QStructureType,QUser> {

    Iterator<QStructureType> getCreateds(QUser creator, Filter<QStructureType> filter);

    Iterator<QStructureType> getCreateds(QUser creator, Filter<QStructureType> filter, int offset, int limit);

    QUser getCreator(QStructureType created);

    void setCreator(QStructureType created, QUser creator);

}