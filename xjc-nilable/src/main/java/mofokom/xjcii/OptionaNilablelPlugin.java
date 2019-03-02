package mofokom.xjcii;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.addon.code_injector.Const;
import com.sun.tools.xjc.model.Aspect;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CClassInfoParent;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public class OptionaNilablelPlugin extends Plugin {

    private String INTERFACE_SUFFIX = "Interface";

    public String getOptionName() {
        return "Xii";
    }

    public List<String> getCustomizationURIs() {
        return Collections.singletonList(Const.NS);
    }

    public boolean isCustomizationTagName(String nsUri, String localName) {
        return true; //nsUri.equals(Const.NS) && localName.equals("code");
    }

    public String getUsage() {
        return "  -Xnil      :  add a has method for each nilable optional";
    }
    Map<JType, JType> gmap = new HashMap<JType, JType>();
    Map<JType, JType> imap = new HashMap<JType, JType>();
    Map<JClass, JClass> cmap = new HashMap<JClass, JClass>();

    @Override
    public boolean run(Outline otln, Options optns, ErrorHandler eh) throws SAXException {

        for (ClassOutline c : otln.getClasses()) {
            //c.getDeclaredFields()[0].getPropertyInfo().getSchemaComponent().getOwnerSchema()
            System.out.println("Processing class for " + c.implClass.name());
            JClassContainer container = otln.getContainer((CClassInfoParent) c.target, Aspect.IMPLEMENTATION);

            //todo check container for package and set true accordingly
            try {
                createInterface(c, otln, container, true);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(OptionaNilablelPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("----------------------------------");

        }
        //System.exit(1);

        for (ClassOutline c : otln.getClasses()) {
            System.out.println("Processing Methods for " + c.implClass.name());
            JClassContainer container = otln.getContainer((CClassInfoParent) c.target, Aspect.IMPLEMENTATION);

            try {
                JDefinedClass dc = (JDefinedClass) cmap.get(c.implClass);
                m2(c, dc);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(OptionaNilablelPlugin.class.getName()).log(Level.SEVERE, null, ex);
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

    private JDefinedClass createInterface(ClassOutline c, Outline otln, JClassContainer container, boolean inner) throws ClassNotFoundException {
        JClassContainer ccon = container;


        //if its already defined then return it without processing.
        if (cmap.containsKey(c.implClass)) {
            System.out.println("already done " + c.implClass.name());
            return (JDefinedClass) cmap.get(c.implClass);
        }

        if (container.parentContainer().isClass()) {
            System.out.println("It is an inner class");
            ccon = container.parentContainer();

            if (cmap.containsKey(container.parentContainer())) {
                System.out.println("Parent interface has been created");
                ccon = (JClassContainer) cmap.get(container.parentContainer());
            } else {
                CClassInfo ci = getOutline(((JClass) container.parentContainer()), otln).target;
                ClassOutline co = otln.getClazz(ci);
                JDefinedClass dc2 = (JDefinedClass) container.parentContainer();
                JClassContainer dcc = dc2.parentContainer();
                System.out.println("Parent not found, Creating " + dc2.name() + " in " + ((dcc.isPackage()) ? dcc.getPackage().name() : dcc.toString()));

                //create parent
                ccon = createInterface(co, otln, dc2, false);
                //ccon = container.parentContainer();
            }
        } else if (c.implClass.outer() == null)
            ccon = container.getPackage();

        System.out.println("creating interface " + c.implClass.name() + " in " + ((ccon.isPackage()) ? ccon.getPackage().name() : ccon.toString()));

        /*
        container = createInterface(getOutline(c.implClass.outer(), otln), otln, container);
        System.out.println(c.implClass.name() + " is outer of " + c.implClass.outer().name() + " " + cmap.containsKey(c.implClass.outer()));
        }*/

        JDefinedClass dc = otln.getClassFactory().createInterface(ccon, c.implClass.name() + INTERFACE_SUFFIX, null);

        c.implClass._implements(dc);
        imap.put(c.implClass.unboxify(), dc.unboxify());
        cmap.put(c.implClass, dc);
        System.out.println("put " + c.implClass.name() + " " + dc.name());

        if (!inner)
            return dc;

        for (Iterator<JDefinedClass> ii = c.implClass.classes(); ii.hasNext();) {
            JClass c1 = ii.next();
            if (c1.name().endsWith("Interface"))
                continue;

            System.out.println("Processing Inner class " + c1 + " for " + c.toString());

            CClassInfo ci = getOutline(c1, otln).target;
            ClassOutline co = otln.getClazz(ci);
            ClassOutline co2 = getOutline(dc, otln);
            ccon = dc;
            System.out.println("Creating inner " + co.implClass.name() + " of " + dc + " in " + ccon);
            createInterface(co, otln, ccon, true);
        }

        return dc;
    }

    private void m2(ClassOutline c, JDefinedClass dc) throws ClassNotFoundException {
        JMethod[] mm = (JMethod[]) c.implClass.methods().toArray(new JMethod[]{});
        Map<String, JFieldVar> fields = c.implClass.fields();

        for (JFieldVar f : fields.values()) {

            JType type = f.type();
            System.out.println("field " + f.name() + " " + type.fullName() + " " + type.isArray() + " " + type.isReference() + " " + type.unboxify() + " " + type.getClass());
            JType gtype = generateConcreteType(dc, type);
            //JType gtype = generateGenericType(dc, type);
            gtype = f.type();

            if (f.type().fullName().contains("List")) {
                /*
                System.out.println("initialize field " + f.name() + " " + gtype.fullName() + " " + gtype.isArray() + " " + gtype.isReference() + " " + gtype.unboxify() + " " + gtype.getClass());
                StringBuilder bob = new StringBuilder(gtype.fullName());
                bob.insert(10, "Array");

                gtype = dc.owner().parseType(bob.toString());
                 */
                //if.init(JExpr._new(gtype));
                //f.type(gtype);
            } else
                f.type(gtype);



        }

        List<JMethod> c2 = Arrays.asList(mm);
        for (JMethod m : c2) {
            String name = m.name();
            JType type = m.type();

            JType gtype = generateConcreteType(dc, type);

            if (!m.type().name().contains("List"))
                m.type(gtype);



            System.out.println("method " + m.name() + " " + type.fullName() + " " + type.isArray() + " " + type.isReference() + " " + type.unboxify() + " " + type.getClass() + ">" + gtype.name());

            //change the impl gtype to the interface gtype
            gtype = generateGenericType(dc, type);
            //gtype = generateConcreteType(dc, type);


            if (imap.containsKey(type))
                gtype = imap.get(type);

            System.out.println("resolved to " + gtype.fullName());

            //create the method in the interface
            int mods = JMod.PUBLIC;
            JMethod ifMthd = dc.method(mods, gtype, name);
            boolean doIt = false;

            JType[] t = m.listParamTypes();
            JVar[] v = m.listParams();

            for (int i = 0; i < v.length; i++) {
                JType tt = t[i];
                if (tt.isArray())
                    tt = imap.get(tt.unboxify());
                if (imap.containsKey(tt)) {
                    doIt = true;

                    tt = imap.get(tt);
                }
                //old v[i].type(generateGenericType(dc, tt));
                v[i].type(generateConcreteType(dc, tt));
                ifMthd.param(tt, v[i].name());
            }


            if (name.startsWith("get") && !m.type().name().contains("List")) {
                //m.type(generateGenericType(dc, type));
                m.type(generateConcreteType(dc, type));
            }
            /*
            if (name.startsWith("get") && !m.type().name().contains("List")) {
            c.implClass.methods().remove(m);
            JMethod var2 = c.implClass.method(mods, type, name);
            String n = name.substring(3, 4).toLowerCase() + name.substring(4);
            var2.body()._return(JExpr.ref(n));
            }*/

            if (name.startsWith("set") && doIt) {
                c.implClass.methods().remove(m);
                JMethod var2 = c.implClass.method(mods, gtype, name);
                for (int i = 0; i < v.length; i++) {
                    JType tt = t[i];
                    if (tt.isArray())
                        tt = imap.get(tt.unboxify());

                    System.out.println("checking type " + tt.fullName());
                    if (imap.containsKey(tt))
                        tt = imap.get(tt);
                    var2.param(tt, v[i].name());
                    StringBuilder f = new StringBuilder(name);
                    f.delete(0, 3);
                    var2.body().assign(JExpr.refthis(f.toString().toLowerCase()), JExpr.cast(t[i], JExpr.ref(v[i].name())));
                }

            }
        }
    }

    private JType generateGenericType(JDefinedClass dc, JType type) throws ClassNotFoundException {
        JType newType = type;

        //fix the return gtype
        if (type.isReference() && type.fullName().startsWith("java.util.List")) {
            StringBuilder bob = new StringBuilder(type.fullName());

            //TODO make this more specific
            if (!bob.toString().contains("Serializable")) {
                bob.insert(bob.indexOf("<") + 1, "? extends ");
                bob.insert(bob.length() - 1, INTERFACE_SUFFIX);
            }

            newType = dc.owner().parseType(bob.toString());
            System.out.println(type.fullName() + " " + type.isArray() + " " + type.isReference() + " " + type.unboxify() + " " + type.getClass());

            //JTypeVar tt = .generify("java.util.List<" + dc.name()+">");
            //gtype = tt;
        }
        return newType;
    }

    private JType generateConcreteType(JDefinedClass dc, JType type) throws ClassNotFoundException {
        if (imap.containsKey(type))
            return imap.get(type);

        JType newType = type;
        //fix the return gtype
        System.out.println("processing " + type.fullName());
        if (type.isReference() && type.fullName().startsWith("java.util.List")) {
            StringBuilder bob = new StringBuilder(type.fullName());

            //TODO make this more specific
            if (!bob.toString().contains("Serializable")) {
                bob.insert(bob.length() - 1, INTERFACE_SUFFIX);
                int b = 0;
            }

            newType = dc.owner().parseType(bob.toString());
            System.out.println(type.fullName() + " " + type.isArray() + " " + type.isReference() + " " + type.unboxify() + " " + type.getClass());

            //JTypeVar tt = .generify("java.util.List<" + dc.name()+">");
            //gtype = tt;
        }
        return newType;
    }
}
