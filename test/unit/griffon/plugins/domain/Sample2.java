package griffon.plugins.domain;

import griffon.plugins.validation.constraints.ConstraintDef;
import org.codehaus.griffon.runtime.validation.AbstractValidateable;

import java.util.List;
import java.util.Map;

import static griffon.plugins.validation.constraints.Constraints.*;

public class Sample2 extends AbstractValidateable {
    private Integer num;
    private String name;
    private String lastName;

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public static Map<String, List<ConstraintDef>> constraints = map()
        .e("num", list(range(30, 40)))
        .e("name", list(nullable(false), blank(false), unique(true)))
        .e("lastName", list(nullable(false), blank(false)));
}
