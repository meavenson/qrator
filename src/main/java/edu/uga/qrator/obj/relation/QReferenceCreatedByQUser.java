package edu.uga.qrator.obj.relation;

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.RelationM1;
import edu.uga.qrator.obj.entity.QReference;
import edu.uga.qrator.obj.entity.QUser;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QReferenceCreatedByQUser extends RelationM1<QReference,QUser> {

    Iterator<QReference> getCreateds(QUser creator, Filter<QReference> filter);

    Iterator<QReference> getCreateds(QUser creator, Filter<QReference> filter, int offset, int limit);

    QUser getCreator(QReference created);

    void setCreator(QReference created, QUser creator);

}