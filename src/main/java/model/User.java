package model;

import java.util.Date;

public class User {
    private String name;
    private String surname;
    private String number;
    private int age;
    private String data;

    public User(String name, String surname, String number, int age, String data) {
        this.name = name;
        this.surname = surname;
        this.number = number;
        this.age = age;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
