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

import edu.uga.qrator.obj.entity.QEntityFactory;
import edu.uga.qrator.obj.entity.QReference;
import edu.uga.qrator.obj.entity.QSource;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QUser;
import edu.uga.qrator.obj.entity.impl.QEntityFactoryImpl;
import edu.uga.qrator.obj.relation.QReferenceCreatedByQUser;
import edu.uga.qrator.obj.relation.QReferenceHasSourceQSource;
import edu.uga.qrator.obj.relation.QRelationFactory;
import edu.uga.qrator.obj.relation.QStructureHasReferenceQReference;
import edu.uga.qrator.obj.relation.impl.QRelationFactoryImpl;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.QueryBuilder;
import persist.query.Select;
import persist.query.filter.Filter;
import persist.query.sql.QueryBuilderSQL;

/**
 *
 * @author durandal
 */
public class ReferenceManager {
    
    private final QEntityFactory efac;
    private final QReferenceHasSourceQSource qrqs;
    private final QStructureHasReferenceQReference qsqr;
    private final QReferenceCreatedByQUser qrqu;
    private final QueryBuilder query;
    
    public ReferenceManager(Connection conn){
        efac = QEntityFactoryImpl.getFactory(conn);
        QRelationFactory afac = QRelationFactoryImpl.getFactory(conn);
        qrqs = afac.getQReferenceHasSourceQSource();
        qsqr = afac.getQStructureHasReferenceQReference();
        qrqu = afac.getQReferenceCreatedByQUser();
        query = new QueryBuilderSQL(conn);
    }
    
    public QUser getCreator(QReference reference){
        return qrqu.getCreator(reference);
    }
    
    public QReference get(QStructure structure, QSource source, String srcId){
        Select<QReference> select = query.select(QReference.class);
        Filter<QSource> sourceFilter = new Filter<QSource>(QSource.class).eq("sid", source.getId()+"");
        Filter<QReference> refFilter = new Filter<QReference>(QReference.class).eq("srcId", srcId);
        select.traverseFrom(QStructureHasReferenceQReference.class, structure)
              .traverse(QReferenceHasSourceQSource.class)
              .where(sourceFilter).where(refFilter);
        return select.singleResult();
    }
    
    public QReference get(QSource source, String srcId){
        Filter<QReference> filter = new Filter<QReference>(QReference.class).eq("srcId", srcId);
        Iterator<QReference> references = qrqs.getReferences(source, filter);
        return references.hasNext()? references.next() : null;
    }
    
    public QStructure getStructure(QReference reference){
        return qsqr.getStructure(reference);
    }
    
    public QSource getSource(QReference reference){
        return qrqs.getSource(reference);
    }

    public QReference create(String srcId, QSource source, QStructure structure, QUser creator){
        return efac.createQReference(srcId, creator, source, structure);
    }
    
    public void update(QReference reference){
        efac.updateQReference(reference);
    }
    
    public void remove(QReference reference){
        efac.removeQReference(reference);
    }
    
    public Iterator<QReference> list(Filter<QReference> filter){
        return efac.findReferences(filter);
    }
    
    public Iterator<QReference> list(QStructure structure, int offset, int limit){
        return qsqr.getReferences( structure, null, offset, limit);
    }
    
    public Iterator<QReference> list(QStructure structure, QSource source){
        Select<QReference> select = query.select(QReference.class);
        Filter<QSource> sf = new Filter<QSource>(QSource.class).eq("sid", source.getId()+"");
        select.traverseFrom(QStructureHasReferenceQReference.class, structure)
              .traverse(QReferenceHasSourceQSource.class)
              .where(sf);
        return select.listResults();
    }
    
    public Iterator<QReference> list(QStructure structure, Filter<QReference> filter){
        return qsqr.getReferences(structure, filter);
    }
    
    public void removeAll(QStructure structure){
        for(Iterator<QReference> refs = qsqr.getReferences( structure, null); refs.hasNext();){
            QReference ref = refs.next();
            remove(ref);
        }
    }
    
}
