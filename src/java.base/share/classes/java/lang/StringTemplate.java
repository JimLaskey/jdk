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

package java.lang;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.StringConcatException;
import java.lang.invoke.StringConcatFactory;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import ST;
import jdk.internal.access.JavaTemplateAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.javac.PreviewFeature;

/**
 * {@link StringTemplate} is the run-time representation of a string template or
 * text block template in a template expression.
 * <p>
 * In the source code of a Java program, a string template or text block template
 * contains an interleaved succession of <em>fragment literals</em> and <em>embedded
 * expressions</em>. The {@link StringTemplate#fragments()} method returns the
 * fragment literals, and the {@link StringTemplate#values()} method returns the
 * results of evaluating the embedded expressions. {@link StringTemplate} does not
 * provide access to the source code of the embedded expressions themselves; it is
 * not a compile-time representation of a string template or text block template.
 * <p>
 * {@link StringTemplate} is primarily used in conjunction with APIs
 * to produce a string or other meaningful value. Evaluation of a template expression
 * produces an instance of {@link StringTemplate}, with fragements and the values
 * of embedded expressions evaluated from left to right.
 * <p>
 * For example, the following code contains a template expression, which simply yields
 * a {@link StringTemplate}:
 * {@snippet :
 * int x = 10;
 * int y = 20;
 * StringTemplate st = "\{x} + \{y} = \{x + y}";
 * List<String> fragments = st.fragments();
 * List<Object> values = st.values();
 * }
 * {@code fragments} will be equivalent to {@code List.of("", " + ", " = ", "")},
 * which includes the empty first and last fragments. {@code values} will be the
 * equivalent of {@code List.of(10, 20, 30)}.
 * <p>
 * The following code contains a template expression with the same template but converting
 * to a string using the {@link #STR(StringTemplate)} static method:
 * {@snippet :
 * int x = 10;
 * int y = 20;
 * String s = STR("\{x} + \{y} = \{x + y}");
 * }
 * When the template expression is evaluated, an instance of {@link StringTemplate} is
 * produced that returns the same lists from {@link StringTemplate#fragments()} and
 * {@link StringTemplate#values()} as shown above. The {@link #STR(StringTemplate)} template
 * processor uses these lists to yield an interpolated string. The value of {@code s} will
 * be equivalent to {@code "10 + 20 = 30"}.
 * <p>
 * The {@code interpolate()} method provides a direct way to perform string interpolation
 * of a {@link StringTemplate}. Template processors can use the following code pattern:
 * {@snippet :
 * List<String> fragments = st.fragments();
 * List<Object> values    = st.values();
 * ... check or manipulate the fragments and/or values ...
 * String result = StringTemplate.interpolate(fragments, values);
 * }
 * The {@link StringTemplate#process(Function)} method, may be used to process a
 * {@link StringTemplate} in an invoke chain.
 * {@snippet :
 * StringTemplate st = "\{x} + \{y} = \{x + y}";
 * ...other steps...
 * String result = st.process(StringTemplate::interpolate);
 * }
 * The factory methods {@link StringTemplate#of(String)} and
 * {@link StringTemplate#of(List, List)} can be used to construct a {@link StringTemplate}.
 *
 * @implNote Implementations of {@link StringTemplate} must minimally implement the
 * methods {@link StringTemplate#fragments()} and {@link StringTemplate#values()}.
 * Instances of {@link StringTemplate} are considered immutable. To preserve the
 * semantics of string templates and text block templates, the list returned by
 * {@link StringTemplate#fragments()} must be one element larger than the list returned
 * by {@link StringTemplate#values()}.
 *
 * @since 21
 *
 * @jls 15.8.6 Process Template Expressions
 */
