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

import edu.uga.qrator.obj.entity.QStructure;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StructureCheckout extends Thread{
        
    private static final Map<Long, Loan> checkedOut = new HashMap<Long, Loan>();
    
    protected static void checkin(QStructure structure){
        checkedOut.remove(structure.getId());
    }

    protected static boolean checkout(QStructure structure){
        if(isCheckedOut(structure)) return false;
        Loan loan = new Loan(structure.getId());
        checkedOut.put(loan.id, loan);
        return true;
    }
    
    protected static boolean isCheckedOut(QStructure structure){
        return isCheckedOut(structure.getId());
    }

    protected static boolean isCheckedOut(long id){
        return checkedOut.containsKey(id);
    }

    public void run(){
        while(true){
            try{
                sleep(1800000); // 30 minutes...  I think?
            }catch(InterruptedException ie){
            }catch(Exception e){ e.printStackTrace(); }
            for(Loan co: checkedOut.values()){
                if(co.expiry.after(new Date()))
                    checkedOut.remove(co.id);
            }
        }
    }
    
    private static class Loan{
    
        private long id;
        private Date expiry;

        public Loan(long id){
            this.id = id;
            Calendar c = Calendar.getInstance();
            c.add(Calendar.MINUTE, 30);
            expiry = c.getTime();
        }

    }
}