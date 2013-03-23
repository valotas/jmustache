package com.samskivert.mustache;

import java.io.IOException;
import java.io.Writer;

/** A template is broken into segments. */
abstract class Segment
{
    abstract void execute (Template tmpl, Context ctx, Writer out);

    protected static void write (Writer out, String data) {
        try {
            out.write(data);
        } catch (IOException ioe) {
            throw new MustacheException(ioe);
        }
    }
}