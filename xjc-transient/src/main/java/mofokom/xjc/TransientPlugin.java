package mofokom.xjc;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.Aspect;
import com.sun.tools.xjc.model.CClassInfoParent;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlTransient;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;


public class TransientPlugin extends Plugin {

    private String NS = "http://www.mofokom.biz/xjc/transient";

    public String getOptionName() {
        return "Xtransient";
    }

    public List<String> getCustomizationURIs() {
        return Collections.singletonList(NS);
    }

    public boolean isCustomizationTagName(String nsUri, String localName) {
        return nsUri.equals(NS) && localName.equals("transient");
    }

    @Override
    public void onActivated(Options opts) throws BadCommandLineException {
    }



    public String getUsage() {
        return "  -Xtransient      :  marks <transient> customizations XmlTransient";
    }
    Map<JType, JType> imap = new HashMap<JType, JType>();
    Map<JClass, JClass> cmap = new HashMap<JClass, JClass>();

    @Override
    public boolean run(Outline otln, Options optns, ErrorHandler eh) throws SAXException {

        for (ClassOutline c : otln.getClasses()) {

            System.out.println("Transient pluging processing " + c.implClass.name());
            JClassContainer container = otln.getContainer((CClassInfoParent) c.target, Aspect.IMPLEMENTATION);

            try {
                markTransient(c, otln, container, false);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(TransientPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("----------------------------------");

        }
        return true;
    }

    @Override
    public void postProcessModel(Model model, ErrorHandler errorHandler) {
    }




    private ClassOutline getOutline(JClass c, Outline otln) {
        for (ClassOutline c2 : otln.getClasses()) {
            if (c.equals(c2.implClass))
                return c2;
        }
        return null;
    }

    private JDefinedClass markTransient(ClassOutline c, Outline otln, JClassContainer container, boolean inner) throws ClassNotFoundException {
        JClassContainer ccon = (inner) ? container : container.parentContainer();

        //if its already defined then return it without processing.

        if (cmap.containsKey(c.implClass)) {
            System.out.println("already done " + c.implClass.name());
            return (JDefinedClass) cmap.get(c.implClass);
        }

        JDefinedClass dc = c.implClass;
        Map<String, JFieldVar> fields = dc.fields();

        CPluginCustomization cp = c.target.getCustomizations().find(NS);//,"transient");

        if (cp != null) {
            System.out.print("transient customizations found");
            cp.markAsAcknowledged();
            String[] fm = cp.element.getTextContent().split(" ");
            List<String> fl = Arrays.asList(fm);
            System.out.println("transient customizations for " + dc.fullName() + "  " + fl.toString());
            for (Entry<String, JFieldVar> e : fields.entrySet()) {
                //c.getDeclaredFields()[0].getPropertyInfo().   getCustomizations().e.getValue()
                if (fl.contains(e.getKey())) {
                    dc.removeField(e.getValue());
                    JFieldVar nf = dc.field(e.getValue().mods().getValue(),
                            e.getValue().type(),
                            e.getValue().name());
                    System.out.println("marking field transient" + c.implClass.name() + " in " + ((ccon.isPackage()) ? ccon.getPackage().name() : ccon.toString()) + " " + e.getKey());
                    nf.annotate(XmlTransient.class);
                }



            }
        }


        return dc;
    }
}
