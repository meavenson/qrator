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

/**
 * StructureParsingException
 *
 * An exception thrown when structure is unable to be parsed.
 *
 * @author Matthew Eavenson (durandal@uga.edu)
 *
 */
public class StructureParsingException extends QException {
    
    public StructureParsingException(){
	super( "Cause of StructureParsingException unknown." );
    }

    public StructureParsingException( String msg){
	super( msg );
    }
    
    public StructureParsingException( Throwable cause ){
        super( cause );
    }
}