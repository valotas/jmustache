package com.samskivert.mustache;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Mustache.Lambda;

/** A segment that represents a section. */
class SectionSegment extends BlockSegment {
    public SectionSegment (String name, Segment[] segs, int line, Compiler compiler) {
        super(name, segs, line);
        _compiler = compiler;
    }
    @Override public void execute (Template tmpl, Context ctx, Writer out)  {
        Object value = ctx.getSectionValue(_name, _line); // won't return null
        Iterator<?> iter = tmpl._compiler.collector.toIterator(value);
        if (iter != null) {
            int index = 0;
            while (iter.hasNext()) {
                Object elem = iter.next();
                boolean onFirst = (index == 0), onLast = !iter.hasNext();
                executeSegs(tmpl, ctx.nest(elem, ++index, onFirst, onLast), out);
            }
        } else if (value instanceof Boolean) {
            if ((Boolean)value) {
                executeSegs(tmpl, ctx, out);
            }
        } else if (value instanceof Lambda) {
            try {
                ((Lambda)value).execute(tmpl.createFragment(_segs, ctx), out);
            } catch (IOException ioe) {
                throw new MustacheException(ioe);
            }
        } else if (_compiler.emptyStringIsFalse && "".equals(value)) {
            // omit the section
        } else {
            executeSegs(tmpl, ctx.nest(value, 0, false, false), out);
        }
    }
    protected final Compiler _compiler;
}