//
// $Id$

package com.samskivert.mustache;

/**
 * An exception thrown if we encounter an error while parsing a template.
 */
@SuppressWarnings("serial")
public class MustacheParseException extends MustacheException
{
    public MustacheParseException (String message)
    {
        super(message);
    }

    public MustacheParseException (String message, int lineNo)
    {
        super(message + " @ line " + lineNo);
    }
}