@PreviewFeature(feature=PreviewFeature.Feature.STRING_TEMPLATES)
public interface StringTemplate {
    /**
     * Returns a list of fragment literals for this {@link StringTemplate}.
     * The fragment literals are the character sequences preceding each of the embedded
     * expressions in source code, plus the character sequence following the last
     * embedded expression. Such character sequences may be zero-length if an embedded
     * expression appears at the beginning or end of a template, or if two embedded
     * expressions are directly adjacent in a template.
     * In the example: {@snippet :
     * String student = "Mary";
     * String teacher = "Johnson";
     * StringTemplate st = "The student \{student} is in \{teacher}'s classroom.";
     * List<String> fragments = st.fragments(); // @highlight substring="fragments()"
     * }
     * {@code fragments} will be equivalent to
     * {@code List.of("The student ", " is in ", "'s classroom.")}
     *
     * @return list of string fragments
     *
     * @implSpec the list returned is immutable
     */
    List<String> fragments();

    /**
     * Returns a list of embedded expression results for this {@link StringTemplate}.
     * In the example:
     * {@snippet :
     * String student = "Mary";
     * String teacher = "Johnson";
     * StringTemplate st = "The student \{student} is in \{teacher}'s classroom.";
     * List<Object> values = st.values(); // @highlight substring="values()"
     * }
     * {@code values} will be equivalent to {@code List.of(student, teacher)}
     *
     * @return list of expression values
     *
     * @implSpec the list returned is immutable
     */
    List<Object> values();

    /**
     * Returns the string interpolation of the fragments and values for this
     * {@link StringTemplate}.
     * @apiNote For better visibility and when practical, it is recommended to use the
     * {@link #STR(StringTemplate)} processor instead of invoking the
     * {@link StringTemplate#interpolate()} method.
     * {@snippet :
     * String student = "Mary";
     * String teacher = "Johnson";
     * StringTemplate st = "The student \{student} is in \{teacher}'s classroom.";
     * String result = st.interpolate(); // @highlight substring="interpolate()"
     * }
     * In the above example, the value of  {@code result} will be
     * {@code "The student Mary is in Johnson's classroom."}. This is
     * produced by the interleaving concatenation of fragments and values from the supplied
     * {@link StringTemplate}. To accommodate concatenation, values are converted to strings
     * as if invoking {@link String#valueOf(Object)}.
     *
     * @return interpolation of this {@link StringTemplate}
     *
     * @implSpec The default implementation returns the result of invoking
     * {@code StringTemplate.interpolate(this.fragments(), this.values())}.
     */
    default String interpolate() {
        return StringTemplate.interpolate(fragments(), values());
    }

    /**
     * Returns the result of applying the specified processor to this {@link StringTemplate}.
     * This method can be used as an alternative to string template expressions. For example,
     * {@snippet :
     * String student = "Mary";
     * String teacher = "Johnson";
     * String result1 = STR("The student \{student} is in \{teacher}'s classroom.");
     * String result2 = "The student \{student} is in \{teacher}'s classroom.".process(STR); // @highlight substring="process"
     * }
     * Produces an equivalent result for both {@code result1} and {@code result2}.
     *
     * @param processor to apply
     *
     * @param <R> result type
      *
     * @return constructed object of type {@code R}
     *
     * @throws NullPointerException if processor is null
     *
     * @implSpec The default implementation returns the result of invoking
     * {@code processor.apply(this)}. If the invocation throws an exception that
     * exception is forwarded to the caller.
     */
    default <R> R
    process(Function<StringTemplate, R> processor) {
        Objects.requireNonNull(processor, "processor should not be null");
        return processor.apply(this);
    }

    /**
     * Produces a diagnostic string that describes the fragments and values of the supplied
     * {@link StringTemplate}.
     *
     * @param stringTemplate  the {@link StringTemplate} to represent
     *
     * @return diagnostic string representing the supplied string template
     *
     * @throws NullPointerException if stringTemplate is null
     */
    static String toString(StringTemplate stringTemplate) {
        Objects.requireNonNull(stringTemplate, "stringTemplate should not be null");
        return "StringTemplate{ fragments = [ \"" +
                String.join("\", \"", stringTemplate.fragments()) +
                "\" ], values = " +
                stringTemplate.values() +
                " }";
    }

