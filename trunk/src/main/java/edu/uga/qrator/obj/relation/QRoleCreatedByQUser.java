package edu.uga.qrator.obj.relation;

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.RelationM1;
import edu.uga.qrator.obj.entity.QRole;
import edu.uga.qrator.obj.entity.QUser;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QRoleCreatedByQUser extends RelationM1<QRole,QUser> {

    Iterator<QRole> getCreateds(QUser creator, Filter<QRole> filter);

    Iterator<QRole> getCreateds(QUser creator, Filter<QRole> filter, int offset, int limit);

    QUser getCreator(QRole created);

    void setCreator(QRole created, QUser creator);

}