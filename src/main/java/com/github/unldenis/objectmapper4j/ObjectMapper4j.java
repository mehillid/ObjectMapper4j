package com.github.unldenis.objectmapper4j;

import java.lang.reflect.Field;
import java.util.*;

public class ObjectMapper4j {


    public static Object fromObject(Object obj) {
        if (obj == null) {
            return null;
        }
        Class type = obj.getClass();
        if (obj instanceof Number || obj instanceof String || obj instanceof Boolean) {
            return obj;
        }

        if (List.class.isAssignableFrom(type)) {
            List list = (List) obj;
            List newList = new ArrayList();
            for (Object o : list) {
                newList.add(fromObject(o));
            }
            return newList;
        }

        if (Map.class.isAssignableFrom(type)) {
            Map map = (Map) obj;
            Map newMap = new LinkedHashMap();
            Iterator<Map.Entry> mapIterator = map.entrySet().iterator();
            while (mapIterator.hasNext()) {
                Map.Entry next = mapIterator.next();
                newMap.put(fromObject(next.getKey()), fromObject(next.getValue()));
            }
            return newMap;
        }

        // Data class
//        System.out.println(obj.getClass());
        Field[] fields = obj.getClass().getDeclaredFields();
        Map mapObj = new ClassMap(type);
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                mapObj.put(field.getName(), fromObject(field.get(obj)));
            } catch (IllegalAccessException e) {
                throw new ObjectMapperException(String.format("Field '%s' from '%s' is not accesible",
                        field.getName(), type.getName()), e);
            }
        }
        return mapObj;
    }

    public static Object toObject(Object obj) throws IllegalAccessException, InstantiationException {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number || obj instanceof String || obj instanceof Boolean) {
            return obj;
        }

        Class type = obj.getClass();


        if (type.equals(ArrayList.class)) {
            List list = (List) obj;
            List newList = new ArrayList();
            for (Object o : list) {
                newList.add(toObject(o));
            }
            return newList;
        }

        if (type.equals(LinkedHashMap.class)) {
            Map map = (Map) obj;
            Map newMap = new LinkedHashMap();
            Iterator<Map.Entry> mapIterator = map.entrySet().iterator();
            while (mapIterator.hasNext()) {
                Map.Entry next = mapIterator.next();
                newMap.put(toObject(next.getKey()), toObject(next.getValue()));
            }
            return newMap;
        }

        // Data class
//        System.out.println(obj.getClass());
        if (type.equals(ClassMap.class)) {
            ClassMap mapObj = (ClassMap) obj;
            Iterator<Map.Entry> mapIterator = mapObj.entrySet().iterator();

            type = mapObj.type;
            Object instance = type.newInstance();
            List<Field> fields = Arrays.asList(type.getDeclaredFields());



            while (mapIterator.hasNext()) {
                Map.Entry next = mapIterator.next();

                Class finalType = type;
                Field field = fields
                        .stream()
                        .filter(f -> f.getName().equals(next.getKey()))
                        .findFirst().orElseThrow(() -> new ObjectMapperException("Class " + finalType.getName()
                                + "has not a field called " + next.getKey()));

                field.setAccessible(true);
                field.set(instance, toObject(next.getValue()));
            }

            return instance;

        }
        throw new ObjectMapperException("Cannot convert object to class");
    }
    private static class ClassMap<K, V> extends LinkedHashMap<K, V> {
        private final Class<?> type;

        private ClassMap(Class<?> type) {
            this.type = type;
        }
    }

    public static class ObjectMapperException extends RuntimeException {

        public ObjectMapperException(String message) {
            super(message);
        }

        public ObjectMapperException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}