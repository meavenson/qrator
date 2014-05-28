package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.relation.QStructureHasTypeQStructureType;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.RelationM1SQL;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QStructureType;

/**
 * A many-to-one relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QStructureHasTypeQStructureTypeImpl extends RelationM1SQL<QStructure,QStructureType> implements QStructureHasTypeQStructureType {

    public QStructureHasTypeQStructureTypeImpl(Connection conn){
        super(conn, QStructureHasTypeQStructureType.class);
    }

    @Override
    public Iterator<QStructure> getStructures(QStructureType type, Filter<QStructure> filter){
        return getFrom(type, filter);
    }

    @Override
    public Iterator<QStructure> getStructures(QStructureType type, Filter<QStructure> filter, int offset, int limit){
        return getFrom(type, filter, offset, limit);
    }

    @Override
    public QStructureType getType(QStructure structure){
        return getTo(structure);
    }

    @Override
    public void setType(QStructure structure, QStructureType type){
        add(structure, type);
    }

}