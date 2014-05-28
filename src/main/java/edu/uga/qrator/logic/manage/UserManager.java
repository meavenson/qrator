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
public class UserManager{
    
    private final QEntityFactory efac;
    private final QUserHasRoleQRole quqr;
    
    public UserManager(Connection conn){
        efac = QEntityFactoryImpl.getFactory(conn);
        QRelationFactory rFac = QRelationFactoryImpl.getFactory(conn);
        quqr = rFac.getQUserHasRoleQRole();
    }
    
    public void addRole(QUser user, QRole role){
        quqr.add(user, role);
    }
    
    public void addRole(QUser user, String roleName){
        QRole role = getRole(roleName);
        if(role != null) addRole(user, role);
    }

    public void removeRole(QUser user, QRole role){
        quqr.remove(user, role);
    }
    
    public void removeRole(QUser user, String roleName){
        QRole role = getRole(roleName);
        if(role != null) removeRole(user, role);
    }
    
    public boolean hasRole(QUser user, QRole role){
        String roleName = role.getName();
        for(Iterator<QRole> roles = getRoles(user); roles.hasNext();){
            if(roles.next().getName().equals(roleName)) return true;
        }
        return false;
    }
    
    public boolean hasRole(QUser user, String roleName){
        for(Iterator<QRole> roles = getRoles(user); roles.hasNext();){
            if(roles.next().getName().equals(roleName)) return true;
        }
        return false;
    }
    
    public QRole getRole(String name){
        Filter<QRole> filter = new Filter<QRole>(QRole.class).eq("name", name);
        Iterator<QRole> roles = efac.findRoles(filter);
        if(roles.hasNext()) return roles.next();
        return null;
    }

    public Iterator<QRole> getRoles(QUser user){
        return getRoles(user, null);
    }
    
    public Iterator<QRole> getRoles(QUser user, Filter<QRole> filter){
        return quqr.getRoles(user, filter);
    }
    
    public Iterator<QUser> list(Filter<QUser> filter){
        return efac.findUsers(filter);
    }
    
    public Iterator<QUser> list(){
        return list(new Filter<QUser>(QUser.class));
    }

    public QUser create(String handle, String password, String name, String email){
        QUser user = efac.createQUser(handle, password, name, email);
        addRole(user, "user");
        return user;
    }

    public QUser getUser( String handle, String password){
        Filter<QUser> filter = new Filter<QUser>(QUser.class)
                .eq("username", handle)
                .eq("password", password);
        Iterator<QUser> users = efac.findUsers(filter);
        if(users.hasNext()) return users.next();
        return null;
    }
    
    public QUser getUserByUsername( String username ){
        Filter<QUser> filter = new Filter<QUser>(QUser.class)
                .eq("username", username);
        Iterator<QUser> users = efac.findUsers(filter);
        if(users.hasNext()) return users.next();
        return null;
    }
    
    public QUser getUserByEmail( String email ){
        Filter<QUser> filter = new Filter<QUser>(QUser.class)
                .eq("email", email);
        Iterator<QUser> users = efac.findUsers(filter);
        if(users.hasNext()) return users.next();
        return null;
    }
    
    public void update(QUser user){
        efac.updateQUser(user);
    }
    
    public void remove(QUser user){
        efac.removeQUser(user);
    }

}