import com.github.unldenis.objectmapper4j.ObjectMapper4j;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Stack;

public class UnitTests {


    private Person mockPerson;

    @Before
    public void before() {
        mockPerson = new Person("Denis", "Mehilli", 20,
                new Person("Irena", "CognomeMadre", 44, null, null),
                Collections.singletonMap(new Person("MoglieTani", "CognomeMoglieTani",
                        46, null, Collections.emptyMap()), Arrays.asList(0, 1, 2, 3)));

    }

    @Test
    public void serializeAndDeserialize() throws IllegalAccessException {
        Map<String, Object> mapped = ObjectMapper4j.fromObject(mockPerson);

        Person person = ObjectMapper4j.toObject(mapped);

        assert mockPerson.equals(person);
    }

    @Test
    public void testEnums() throws IllegalAccessException, InvocationTargetException {
        Map<String, Object> mapped = ObjectMapper4j.fromObject(new City("NY", City.Country.US));

        mapped.put("name", "NY");
        Object country = mapped.get("country");
        if (country instanceof ObjectMapper4j.EnumWrapper) {
            ObjectMapper4j.EnumWrapper wrapper = (ObjectMapper4j.EnumWrapper) country;
            assert Arrays.equals(new String[]{"Italy", "US"}, wrapper.getValues());

            wrapper.setValue("Italy");
            assert wrapper.parseEnum().equals(City.Country.Italy);
        }

        City city = ObjectMapper4j.toObject(mapped);
    }
}
