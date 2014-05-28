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
package edu.uga.qrator.logic.session;

import edu.uga.qrator.logic.manage.UserManager;
import edu.uga.qrator.obj.entity.QUser;
import java.util.Date;
import java.util.Iterator;
import persist.entity.manage.EntityManager;
import persist.entity.manage.sql.EntityManagerSQL;
import persist.query.filter.Filter;
import session.LoginException;
import session.Session;
import session.SessionManager;
import session.SessionUser;

/**
 * @author Matthew
 *
 */
public class QSessionManager extends SessionManager<Session, QAuthentication>{

    @Override
    protected Session createSession( QAuthentication auth ){
        
        if(!auth.hasCredential("username"))
            throw new LoginException("No username given");
        if(!auth.hasCredential("password"))
            throw new LoginException("No password given");
        
        String username = auth.getCredential("username").toString();
        String password = auth.getCredential("password").toString();
               
        UserManager manager = new UserManager(auth.conn);
        QUser user = manager.getUser(username, password);
        
        if(user != null && user.isActive()){
            Date now = new Date();
            user.setLastLogin(now);
            manager.update(user);
            if(isLoggedIn(user.getId())){
                Session session = getSession(user.getId());
                return session;
            }
            return new Session(generateSessionId(), user.getId());
        }
        return null;
    }

    @Override
    protected SessionUser createUser( QAuthentication auth ){
        UserManager userManager = new UserManager(auth.conn);
        if(!auth.hasCredential("username"))
            throw new LoginException("No username given");
        if(!auth.hasCredential("password"))
            throw new LoginException("No password given");
        if(!auth.hasCredential("name"))
            throw new LoginException("No name given");
        if(!auth.hasCredential("email"))
            throw new LoginException("No email given");
        
        String username = auth.getCredential("username").toString();
        String password = auth.getCredential("password").toString();
        String name = auth.getCredential("name").toString();
        String email = auth.getCredential("email").toString();
        Filter<QUser> filter = new Filter<QUser>(QUser.class)
                .eq("username", username).or().eq("email", email);
        EntityManager manager = new EntityManagerSQL(auth.conn);
        Iterator<QUser> users = manager.get(filter);
        QUser user = users.hasNext()? users.next() : null;
        if(user != null){
            throw new LoginException("A user already exists with that username or email");
        }
        
        return userManager.create(username, password, name, email);
        
    }

}