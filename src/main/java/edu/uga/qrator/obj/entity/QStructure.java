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
package edu.uga.qrator.obj.entity;

import edu.uga.qrator.obj.entity.QStructure.ReviewStatus;

/*********************************************************************************
 * This interface represents a QStructure.
 */
public interface QStructure extends QEntity {

    public enum ReviewStatus{
        pending,
        reviewed,
        deferred,
        approved,
        rejected,
        committed
    }

    String getFilename();
    
    void setFilename(String filename);

    String getHash();
    
    void setHash(String hash);

    String getSpec();
    
    void setSpec(String spec);

    String getContents();
    
    void setContents(String contents);

    ReviewStatus getStatus();
    
    void setStatus(ReviewStatus status);

    String getVersion();
    
    void setVersion(String version);

    String getUri();
    
    void setUri(String uri);

}