    /**
     * Returns a {@link StringTemplate} as if constructed by invoking
     * {@code StringTemplate.of(List.of(string), List.of())}. That is, a {@link StringTemplate}
     * with one fragment and no values.
     *
     * @param string  single string fragment
     *
     * @return StringTemplate composed from string
     *
     * @throws NullPointerException if string is null
     */
    static StringTemplate of(String string) {
        Objects.requireNonNull(string, "string must not be null");
        JavaTemplateAccess JTA = SharedSecrets.getJavaTemplateAccess();
        return JTA.of(List.of(string), List.of());
    }

    /**
     * Returns a StringTemplate with the given fragments and values.
     *
     * @implSpec The {@code fragments} list size must be one more that the
     * {@code values} list size.
     *
     * @param fragments list of string fragments
     * @param values    list of expression values
     *
     * @return StringTemplate composed from string
     *
     * @throws IllegalArgumentException if fragments list size is not one more
     *         than values list size
     * @throws NullPointerException if fragments is null or values is null or if any fragment is null.
     *
     * @implNote Contents of both lists are copied to construct immutable lists.
     */
    static StringTemplate of(List<String> fragments, List<?> values) {
        Objects.requireNonNull(fragments, "fragments must not be null");
        Objects.requireNonNull(values, "values must not be null");
        if (values.size() + 1 != fragments.size()) {
            throw new IllegalArgumentException(
                    "fragments list size is not one more than values list size");
        }
        JavaTemplateAccess JTA = SharedSecrets.getJavaTemplateAccess();
        return JTA.of(fragments, values);
    }

    /**
     * Creates a string that interleaves the elements of values between the
     * elements of fragments. To accommodate interpolation, values are converted to strings
     * as if invoking {@link String#valueOf(Object)}.
     *
     * @param fragments  list of String fragments
     * @param values     list of expression values
     *
     * @return String interpolation of fragments and values
     *
     * @throws IllegalArgumentException if fragments list size is not one more
     *         than values list size
     * @throws NullPointerException fragments or values is null or if any of the fragments is null
     */
    static String interpolate(List<String> fragments, List<?> values) {
        Objects.requireNonNull(fragments, "fragments must not be null");
        Objects.requireNonNull(values, "values must not be null");
        int fragmentsSize = fragments.size();
        int valuesSize = values.size();
        if (fragmentsSize != valuesSize + 1) {
            throw new IllegalArgumentException("fragments must have one more element than values");
        }
        JavaTemplateAccess JTA = SharedSecrets.getJavaTemplateAccess();
        return JTA.interpolate(fragments, values);
    }

    /**
     * Combine zero or more {@link StringTemplate StringTemplates} into a single
     * {@link StringTemplate}.
     * {@snippet :
     * StringTemplate st = StringTemplate.combine("\{a}", "\{b}", "\{c}");
     * assert st.interpolate().equals("\{a}\{b}\{c}".interpolate());
     * }
     * Fragment lists from the {@link StringTemplate StringTemplates} are combined end to
     * end with the last fragment from each {@link StringTemplate} concatenated with the
     * first fragment of the next. To demonstrate, if we were to take two strings and we
     * combined them as follows: {@snippet lang = "java":
     * String s1 = "abc";
     * String s2 = "xyz";
     * String sc = s1 + s2;
     * assert Objects.equals(sc, "abcxyz");
     * }
     * the last character {@code "c"} from the first string is juxtaposed with the first
     * character {@code "x"} of the second string. The same would be true of combining
     * {@link StringTemplate StringTemplates}.
     * {@snippet lang ="java":
     * StringTemplate st1 = "a\{}b\{}c";
     * StringTemplate st2 = "x\{}y\{}z";
     * StringTemplate st3 = "a\{}b\{}cx\{}y\{}z";
     * StringTemplate stc = StringTemplate.combine(st1, st2);
     *
     * assert Objects.equals(st1.fragments(), List.of("a", "b", "c"));
     * assert Objects.equals(st2.fragments(), List.of("x", "y", "z"));
     * assert Objects.equals(st3.fragments(), List.of("a", "b", "cx", "y", "z"));
     * assert Objects.equals(stc.fragments(), List.of("a", "b", "cx", "y", "z"));
     * }
     * Values lists are simply concatenated to produce a single values list.
     * The result is a well-formed {@link StringTemplate} with n+1 fragments and n values, where
     * n is the total of number of values across all the supplied
     * {@link StringTemplate StringTemplates}.
     *
     * @param stringTemplates  zero or more {@link StringTemplate}
     *
     * @return combined {@link StringTemplate}
     *
     * @throws NullPointerException if stringTemplates is null or if any of the
     * {@code stringTemplates} are null
     *
     * @implNote If zero {@link StringTemplate} arguments are provided then a
     * {@link StringTemplate} with an empty fragment and no values is returned, as if invoking
     * <code>StringTemplate.of("")</code> . If only one {@link StringTemplate} argument is provided
     * then it is returned unchanged.
     */
    static StringTemplate combine(StringTemplate... stringTemplates) {
        JavaTemplateAccess JTA = SharedSecrets.getJavaTemplateAccess();
        return JTA.combine(stringTemplates);
    }

