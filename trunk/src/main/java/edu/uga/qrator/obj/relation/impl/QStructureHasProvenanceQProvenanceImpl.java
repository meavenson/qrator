package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.relation.QStructureHasProvenanceQProvenance;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.Relation1MSQL;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QProvenance;

/**
 * A one-to-many relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public class QStructureHasProvenanceQProvenanceImpl extends Relation1MSQL<QStructure,QProvenance> implements QStructureHasProvenanceQProvenance {

    public QStructureHasProvenanceQProvenanceImpl(Connection conn){
        super(conn, QStructureHasProvenanceQProvenance.class);
    }

    @Override
    public Iterator<QProvenance> getProvenances(QStructure structure, Filter<QProvenance> filter){
        return getTo(structure, filter);
    }

    @Override
    public Iterator<QProvenance> getProvenances(QStructure structure, Filter<QProvenance> filter, int offset, int limit){
        return getTo(structure, filter, offset, limit);
    }

    @Override
    public QStructure getStructure(QProvenance provenance){
        return getFrom(provenance);
    }

    @Override
    public void setStructure(QStructure structure, QProvenance provenance){
        add(structure, provenance);
    }

}