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

import edu.uga.qrator.obj.entity.QTree;
import java.util.Date;

/*********************************************************************************
 * This class represents the implementation of a QTree.
 */
public class QTreeImpl extends QEntityImpl implements QTree {

    private String name;
    private String description;
    private String spec;

    /*****************************************************************************
     * Construct a QTreeImpl.
     * 
     */
    public QTreeImpl(String name, String description, String spec, Date createdOn, Date modifiedOn) {
        super(createdOn, modifiedOn);
        this.name = name;
        this.description = description;
        this.spec = spec;
    }

    /*****************************************************************************
     * Construct a QTreeImpl.
     * 
     */
    public QTreeImpl(String name, String spec) {
        this.name = name;
        this.spec = spec;
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
    public String getDescription(){
        return description;
    }
    
    @Override
    public void setDescription(String description){
        this.description = description;
    }

    @Override
    public String getSpec(){
        return spec;
    }
    
    @Override
    public void setSpec(String spec){
        this.spec = spec;
    }

}
