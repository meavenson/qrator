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

import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QStructure.ReviewStatus;
import java.util.Date;

/*********************************************************************************
 * This class represents the implementation of a QStructure.
 */
public class QStructureImpl extends QEntityImpl implements QStructure {

    private String filename;
    private String hash;
    private String spec;
    private String contents;
    private ReviewStatus status;
    private String version;
    private String uri;

    /*****************************************************************************
     * Construct a QStructureImpl.
     * 
     */
    public QStructureImpl(String filename, String hash, String spec, String contents, ReviewStatus status, String version, String uri, Date createdOn, Date modifiedOn) {
        super(createdOn, modifiedOn);
        this.filename = filename;
        this.hash = hash;
        this.spec = spec;
        this.contents = contents;
        this.status = status;
        this.version = version;
        this.uri = uri;
    }

    /*****************************************************************************
     * Construct a QStructureImpl.
     * 
     */
    public QStructureImpl(String filename, String hash, String spec, String contents, ReviewStatus status) {
        this.filename = filename;
        this.hash = hash;
        this.spec = spec;
        this.contents = contents;
        this.status = status;
    }

    @Override
    public String getFilename(){
        return filename;
    }
    
    @Override
    public void setFilename(String filename){
        this.filename = filename;
    }

    @Override
    public String getHash(){
        return hash;
    }
    
    @Override
    public void setHash(String hash){
        this.hash = hash;
    }

    @Override
    public String getSpec(){
        return spec;
    }
    
    @Override
    public void setSpec(String spec){
        this.spec = spec;
    }

    @Override
    public String getContents(){
        return contents;
    }
    
    @Override
    public void setContents(String contents){
        this.contents = contents;
    }

    @Override
    public ReviewStatus getStatus(){
        return status;
    }
    
    @Override
    public void setStatus(ReviewStatus status){
        this.status = status;
    }

    @Override
    public String getVersion(){
        return version;
    }
    
    @Override
    public void setVersion(String version){
        this.version = version;
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
