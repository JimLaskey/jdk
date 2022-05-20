/*
 * Copyright (c) 2020, Red Hat, Inc. All rights reserved.
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

#include "precompiled.hpp"
#include "code/vmreg.hpp"
#include "prims/foreign_globals.hpp"
#include "utilities/debug.hpp"

class MacroAssembler;

const ABIDescriptor ForeignGlobals::parse_abi_descriptor(jobject jabi) {
  Unimplemented();
  return {};
}

VMReg ForeignGlobals::vmstorage_to_vmreg(int type, int index) {
  Unimplemented();
  return VMRegImpl::Bad();
}

int RegSpiller::pd_reg_size(VMReg reg) {
  Unimplemented();
  return -1;
}

void RegSpiller::pd_store_reg(MacroAssembler* masm, int offset, VMReg reg) {
  Unimplemented();
}

void RegSpiller::pd_load_reg(MacroAssembler* masm, int offset, VMReg reg) {
  Unimplemented();
}

void ArgumentShuffle::pd_generate(MacroAssembler* masm, VMReg tmp, int in_stk_bias, int out_stk_bias) const {
  Unimplemented();
}
