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
package edu.uga.qrator.logic.manage;

import edu.uga.qrator.obj.entity.QEntityFactory;
import edu.uga.qrator.obj.entity.QSource;
import edu.uga.qrator.obj.entity.QUser;
import edu.uga.qrator.obj.entity.impl.QEntityFactoryImpl;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;

/**
 *
 * @author Matthew
 */
public class SourceManager {
    
    private final QEntityFactory efac;
    
    public SourceManager(Connection conn){
        efac = QEntityFactoryImpl.getFactory(conn);
    }
    
    public QSource create(String name, String uri, QUser creator){
        return efac.createQSource(name, uri, creator);
    }
    
    public void update(QSource source){
        efac.updateQSource(source);
    }
    
    public void remove(QSource source){
        efac.removeQSource(source);
    }
    
    public QSource get(String name){
        Filter<QSource> filter = new Filter<QSource>(QSource.class).eq("name", name);
        Iterator<QSource> sources = efac.findSources(filter);
        if(sources.hasNext()) return sources.next();
        return null;
    }
    
    public QSource getLowerCase(String name){
        QSource source;
        for(Iterator<QSource> sources = efac.findSources(null); sources.hasNext();){
            source = sources.next();
            String srcName = source.getName().replaceAll(" ", "").toLowerCase();
            if(srcName.equals(name)) return source;
        }
        return null;
    }
    
    public Iterator<QSource> list(int offset, int limit){
        return efac.findSources(null, offset, limit);
    }
    
    public Iterator<QSource> list(Filter<QSource> filter){
        return efac.findSources(filter);
    }
}
