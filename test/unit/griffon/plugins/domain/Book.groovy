package griffon.plugins.domain

import griffon.persistence.BelongsTo
import griffon.transform.Domain

@Domain
class Book {
    String title

    @BelongsTo
    Author author

    static constraints = {
        title(nullable: false, blank: false)
    }

    String toString() { "<$id> $title" }
}
