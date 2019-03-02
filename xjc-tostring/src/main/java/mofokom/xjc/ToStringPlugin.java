package mofokom.xjc;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.addon.code_injector.Const;
import com.sun.tools.xjc.model.Aspect;
import com.sun.tools.xjc.model.CClassInfoParent;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public class ToStringPlugin extends Plugin {

    public String getOptionName() {
        return "Xtostring";
    }

    public List<String> getCustomizationURIs() {
        return Collections.singletonList(Const.NS);
    }

    public boolean isCustomizationTagName(String nsUri, String localName) {
        return false; //nsUri.equals(Const.NS) && localName.equals("code");
    }

    public String getUsage() {
        return "  -Xtostring      :  implements an overriding toString method";
    }
    Map<JType, JType> imap = new HashMap<JType, JType>();
    Map<JClass, JClass> cmap = new HashMap<JClass, JClass>();

    @Override
    public boolean run(Outline otln, Options optns, ErrorHandler eh) throws SAXException {

        for (ClassOutline c : otln.getClasses()) {
            System.out.println("Processing " + c.implClass.name());
            JClassContainer container = otln.getContainer((CClassInfoParent) c.target, Aspect.IMPLEMENTATION);

            try {
                addToStringMethod(c, otln, container, false);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ToStringPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("----------------------------------");

        }

        return true;
    }

    private ClassOutline getOutline(JClass c, Outline otln) {
        for (ClassOutline c2 : otln.getClasses()) {
            if (c.equals(c2.implClass))
                return c2;
        }
        return null;
    }

    private JDefinedClass addToStringMethod(ClassOutline c, Outline otln, JClassContainer container, boolean inner) throws ClassNotFoundException {
        JClassContainer ccon = (inner) ? container : container.parentContainer();
        System.out.println("implementing toString " + c.implClass.name() + " in " + ((ccon.isPackage()) ? ccon.getPackage().name() : ccon.toString()));
        //if its already defined then return it without processing.

        if (cmap.containsKey(c.implClass)) {
            System.out.println("already done " + c.implClass.name());
            return (JDefinedClass) cmap.get(c.implClass);
        }

        JDefinedClass dc = c.implClass;
        int mods = JMod.PUBLIC;

        JMethod var2 = dc.method(mods, String.class, "toString");

        //JExpr._new(JType.parseStringBuilder.class).
        JType buffy = dc.owner().parseType("java.lang.StringBuilder");
        JType jaxbe = dc.owner().parseType("javax.xml.bind.JAXBElement");
        JType l = dc.owner().parseType("java.util.List");

        JBlock jblock = var2.body();
        jblock.decl(buffy, "buffy");
        jblock.assign(JExpr.ref("buffy"), JExpr._new(buffy));
        jblock.invoke(JExpr.ref("buffy"), "append").arg(JExpr.lit(c.implClass.name() + "{"));
        JBlock jblock2;
        for (String o : dc.fields().keySet()) {
            if("serialVersionUID".equals(o))
                continue;
            jblock.invoke(JExpr.ref("buffy"), "append").arg(JExpr.lit(o + "="));
            JConditional _if;

            if(!dc.fields().get(o).type().isPrimitive()) {
            _if = jblock._if(JExpr.ref(o).eq(JExpr._null()));
            _if._then().invoke(JExpr.ref("buffy"), "append").arg("<null>");


            System.out.println("toString - field " + dc.fields().get(o).type().boxify().fullName() + " "
                    + (dc.fields().get(o).type().boxify().fullName().contains("java.util.List")));

            if (dc.fields().get(o).type().boxify().fullName().contains("java.util.List")) {

                jblock2 = _if._else().block();
                jblock2.invoke(JExpr.ref("buffy"), "append").arg(JExpr.lit("["));

                _if = jblock2.forEach(dc.owner().parseType("java.lang.Object"), "e", JExpr.ref(o)).body()._if(
                        JExpr.ref("e")._instanceof(jaxbe));

                _if._then().invoke(JExpr.ref("buffy"), "append").arg(JExpr.invoke(JExpr.cast(jaxbe, JExpr.ref("e")), "getValue").plus(JExpr.lit(",")));
                _if._else().invoke(JExpr.ref("buffy"), "append").arg(JExpr.invoke(JExpr.ref("e"), "toString").plus(JExpr.lit(",")));
                jblock2.invoke(JExpr.ref("buffy"), "append").arg(JExpr.lit("]"));
            } else if (dc.fields().get(o).type().isPrimitive()) {
                _if._else().invoke(JExpr.ref("buffy"), "append").arg(JExpr.ref(o));
            } else
                _if._else().invoke(JExpr.ref("buffy"), "append").arg(JExpr.invoke(JExpr.ref(o), "toString"));
            }else{
                jblock.invoke(JExpr.ref("buffy"), "append").arg("<null>");
            }

            /*
            expr = expr.plus(JExpr.lit(o + "=").plus(
            JExpr.lit("(" + o +"== null)?\"<null>\":(" + o +" instanceof JAXBElement )?")
            .invoke(JExpr.ref(o),"toString"))
            ).plus(JExpr.lit(","));*/
            jblock.invoke(JExpr.ref("buffy"), "append").arg(JExpr.lit(","));
        }

            jblock.invoke(JExpr.ref("buffy"), "append").arg(JExpr.lit("}"));
        jblock._return(JExpr.invoke(JExpr.ref("buffy"), "toString"));
        cmap.put(c.implClass, dc);
        return dc;
    }
}
