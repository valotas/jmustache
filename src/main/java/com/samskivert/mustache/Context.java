package com.samskivert.mustache;



class Context
{

    public final Object data;
    public final Context parent;
    public final Mustache.Compiler compiler;
    public final int index;
    public final boolean onFirst;
    public final boolean onLast;

    Context (Object data, Mustache.Compiler compiler, int index, boolean onFirst, boolean onLast) {
    	this(data, null, compiler, index, onFirst, onLast);
    }
    
    private Context (Object data, Context parent, Mustache.Compiler compiler, int index, boolean onFirst, boolean onLast) {
        this.data = data;
        this.parent = parent;
        this.compiler = compiler;
        this.index = index;
        this.onFirst = onFirst;
        this.onLast = onLast;
    }

    Context nest (Object data, int index, boolean onFirst, boolean onLast) {
        return new Context(data, this, compiler, index, onFirst, onLast);
    }
}