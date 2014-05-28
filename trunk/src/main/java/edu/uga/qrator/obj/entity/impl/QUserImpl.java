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
package edu.uga.qrator.obj.entity.impl;

import edu.uga.qrator.obj.entity.QUser;
import java.util.Date;

/*********************************************************************************
 * This class represents the implementation of a QUser.
 */
public class QUserImpl implements QUser {

    private long id;
    private String username;
    private String password;
    private String name;
    private String email;
    private boolean active;
    private Date createdOn;
    private Date lastLogin;

    /*****************************************************************************
     * Construct a QUserImpl.
     * 
     */
    public QUserImpl(String username, String password, String name, String email, boolean active, Date createdOn, Date lastLogin) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.active = active;
        this.createdOn = createdOn;
        this.lastLogin = lastLogin;
    }

    /*****************************************************************************
     * Construct a QUserImpl.
     * 
     */
    public QUserImpl(String username, String password, String name, String email) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    @Override
    public long getId(){
        return id;
    }
    
    @Override
    public void setId(long id){
        this.id = id;
    }

    @Override
    public String getUsername(){
        return username;
    }
    
    @Override
    public void setUsername(String username){
        this.username = username;
    }

    @Override
    public String getPassword(){
        return password;
    }
    
    @Override
    public void setPassword(String password){
        this.password = password;
    }

    @Override
    public String getName(){
        return name;
    }
    
    @Override
    public void setName(String name){
        this.name = name;
    }

    @Override
    public String getEmail(){
        return email;
    }
    
    @Override
    public void setEmail(String email){
        this.email = email;
    }

    @Override
    public boolean isActive(){
        return active;
    }
    
    @Override
    public void setActive(boolean active){
        this.active = active;
    }

    @Override
    public Date getCreatedOn(){
        return createdOn;
    }
    
    @Override
    public void setCreatedOn(Date createdOn){
        this.createdOn = createdOn;
    }

    @Override
    public Date getLastLogin(){
        return lastLogin;
    }
    
    @Override
    public void setLastLogin(Date lastLogin){
        this.lastLogin = lastLogin;
    }

}
