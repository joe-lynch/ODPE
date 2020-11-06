/*
 *
 * This file is part of the Open Deduction Proof Editor.
 *
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.syntax;

import odpe.frontend.ast.Node;
import odpe.frontend.model.InferenceSystem;
import odpe.frontend.view.InferenceFork;
import odpe.frontend.view.WidgetView;

public class SubatomicRule extends InferenceRule {

    private String tex;

    public SubatomicRule(String id, String name, InferenceSystem sys, String tex, boolean defaultEnabled, boolean upDirection) {
        super(id, name, sys, tex, defaultEnabled, upDirection);
    }
}
