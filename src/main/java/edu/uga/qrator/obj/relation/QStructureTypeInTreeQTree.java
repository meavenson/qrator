package edu.uga.qrator.obj.relation;

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.RelationM1;
import edu.uga.qrator.obj.entity.QTree;
import edu.uga.qrator.obj.entity.QStructureType;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QStructureTypeInTreeQTree extends RelationM1<QStructureType,QTree> {

    Iterator<QStructureType> getTypes(QTree tree, Filter<QStructureType> filter);

    Iterator<QStructureType> getTypes(QTree tree, Filter<QStructureType> filter, int offset, int limit);

    QTree getTree(QStructureType type);

    void setTree(QStructureType type, QTree tree);

}