package com.samskivert.mustache;

import java.io.Writer;


/** A segment that substitutes the contents of a variable. */
class VariableSegment extends NamedSegment {
    public VariableSegment (String name, boolean escapeHTML, int line) {
        super(name, line);
        _escapeHTML = escapeHTML;
    }
    @Override public void execute (Template tmpl, Context ctx, Writer out)  {
        Object value = ctx.getValueOrDefault(_name, _line);
        if (value == null) {
            throw new MustacheException.Context("No key, method or field with name '" + _name +
                                                "' on line " + _line, _name, _line);
        }
        String text = String.valueOf(value);
        write(out, _escapeHTML ? Mustache.escapeHTML(text) : text);
    }
    protected boolean _escapeHTML;
}