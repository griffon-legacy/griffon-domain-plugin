package griffon.plugins.domain;

import griffon.plugins.validation.constraints.ConstraintDef;
import griffon.transform.Domain;

import java.util.List;
import java.util.Map;

import static griffon.plugins.validation.constraints.Constraints.*;

@Domain
public class Sample2 {
    private Integer num;
    private String name;
    private String lastName;

    public static Map<String, List<ConstraintDef>> constraints = map()
        .e("num", list(range(30, 40)))
        .e("name", list(nullable(false), blank(false), unique(true)))
        .e("lastName", list(nullable(false), blank(false)));
}
