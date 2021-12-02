[#ftl]
[#-- Copyright 2010,2019 Nikolas S Boyd.
Permission is granted to copy this work provided this copyright statement is retained in all copies.
--]
[#if file.notice()??]
/**
* ${file.notice()}
*/
[/#if]
package ${file.packageName()};

[#list file.faceImports() as faceImport]
${faceImport.emitItem().render()}
[/#list]
