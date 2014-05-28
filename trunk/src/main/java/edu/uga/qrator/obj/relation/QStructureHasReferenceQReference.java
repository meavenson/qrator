package edu.uga.qrator.obj.relation;

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.Relation1M;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QReference;

/**
 * A one-to-many relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QStructureHasReferenceQReference extends Relation1M<QStructure,QReference> {

    Iterator<QReference> getReferences(QStructure structure, Filter<QReference> filter);

    Iterator<QReference> getReferences(QStructure structure, Filter<QReference> filter, int offset, int limit);

    QStructure getStructure(QReference reference);

    void setStructure(QStructure structure, QReference reference);

}