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

import edu.uga.qrator.obj.entity.QProvenance;
import java.util.Date;
import edu.uga.qrator.obj.entity.QProvenance.ProvenanceAction;

/*********************************************************************************
 * This class represents the implementation of a QProvenance.
 */
public class QProvenanceImpl extends QEntityImpl implements QProvenance {

    private ProvenanceAction action;
    private String uri;

    /*****************************************************************************
     * Construct a QProvenanceImpl.
     * 
     */
    public QProvenanceImpl(ProvenanceAction action, String uri, Date createdOn, Date modifiedOn) {
        super(createdOn, modifiedOn);
        this.action = action;
        this.uri = uri;
    }

    /*****************************************************************************
     * Construct a QProvenanceImpl.
     * 
     */
    public QProvenanceImpl(ProvenanceAction action) {
        this.action = action;
    }

    @Override
    public ProvenanceAction getAction(){
        return action;
    }
    
    @Override
    public void setAction(ProvenanceAction action){
        this.action = action;
    }

    @Override
    public String getUri(){
        return uri;
    }
    
    @Override
    public void setUri(String uri){
        this.uri = uri;
    }

}
