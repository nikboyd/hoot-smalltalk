package Hoot.Runtime;

import java.util.*;
import java.lang.reflect.*;
import org.junit.*;
import static org.junit.Assert.*;

import Hoot.Runtime.Names.Name;
import Hoot.Runtime.Names.TypeName;
import Hoot.Runtime.Names.Signature;
import Hoot.Runtime.Names.Primitive;
import Hoot.Runtime.Faces.Logging;
import Hoot.Runtime.Behaviors.Mirror;
import Hoot.Runtime.Behaviors.Typified;
import static Hoot.Runtime.Maps.Library.*;
import static Hoot.Runtime.Maps.ClassPath.*;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Names.Keyword.Object;

/**
 * Confirms proper operation of types and their names.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class TypesTest implements Logging {

    public class NestedSample { }
    @BeforeClass public static void prepare() { CurrentLib.loadHootSmalltalkLibs(); CurrentPath.println(2); }

    @Test public void depthTest() {
        assertTrue(Primitive.hierarchyDepth(null) == 0);
        assertTrue(Primitive.hierarchyDepth(getClass()) == 2);
    }

    @Test public void nameTest() {
        assertTrue("".equals(Name.typeName("java.lang")));
        assertTrue("int".equals(Name.typeName("java.lang.int")));
        assertTrue("Float".equals(Name.typeName("java.lang.Float")));
        assertTrue("Class$Metaclass".equals(Name.typeName("Hoot.Behaviors.Class.Metaclass")));
        assertTrue("Class$Metaclass".equals(Name.typeName("Hoot.Behaviors.Class$Metaclass")));
        assertTrue("Subject$Metatype".equals(Name.typeName("Smalltalk.Core.Subject.Metatype")));
        assertTrue("Subject$Metatype".equals(Name.typeName("Smalltalk.Core.Subject$Metatype")));
        assertTrue("Object".equals(Name.withoutMeta(Name.typeName("Hoot.Behaviors.Object$Metaclass"))));

        assertTrue("java.lang".equals(Name.packageName("java.lang")));
        assertTrue("java.lang".equals(Name.packageName("java.lang.int")));
        assertTrue("java.lang".equals(Name.packageName("java.lang.Float")));
        assertTrue("Hoot.Behaviors".equals(Name.packageName("Hoot.Behaviors.Class")));
        assertTrue("Hoot.Behaviors".equals(Name.packageName("Hoot.Behaviors.Class.Metaclass")));
    }

    @Test public void wildTypes() {
        TypeName wildSample = TypeName.with("Hoot", "Runtime", "Names", "*");
        assertTrue(wildSample.typeName().equals("Hoot.Runtime.Names.*"));
        assertTrue(TypeName.fromName("Hoot.Runtime.Names.*").typeName().equals(wildSample.typeName()));
        assertTrue(wildSample.findClass() == null);
        assertTrue(wildSample.isWild());

        TypeName wildJava = TypeName.with("Java", "Util", "Math", "**");
        assertTrue(wildJava.typeName().equals("java.util.Math.*"));

        wildJava = TypeName.with("Java", "Util", "*");
        assertTrue(wildJava.typeName().equals("java.util.*"));
    }

    @Test public void typeConversions() {
        assertTrue(TypeName.VoidType.typeName().equals("void"));
        assertTrue(TypeName.with().typeName().equals(Object));
        assertTrue(TypeName.with("Any").typeName().equals("?"));
        assertTrue(TypeName.with("Int").typeName().equals("int"));
        assertTrue(TypeName.with("Char").typeName().equals("char"));
        assertTrue(TypeName.with("Float").typeName().equals("Float"));
        assertTrue(TypeName.with("Double").typeName().equals("Double"));
        assertTrue(TypeName.with("Character").typeName().equals("Character"));
        assertTrue(TypeName.with("Boolean").typeName().equals("Boolean"));
        assertTrue(TypeName.with("Object").typeName().equals("Object"));

        assertTrue(TypeName.with("Java", "Lang", "Int").typeName().equals("int"));
        assertTrue(TypeName.with("Java", "Lang", "Char").typeName().equals("char"));
        assertTrue(TypeName.with("Java", "Lang", "Boolean").typeName().equals("boolean"));
        assertTrue(TypeName.with("Java", "Lang", "Float").typeName().equals("java.lang.Float"));
        assertTrue(TypeName.with("Java", "Lang", "Double").typeName().equals("java.lang.Double"));

        assertTrue(TypeName.with("Java", "Lang", "Object").typeName().equals("java.lang.Object"));
        assertTrue(TypeName.from(TypeName.JavaRoot).typeName().equals("java.lang.Object"));
        assertTrue(TypeName.ObjectType.isObjectType());
        assertTrue(TypeName.fromOther(TypeName.ObjectType().findType()).isObjectType());

//        assertTrue(TypeName.from(getClass()).typeName().equals(getClass().getName()));
//        assertTrue(TypeName.from(getClass()).findType().fullName().equals(getClass().getCanonicalName()));
        assertTrue(TypeName.fromOther(new NestedSample()).typeName().equals(NestedSample.class.getName()));
        assertTrue(TypeName.with("Hoot", "Behaviors", "Boolean").typeName().equals("Hoot.Behaviors.Boolean"));
        assertTrue(TypeName.with("Hoot", "Behaviors", "Object").typeName().equals("Hoot.Behaviors.Object"));
        assertTrue("Hoot Behaviors Object class".equals(TypeName.fromName("Hoot.Behaviors.Object$Metaclass").hootName()));
    }

    @Test public void typeInference() {
        assertTrue(TypeName.inferFrom("aPoint").typeName().equals("Point"));
        assertTrue(TypeName.inferFrom("anInterest").typeName().equals("Interest"));
        assertTrue(TypeName.inferFrom("aninterest").typeName().equals("Hoot.Behaviors.Object"));
        assertTrue(TypeName.inferFrom("").typeName().equals("Hoot.Behaviors.Object"));
        assertTrue(TypeName.inferFrom(null).typeName().equals("Hoot.Behaviors.Object"));
    }

    @Test public void sampleTypes() {
        int[] samples = { 0, 1, 2, };
        TypeName sampleType = TypeName.fromOther(samples);
        String sampleName = sampleType.typeName();
        assertTrue(sampleType.isArrayed());
        assertTrue("int[]".equals(sampleType.shortName()));
        assertTrue("int[]".equals(sampleType.fullName()));
        assertTrue("int[]".equals(sampleType.typeName()));

        String[] texts = { "a", "b", "c", };
        TypeName textType = TypeName.fromOther(texts);
        assertTrue("String[]".equals(textType.shortName()));
        assertTrue("java.lang.String[]".equals(textType.fullName()));
        assertTrue("java.lang.String[]".equals(textType.typeName()));
    }

    @Test public void sampleGenerics() {
        ArrayList<String> list = emptyList(String.class);
        Class<?> lc = list.getClass();
        Mirror m = Mirror.forClass(lc);
        report(m.description());

        Class<?>[] argTypes = { Object.class, };
        Method md = m.findMethod("add", argTypes);
        Class<?>[] margTypes = md.getParameterTypes();
        Type[] margGens = md.getGenericParameterTypes();
        Signature s = Signature.from(md);
        List<TypeName> mdArgTypes = s.argumentTypeNames();
        assertTrue(mdArgTypes.get(0).isGeneric());
        report(s.matchSignature());

        List<Typified> ts = Mirror.argumentTypes(md);
        Class[] unw = Mirror.unwrapTypes(ts);

        TypeVariable<?>[] tps = lc.getTypeParameters();
        ParameterizedType pt = (ParameterizedType)lc.getGenericSuperclass();
        Type[] gts = pt.getActualTypeArguments();
        report(pt.toString());
        report(wrap(gts).toString());
    }

} // TypesTest
