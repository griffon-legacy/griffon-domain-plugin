package griffon.util

class SortableTests extends GroovyTestCase {
    void testSmoke() {
        Person person1 = new Person(
                firstName: 'a',
                lastName: 'b',
                address: 'c',
                zip: 1,
                country: 'd',
                index: 1
        )

        Person person2 = new Person(
                firstName: 'a',
                lastName: 'b',
                address: 'c',
                zip: 1,
                country: 'd',
                index: 1
        )

        Person person3 = new Person(
                firstName: 'za',
                lastName: 'zb',
                address: 'zc',
                zip: 91,
                country: 'zd',
                index: 1
        )

        assert person1 <=> person2 == 0
        assert person1 <=> person3 == -1
        assert person3 <=> person1 == 1

        assert person1.comparatorByZip().compare(person1, person2) == 0
        assert person1.comparatorByZip().compare(person1, person3) == -1
        assert person1.comparatorByZip().compare(person3, person1) == 1

        shouldFail(MissingMethodException) {
            person1.comparatorByIndex()
        }
    }
}
