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

import edu.uga.qrator.obj.entity.QEntity;
import java.util.Date;

/*********************************************************************************
 * This class represents the implementation of a QEntity.
 */
public class QEntityImpl implements QEntity {

    private long id;
    private Date createdOn;
    private Date modifiedOn;

    /*****************************************************************************
     * Construct a QEntityImpl.
     * 
     */
    public QEntityImpl(Date createdOn, Date modifiedOn) {
        this.createdOn = createdOn;
        this.modifiedOn = modifiedOn;
    }

    /*****************************************************************************
     * Construct a QEntityImpl.
     * 
     */
    public QEntityImpl() {
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
    public Date getCreatedOn(){
        return createdOn;
    }
    
    @Override
    public void setCreatedOn(Date createdOn){
        this.createdOn = createdOn;
    }

    @Override
    public Date getModifiedOn(){
        return modifiedOn;
    }
    
    @Override
    public void setModifiedOn(Date modifiedOn){
        this.modifiedOn = modifiedOn;
    }

}
