package edu.uga.qrator.obj.relation;

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.RelationM1;
import edu.uga.qrator.obj.entity.QUser;
import edu.uga.qrator.obj.entity.QSource;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QSourceCreatedByQUser extends RelationM1<QSource,QUser> {

    Iterator<QSource> getCreateds(QUser creator, Filter<QSource> filter);

    Iterator<QSource> getCreateds(QUser creator, Filter<QSource> filter, int offset, int limit);

    QUser getCreator(QSource created);

    void setCreator(QSource created, QUser creator);

}