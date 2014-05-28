package edu.uga.qrator.obj.relation;

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.Relation1M;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QProvenance;

/**
 * A one-to-many relation.
 * 
 * @author  Matthew Eavenson
 * @date    May 27, 2014 6:18:18 PM
 */
public interface QStructureHasProvenanceQProvenance extends Relation1M<QStructure,QProvenance> {

    Iterator<QProvenance> getProvenances(QStructure structure, Filter<QProvenance> filter);

    Iterator<QProvenance> getProvenances(QStructure structure, Filter<QProvenance> filter, int offset, int limit);

    QStructure getStructure(QProvenance provenance);

    void setStructure(QStructure structure, QProvenance provenance);

}