package griffon.plugins.domain

import griffon.transform.Domain

@Domain
class Sample {
    String name
    String lastName
    Integer num

    static constraints = {
        num(range: 30..40)
        name(nullable: false, blank: false, unique: true)
        lastName(nullable: false, blank: false)
    }

    String toString() {"<$id> $name $lastName [$num]"}
}
