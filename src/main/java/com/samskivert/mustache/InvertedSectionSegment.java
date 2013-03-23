package com.samskivert.mustache;

import java.io.Writer;
import java.util.Iterator;


/** A segment that represents an inverted section. */
class InvertedSectionSegment extends BlockSegment {
    public InvertedSectionSegment (String name, Segment[] segs, int line) {
        super(name, segs, line);
    }
    @Override public void execute (Context ctx, Writer out)  {
        Object value = ctx.getSectionValue(_name, _line); // won't return null
        Iterator<?> iter = ctx.compiler.collector.toIterator(value);
        if (iter != null) {
            if (!iter.hasNext()) {
                executeSegs(ctx, out);
            }
        } else if (value instanceof Boolean) {
            if (!(Boolean)value) {
                executeSegs(ctx, out);
            }
        } // TODO: fail?
    }
}