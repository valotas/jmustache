package com.samskivert.mustache;

import java.util.ArrayList;
import java.util.List;

import com.samskivert.mustache.Mustache.Compiler;

class Accumulator {
    public Accumulator (Compiler compiler) {
        _compiler = compiler;
        _segs = new ArrayList<Segment>();
    }

    public boolean justOpenedOrClosedBlock () {
        // return true if we just closed a block segment; we'll handle just opened elsewhere
        return (!isEmpty() && _segs.get(_segs.size()-1) instanceof BlockSegment);
    }
    
    private final boolean isEmpty() {
    	return _segs.isEmpty();
    }

    public final void addTextSegment (StringBuilder text) {
        if (text.length() > 0) {
        	addSegment(new StringSegment(text.toString()));
            text.setLength(0);
        }
    }
    
    private final void addSegment(Segment seg) {
    	_segs.add(seg);
    }

    public Accumulator addTagSegment (final StringBuilder accum, final int tagLine) {
        final Accumulator outer = this;
        String tag = accum.toString().trim();
        final String tag1 = tag.substring(1).trim();
        accum.setLength(0);

        switch (tag.charAt(0)) {
        case '#':
            requireNoNewlines(tag, tagLine);
            return new Accumulator(_compiler) {
                @Override public boolean justOpenedOrClosedBlock () {
                    // if we just opened this section, we'll have no segments
                    return super.isEmpty() || super.justOpenedOrClosedBlock();
                }
                @Override public Segment[] finish () {
                    throw new MustacheParseException(
                        "Section missing close tag '" + tag1 + "'", tagLine);
                }
                @Override protected Accumulator addCloseSectionSegment (String itag, int line) {
                    requireSameName(tag1, itag, line);
                    outer.addSegment(
                        new SectionSegment(itag, super.finish(), tagLine, _compiler));
                    return outer;
                }
            };

        case '>':
            addSegment(new IncludedTemplateSegment(tag1, _compiler));
            return this;

        case '^':
            requireNoNewlines(tag, tagLine);
            return new Accumulator(_compiler) {
                @Override public boolean justOpenedOrClosedBlock () {
                    // if we just opened this section, we'll have no segments
                    return super.isEmpty() || super.justOpenedOrClosedBlock();
                }
                @Override public Segment[] finish () {
                    throw new MustacheParseException(
                        "Inverted section missing close tag '" + tag1 + "'", tagLine);
                }
                @Override protected Accumulator addCloseSectionSegment (String itag, int line) {
                    requireSameName(tag1, itag, line);
                    outer.addSegment(new InvertedSectionSegment(itag, super.finish(), tagLine));
                    return outer;
                }
            };

        case '/':
            requireNoNewlines(tag, tagLine);
            return addCloseSectionSegment(tag1, tagLine);

        case '!':
            // comment!, ignore
            return this;

        case '&':
            requireNoNewlines(tag, tagLine);
            addSegment(new VariableSegment(tag1, false, tagLine));
            return this;

        default:
            requireNoNewlines(tag, tagLine);
            addSegment(new VariableSegment(tag, _compiler.escapeHTML, tagLine));
            return this;
        }
    }

    public Segment[] finish () {
        return _segs.toArray(new Segment[_segs.size()]);
    }

    protected Accumulator addCloseSectionSegment (String tag, int line) {
        throw new MustacheParseException(
            "Section close tag with no open tag '" + tag + "'", line);
    }

    private static void requireNoNewlines (String tag, int line) {
        if (tag.indexOf("\n") != -1 || tag.indexOf("\r") != -1) {
            throw new MustacheParseException(
                "Invalid tag name: contains newline '" + tag + "'", line);
        }
    }

    private static void requireSameName (String name1, String name2, int line)
    {
        if (!name1.equals(name2)) {
            throw new MustacheParseException("Section close tag with mismatched open tag '" +
                                             name2 + "' != '" + name1 + "'", line);
        }
    }

    private final Compiler _compiler;
    private final List<Segment> _segs;
}