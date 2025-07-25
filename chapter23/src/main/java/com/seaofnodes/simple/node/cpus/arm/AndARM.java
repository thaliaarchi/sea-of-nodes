package com.seaofnodes.simple.node.cpus.arm;

import com.seaofnodes.simple.codegen.*;
import com.seaofnodes.simple.node.MachConcreteNode;
import com.seaofnodes.simple.node.MachNode;
import com.seaofnodes.simple.node.Node;
import com.seaofnodes.simple.util.SB;

public class AndARM extends MachConcreteNode implements MachNode {
    AndARM(Node and) { super(and); }
    @Override public String op() { return "and"; }
    @Override public String glabel() { return "&"; }
    @Override public RegMask regmap(int i) { return arm.RMASK; }
    @Override public RegMask outregmap() { return arm.WMASK; }
    // AND (shifted register)
    @Override public void encoding( Encoding enc ) { arm.r_reg(enc,this,arm.OP_AND); }
    // General form:  #rd = rs1 & rs2
    @Override public void asm(CodeGen code, SB sb){
        sb.p(code.reg(this)).p(" = ").p(code.reg(in(1))).p(" & ").p(code.reg(in(2)));
    }
}
