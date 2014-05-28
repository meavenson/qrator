package edu.uga.qrator.obj.relation;

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.RelationM1;
import edu.uga.qrator.obj.entity.QReference;
import edu.uga.qrator.obj.entity.QSource;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QReferenceHasSourceQSource extends RelationM1<QReference,QSource> {

    Iterator<QReference> getReferences(QSource source, Filter<QReference> filter);

    Iterator<QReference> getReferences(QSource source, Filter<QReference> filter, int offset, int limit);

    QSource getSource(QReference reference);

    void setSource(QReference reference, QSource source);

}