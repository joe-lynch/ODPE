/*  
 * This file is part of the Open Deduction Proof Editor
 * 
 * @author It was originally created for GraPE by Max Schaefer
 */

package odpe.frontend.view;

import odpe.frontend.ast.ActiveCharacter;

public class ActiveCharacterInfo {
	
	public ActiveCharacter activeCharacter;
	public int charpos;
	
	public ActiveCharacterInfo(ActiveCharacter ac, int cp) {
		this.activeCharacter = ac;
		this.charpos = cp;
	}
	
	public void shift(int offset) {
		this.charpos += offset;
	}

}
