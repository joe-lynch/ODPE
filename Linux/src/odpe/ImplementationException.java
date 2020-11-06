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

package odpe;

/**
 * Exception thrown to indicate an unimplemented feature.
 * 
 * @author Max Schaefer
 */

public class ImplementationException extends Exception {
	
	private static final long serialVersionUID = 1L;

	/**
	 *  Constructs an implementation exception from an error message.
	 * 
	 * @param msg the error message
	 */
	public ImplementationException(String msg) {
		super(msg);
	}

}
