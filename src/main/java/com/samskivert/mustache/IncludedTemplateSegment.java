package com.samskivert.mustache;

import java.io.Writer;

import com.samskivert.mustache.Mustache.Compiler;

class IncludedTemplateSegment extends Segment {
    public IncludedTemplateSegment (String name, Compiler compiler) {
        _name = name;
        _compiler = compiler;
    }
    @Override public void execute (Template tmpl, Context ctx, Writer out) {
        // we compile our template lazily to avoid infinie recursion if a template includes
        // itself (see issue #13)
        if (_template == null) {
            try {
                _template = _compiler.compile(_compiler.loader.getTemplate(_name));
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException)e;
                } else {
                    throw new MustacheException("Unable to load template: " + _name, e);
                }
            }
        }
        // we must take care to preserve our context rather than creating a new one, which
        // would happen if we just called execute() with ctx.data
        _template.executeSegs(ctx, out);
    }
    protected final String _name;
    protected final Compiler _compiler;
    protected Template _template;
}