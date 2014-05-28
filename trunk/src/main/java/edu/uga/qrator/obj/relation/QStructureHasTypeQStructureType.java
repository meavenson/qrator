package edu.uga.qrator.obj.relation;

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.RelationM1;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QStructureType;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QStructureHasTypeQStructureType extends RelationM1<QStructure,QStructureType> {

    Iterator<QStructure> getStructures(QStructureType type, Filter<QStructure> filter);

    Iterator<QStructure> getStructures(QStructureType type, Filter<QStructure> filter, int offset, int limit);

    QStructureType getType(QStructure structure);

    void setType(QStructure structure, QStructureType type);

}