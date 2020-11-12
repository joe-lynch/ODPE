/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author It has been adapted by Joe Lynch from the original file for GraPE by Max Schaefer
 *
 */

package odpe.frontend.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Vector;

import odpe.frontend.ast.ActiveCharacter;
import odpe.frontend.ast.Selection;
import odpe.util.Pair;

public class MarkableLabel extends StructuredLabel implements PropertyChangeListener {
    
    private static final long serialVersionUID = 1L;

    Collection<SelectionInfo> ranges;
    Collection<ActiveCharacterInfo> actchars;
    
    public MarkableLabel(String txt, Vector<SelectionInfo> r, Vector<ActiveCharacterInfo> a) {
        super(txt, get_markingposs(r), get_active_chars(a));
        super.addPropertyChangeListener(this);
        ranges = r;
        actchars = a;
    }
    
    public MarkableLabel(String string, Vector<SelectionInfo> r, Vector<ActiveCharacterInfo> a, boolean b) {
        super(string, get_markingposs(r), get_active_chars(a));
        super.addPropertyChangeListener(this);
        ranges = r;
        actchars = a;
    }

    private static int[] get_markingposs(Vector<SelectionInfo> r) {
        if(r == null)
            return new int[0];
        int[] markingposs = new int[r.size()*2];
        for(int i = 0; i < markingposs.length - 1; i += 2) {
            markingposs[i] = r.get(i/2).startchar;
            markingposs[i+1] = r.get(i/2).endchar;
        }
        return markingposs;
    }
    
    private static int[] get_active_chars(Vector<ActiveCharacterInfo> a) {
        if(a == null)
            return new int[0];
        int[] active_chars = new int[a.size()];
        for(int i = 0; i < active_chars.length; ++i)
            active_chars[i] = a.get(i).charpos;
        return active_chars;
    }
    
    private ActiveCharacter actchar_for_pos(int i) {
        if(actchars == null)
            return null;
        for(ActiveCharacterInfo c : actchars)
            if(c.charpos == i)
                return c.activeCharacter;
        return null;
    }

    public Selection current_range() {
        int s = super.getSelectionStart();
        int e = super.getSelectionEnd();
        return get_range(s, e);
    }
    
    private Selection get_range(int s, int e) {
        if(s == e)
            return null;
        Selection sel = null;
        for(SelectionInfo rng : ranges)
            if(rng.startchar == s && rng.endchar == e)
                sel = rng.selection;
        if(sel == null) {
            throw new IndexOutOfBoundsException("invalid selection range (shouldn't happen!)");
        }
        else
            return sel;
    }
    
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals("ActiveCharacterClicked")) {
            int idx = (Integer)evt.getNewValue();
            firePropertyChange("ActiveCharacterEvent", null, actchar_for_pos(idx));
        }
        else if(evt.getPropertyName().equals("NewSelection")) {
            Pair<Integer, Integer> rng = (Pair<Integer, Integer>) evt.getNewValue();
            Selection sel = get_range(rng.fst, rng.snd);
            firePropertyChange("NewSelectionEvent", evt.getOldValue(), sel);
        }
    }

}
