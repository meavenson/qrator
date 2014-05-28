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
package edu.uga.qrator.except;

import edu.uga.qrator.obj.entity.QStructure;

/**
 * StructureDuplicateException
 *
 * An exception thrown when an attempt is made to create a duplicate structure.
 *
 * @author Matthew Eavenson (durandal@uga.edu)
 *
 */
public class StructureDuplicateException extends QException {
    
    private final QStructure duplicated;

    public StructureDuplicateException(){
	super( "Cause of StructureDuplicateException unknown." );
        duplicated = null;
    }

    public StructureDuplicateException( String msg, QStructure duplicated ){
	super( msg );
        this.duplicated = duplicated;
    }
    
    public StructureDuplicateException( Throwable cause, QStructure duplicated ){
        super( cause );
        this.duplicated = duplicated;
    }
    
    public QStructure getDuplicate(){
        return duplicated;
    }
}