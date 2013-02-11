package griffon.plugins.domain

import griffon.plugins.validation.exceptions.ValidationException
import griffon.test.GriffonUnitTestCase
import griffon.test.mock.MockGriffonApplication
import griffon.transform.CommandObject

import java.beans.PropertyChangeListener

class SampleTests extends GriffonUnitTestCase {
    private GriffonApplication app

    void setUp() {
        ExpandoMetaClassCreationHandle.enable()
        app = new MockGriffonApplication()
        app.builderClass = SampleBuilderConfig
        app.initialize()
    }

    void testDynamicMethodInjection() {
        Sample one = Sample.create(name: 'Andres', lastName: 'Almiray', num: 30).save()
        assert Sample.findBy('Name', ['Andres']).lastName == 'Almiray'
        assert Sample.findBy('NameAndLastName', ['Andres', 'Almiray']).lastName == 'Almiray'
        assert !Sample.findBy('NameAndLastName', 'Dierk', 'Koenig')
        Sample.create(name: 'Dierk', lastName: 'Koenig', num: 40).save()
        assert Sample.findBy('NameAndLastName', 'Dierk', 'Koenig').lastName == 'Koenig'
        assert Sample.list().name == ['Andres', 'Dierk']
        assert Sample.count() == 2
        assert Sample.list(max: 1, offset: 0).name == ['Andres']
        assert Sample.list(max: 1, offset: 1).name == ['Dierk']
        assert Sample.list(sort: 'num', order: 'asc').num == [30, 40]
        assert Sample.list(sort: 'num', order: 'desc').num == [40, 30]
        assert Sample.first().name == 'Andres'
        assert Sample.last().name == 'Dierk'
        assert Sample.first(sort: 'name').name == 'Andres'
        assert Sample.last(sort: 'name').name == 'Dierk'
        assert Sample.first('name').name == 'Andres'
        assert Sample.last('name').name == 'Dierk'
        Sample.get(2).delete()
        assert Sample.count() == 1
        assert Sample.list().name == ['Andres']
        assert Sample.first().name == 'Andres'
        assert Sample.last().name == 'Andres'

        assert Sample.findByName('Andres').lastName == 'Almiray'
        Sample.create(name: 'Dierk', lastName: 'Koenig', num: 40).save()
        Sample.create(name: 'Guillaume', lastName: 'Laforge', num: 30).save()
        assert Sample.countByNum(30) == 2
        assert Sample.findAllByNum(30).name == ['Andres', 'Guillaume']

        assert Sample.listOrderBy('Name').name == ['Andres', 'Dierk', 'Guillaume']
        assert Sample.listOrderByName(order: 'desc').name == ['Guillaume', 'Dierk', 'Andres']
        assert Sample.listOrderByNum().name == ['Andres', 'Guillaume', 'Dierk']

        assert Sample.findOrCreateByNameAndLastName('Andres', 'Almiray').id
        assert !Sample.findOrCreateByNameAndLastName('Sascha', 'Klein').id

        println one.griffonClass
        println '--------------'
        one.griffonClass.properties.each { println it }
        println '--------------'
        one.griffonClass.persistentProperties.each { println it }
        println '--------------'
        one.griffonClass.constrainedProperties.each { println it }
        println '--------------'

        assert !Sample.create().save()
        assert Sample.create().save(validate: false)
        shouldFail(ValidationException) {
            assert !Sample.create().save(failOnError: true)
        }

        assert Sample.findByName('Dierk')
        Sample dierk2 = Sample.create(name: 'Dierk', lastName: 'Koenig', num: 35)
        assert !dierk2.save()
        dierk2.errors.allErrors.each { println it }
        shouldFail(ValidationException) {
            assert !Sample.create(name: 'Dierk', lastName: 'Koenig', num: 35).save(failOnError: true)
        }

    }

    void testRelationships() {
        Author author = Author.create(name: 'Venkat', lastName: 'Subramanian')
        Book book1 = Book.create(title: 'Programming Groovy')
        Book book2 = Book.create(title: 'Programming Scala')
        assert !author.books
        assert !book1.author

        author.addToBooks(book1)
        assert 1 == author.books.size()
        assert author.books == ([book1] as Set)
        assert book1.author == author

        assert !book2.author
        author.addToBooks(book2)
        assert 2 == author.books.size()
        assert author.books == ([book1, book2] as Set)
        assert book2.author == author

        author.removeFromBooks(book1)
        assert 1 == author.books.size()
        assert author.books == ([book2] as Set)
        assert !book1.author
    }

    void testCommandObject() {
        def command = new LoginCommand()

        println '--------------'
        command.constrainedProperties().each { println it }
        println '--------------'
        command.errors.addPropertyChangeListener({ e ->
            println "${e.propertyName} ${e.oldValue} ${e.newValue}"
        } as PropertyChangeListener)
        command.num = 0
        command.validate('num')
        println command.errors.hasErrors()
        command.errors.allErrors.each { println it }
        command.errors.clearAllErrors()
        println '--------------'
        command.num = -1
        command.validate()
        println command.errors.hasErrors()
        command.errors.allErrors.each { println it }
        command.errors.clearAllErrors()
    }

    void testCommandObject2() {
        def command = new LoginCommand2()

        println '--------------'
        command.constrainedProperties().each { println it }
        println '--------------'
        command.errors.addPropertyChangeListener({ e ->
            println "${e.propertyName} ${e.oldValue} ${e.newValue}"
        } as PropertyChangeListener)
        command.num = 0
        command.validate('num')
        println command.errors.hasErrors()
        command.errors.allErrors.each { println it }
        command.errors.clearAllErrors()
        println '--------------'
        command.num = -1
        command.validate()
        println command.errors.hasErrors()
        command.errors.allErrors.each { println it }
        command.errors.clearAllErrors()
    }
}

@CommandObject
class LoginCommand {
    // final StringValue name = new StringValue()
    // final StringValue lastName = new StringValue()
    // final IntegerValue num = new IntegerValue()
    String name
    String lastName
    Integer num

    static constraints = {
        num(range: 0..10)
        name(nullable: false, blank: false)
        lastName(nullable: true)
    }
}

@CommandObject(Sample)
class LoginCommand2 {
    static constraints = {
        num(range: 0..10)
        name(nullable: false, blank: false)
        lastName(nullable: true)
    }
}