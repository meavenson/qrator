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

import edu.uga.qrator.obj.relation.QRelationFactory;
import edu.uga.qrator.obj.relation.QStructureTypeInTreeQTree;
import edu.uga.qrator.obj.relation.impl.QRelationFactoryImpl;
import edu.uga.qrator.obj.entity.QEntityFactory;
import edu.uga.qrator.obj.entity.QStructureType;
import edu.uga.qrator.obj.entity.QTree;
import edu.uga.qrator.obj.entity.QUser;
import edu.uga.qrator.obj.entity.impl.QEntityFactoryImpl;
import java.sql.Connection;
import java.util.Date;
import java.util.Iterator;
import persist.query.filter.Filter;

/**
 *
 * @author durandal
 */
public class TypeManager {
    
    private final QEntityFactory efac;
    private final QStructureTypeInTreeQTree qtqt;
    
    public TypeManager(Connection conn){
        efac = QEntityFactoryImpl.getFactory(conn);
        QRelationFactory afac = QRelationFactoryImpl.getFactory(conn);
        qtqt = afac.getQStructureTypeInTreeQTree();
    }
    
    public QStructureType createType(String name, String description, String glycoName, QTree tree, QUser creator){
        Date now = new Date();
        return efac.createQStructureType(name, description, glycoName, now, now, creator, tree);
    }
    
    public QStructureType getType(String name){
        Filter<QStructureType> filter = new Filter<QStructureType>(QStructureType.class).eq("name", name);
        Iterator<QStructureType> types = efac.findStructureTypes(filter);
        if(types.hasNext()) return types.next();
        return null;
    }
    
    public void removeType(QStructureType type){
        efac.removeQStructureType(type);
    }
    
    public Iterator<QStructureType> listTypes(QTree tree, Filter<QStructureType> filter){
        return qtqt.getTypes(tree, filter);
    }
    
    public Iterator<QStructureType> listTypes(QTree tree){
        return listTypes(tree, null);
    }
    
    public Iterator<QStructureType> listTypes(Filter<QStructureType> filter){
        return efac.findStructureTypes(filter);
    }
    
    
}
