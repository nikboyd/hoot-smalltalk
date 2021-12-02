[#ftl]
[#-- Copyright 2010,2019 Nikolas S Boyd.
Permission is granted to copy this work provided this copyright statement is retained in all copies.
--]
[#include "java-preface.ftl"]

${face.emitSignature().render()}
{
[#if face.hasMetaface()]
  [#if !face.isMetaclassBase()]
  public static Metaclass type() { return (Metaclass)Metaclass.$class; }
  @Override public Metaclass $class() { return (Metaclass)Metaclass.$class; }
  ${face.metaFace().emitSignature().render()}
  {
    static final ${face.name()}.Metaclass $class = new ${face.name()}.Metaclass();
    public Metaclass() {
      this(${face.name()}.Metaclass.class);
    }

    public Metaclass(java.lang.Class aClass) {
      super(aClass);
      instanceClass = ${face.name()}.class;
    }

    [#list face.metaFace().emitLocalVariables() as v]
    ${v.render()};
    [/#list]
  [#list face.metaFace().methods() as m]
  ${m.emitScope().render()}
  [/#list]
  }
  [/#if]

[/#if]
[#list face.emitLocalVariables() as v]
  ${v.render()};
[/#list]
[#list face.methods() as m]
${m.emitScope().render()}
[/#list]
}
