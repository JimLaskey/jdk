/*
 * Copyright (c) 2023, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package java.lang.runtime;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import jdk.internal.access.JavaLangAccess;
import jdk.internal.access.JavaTemplateAccess;
import jdk.internal.access.SharedSecrets;

/**
 * This class provides runtime support for string templates. The methods within
 * are intended for internal use only.
 *
 * @since 21
 *
 * Warning: This class is part of PreviewFeature.Feature.STRING_TEMPLATES.
 *          Do not rely on its availability.
 */
final class TemplateSupport implements JavaTemplateAccess {

    /**
     * {@link StringTemplate} values method.
     */
    private static final MethodHandle VALUES_MH;

    /**
     * {@link List} get method
     */
    private static final MethodHandle GET_MH;

    /**
     * Private constructor.
     */
    private TemplateSupport() {
    }

    static {
        SharedSecrets.setJavaTemplateAccess(new TemplateSupport());

        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodType mt = MethodType.methodType(List.class);
            VALUES_MH = lookup.findVirtual(StringTemplate.class, "values", mt);
            mt = MethodType.methodType(Object.class, int.class);
            GET_MH = lookup.findVirtual(List.class, "get", mt);
        } catch (ReflectiveOperationException ex) {
            throw new InternalError(ex);
        }

    }

    private static final JavaLangAccess JLA = SharedSecrets.getJavaLangAccess();

    /**
     * Returns a StringTemplate composed from fragments and values.
     *
     * @implSpec The {@code fragments} list size must be one more that the
     * {@code values} list size.
     *
     * @param fragments list of string fragments
     * @param values    list of expression values
     *
     * @return StringTemplate composed from fragments and values
     *
     * @throws IllegalArgumentException if fragments list size is not one more
     *         than values list size
     * @throws NullPointerException if fragments is null or values is null or if any fragment is null.
     *
     * @implNote Contents of both lists are copied to construct immutable lists.
     */
    @Override
    public StringTemplate of(List<String> fragments, List<?> values) {
        return StringTemplateImplFactory.newStringTemplate(fragments, values);
    }

    /**
     * Creates a string that interleaves the elements of values between the
     * elements of fragments.
     *
     * @param fragments  list of String fragments
     * @param values     list of expression values
     *
     * @return String interpolation of fragments and values
     */
    @Override
    public String interpolate(List<String> fragments, List<?> values) {
        int fragmentsSize = fragments.size();
        int valuesSize = values.size();
        if (fragmentsSize == 1) {
            return fragments.get(0);
        }
        int size = fragmentsSize + valuesSize;
        String[] strings = new String[size];
        int i = 0, j = 0;
        for (; j < valuesSize; j++) {
            strings[i++] = fragments.get(j);
            strings[i++] = String.valueOf(values.get(j));
        }
        strings[i] = fragments.get(j);
        return JLA.join("", "", "", strings, size);
    }

    /**
     * Combine one or more {@link StringTemplate StringTemplates} to produce a combined {@link StringTemplate}.
     * {@snippet :
     * StringTemplate st = StringTemplate.combine("\{a}", "\{b}", "\{c}");
     * assert st.interpolate().equals("\{a}\{b}\{c}");
     * }
     *
     * @param sts  zero or more {@link StringTemplate}
     *
     * @return combined {@link StringTemplate}
     *
     * @throws NullPointerException if sts is null or if any element of sts is null
     */
    @Override
    public StringTemplate combine(StringTemplate... sts) {
        Objects.requireNonNull(sts, "sts must not be null");
        if (sts.length == 0) {
            return StringTemplate.of("");
        } else if (sts.length == 1) {
            return Objects.requireNonNull(sts[0], "string templates should not be null");
        }
        int size = 0;
        for (StringTemplate st : sts) {
            Objects.requireNonNull(st, "string templates should not be null");
            size += st.values().size();
        }
        String[] combinedFragments = new String[size + 1];
        Object[] combinedValues = new Object[size];
        combinedFragments[0] = "";
        int fragmentIndex = 1;
        int valueIndex = 0;
        for (StringTemplate st : sts) {
            Iterator<String> iterator = st.fragments().iterator();
            combinedFragments[fragmentIndex - 1] += iterator.next();
            while (iterator.hasNext()) {
                combinedFragments[fragmentIndex++] = iterator.next();
            }
            for (Object value : st.values()) {
                combinedValues[valueIndex++] = value;
            }
        }
        return StringTemplateImplFactory.newTrustedStringTemplate(combinedFragments, combinedValues);
    }

    /**
     * Bind the getters of this {@link StringTemplate StringTemplate's} values to the inputs of the
     * supplied  {@link MethodHandle}.
     *
     * @param st  target {@link StringTemplate}
     * @param mh  {@link MethodHandle} to bind to
     *
     * @return bound {@link MethodHandle}
     */
    @Override
    public MethodHandle bindTo(StringTemplate st, MethodHandle mh) {
        Objects.requireNonNull(st, "st must not be null");
        Objects.requireNonNull(mh, "mh must not be null");

        int size = st.fragments().size() - 1; // cheaper to access than values()
        MethodHandle[] getters = new MethodHandle[size];
        for (int i = 0; i < size; i++) {
            getters[i] = MethodHandles.insertArguments(GET_MH, 1, i);
        }

        mh = MethodHandles.filterArguments(mh, 0, getters);
        int[] permute = new int[size];
        MethodType mt = MethodType.methodType(void.class, List.class);
        mh = MethodHandles.permuteArguments(mh, mt, permute);
        mh = MethodHandles.filterArguments(mh, 0, VALUES_MH);
        return mh;
    }
}
