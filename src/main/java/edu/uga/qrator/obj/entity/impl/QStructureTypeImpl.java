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

import edu.uga.qrator.obj.entity.QStructureType;
import java.util.Date;

/*********************************************************************************
 * This class represents the implementation of a QStructureType.
 */
public class QStructureTypeImpl extends QEntityImpl implements QStructureType {

    private String name;
    private String description;
    private String glycoName;

    /*****************************************************************************
     * Construct a QStructureTypeImpl.
     * 
     */
    public QStructureTypeImpl(String name, String description, String glycoName, Date createdOn, Date modifiedOn) {
        super(createdOn, modifiedOn);
        this.name = name;
        this.description = description;
        this.glycoName = glycoName;
    }

    /*****************************************************************************
     * Construct a QStructureTypeImpl.
     * 
     */
    public QStructureTypeImpl(String name, String glycoName) {
        this.name = name;
        this.glycoName = glycoName;
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
    public String getGlycoName(){
        return glycoName;
    }
    
    @Override
    public void setGlycoName(String glycoName){
        this.glycoName = glycoName;
    }

}
