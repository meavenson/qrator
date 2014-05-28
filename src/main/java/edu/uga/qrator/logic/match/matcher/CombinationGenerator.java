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


import edu.uga.glydeII.gom.CompositeResidue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;



/**
 * This class is a generator of combinations of n-choose-k combinations from 
 * a set of elements.  The combinations are given by an Iterator.
 */
public class CombinationGenerator<E> 
    implements Iterator<List<E>>, Iterable<List<E>> 
{
    private final List<E> set;
    private final int[]   lastIdxs;
    private int[]         currentIdxs;
    
    /**
     * Create a new CombinationGenerator object.  It can be used to 
     * generate n choose set.size() element combinations from a given set 
     * of elements.
     * @param set a List of elements from which the combinations are selected
     * @param n is the size of each combination
     */
    public CombinationGenerator( List<E> set, int n )
    {
        if(n < 1 || n > set.size()) {
            throw new IllegalArgumentException("n < 1 || n > set.size()");
        }
        this.set = new ArrayList<E>(set);
        this.currentIdxs = new int[n];
        this.lastIdxs = new int[n];
        for(int i = 0; i < n; i++) {
            this.currentIdxs[i] = i;
            this.lastIdxs[i] = set.size() - n + i;
        }
    }

    /**
     * Return true iff there is another element in this iteration.
     * @return true iff there isanotherelementin thisiteration and false otherwise.
     */
    @Override
    public boolean hasNext() 
    {
        return currentIdxs != null;
    }

    /**
     * Return an Iterator of combinations for this CombinationGenerator.
     * @return an Iterator of combinations for this CombinationGenerator.
     */
    @Override
    public Iterator<List<E>> iterator() 
    {
        return this;
    }
    
    /**
     * Return the next combination from this CombinationGenerator.
     * @return the next combination. It is represented as a List of the elements in the combination.
     */
    @Override
    public List<E> next() 
    {
        if(!hasNext())
            throw new NoSuchElementException();

        List<E> currentCombination = new ArrayList<E>();
        for( int i : currentIdxs )
            currentCombination.add(set.get(i));
        setNextIndexes();
        return currentCombination;
    }

    /**
     * Not possible to remove a combination from this CombinationGenerator.  Do not use!
     * @throws UnsupportedOperationException if used.
     */
    @Override
    public void remove() 
    {
        throw new UnsupportedOperationException();
    }
    
    // private (aux) method
    private void setNextIndexes() 
    {
        for( int i = currentIdxs.length-1, j = set.size()-1; i >= 0; i--, j-- ) {
            if(currentIdxs[i] != j) {
                currentIdxs[i]++;
                for( int k = i+1; k < currentIdxs.length; k++ ) {
                    currentIdxs[k] = currentIdxs[k-1]+1;
                }
                return;
            }
        }
        currentIdxs = null;
    }
    
    // generate all permutation of Residues in a
    public static <E extends Object> List<E[]> permute( E[] s ) 
    {
	int n = s.length;
        @SuppressWarnings("unchecked")
	E[] a = (E[]) Arrays.copyOf(s, n, s.getClass());
        List<E[]> permutations = new ArrayList<E[]>();
	permute(a, n, permutations);
        return permutations;
    }

    // permute n Residues in array a
    private static <E extends Object> void permute(E[] a, int n, List<E[]> permutations) 
    {
        if (n == 1) {
	    permutations.add( Arrays.copyOf( a, a.length ) );
            return;
        }
        for (int i = 0; i < n; i++) {
            swap(a, i, n-1);
            permute(a, n-1, permutations);
            swap(a, i, n-1);
        }
    }  

    // swap the Residues at positions i and j
    private static <E extends Object> void swap( E[] a, int i, int j ) 
    {
        E tmp;

        tmp = a[i]; 
        a[i] = a[j]; 
        a[j] = tmp;
    }
    
    /*
    // simple test
    public static void main(String[] args) {
        List<Object> set = new ArrayList<Object>();
        set.add( new Character( 'A' ) );
        set.add( new Character( 'B' ) );
        set.add( new Character( 'C' ) );
        set.add( new Character( 'D' ) );
        set.add( new Character( 'E' ) );

        CombinationGenerator<Object> cg = new CombinationGenerator<Object>(set, 3);

        for(List<Object> combination : cg) {
            System.out.println(combination);
        }
    }
    */
}
