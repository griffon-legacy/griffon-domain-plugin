package griffon.util

@griffon.transform.Sortable(excludes = ['index'])
class Person {
    String firstName
    String lastName
    String address
    int zip
    String country
    int index

    String toString() {
        super.toString() + "[" + [
                "firstName: $firstName",
                "lastName: $lastName",
                "address: $address",
                "zip: $zip",
                "country: $country",
                "index: $index",
        ].join(', ') + "]"
    }
}