    /**
     * Combine a list of {@link StringTemplate StringTemplates} into a single
     * {@link StringTemplate}.
     * {@snippet :
     * StringTemplate st = StringTemplate.combine(List.of("\{a}", "\{b}", "\{c}"));
     * assert st.interpolate().equals(STR("\{a}\{b}\{c}"));
     * }
     * Fragment lists from the {@link StringTemplate StringTemplates} are combined end to
     * end with the last fragment from each {@link StringTemplate} concatenated with the
     * first fragment of the next. To demonstrate, if we were to take two strings and we
     * combined them as follows: {@snippet lang = "java":
     * String s1 = "abc";
     * String s2 = "xyz";
     * String sc = s1 + s2;
     * assert Objects.equals(sc, "abcxyz");
     * }
     * the last character {@code "c"} from the first string is juxtaposed with the first
     * character {@code "x"} of the second string. The same would be true of combining
     * {@link StringTemplate StringTemplates}.
     * {@snippet lang ="java":
     * StringTemplate st1 = "a\{}b\{}c";
     * StringTemplate st2 = "x\{}y\{}z";
     * StringTemplate st3 = "a\{}b\{}cx\{}y\{}z";
     * StringTemplate stc = StringTemplate.combine(List.of(st1, st2));
     *
     * assert Objects.equals(st1.fragments(), List.of("a", "b", "c"));
     * assert Objects.equals(st2.fragments(), List.of("x", "y", "z"));
     * assert Objects.equals(st3.fragments(), List.of("a", "b", "cx", "y", "z"));
     * assert Objects.equals(stc.fragments(), List.of("a", "b", "cx", "y", "z"));
     * }
     * Values lists are simply concatenated to produce a single values list.
     * The result is a well-formed {@link StringTemplate} with n+1 fragments and n values, where
     * n is the total of number of values across all the supplied
     * {@link StringTemplate StringTemplates}.
     *
     * @param stringTemplates  list of {@link StringTemplate}
     *
     * @return combined {@link StringTemplate}
     *
     * @throws NullPointerException if stringTemplates is null or if any of the
     * its elements are null
     *
     * @implNote If {@code stringTemplates.size() == 0} then a {@link StringTemplate} with
     * an empty fragment and no values is returned, as if invoking
     * <code>StringTemplate.of("")</code> . If {@code stringTemplates.size() == 1}
     * then the first element of the list is returned unchanged.
     */
    static StringTemplate combine(List<StringTemplate> stringTemplates) {
        JavaTemplateAccess JTA = SharedSecrets.getJavaTemplateAccess();
        return JTA.combine(stringTemplates.toArray(new StringTemplate[0]));
    }

