import com.github.unldenis.objectmapper4j.ObjectMapper4j;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

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
    public void serializeAndDeserialize() throws IllegalAccessException, InstantiationException {
        Map<String, Object> mapped = (Map<String, Object>) ObjectMapper4j.fromObject(mockPerson);

        Person person = (Person) ObjectMapper4j.toObject(mapped);

        assert mockPerson.equals(person);
    }
}
