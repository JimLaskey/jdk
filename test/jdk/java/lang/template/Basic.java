/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 0000000
 * @summary Exercise runtime handing of templated strings.
 * @enablePreview true
 */

import java.lang.StringTemplate.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import static java.lang.StringTemplate.str;

public class Basic {
    public static void main(String... arg) {
        equalsHashCode();
        concatenationTests();
        componentTests();
        stringTemplateCoverage();
    }

    static void ASSERT(String a, String b) {
        if (!Objects.equals(a, b)) {
            System.out.println(a);
            System.out.println(b);
            throw new RuntimeException("Test failed");
        }
    }

    static void ASSERT(Object a, Object b) {
        if (!Objects.deepEquals(a, b)) {
            System.out.println(a);
            System.out.println(b);
            throw new RuntimeException("Test failed");
        }
    }

    /*
     * equals and hashCode tests.
     */
    static void equalsHashCode() {
        int x = 10;
        int y = 20;
        int a = 10;
        int b = 20;

        StringTemplate st0 = "\{x} + \{y} = \{x + y}";
        StringTemplate st1 = "\{a} + \{b} = \{a + b}";
        StringTemplate st2 = "\{x} + \{y} = \{x + y}!";
        x++;
        StringTemplate st3 = "\{x} + \{y} = \{x + y}";

        if (!st0.equals(st1)) throw new RuntimeException("st0 != st1");
        if (st0.equals(st2)) throw new RuntimeException("st0 == st2");
        if (st0.equals(st3)) throw new RuntimeException("st0 == st3");

        if (st0.hashCode() != st1.hashCode()) throw new RuntimeException("st0.hashCode() != st1.hashCode()");
    }

    /*
     * Concatenation tests.
     */
    static void concatenationTests() {
        int x = 10;
        int y = 20;

        ASSERT(str("\{x} \{y}"), x + " " + y);
        ASSERT(str("\{x + y}"), "" + (x + y));
    }

    /*
     * Component tests.
     */
    static void componentTests() {
        int x = 10;
        int y = 20;

        StringTemplate st = "\{x} + \{y} = \{x + y}";
        ASSERT(st.values(), List.of(x, y, x + y));
        ASSERT(st.fragments(), List.of("", " + ", " = ", ""));
        ASSERT(str(st), x + " + " + y + " = " + (x + y));
    }

    /*
     *  StringTemplate coverage
     */
    static void stringTemplateCoverage() {
        StringTemplate tsNoValues = t"No Values";

        ASSERT(tsNoValues.values(), List.of());
        ASSERT(tsNoValues.fragments(), List.of("No Values"));
        ASSERT(str(tsNoValues), "No Values");

        int x = 10, y = 20;
        StringTemplate src = "\{x} + \{y} = \{x + y}";
        ASSERT(src.fragments(), List.of("", " + ", " = ", ""));
        ASSERT(src.values(), List.of(x, y, x + y));
        ASSERT(str(src), x + " + " + y + " = " + (x + y));
        ASSERT(str(src), x + " + " + y + " = " + (x + y));
        ASSERT(str(t"a string"), "a string");
        StringTemplate color = "\{"red"}";
        StringTemplate shape = "\{"triangle"}";
        StringTemplate statement = "This is a \{color} \{shape}.";
        ASSERT(str(statement, v -> {
            if (v instanceof StringTemplate st) {
                return str(st).toUpperCase();

            } else {
                return String.valueOf(v).toUpperCase();
            }
         }), "This is a RED TRIANGLE.");
    }

}
