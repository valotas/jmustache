package com.samskivert.mustache;

import java.io.Writer;


/** A helper class for block segments. */
abstract class BlockSegment extends NamedSegment {
    protected BlockSegment (String name, Segment[] segs, int line) {
        super(name, line);
        _segs = segs;
    }
    protected void executeSegs (Context ctx, Writer out)  {
        for (Segment seg : _segs) {
            seg.execute(ctx, out);
        }
    }
    protected final Segment[] _segs;
}