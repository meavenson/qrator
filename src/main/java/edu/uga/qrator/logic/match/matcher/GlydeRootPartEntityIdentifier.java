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
package edu.uga.qrator.logic.match.matcher;

import edu.uga.glydeII.gom.ChemicalEntity;
import edu.uga.glydeII.gom.GlydeAction;
import edu.uga.glydeII.gom.Residue;
import java.util.HashSet;
import java.util.Set;


public class GlydeRootPartEntityIdentifier implements GlydeAction {
    private HashSet<Residue> rootPartEntities = null;

    public GlydeRootPartEntityIdentifier(){
        rootPartEntities = new HashSet<Residue>();
    }

    public Set<Residue> getRootPartEntities() {
        return rootPartEntities;
    }

    @Override
    public void doAction( ChemicalEntity ce, int level ) {
        if( ce instanceof Residue ) {
            if( ((Residue)ce).getLinkOut() == null ) {
                rootPartEntities.add( (Residue)ce );
            }
        }
    }
}
