package com.github.unldenis.objectmapper4j;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class ObjectMapper4j {


    public static <T> T fromObject(Object obj) {
        if (obj == null) {
            return null;
        }
        Class type = obj.getClass();

        if (obj instanceof Enum) {
            Enum e = (Enum) obj;

            try {
                Method valuesMethod = Arrays.stream(type.getDeclaredMethods())
                        .filter(method -> method.getName().equals("values"))
                        .filter(method -> Modifier.isStatic(method.getModifiers()))
                        .findFirst().orElseThrow(ObjectMapperException::new);
                Enum[] enums = (Enum[]) valuesMethod.invoke(null);

                EnumWrapper wrapper = new EnumWrapper(type, e.name(), Arrays.stream(enums).map(anEnum -> anEnum.name()).toArray(
                        String[]::new));
                return (T) wrapper;
            } catch (InvocationTargetException | IllegalAccessException ex) {
                throw new ObjectMapperException("Error loading " + e + " from enum " + type.getName(), ex);
            }
        }


        if (obj instanceof Number || obj instanceof String || obj instanceof Boolean) {
            return (T) obj;
        }

        if (List.class.isAssignableFrom(type)) {
            List list = (List) obj;
            List newList = new ArrayList();
            for (Object o : list) {
                newList.add(fromObject(o));
            }
            return (T) newList;
        }

        if (Map.class.isAssignableFrom(type)) {
            Map map = (Map) obj;
            Map newMap = new LinkedHashMap();
            Iterator<Map.Entry> mapIterator = map.entrySet().iterator();
            while (mapIterator.hasNext()) {
                Map.Entry next = mapIterator.next();
                newMap.put(fromObject(next.getKey()), fromObject(next.getValue()));
            }
            return (T) newMap;
        }

        // Data class
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
        return (T) mapObj;
    }

    public static <T> T toObject(Object obj) throws IllegalAccessException {
        if (obj == null) {
            return null;
        }

        if (obj instanceof EnumWrapper) {
            EnumWrapper enumWrapper = (EnumWrapper) obj;

            try {
                return (T) enumWrapper.parseEnum();
            } catch (InvocationTargetException | IllegalAccessException ex) {
                throw new ObjectMapperException("Error loading " + enumWrapper.value + " from enum " + enumWrapper.type.getName(), ex);
            }
        }

        if (obj instanceof Number || obj instanceof String || obj instanceof Boolean) {
            return (T) obj;
        }

        Class type = obj.getClass();


        if (type.equals(ArrayList.class)) {
            List list = (List) obj;
            List newList = new ArrayList();
            for (Object o : list) {
                newList.add(toObject(o));
            }
            return (T) newList;
        }

        if (type.equals(LinkedHashMap.class)) {
            Map map = (Map) obj;
            Map newMap = new LinkedHashMap();
            Iterator<Map.Entry> mapIterator = map.entrySet().iterator();
            while (mapIterator.hasNext()) {
                Map.Entry next = mapIterator.next();
                newMap.put(toObject(next.getKey()), toObject(next.getValue()));
            }
            return (T) newMap;
        }

        // Data class
        if (type.equals(ClassMap.class)) {
            ClassMap mapObj = (ClassMap) obj;
            Iterator<Map.Entry> mapIterator = mapObj.entrySet().iterator();

            type = mapObj.type;
            Object instance;
            try {
                instance = type.newInstance();
            } catch (InstantiationException e) {
                throw new ObjectMapperException("Missing empty constructor in " + type.getName(), e);
            }
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

            return (T) instance;


        }
        throw new ObjectMapperException("Cannot convert object to class");
    }

    public static final class EnumWrapper implements Serializable {
        private final Class<?> type;
        private String value;
        private final String[] values;
        private final Method valuesMethod;

        private EnumWrapper(Class<?> type, String value, String... values) {
            this.type = type;
            this.value = value;
            this.values = values;
            this.valuesMethod = Arrays.stream(type.getDeclaredMethods())
                    .filter(method -> method.getName().equals("valueOf"))
                    .filter(method -> Modifier.isStatic(method.getModifiers()))
                    .findFirst().orElseThrow(ObjectMapperException::new);
        }

        public Object parseEnum() throws InvocationTargetException, IllegalAccessException {

            return valuesMethod.invoke(type, value);
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String[] getValues() {
            return values;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            EnumWrapper that = (EnumWrapper) obj;
            return Objects.equals(this.type, that.type) &&
                    Objects.equals(this.value, that.value) &&
                    Arrays.equals(this.values, that.values);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, value, Arrays.hashCode(values));
        }

        @Override
        public String toString() {
            return "EnumWrapper[" +
                    "type=" + type + ", " +
                    "value=" + value + ", " +
                    "values=" + Arrays.toString(values) + ']';
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    private static class ClassMap<V> extends LinkedHashMap<String, V> {
        private final Class<?> type;

        private ClassMap(Class<?> type) {
            this.type = type;
        }
    }

    public static class ObjectMapperException extends RuntimeException {

        public ObjectMapperException() {
        }

        public ObjectMapperException(String message) {
            super(message);
        }

        public ObjectMapperException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static Node asTree(Object mappedObj) {
        if (mappedObj == null) {
            return null;
        }

        if (mappedObj instanceof EnumWrapper) {
            return new Node(NodeType.ENUM, mappedObj);
        }
        if (mappedObj instanceof Number) {
            return new Node(NodeType.NUMBER, mappedObj);
        }
        if (mappedObj instanceof String) {
            return new Node(NodeType.STRING, mappedObj);
        }
        if (mappedObj instanceof Boolean) {
            return new Node(NodeType.BOOLEAN, mappedObj);
        }

        Class type = mappedObj.getClass();
        if (type.equals(ArrayList.class)) {
            List list = (List) mappedObj;
            List<Node> newList = new ArrayList<>();
            for (Object o : list) {
                newList.add(asTree(o));
            }
            return new Node(NodeType.LIST, newList);
        }

        if (type.equals(LinkedHashMap.class)) {
            Map map = (Map) mappedObj;
            Iterator<Map.Entry> mapIterator = map.entrySet().iterator();
            Node mapNode = new Node(NodeType.MAP, new ArrayList<>());
            while (mapIterator.hasNext()) {
                Map.Entry next = mapIterator.next();
                mapNode.addChild(new Node(NodeType.ENTRY, Arrays.asList(asTree(next.getKey()),
                        asTree(next.getValue()))));
            }
            return mapNode;
        }

        // Data class
        if (type.equals(ClassMap.class)) {
            ClassMap<Object> mapObj = (ClassMap<Object>) mappedObj;
            Iterator<Map.Entry<String, Object>> mapIterator = mapObj.entrySet().iterator();

            Node mapNode = new Node(NodeType.MAP, new ArrayList<>());

            while (mapIterator.hasNext()) {
                Map.Entry<String, Object> next = mapIterator.next();
                mapNode.addChild(new Node(NodeType.ENTRY, Arrays.asList(asTree(next.getKey()),
                        asTree(next.getValue()))));
            }
            return mapNode;
        }
        throw new ObjectMapperException("Cannot convert object to tree");
    }

    public static class Node {

        private final NodeType nodeType;
        private final Object value;
        private final List<Node> children;

        public Node(NodeType nodeType, Object value, List<Node> children) {
            this.nodeType = nodeType;
            this.value = value;
            this.children = children;
        }

        public Node(NodeType nodeType, Object value) {
            this(nodeType, value, null);
        }

        public Node(NodeType nodeType, List<Node> children) {
            this(nodeType, null, children);
        }

        private void addChild(Node node) {
            children.add(node);
        }

        public NodeType getNodeType() {
            return nodeType;
        }

        public Object getValue() {
            return value;
        }

        public List<Node> getChildren() {
            return children;
        }

        public Node child(int index) {
            return children.get(index);
        }

        @Override
        public String toString() {
            return "Node{" +
                    "nodeType=" + nodeType +
                    (value != null ? ", value=" + value : "") +
                    (children != null ? ", children=" + children : "") +
                    '}';
        }
    }

    public enum NodeType {
        LIST,
        MAP,


        NUMBER,
        STRING,
        BOOLEAN,


        ENUM,


        ENTRY;
    }

}