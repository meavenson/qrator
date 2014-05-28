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

import edu.uga.qrator.obj.entity.QReference;
import java.util.Date;

/*********************************************************************************
 * This class represents the implementation of a QReference.
 */
public class QReferenceImpl extends QEntityImpl implements QReference {

    private String srcId;
    private String uri;

    /*****************************************************************************
     * Construct a QReferenceImpl.
     * 
     */
    public QReferenceImpl(String srcId, String uri, Date createdOn, Date modifiedOn) {
        super(createdOn, modifiedOn);
        this.srcId = srcId;
        this.uri = uri;
    }

    /*****************************************************************************
     * Construct a QReferenceImpl.
     * 
     */
    public QReferenceImpl(String srcId) {
        this.srcId = srcId;
    }

    @Override
    public String getSrcId(){
        return srcId;
    }
    
    @Override
    public void setSrcId(String srcId){
        this.srcId = srcId;
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
