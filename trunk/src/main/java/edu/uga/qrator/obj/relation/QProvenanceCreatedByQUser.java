package edu.uga.qrator.obj.relation;

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.RelationM1;
import edu.uga.qrator.obj.entity.QProvenance;
import edu.uga.qrator.obj.entity.QUser;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QProvenanceCreatedByQUser extends RelationM1<QProvenance,QUser> {

    Iterator<QProvenance> getCreateds(QUser creator, Filter<QProvenance> filter);

    Iterator<QProvenance> getCreateds(QUser creator, Filter<QProvenance> filter, int offset, int limit);

    QUser getCreator(QProvenance created);

    void setCreator(QProvenance created, QUser creator);

}