package griffon.plugins.domain

import griffon.transform.Domain
import static griffon.plugins.validation.constraints.Constraints.*

@Domain
class Sample {
    String name
    String lastName
    Integer num

    /*
    static constraints = {
        num(range: 30..40)
        name(nullable: false, blank: false, unique: true)
        lastName(nullable: false, blank: false)
    }
    */

    static constraints = [
        num: [range(30, 40)],
        name: [nullable(false), blank(false), unique(true)],
        lastName: [nullable(false), blank(false)]
    ]

    String toString() {"<$id> $name $lastName [$num]"}
}
