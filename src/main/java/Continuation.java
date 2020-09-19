import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Continuation {
    private List<Object> code = new ArrayList<>();
    private final Map<String, Object> scope = new HashMap<String, Object>();
    private int current = 0;

    public Continuation() {
    }

    public Continuation(List<Object> code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "Continuation{" +
                "contents=" + code +
                ", scope=" + scope +
                '}';
    }

    public List<Object> getCode() {
        return code;
    }

    public Map<String, Object> getScope() {
        return scope;
    }

    public Object next() {
        if (current == code.size()) {
            return null;
        }

        return code.get(++current);
    }

    public Boolean hasLocal(String name) {
        return getLocal(name) != null;
    }

    public Object getLocal(String name) {
        return scope.get(name);
    }

    public void setLocal(String name, Object val) {
        scope.put(name, val);
    }
}
