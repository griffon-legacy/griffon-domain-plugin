package griffon.plugins.domain

import griffon.persistence.HasMany
import griffon.transform.Domain

@Domain
class Author {
    String name
    String lastName

    @HasMany
    Set<Book> books

    static constraints = {
        name(nullable: false, blank: false, unique: true)
        lastName(nullable: false, blank: false)
    }

    String toString() {"<$id> $name $lastName"}
}
