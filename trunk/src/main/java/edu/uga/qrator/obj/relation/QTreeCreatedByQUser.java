package edu.uga.qrator.obj.relation;

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.RelationM1;
import edu.uga.qrator.obj.entity.QTree;
import edu.uga.qrator.obj.entity.QUser;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QTreeCreatedByQUser extends RelationM1<QTree,QUser> {

    Iterator<QTree> getCreateds(QUser creator, Filter<QTree> filter);

    Iterator<QTree> getCreateds(QUser creator, Filter<QTree> filter, int offset, int limit);

    QUser getCreator(QTree created);

    void setCreator(QTree created, QUser creator);

}