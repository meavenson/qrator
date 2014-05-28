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
package edu.uga.qrator.logic.session;

/**
 *
 * @author durandal
 */
public class GlydeFile {

    private String name;
    private String data;
    private boolean reviewed;

    public GlydeFile(String name, String data){
        this.name = name;
        this.data = data;
        reviewed = false;
    }

    public void setReviewed(boolean reviewed){
        this.reviewed = reviewed;
    }

    public boolean isReviewed(){
        return reviewed;
    }

    public String getName(){
        return name;
    }

    public String getData(){
        return data;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof GlydeFile){
            GlydeFile that = (GlydeFile)obj;
            if(name.equals(that.name))
                return true;
            else return false;
        }else return false;
    }

}
