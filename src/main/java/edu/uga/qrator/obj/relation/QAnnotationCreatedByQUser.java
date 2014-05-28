package edu.uga.qrator.obj.relation;

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.RelationM1;
import edu.uga.qrator.obj.entity.QAnnotation;
import edu.uga.qrator.obj.entity.QUser;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QAnnotationCreatedByQUser extends RelationM1<QAnnotation,QUser> {

    Iterator<QAnnotation> getCreateds(QUser creator, Filter<QAnnotation> filter);

    Iterator<QAnnotation> getCreateds(QUser creator, Filter<QAnnotation> filter, int offset, int limit);

    QUser getCreator(QAnnotation created);

    void setCreator(QAnnotation created, QUser creator);

}