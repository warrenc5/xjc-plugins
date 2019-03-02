package mofokom.xjc;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
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
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public class DebugPlugin extends Plugin {


    public String getOptionName() {
        return "Xdebug";
    }

    public List<String> getCustomizationURIs() {
        return Collections.singletonList(Const.NS);
    }

    public boolean isCustomizationTagName(String nsUri, String localName) {
        return false; //nsUri.equals(Const.NS) && localName.equals("code");
    }

    public String getUsage() {
        return "  -Xdebug      :  shows whats being generated";
    }
    Map<JType, JType> imap = new HashMap<JType, JType>();
    Map<JClass, JClass> cmap = new HashMap<JClass, JClass>();

    @Override
    public boolean run(Outline otln, Options optns, ErrorHandler eh) throws SAXException {

        for (ClassOutline c : otln.getClasses()) {
            System.out.println("Processing " + c.implClass.name());
            JClassContainer container = otln.getContainer((CClassInfoParent) c.target, Aspect.IMPLEMENTATION);

            /*
            try {
                makeCloneable(c, otln, container, false);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(DebugPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
            * 
            */
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

    private JDefinedClass makeCloneable(ClassOutline c, Outline otln, JClassContainer container, boolean inner) throws ClassNotFoundException {
        JClassContainer ccon = (inner) ? container : container.parentContainer();
        System.out.println("implementing clonable " + c.implClass.name() + " in " + ((ccon.isPackage()) ? ccon.getPackage().name() : ccon.toString()));
        //if its already defined then return it without processing.

        if (cmap.containsKey(c.implClass)) {
            System.out.println("already done " + c.implClass.name());
            return (JDefinedClass) cmap.get(c.implClass);
        }

        JDefinedClass dc = c.implClass;

        dc._implements(Cloneable.class);
        cmap.put(c.implClass, dc);
        int mods = JMod.PUBLIC;

        JMethod var2 = dc.method(mods, Object.class, "clone");
        var2._throws(CloneNotSupportedException.class);

        var2.body()._return(JExpr.invoke(JExpr._super(),"clone"));

        return dc;
    }
}
