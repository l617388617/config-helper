package com.github.config.helper.localstorage;

import java.util.regex.Pattern;

/**
 * LocalStorageTest
 *
 * @author: lupeng10
 * @create: 2023-05-23 11:38
 */
public class LocalStorageTest {

    private static final Pattern PATTERN = Pattern.compile("^\"[\\da-zA-Z-_/]*\"$");

    public static void main(String[] args) {
        System.out.println(PATTERN.matcher("\"/zpadbiz/grayusersuffix\"").find());
        System.out.println(PATTERN.matcher("\"/zpadbiz/graypricesuffix\"").find());
        System.out.println(PATTERN.matcher("\"/zpadbiz/3/fee\"").find());
        System.out.println(PATTERN.matcher("\"/zpadbiz/shelves/resource_base_price\"").find());
    }

}