    /**
     * This static method is conventionally used for the string interpolation
     * of a supplied {@link StringTemplate}.
     * <p>
     * For better visibility and when practical, it is recommended that users use the
     * {@link #STR(StringTemplate)} method instead of invoking the
     * {@link #interpolate()} method.
     * Example: {@snippet :
     * int x = 10;
     * int y = 20;
     * String result = STR("\{x} + \{y} = \{x + y}"); // @highlight substring="STR"
     * }
     * In the above example, the value of {@code result} will be {@code "10 + 20 = 30"}. This is
     * produced by the interleaving concatenation of fragments and values from the supplied
     * {@link StringTemplate}. To accommodate concatenation, values are converted to strings
     * as if invoking {@link String#valueOf(Object)}.
     * @apiNote {@link #STR(StringTemplate)} is statically imported implicitly into every
     * Java compilation unit.
     *
     * @param stringTemplate {@link StringTemplate} to interpolate
     * @return resulting string
     */
    static String STR(StringTemplate stringTemplate) {
        Objects.requireNonNull(stringTemplate, "stringTemplate must not be null");
        return stringTemplate.interpolate();
    }

    /**
     * Metadata associated with a {@link ProcessorBuilder} built processor. The metadata must
     * minimumly provide a {@link MethodHandle} for the fast implementation of the processor.
     */
    interface MetaData {
        /**
         * {@return the {@link MethodHandle} for the fast implementation of the processor}
         */
        MethodHandle getMethodHandle()
    }

    /**
     * Create a {@link java.lang.StringTemplate} processor.
     * @param <R> return type of processor
     * @param <M> type of the metadata
     */
    public class ProcessorBuilder<R, M extends ST.MetaData> {

        // Mapping support

        /**
         * Interface used for indexed mapping of list elements.
         */
        @FunctionalInterface
        protected interface IndexedMapFunction<T> {
            /**
             * Map an element at index position.
             * @param index    index in list
             * @param element  element to map
             * @return mapped element
             */
            T map(int index, T element);
        }

        /**
         * Index map elements of a list using the supplied function.
         * @param list  list to map
         * @param func  mapping function
         * @return a new list containing mapped elements
         */
        protected static <T> List<T> map(List<T> list, ST.ProcessorBuilder.IndexedMapFunction<T> func) {
            Objects.requireNonNull(list, "list must not be null");
            Objects.requireNonNull(list, "func must not be null");
            return map(list, func, func);
        }

        /**
         * Index map elements of a list using the supplied function, special-casing last element.
         * @param oldList  list to map
         * @param func     mapping function
         * @param last     mapping function for last element
         * @return a new list containing mapped elements
         */
        protected static <T> List<T> map(List<T> oldList,
                                         ST.ProcessorBuilder.IndexedMapFunction<T> func, ST.ProcessorBuilder.IndexedMapFunction<T> last) {
            Objects.requireNonNull(oldList, "list must not be null");
            Objects.requireNonNull(func, "func must not be null");
            Objects.requireNonNull(last, "last must not be null");
            List<T> newList = new ArrayList<>();
            int size = oldList.size();
            for (int index = 0; index < size; index++) {
                T oldElement = oldList.get(index);
                T newElement = index + 1 == size ? last.map(index, oldElement) :
                    func.map(index, oldElement);
                newList.add(newElement);
            }
            return newList;
        }

        /**
         * Owner used to lookup metadata.
         */
        private final Object owner;

        /**
         * Constructor.
         */
        public ProcessorBuilder() {
            owner = selectOwner();
        }

        // JavaTemplateAccess

        /**
         * Access to internal {@link java.lang.StringTemplate} internals.
         */
        private static JavaTemplateAccess JTA = SharedSecrets.getJavaTemplateAccess();

        /**
         * {@return true if the {@link java.lang.StringTemplate } was constructed from a literal, false
         * otherwise}
         * @param st  {@link java.lang.StringTemplate} to test
         */
        protected static boolean isLiteral(java.lang.StringTemplate st) {
            return JTA.isLiteral(st);
        }

        /**
         * {@return a list of value types for values in the {@link java.lang.StringTemplate }.}
         * @param st  {@link java.lang.StringTemplate} to query
         */
        protected static List<Class<?>> getTypes(java.lang.StringTemplate st) {
            return JTA.getTypes(st);
        }

