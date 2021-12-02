package Hoot.Runtime.Emissions;

import java.util.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;
import Hoot.Runtime.Faces.Logging;

/**
 * Emits generated code using StringTemplate v4.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2019 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Emission implements Emitter, Logging {

    // standard emission components
    public static final String Items = "items";
    public static final String Values = "values";
    public static final String Value = "value";
    public static final String Notes = "notes";

    private static final String Emit = "emit";
    public static final Emission NoValue = null;

    private static final ST None = null;
    private static final String CodeFile = "CodeTemplates.stg";
    private static STGroupFile CodeGroup = null;
    private static STGroupFile getCodeGroup() {
        if (CodeGroup == null) {
            CodeGroup = new STGroupFile(CodeFile);
        }
        return CodeGroup;
    }

    private ST builder;

    /**
     * @return whether the list is empty
     * @param <ItemType> an item type
     * @param list a list
     */
    public static <ItemType> boolean isEmpty(List<ItemType> list) {
        return (list == null || list.isEmpty()); }

    /**
     * @return a capitalized name
     * @param name a name
     */
    public static String capitalize(String name) {
        if (name == null || name.length() == 0) return EmissionSource.Empty;
        StringBuilder buffer = new StringBuilder(name.trim());
        buffer.setCharAt(0, Character.toUpperCase(buffer.charAt(0)));
        return buffer.toString();
    }

    /**
     * @return a new Emission
     * @param name a named code generator
     */
    public static Emission emit(String name) { return named(Emit + name); }
    public static Emission named(String groupName) {
        Emission result = new Emission();
        result.builder = getCodeGroup().getInstanceOf(groupName);
        return result;
    }

    public Emission notice(String notice) { return this.with("notice", notice); }
    public Emission comment(String comment) { return this.with("comment", comment); }
    public Emission packageName(String packageName) { return this.with("packageName", packageName); }
    public Emission signature(ST signature) { return this.with("signature", signature); }
    public Emission type(Emission type) { return this.with("type", type); }
    public Emission type(String type) { return this.with("type", type); }
    public Emission base(Emission base) { return this.with("base", base); }
    public Emission details(Emission details) { return this.with("details", details); }
    public Emission notes(List<Emission> notes) { return this.with(Notes, notes); }
    public Emission notes(Emission notes) { return this.with(Notes, notes); }
    public Emission names(List<String> names) { return isEmpty(names) ? this : this.with("names", names); }
    public Emission name(String name) { return this.with("name", name); }
    public Emission name(Emission value) { return this.with("name", value == null ? None : value.result()); }
    public Emission values(List<Emission> values) { return this.with(Values, values); }
    public Emission values(Emission values) { return this.with(Values, values); }
    public Emission value(Emission value) { return this.value(value == null ? None : value.result()); }
    public Emission value(ST value) { return this.with(Value, value); }
    public Emission value(String value) { return this.with(Value, value); }
    public Emission items(List<Emission> items) { return this.with(Items, items); }
    public Emission items(ST items) { return this.with(Items, items); }
    public Emission item(ST item) { return this.with("item", item); }
    public Emission item(String item) { return this.with("item", item); }

    public Emission with(String name, Emission e) { return this.with(name, e == null ? None : e.result()); }
    public Emission with(String name, ST item) { builder.add(name, item); return this; }
    public Emission with(String name, String item) { builder.add(name, item); return this; }
    public <ItemType> Emission with(String name, List<ItemType> items) {
        if (isEmpty(items)) {
            builder.add(name, new ArrayList());
            return this;
        }

        if (items.get(0) instanceof Emission) {
            builder.add(name, new ArrayList());
            items.forEach(item -> this.with(name, (Emission)item));
        }
        else {
            builder.add(name, items);
        }

        return this;
    }


    @Override public ST emitCode() { return result(); }
    public ST result() { return this.builder; }
    public String render() { return result().render(); }
}
