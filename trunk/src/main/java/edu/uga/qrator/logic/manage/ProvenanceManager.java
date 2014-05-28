/* 
 * Copyright (C) 2014 Matthew Eavenson <matthew.eavenson at gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package edu.uga.qrator.logic.manage;

import edu.uga.qrator.obj.entity.*;
import edu.uga.qrator.obj.entity.impl.QEntityFactoryImpl;
import edu.uga.qrator.obj.relation.QProvenanceCreatedByQUser;
import edu.uga.qrator.obj.relation.QRelationFactory;
import edu.uga.qrator.obj.relation.QStructureHasProvenanceQProvenance;
import edu.uga.qrator.obj.relation.impl.QRelationFactoryImpl;
import edu.uga.qrator.service.ServiceUtil;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.util.PersistenceUtil;

/**
 *
 * @author Matthew
 */
public class ProvenanceManager {
    
    private final QEntityFactory efac;
    private final QRelationFactory rfac;
    private final QStructureHasProvenanceQProvenance qsqp;
    private final QProvenanceCreatedByQUser qpqu;
    
    public ProvenanceManager(Connection conn){
        efac = QEntityFactoryImpl.getFactory(conn);
        rfac = QRelationFactoryImpl.getFactory(conn);
        qsqp = rfac.getQStructureHasProvenanceQProvenance();
        qpqu = rfac.getQProvenanceCreatedByQUser();
    }
    
    public QUser getCreator(QProvenance provenance){
        return qpqu.getCreator(provenance);
    }
    
    public void setCreator(QProvenance provenance, QUser user){
        qpqu.setCreator(provenance, user);
    }
    
    public QStructure getStructure(QProvenance provenance){
        return qsqp.getStructure(provenance);
    }
    
    public QProvenance create(QProvenance.ProvenanceAction action, QStructure structure, QUser creator){
        return efac.createQProvenance(action, creator, structure);
    }
    
    public void update(QProvenance provenance){
        efac.updateQProvenance(provenance);
    }
    
    public void remove(QProvenance provenance){
        // TODO: revise this
        //if(provenance.getURI() != null) ReferoManager.removeResource(ReferoManager.getReferO(), provenance.getURI());
        efac.removeQProvenance(provenance);
    }
    
    public Iterator<QProvenance> list(QStructure structure){
        return list(structure, -1, -1);
    }
    
    public Iterator<QProvenance> list(QStructure structure, int offset, int limit){
        Filter<QProvenance> filter = new Filter<QProvenance>(QProvenance.class).ascending("createdOn");
        return qsqp.getProvenances( structure, filter, offset, limit);
    }
    
    public Iterator<QProvenance> list(QUser user, QProvenance.ProvenanceAction action, int offset, int limit){
        Filter<QProvenance> filter = new Filter<QProvenance>(QProvenance.class).ascending("createdOn");
        if(action != null) filter.eq("action", action.toString());
        return qpqu.getCreateds(user, filter, offset, limit);
    }
    
}
