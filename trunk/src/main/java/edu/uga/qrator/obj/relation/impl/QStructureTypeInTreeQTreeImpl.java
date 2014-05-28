package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.relation.QStructureTypeInTreeQTree;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.RelationM1SQL;
import edu.uga.qrator.obj.entity.QTree;
import edu.uga.qrator.obj.entity.QStructureType;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QStructureTypeInTreeQTreeImpl extends RelationM1SQL<QStructureType,QTree> implements QStructureTypeInTreeQTree {

    public QStructureTypeInTreeQTreeImpl(Connection conn){
        super(conn, QStructureTypeInTreeQTree.class);
    }

    @Override
    public Iterator<QStructureType> getTypes(QTree tree, Filter<QStructureType> filter){
        return getFrom(tree, filter);
    }

    @Override
    public Iterator<QStructureType> getTypes(QTree tree, Filter<QStructureType> filter, int offset, int limit){
        return getFrom(tree, filter, offset, limit);
    }

    @Override
    public QTree getTree(QStructureType type){
        return getTo(type);
    }

    @Override
    public void setTree(QStructureType type, QTree tree){
        add(type, tree);
    }

}