        /**
         * Get metadata associated with processor.
         ]        * @param owner      owner of metadata
         * @param supplier   supplier of metadata
         * @return metadata  metadata
         * @param <T> type of metadata
         */
        protected static <T> T getMetaData(java.lang.StringTemplate st, Object owner, Supplier<T> supplier) {
            return JTA.getMetaData(st, owner, supplier);
        }

        /**
         * Bond {@link java.lang.StringTemplate} to {@link MethodHandle}.
         */
        protected static MethodHandle bindTo(java.lang.StringTemplate st, MethodHandle mh) {
            Objects.requireNonNull(mh, "mh must not be null");
            return JTA.bindTo(st, mh);
        }

        // Fast implementation

        /**
         * {@return the metadata owner. May be overridden to use alternate value}
         */
        protected Object selectOwner() {
            return this;
        }

        /**
         * Create a {@link MethodHandle} to process a {@link java.lang.StringTemplate}. May be
         * overridden to provide an alternate methodology.
         * @param st  a {@link java.lang.StringTemplate}
         * @return a {@link MethodHandle} to process a {@link java.lang.StringTemplate}
         */
        protected MethodHandle createMethodHandle(java.lang.StringTemplate st) {
            Objects.requireNonNull(st, "st must not be null");
            List<String> fragments = mapFragments(st);
            List<Class<?>> inTypes = getTypes(st);
            List<MethodHandle> valueFilters = createValueFilters(inTypes);
            MethodHandle resultFilter = createResultFilter();
            List<Class<?>> outTypes = new ArrayList<>();
            for (MethodHandle h : valueFilters) {
                outTypes.add(h.type().returnType());
            }
            try {
                MethodHandle mh = StringConcatFactory.makeConcatWithTemplate(fragments, outTypes);
                mh = MethodHandles.filterArguments(mh, 0,
                    valueFilters.toArray(new MethodHandle[0]));
                mh = bindTo(st, mh);
                mh = MethodHandles.filterReturnValue(mh, resultFilter);
                return mh;
            } catch (StringConcatException ex) {
                throw new InternalError(ex);
            }
        }

        /**
         * Map {@link java.lang.StringTemplate} value types. May be overridden for
         * an alternate methodolgy. Ex. can be used to box/unbox types.
         * @param types list of {@link java.lang.StringTemplate} value types
         * @return mapped list of {@link java.lang.StringTemplate} value types
         */
        protected List<Class<?>> mapTypes(List<Class<?>> types) {
            Objects.requireNonNull(types, "types must not be null");
            return map(types, (i, t) -> mapType(i, t));
        }

        /**
         * Map a {@link java.lang.StringTemplate} value type. May be overridden for
         * an alternate methodolgy. Ex. can be used to box/unbox types.
         * @param index  a zero based index of value
         * @param type   a {@link java.lang.StringTemplate} value type
         * @return mapped {@link java.lang.StringTemplate} value type
         */
        protected Class<?> mapType(int index, Class<?> type) {
            Objects.requireNonNull(type, "type must not be null");
            return type;
        }

        /**
         * Retrieve the metadata from the specified {@link java.lang.StringTemplate}. May be overridden for
         * an alternate methodology.
         * @param st  {@link java.lang.StringTemplate} being queried
         * @return found or constructed metadata,
         */
        @SuppressWarnings("unchecked")
        protected M getMetaData(java.lang.StringTemplate st) {
            Objects.requireNonNull(st, "st must not be null");
            return  getMetaData(st, owner, () -> {
                MethodHandle mh = createMethodHandle(st);
                return (M)new ST.MetaData() {
                    @Override
                    public MethodHandle getMethodHandle() {
                        return mh;
                    }
                };
            });
        }

        /**
         * Additional validation of the metadata. May be overridden for
         * an alternate methodology.
         * @param metaData metadata to test
         * @return true if test passes
         */
        protected boolean isMetaDataUseful(M metaData) {
            return metaData != null;
        }

