package net.northking.atp.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class BeanUtil {

    public static <T> T mapToBean(Map<String, Object> source, T t) {
        Class clazz = t.getClass();
        try {
            List<Field> fieldList = new LinkedList<>();
            Field[] fields = clazz.getDeclaredFields(); // 获取目标的所有属性

            for (Field field : fields) {
                if (Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                fieldList.add(field);
            }
            Field[] superFields = clazz.getSuperclass().getDeclaredFields();
            for (Field superField : superFields) {
                if (Modifier.isFinal(superField.getModifiers())) {
                    continue;
                }
                if (Modifier.isStatic(superField.getModifiers())) {
                    continue;
                }
                fieldList.add(superField);
            }
            for (Field field : fieldList) {
                String name = field.getName();
                // map中含有字段名称并且不为null
                if (source.containsKey(name) && source.get(name) != null) {
                    field.setAccessible(true);
                    if (field.getType() == Date.class && source.get(name).getClass() == Long.class) {
                        Date date = new Date((Long) source.get(name));
                        field.set(t, date);
                    } else if (field.getType() == Long.class && source.get(name).getClass() == Integer.class) {
                        Long l = Long.valueOf(source.get(name).toString());
                    } else {
                        field.set(t, source.get(name));
                    }
                    field.setAccessible(false);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return t;
    }

    static class Person {
        String name;
        int age;
        List<String> address;
        Date birthday;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public List<String> getAddress() {
            return address;
        }

        public void setAddress(List<String> address) {
            this.address = address;
        }

        public Date getBirthday() {
            return birthday;
        }

        public void setBirthday(Date birthday) {
            this.birthday = birthday;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", address=" + address +
                    ", birthday=" + birthday +
                    '}';
        }
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "TOM");
        map.put("age", 10);
        ArrayList<String> list = new ArrayList<>();
        list.add("Beijing");
        list.add("广州");
        map.put("address", list);
        map.put("birthday", new Date());
        Person person = new Person();
        mapToBean(map, person);
        System.out.println(person);
    }
}
