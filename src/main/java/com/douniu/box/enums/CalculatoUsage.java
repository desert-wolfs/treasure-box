package com.douniu.box.enums;

public enum CalculatoUsage implements Operation {
    /**
     * 加减乘除操作
     */
    PLUS {
        @Override
        public int apply(int a, int b) {
            return a + b;
        }
    },
    MINUS {
        @Override
        public int apply(int a, int b) {
            return a - b;
        }
    },
    MULTIPLY {
        @Override
        public int apply(int a, int b) {
            return a * b;
        }
    },
    DIVIDE {
        @Override
        public int apply(int a, int b) {
            return a / b;
        }
    },
    ;

    public static void main(String[] args) {
        System.out.println(PLUS.apply(1, 2));
        System.out.println(MINUS.apply(1, 2));
        System.out.println(MULTIPLY.apply(1, 2));
        System.out.println(DIVIDE.apply(1, 2));
    }


}
