package com.seaofnodes.simple.node.cpus.riscv;

import com.seaofnodes.simple.util.SB;
import com.seaofnodes.simple.codegen.*;
import com.seaofnodes.simple.node.*;

public class DivRISC extends MachConcreteNode implements MachNode{
    public DivRISC(Node div) {super(div);}
    @Override public String op() { return "div"; }
    @Override public RegMask regmap(int i) { return riscv.RMASK; }
    @Override public RegMask outregmap() { return riscv.WMASK; }
    @Override public void encoding( Encoding enc ) { riscv.r_type(enc,this,4,1);  }
    @Override public void asm(CodeGen code, SB sb) {
        sb.p(code.reg(this)).p(" = ").p(code.reg(in(1))).p(" / ").p(code.reg(in(2)));
    }

}
