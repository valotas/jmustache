package com.samskivert.mustache;

/** A helper class for named segments. */
abstract class NamedSegment extends Segment {
    protected NamedSegment (String name, int line) {
        _name = name.intern();
        _line = line;
    }
    protected final String _name;
    protected final int _line;
}