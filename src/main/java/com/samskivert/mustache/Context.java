package com.samskivert.mustache;

class Context
{
    public final Object data;
    public final Context parent;
    public final int index;
    public final boolean onFirst;
    public final boolean onLast;

    public Context (Object data, Context parent, int index, boolean onFirst, boolean onLast) {
        this.data = data;
        this.parent = parent;
        this.index = index;
        this.onFirst = onFirst;
        this.onLast = onLast;
    }

    public Context nest (Object data, int index, boolean onFirst, boolean onLast) {
        return new Context(data, this, index, onFirst, onLast);
    }
}