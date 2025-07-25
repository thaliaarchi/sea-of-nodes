package com.seaofnodes.simple.node.cpus.arm;

import com.seaofnodes.simple.codegen.*;
import com.seaofnodes.simple.node.*;
import com.seaofnodes.simple.util.SB;

public class AndIARM extends MachConcreteNode implements MachNode {
    final int _imm;
    AndIARM(Node and, int imm) {
        super(and);
        _inputs.pop();
        _imm = imm;
    }
    @Override public String op() { return "andi"; }
    @Override public RegMask regmap(int i) { return arm.RMASK; }
    @Override public RegMask outregmap() { return arm.WMASK; }
    @Override public void encoding( Encoding enc ) {
        arm.imm_inst_n(enc,this, in(1), arm.OPI_AND,_imm);
    }
    // General form: "andi  rd = rs1 & imm"
    @Override public void asm(CodeGen code, SB sb) {
        sb.p(code.reg(this)).p(" = ").p(code.reg(in(1))).p(" & #").p(arm.decodeImm12(_imm));
    }
}