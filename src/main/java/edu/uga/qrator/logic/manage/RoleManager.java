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
import edu.uga.qrator.obj.entity.QEntityFactory;
import persist.query.filter.Filter;
import java.sql.Connection;
import edu.uga.qrator.obj.relation.impl.QRelationFactoryImpl;
import edu.uga.qrator.obj.relation.QUserHasRoleQRole;
import edu.uga.qrator.obj.entity.QRole;
import edu.uga.qrator.obj.entity.QUser;

import edu.uga.qrator.obj.entity.impl.QEntityFactoryImpl;

import java.util.Iterator;

/**
 * @author Matthew
 *
 *
 */
public class RoleManager{
    
    private final QEntityFactory efac;
    private final QRelationFactory afac;
    
    public RoleManager(Connection conn){
        efac = QEntityFactoryImpl.getFactory(conn);
        afac = QRelationFactoryImpl.getFactory(conn);
    }
    
    public Iterator<QRole> list(Filter<QRole> filter){
        return efac.findRoles(filter);
    }
    
    public Iterator<QRole> list(){
        return list(null);
    }

    public QRole create(String name, QUser creator){
        return efac.createQRole(name, creator);
    }

    public QRole get( String name ){
        Filter<QRole> filter = new Filter<QRole>(QRole.class)
                .eq("name", name);
        Iterator<QRole> roles = list(filter);
        if(roles.hasNext()) return roles.next();
        return null;
    }
    
    public QRole get(long id){
        Filter<QRole> filter = new Filter<QRole>(QRole.class).eq("sid", id+"");
        Iterator<QRole> roles = efac.findRoles(filter);
        if(roles.hasNext()) return roles.next();
        else return null;
    }
    
    public void update(QRole role){
        efac.updateQRole(role);
    }
    
    public void remove(QRole role){
        efac.removeQRole(role);
    }

}