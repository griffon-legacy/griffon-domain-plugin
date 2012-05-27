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
        Sample.get(2).delete()
        assert Sample.count() == 1
        assert Sample.list().name == ['Andres']

        assert Sample.findByName('Andres').lastName == 'Almiray'
        Sample.create(name: 'Dierk', lastName: 'Koenig', num: 40).save()
        Sample.create(name: 'Guillaume', lastName: 'Laforge', num: 30).save()
        assert Sample.countByNum(30) == 2
        assert Sample.findAllByNum(30).name == ['Andres', 'Guillaume']

        assert Sample.mapping() == 'memory'
        // assert Sample.datasource() == 'default'

        println one.griffonClass
        println '--------------'
        one.griffonClass.properties.each {println it}
        println '--------------'
        one.griffonClass.persistentProperties.each {println it}
        println '--------------'
        one.griffonClass.constrainedProperties.each {println it}
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