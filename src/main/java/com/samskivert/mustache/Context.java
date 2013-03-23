package com.samskivert.mustache;


import static com.samskivert.mustache.Consts.DOT_NAME;
import static com.samskivert.mustache.Consts.FIRST_NAME;
import static com.samskivert.mustache.Consts.INDEX_NAME;
import static com.samskivert.mustache.Consts.LAST_NAME;
import static com.samskivert.mustache.Consts.NO_FETCHER_FOUND;

import java.util.Collections;
import java.util.Map;


class Context
{
	
    /** Used to cache variable fetchers for a given context class, name combination. */
    private static final class Key
    {
        public final Class<?> cclass;
        public final String name;

        Key (Class<?> cclass, String name) {
            this.cclass = cclass;
            this.name = name;
        }

        @Override public int hashCode () {
            return cclass.hashCode() * 31 + name.hashCode();
        }

        @Override public boolean equals (Object other) {
            Key okey = (Key)other;
            return okey.cclass == cclass && okey.name == name;
        }
    }
	
    public final Object data;
    public final Context parent;
    public final Mustache.Compiler compiler;
    public final int index;
    public final boolean onFirst;
    public final boolean onLast;
    protected final Map<Key, Mustache.VariableFetcher> fcache;

    Context (Object data, Mustache.Compiler compiler, int index, boolean onFirst, boolean onLast) {
    	this(data, null, compiler, null, index, onFirst, onLast);
    }
    
	private Context (Object data, Context parent, Mustache.Compiler compiler, Map<Key, Mustache.VariableFetcher> fcache, int index, boolean onFirst, boolean onLast) {
        this.data = data;
        this.parent = parent;
        this.compiler = compiler;
        this.index = index;
        this.onFirst = onFirst;
        this.onLast = onLast;
        if (fcache != null) {
        	this.fcache = fcache;
        } else {
        	this.fcache = this.compiler.collector.createFetcherCache();
        }
    }

    Context nest (Object data, int index, boolean onFirst, boolean onLast) {
        return new Context(data, this, compiler, fcache, index, onFirst, onLast);
    }
    
    /**
     * Called by executing segments to obtain the value of the specified variable in the supplied
     * context.
     *
     * @param ctx the context in which to look up the variable.
     * @param name the name of the variable to be resolved, which must be an interned string.
     * @param missingIsNull whether to fail if a variable cannot be resolved, or to return null in
     * that case.
     *
     * @return the value associated with the supplied name or null if no value could be resolved.
     */
    private final Object getValue (Context ctx, String name, int line, boolean missingIsNull)
    {
        if (!compiler.standardsMode) {
            // if we're dealing with a compound key, resolve each component and use the result to
            // resolve the subsequent component and so forth
            if (name != DOT_NAME && name.indexOf(DOT_NAME) != -1) {
                String[] comps = name.split("\\.");
                // we want to allow the first component of a compound key to be located in a parent
                // context, but once we're selecting sub-components, they must only be resolved in
                // the object that represents that component
                Object data = getValue(ctx, comps[0].intern(), line, missingIsNull);
                for (int ii = 1; ii < comps.length; ii++) {
                    if (data == NO_FETCHER_FOUND) {
                        if (!missingIsNull) throw new MustacheException.Context(
                            "Missing context for compound variable '" + name + "' on line " + line +
                            ". '" + comps[ii - 1] + "' was not found.", name, line);
                        return null;
                    } else if (data == null) {
                        return null;
                    }
                    // once we step into a composite key, we drop the ability to query our parent
                    // contexts; that would be weird and confusing
                    data = getValueIn(data, comps[ii].intern(), line);
                }
                return checkForMissing(name, line, missingIsNull, data);
            }
        }

        // handle our special variables
        if (name == FIRST_NAME) {
            return ctx.onFirst;
        } else if (name == LAST_NAME) {
            return ctx.onLast;
        } else if (name == INDEX_NAME) {
            return ctx.index;
        }

        // if we're in standards mode, we don't search our parent contexts
        if (compiler.standardsMode) {
            return checkForMissing(name, line, missingIsNull, getValueIn(ctx.data, name, line));
        }

        while (ctx != null) {
            Object value = getValueIn(ctx.data, name, line);
            if (value != NO_FETCHER_FOUND) return value;
            ctx = ctx.parent;
        }
        // we've popped off the top of our stack of contexts; we never found a fetcher for our
        // variable, so let checkForMissing() decide what to do
        return checkForMissing(name, line, missingIsNull, NO_FETCHER_FOUND);
    }

    /**
     * Returns the value of the specified variable, noting that it is intended to be used as the
     * contents for a segment. Presently this does not do anything special, but eventually this
     * will be the means by which we enact configured behavior for sections that reference null or
     * missing variables. Right now, all such variables result in a length 0 section.
     */
    protected final Object getSectionValue (String name, int line)
    {
        // TODO: configurable behavior on missing values
        Object value = getValue(this, name, line, compiler.missingIsNull);
        // TODO: configurable behavior on null values
        return (value == null) ? Collections.emptyList() : value;
    }

    /**
     * Returns the value for the specified variable, or the configured default value if the
     * variable resolves to null. See {@link #getValue}.
     */
    protected final Object getValueOrDefault (String name, int line)
    {
        Object value = getValue(this, name, line, compiler.missingIsNull);
        // getValue will raise MustacheException if a variable cannot be resolved and missingIsNull
        // is not configured; so we're safe to assume that any null that makes it up to this point
        // can be converted to nullValue
        return (value == null) ? compiler.computeNullValue(name) : value;
    }

    private final Object getValueIn (Object data, String name, int line)
    {
        if (data == null) {
            throw new NullPointerException(
                "Null context for variable '" + name + "' on line " + line);
        }

        Key key = new Key(data.getClass(), name);
        Mustache.VariableFetcher fetcher = fcache.get(key);
        if (fetcher != null) {
            try {
                return fetcher.get(data, name);
            } catch (Exception e) {
                // zoiks! non-monomorphic call site, update the cache and try again
                fetcher = compiler.collector.createFetcher(data, key.name);
            }
        } else {
            fetcher = compiler.collector.createFetcher(data, key.name);
        }

        // if we were unable to create a fetcher, just return null and our caller can either try
        // the parent context, or do le freak out
        if (fetcher == null) {
            return NO_FETCHER_FOUND;
        }

        try {
            Object value = fetcher.get(data, name);
            fcache.put(key, fetcher);
            return value;
        } catch (Exception e) {
            throw new MustacheException.Context(
                "Failure fetching variable '" + name + "' on line " + line, name, line, e);
        }
    }

    private final Object checkForMissing (String name, int line, boolean missingIsNull, Object value)
    {
        if (value == NO_FETCHER_FOUND) {
            if (missingIsNull) return null;
            throw new MustacheException.Context(
                "No method or field with name '" + name + "' on line " + line, name, line);
        } else {
            return value;
        }
    }
}