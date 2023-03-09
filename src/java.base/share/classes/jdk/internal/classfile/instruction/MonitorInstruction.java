/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.classfile.instruction;

import jdk.internal.classfile.CodeElement;
import jdk.internal.classfile.CodeModel;
import jdk.internal.classfile.Instruction;
import jdk.internal.classfile.Opcode;
import jdk.internal.classfile.impl.AbstractInstruction;
import jdk.internal.classfile.impl.Util;

/**
 * Models a {@code monitorenter} or {@code monitorexit} instruction in the
 * {@code code} array of a {@code Code} attribute.  Delivered as a {@link
 * CodeElement} when traversing the elements of a {@link CodeModel}.
 */
public sealed interface MonitorInstruction extends Instruction
        permits AbstractInstruction.UnboundMonitorInstruction {

    /**
     * {@return a monitor instruction}
     *
     * @param op the opcode for the specific type of monitor instruction,
     *           which must be of kind {@link Opcode.Kind#MONITOR}
     */
    static MonitorInstruction of(Opcode op) {
        Util.checkKind(op, Opcode.Kind.MONITOR);
        return new AbstractInstruction.UnboundMonitorInstruction(op);
    }
}
