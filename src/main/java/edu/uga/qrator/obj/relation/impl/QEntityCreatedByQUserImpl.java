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
package edu.uga.qrator.obj.relation.impl;

import edu.uga.qrator.obj.entity.QEntity;
import edu.uga.qrator.obj.entity.QUser;
import edu.uga.qrator.obj.relation.QEntityCreatedByQUser;
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.RelationM1SQL;


/*********************************************************************************
 * Comments forthcoming.
 */
public class QEntityCreatedByQUserImpl extends RelationM1SQL<QEntity,QUser> implements QEntityCreatedByQUser {


    public QEntityCreatedByQUserImpl(Connection conn){
        super(conn, QEntityCreatedByQUser.class);
    }

    @Override
    public Iterator<QEntity> getCreateds(QUser creator, Filter<QEntity> filter){
        return getFrom(creator, filter);
    }

    @Override
    public Iterator<QEntity> getCreateds(QUser creator, Filter<QEntity> filter, int offset, int limit){
        return getFrom(creator, filter, offset, limit);
    }

    @Override
    public QUser getCreator(QEntity created){
        return getTo(created);
    }

    @Override
    public void setCreator(QEntity created, QUser creator){
        add(created, creator);
    }


}
