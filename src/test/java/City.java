import org.junit.Before;

import java.util.Objects;

public class City {

    public String name;
    public Country country;

    public City(String name, Country country) {
        this.name = name;
        this.country = country;
    }

    public City() {}

    public enum Country {
        Italy,
        US
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return Objects.equals(name, city.name) && country == city.country;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, country);
    }

    @Override
    public String toString() {
        return "City{" +
                "name='" + name + '\'' +
                ", country=" + country +
                '}';
    }
}
