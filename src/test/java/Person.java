import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Person {
        private String name;
        private String surname;
        private int age;
        private Person mother;

        private Map<Person, List<Integer>> others;

        public Person(String name, String surname, int age, Person mother, Map<Person, List<Integer>> others) {
            this.name = name;
            this.surname = surname;
            this.age = age;
            this.mother = mother;
            this.others = others;
        }

        public Person() {}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return age == person.age && Objects.equals(name, person.name) && Objects.equals(surname, person.surname) && Objects.equals(mother, person.mother) && Objects.equals(others, person.others);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, surname, age, mother, others);
    }

    @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", surname='" + surname + '\'' +
                    ", age=" + age +
                    ", mother=" + mother +
                    ", others=" + others +
                    '}';
        }
    }