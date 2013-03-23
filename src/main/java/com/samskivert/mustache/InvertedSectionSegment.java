package com.samskivert.mustache;

import java.io.Writer;
import java.util.Iterator;


/** A segment that represents an inverted section. */
class InvertedSectionSegment extends BlockSegment {
    public InvertedSectionSegment (String name, Segment[] segs, int line) {
        super(name, segs, line);
    }
    @Override public void execute (Template tmpl, Context ctx, Writer out)  {
        Object value = ctx.getSectionValue(_name, _line); // won't return null
        Iterator<?> iter = tmpl._compiler.collector.toIterator(value);
        if (iter != null) {
            if (!iter.hasNext()) {
                executeSegs(tmpl, ctx, out);
            }
        } else if (value instanceof Boolean) {
            if (!(Boolean)value) {
                executeSegs(tmpl, ctx, out);
            }
        } // TODO: fail?
    }
}