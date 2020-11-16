package it.areson.aresondeathswap.utils;

public class StringPair {

    private final String left;
    private final String right;

    private StringPair(String left, String right) {
        this.left = left;
        this.right = right;
    }

    public static StringPair of(String left, String right) {
        return new StringPair(left, right);
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }

}
