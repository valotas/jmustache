package com.samskivert.mustache;

import java.io.StringWriter;
import java.io.Writer;

/**
 * Encapsulates a fragment of a template that is passed to a lambda. The fragment is bound to
 * the variable context that was in effect at the time the lambda was called.
 */
class Fragment {
	private final Segment[] segs;
	private final Context ctx;

	Fragment(Context ctx, Segment[] segs) {
		this.segs = segs;
		this.ctx = ctx;
	}

	/** Executes this template fragment, writing its result to {@code out}. */
    public void execute (Writer out) {
    	for (Segment seg : segs) {
            seg.execute(ctx, out);
        }
    }

    /** Executes this template fragment and returns is result as a string. */
    public String execute () {
        StringWriter out = new StringWriter();
        execute(out);
        return out.toString();
    }
}