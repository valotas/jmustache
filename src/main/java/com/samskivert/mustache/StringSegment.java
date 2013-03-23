package com.samskivert.mustache;

import java.io.Writer;

/** A simple segment that reproduces a string. */
class StringSegment extends Segment {
    public StringSegment (String text) {
        _text = text;
    }
    @Override public void execute (Context ctx, Writer out) {
        write(out, _text);
    }
    private final String _text;
}