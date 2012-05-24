package griffon.plugins.domain

import griffon.plugins.domain.atoms.IntegerValue
import griffon.plugins.domain.atoms.StringValue
import griffon.transform.Domain

@Domain
class Sample {
    final StringValue name = new StringValue()
    final StringValue lastName = new StringValue()
    final IntegerValue num = new IntegerValue()

    static constraints = {
        num(range: 0..10)
        name(nullable: false, blank: false, unique: true)
        lastName(nullable: false, blank: false)
    }

    String toString() {"<$id> $name $lastName [$num]"}
}
