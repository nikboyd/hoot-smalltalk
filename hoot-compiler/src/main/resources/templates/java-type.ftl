[#ftl]
[#-- Copyright 2010,2019 Nikolas S Boyd.
Permission is granted to copy this work provided this copyright statement is retained in all copies.
See https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt for more details. --]
[#include "java-preface.ftl"]

${face.emitSignature().render()}
{
[#if face.hasMetaface()]
  ${face.metaFace().emitSignature().render()}
  {
  [#list face.metaFace().methods() as m]
    ${m.emitSignature().render()};
  [/#list]
  }

[/#if]
[#list face.methods() as m]
  ${m.emitSignature().render()};
[/#list]
}
