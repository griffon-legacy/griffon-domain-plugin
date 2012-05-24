package griffon.plugins.domain

import griffon.test.*

import griffon.test.mock.MockGriffonApplication
import org.codehaus.griffon.runtime.domain.AbstractCommandObject
import griffon.plugins.domain.atoms.StringValue
import griffon.plugins.domain.atoms.IntegerValue
import griffon.plugins.validation.exceptions.ValidationException

class SampleTests extends GriffonUnitTestCase {
    private GriffonApplication app

    void setUp() {
        // ExpandoMetaClassCreationHandle.enable()
        app = new MockGriffonApplication()
        app.builderClass = SampleBuilderConfig
        app.initialize()
    }

    void testDynamicMethodInjection() {
        Sample one = Sample.make(name: 'Andres', lastName: 'Almiray', num: 30).save()
        assert Sample.findBy('Name', ['Andres']).lastName.value == 'Almiray'
        assert Sample.findBy('NameAndLastName', ['Andres', 'Almiray']).lastName.value == 'Almiray'
        assert !Sample.findBy('NameAndLastName', 'Dierk', 'Koenig')
        Sample.make(name: 'Dierk', lastName: 'Koenig', num: 40).save()
        assert Sample.findBy('NameAndLastName', 'Dierk', 'Koenig').lastName.value == 'Koenig'
        assert Sample.list().name.value == ['Andres', 'Dierk']
        assert Sample.count() == 2
        assert Sample.list(max: 1, offset: 0).name.value == ['Andres']
        assert Sample.list(max: 1, offset: 1).name.value == ['Dierk']
        assert Sample.list(sort: 'num', order: 'asc').num.value == [30, 40]
        assert Sample.list(sort: 'num', order: 'desc').num.value == [40, 30]
        Sample.fetch(2).delete()
        assert Sample.count() == 1
        assert Sample.list().name.value == ['Andres']

        assert Sample.findByName('Andres').lastName.value == 'Almiray'
        Sample.make(name: 'Dierk', lastName: 'Koenig', num: 40).save()
        Sample.make(name: 'Guillaume', lastName: 'Laforge', num: 30).save()
        assert Sample.countByNum(30) == 2
        assert Sample.findAllByNum(30).name.value == ['Andres', 'Guillaume']

        assert Sample.mapping() == 'memory'
        // assert Sample.datasource() == 'default'

        assert !Sample.make().save()
        assert Sample.make().save(validate: false)
        shouldFail(ValidationException) {
            assert !Sample.make().save(failOnError: true)
        }

        println one.griffonClass
        println '--------------'
        one.griffonClass.properties.each {println it}
        println '--------------'
        one.griffonClass.persistentProperties.each {println it}
        println '--------------'
        one.griffonClass.constrainedProperties.each {println it}
        println '--------------'

        def command = new LoginCommand()

        println '--------------'
        command.constrainedProperties().each {println it}
        println '--------------'
        command.validate()
        println command.errors.hasErrors()
        command.errors.allErrors.each { println it }
    }
}

class LoginCommand extends AbstractCommandObject {
    final StringValue name = new StringValue()
    final StringValue lastName = new StringValue()
    final IntegerValue num = new IntegerValue()

    static constraints = {
        num(range: 0..10)
        name(nullable: false, blank: false, unique: true)
        lastName(nullable: true)
    }
}