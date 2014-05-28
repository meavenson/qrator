package edu.uga.qrator.obj.relation;

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.RelationMM;
import edu.uga.qrator.obj.entity.QRole;
import edu.uga.qrator.obj.entity.QUser;

/**
 * A many-to-many relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QUserHasRoleQRole extends RelationMM<QUser,QRole> {

    Iterator<QUser> getAccounts(QRole role, Filter<QUser> filter);

    Iterator<QRole> getRoles(QUser account, Filter<QRole> filter);

    Iterator<QUser> getAccounts(QRole role, Filter<QUser> filter, int offset, int limit);

    Iterator<QRole> getRoles(QUser account, Filter<QRole> filter, int offset, int limit);

}