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

import edu.uga.qrator.obj.relation.QAnnotationCreatedByQUser;
import edu.uga.qrator.obj.relation.QRelationFactory;
import edu.uga.qrator.obj.relation.QStructureHasAnnotationQAnnotation;
import edu.uga.qrator.obj.relation.impl.QRelationFactoryImpl;
import edu.uga.qrator.obj.entity.QAnnotation;
import edu.uga.qrator.obj.entity.QEntityFactory;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QUser;
import edu.uga.qrator.obj.entity.impl.QEntityFactoryImpl;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;

/**
 *
 * @author Matthew
 */
public class AnnotationManager {
    
    private final QEntityFactory efac;
    private final QStructureHasAnnotationQAnnotation qsqa;
    private final QAnnotationCreatedByQUser qaqu;
    
    public AnnotationManager(Connection conn){
        efac = QEntityFactoryImpl.getFactory(conn);
        QRelationFactory afac = QRelationFactoryImpl.getFactory(conn);
        qsqa = afac.getQStructureHasAnnotationQAnnotation();
        qaqu = afac.getQAnnotationCreatedByQUser();
    }
    
    public QUser getCreator(QAnnotation annotation){
        return qaqu.getCreator(annotation);
    }
    
    public QStructure getStructure(QAnnotation annotation){
        return qsqa.getStructure(annotation);
    }
    
    public QAnnotation create(String comment, QStructure structure, QUser creator){
        return efac.createQAnnotation(comment, creator, structure);
    }
    
    public void remove(QAnnotation annotation){
        // TODO: revise this
        //if(annotation.getURI() != null) ReferoManager.removeResource(ReferoManager.getReferO(), annotation.getURI());
        efac.removeQAnnotation(annotation);
    }
    
    public Iterator<QAnnotation> list(Filter<QAnnotation> filter){
        return efac.findAnnotations(filter);
    }
    
    public Iterator<QAnnotation> list(QStructure structure, int offset, int limit){
        return qsqa.getAnnotations(structure, null, offset, limit);
    }
    
    public Iterator<QAnnotation> list(QStructure structure, Filter<QAnnotation> filter){
        return qsqa.getAnnotations((QStructure) structure, filter);
    }
    
    public Iterator<QAnnotation> list(QUser user, int offset, int limit){
        Filter<QAnnotation> filter = new Filter<QAnnotation>(QAnnotation.class).ascending("createdOn");
        return qaqu.getCreateds(user, filter, offset, limit);
    }
}
