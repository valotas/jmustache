//
// JMustache - A Java implementation of the Mustache templating language
// http://github.com/samskivert/jmustache/blob/master/LICENSE


package com.samskivert.mustache;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents a compiled template. Templates are executed with a <em>context</em> to generate
 * output. The context can be any tree of objects. Variables are resolved against the context.
 * Given a name {@code foo}, the following mechanisms are supported for resolving its value
 * (and are sought in this order):
 * <ul>
 * <li>If the variable has the special name {@code this} the context object itself will be
 * returned. This is useful when iterating over lists.
 * <li>If the object is a {@link Map}, {@link Map#get} will be called with the string {@code foo}
 * as the key.
 * <li>A method named {@code foo} in the supplied object (with non-void return value).
 * <li>A method named {@code getFoo} in the supplied object (with non-void return value).
 * <li>A field named {@code foo} in the supplied object.
 * </ul>
 * <p> The field type, method return type, or map value type should correspond to the desired
 * behavior if the resolved name corresponds to a section. {@link Boolean} is used for showing or
 * hiding sections without binding a sub-context. Arrays, {@link Iterator} and {@link Iterable}
 * implementations are used for sections that repeat, with the context bound to the elements of the
 * array, iterator or iterable. Lambdas are current unsupported, though they would be easy enough
 * to add if desire exists. See the <a href="http://mustache.github.com/mustache.5.html">Mustache
 * documentation</a> for more details on section behavior. </p>
 */
public class Template
{
    /**
     * Encapsulates a fragment of a template that is passed to a lambda. The fragment is bound to
     * the variable context that was in effect at the time the lambda was called.
     */
    public abstract class Fragment {
        /** Executes this template fragment, writing its result to {@code out}. */
        public abstract void execute (Writer out);

        /** Executes this template fragment and returns is result as a string. */
        public String execute () {
            StringWriter out = new StringWriter();
            execute(out);
            return out.toString();
        }
    }

    /**
     * Executes this template with the given context, returning the results as a string.
     * @throws MustacheException if an error occurs while executing or writing the template.
     */
    public String execute (Object context) throws MustacheException
    {
        StringWriter out = new StringWriter();
        execute(context, out);
        return out.toString();
    }

    /**
     * Executes this template with the given context, writing the results to the supplied writer.
     * @throws MustacheException if an error occurs while executing or writing the template.
     */
    public void execute (Object context, Writer out) throws MustacheException
    {
        executeSegs(new Context(context, _compiler, 0, false, false), out);
    }

    /**
     * Executes this template with the supplied context and parent context, writing the results to
     * the supplied writer. The parent context will be searched for variables that cannot be found
     * in the main context, in the same way the main context becomes a parent context when entering
     * a block.
     * @throws MustacheException if an error occurs while executing or writing the template.
     */
    public void execute (Object context, Object parentContext, Writer out) throws MustacheException
    {
        Context pctx = new Context(parentContext, _compiler, 0, false, false);
        Context ctx = pctx.nest(context, 0, false, false);
        executeSegs(ctx, out);
    }

    protected Template (Segment[] segs, Mustache.Compiler compiler)
    {
        _segs = segs;
        _compiler = compiler;
    }

    protected void executeSegs (Context ctx, Writer out) throws MustacheException
    {
        for (Segment seg : _segs) {
            seg.execute(this, ctx, out);
        }
    }

    protected Fragment createFragment (final Segment[] segs, final Context ctx)
    {
        return new Fragment() {
            @Override public void execute (Writer out) {
                for (Segment seg : segs) {
                    seg.execute(Template.this, ctx, out);
                }
            }
        };
    }

    protected final Segment[] _segs;
    protected final Mustache.Compiler _compiler;
}
