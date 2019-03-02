package mofokom.xjcii;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.addon.code_injector.Const;
import com.sun.tools.xjc.outline.Outline;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public class ValidationPlugin extends Plugin {

    public String getOptionName() {
        return "Xvalid";
    }

    public List<String> getCustomizationURIs() {
        return Collections.singletonList(Const.NS);
    }

    public boolean isCustomizationTagName(String nsUri, String localName) {
        return true; //nsUri.equals(Const.NS) && localName.equals("code");
    }

    public String getUsage() {
        return "  -Xvalid      :  add xsd as JEE validation annotations";
    }
    Map<JType, JType> gmap = new HashMap<JType, JType>();
    Map<JType, JType> imap = new HashMap<JType, JType>();
    Map<JClass, JClass> cmap = new HashMap<JClass, JClass>();

    @Override
    public boolean run(Outline otln, Options optns, ErrorHandler eh) throws SAXException {
        System.out.println(optns.getSchemaLanguage().getDeclaringClass());
        return true;
    }

}
