/*  
 * (C) 2006, Max Schaefer 
 * 
 * This file is part of GraPE.
 *
 * GraPE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * GraPE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GraPE; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package odpe.util;

public class Triple <Fst, Snd, Thrd> {

    public Fst fst;
    public Snd snd;
    public Thrd thrd;
    
    public Triple(Fst fst, Snd snd, Thrd thrd) {
        this.fst = fst;
        this.snd = snd;
        this.thrd = thrd;
    }
    
    public Triple() {
        this.fst = null;
        this.snd = null;
        this.thrd = null;
    }
    
    public void copy(Triple<Fst, Snd, Thrd> q) {
        this.fst = q.fst;
        this.snd = q.snd;
        this.thrd = q.thrd;
    }

}