        /**
         * Create value filters for the {@link MethodHandle} being constructed. May
         * be overridden to supply a different methodolgy.
         * @param types types of the {@link java.lang.StringTemplate} values.
         * @return a list of {@link MethodHandle} filters for the {@link java.lang.StringTemplate} values
         */
        protected List<MethodHandle> createValueFilters(List<Class<?>> types) {
            return map(types, (i, t) -> createValueFilter(i, t));
        }

        /**
         * Create a {@link MethodHandle} filter for a specific value. May
         * be overridden to supply a different methodolgy.
         * @param index  zero based index of the value
         * @param type   type of the value
         * @return {@link MethodHandle} filter
         */
        protected Object createValueFilter(int index, Class<?> type) {
            return MethodHandles.identity(type);
        }

        /**
         * Create a {@link MethodHandle} filter the string result of interpolation.
         * This may be overridden to provide and alternate result type.
         * @return {@link MethodHandle} filter
         */
        protected MethodHandle createResultFilter() {
            return MethodHandles.identity(String.class);
        }

        /**
         * Apply processor to {@link java.lang.StringTemplate}
         * @param st  the {@link java.lang.StringTemplate} to process
         * @return result of processing
         */
        public final R process(java.lang.StringTemplate st) {
            if (isLiteral(st)) {
                M metaData = getMetaData(st);
                if (isMetaDataUseful(metaData)) {
                    try {
                        return (R)metaData.getMethodHandle().invokeExact(st);
                    } catch (Throwable ex) {
                        throw new InternalError(ex);
                    }
                }
            }

            return slowProcess(st);
        }

        /**
         * Process {@link java.lang.StringTemplate} by basic interpolation.
         * @param st  the {@link java.lang.StringTemplate} to process
         * @return result of processing
         */
        R slowProcess(java.lang.StringTemplate st) {
            List<String> fragments = map(st.fragments(),
                (i, f) -> mapFragment(i, f, false),
                (i, f) -> mapFragment(i, f, true));
            List<Object> values = map(st.values(), (i, v) -> mapValue(i, v));
            String interpolation = java.lang.StringTemplate.interpolate(fragments, values);
            return mapInterpolation(interpolation);
        }

        /**
         * Map a {@link java.lang.StringTemplate StringTemplate's} fragments. May be overridden for
         * an alternate methodolgy. Ex. stripping out formattng specifications,
         * @param st  {@link java.lang.StringTemplate} being processed
         * @return mapped fragments
         */
        protected List<String> mapFragments(java.lang.StringTemplate st) {
            List<String> fragments = map(st.fragments(),
                (i, f) -> mapFragment(i, f, false),
                (i, f) -> mapFragment(i, f, true));
            return fragments;
        }

        /**
         * Map a single fragment. May be overridden for an alternate methodolgy.
         * Ex. stripping out formattng specifications,
         * @param index     zero based index of the fragment
         * @param fragment  string fragment
         * @param isLast    true if last fragment
         * @return mapped fragment
         */
        protected String mapFragment(int index, String fragment, boolean isLast) {
            return fragment;
        }

        /**
         * Map a {@link java.lang.StringTemplate StringTemplate's} values. May be overridden for
         * an alternate methodolgy. Ex. convert to strings,
         * @param st  {@link java.lang.StringTemplate} being processed
         * @return mapped values
         */
        protected List<Object> mapValues(java.lang.StringTemplate st) {
            List<Object> values = map(st.values(), this::mapValue);
            return values;
        }

        /**
         * Map a single value. May be overridden for an alternate methodolgy.
         * Ex. convert to a string,
         * @param index  zero based index of the value
         * @param value  value to map
         * @return mapped value
         */
        protected Object mapValue(int index, Object value) {
            return value;
        }

        /**
         * Map the result of interpolation.
         * @param interpolation  string result from interpolation
         * @return mapped result
         */
        @SuppressWarnings("unchecked")
        protected R mapInterpolation(String interpolation) {
            return (R)interpolation;
        }

    }
}
