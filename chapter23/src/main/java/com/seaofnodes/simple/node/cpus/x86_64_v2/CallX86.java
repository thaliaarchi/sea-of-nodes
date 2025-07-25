package com.seaofnodes.simple.node.cpus.x86_64_v2;

import com.seaofnodes.simple.codegen.*;
import com.seaofnodes.simple.node.*;
import com.seaofnodes.simple.type.TypeFunPtr;
import com.seaofnodes.simple.util.SB;

public class CallX86 extends CallNode implements MachNode, RIPRelSize {
    final TypeFunPtr _tfp;
    final String _name;
    CallX86( CallNode call, TypeFunPtr tfp ) {
        super(call);
        _inputs.pop(); // Pop constant target
        assert tfp.isConstant();
        _tfp = tfp;
        FunNode fun = CodeGen.CODE.link(tfp);
        _name = fun==null ? ((ExternNode)call.fptr())._extern : fun._name; // Can be null for extern calls
    }
    @Override public String op() { return "call"; }
    @Override public String label() { return op(); }
    @Override public String name() { return _name; }
    @Override public TypeFunPtr tfp() { return _tfp; }
    @Override public RegMask regmap(int i) { return x86_64_v2.callInMask(_tfp,i,fun()._maxArgSlot);  }
    @Override public RegMask outregmap() { return null; }
    @Override public int nargs() { return nIns()-2; } // Minus control, memory, fptr

    @Override public void encoding( Encoding enc ) {
        FunNode fun = CodeGen.CODE.link(_tfp);
        if( fun==null ) enc.external(this,_name);
        else enc.relo(this);
        enc.add1(0xe8).add4(0);
    }

    // Delta is from opcode start, but X86 measures from the end of the 5-byte encoding
    @Override public byte encSize(int delta) { return 5; }

    // Delta is from opcode start
    @Override public void patch( Encoding enc, int opStart, int opLen, int delta ) {
        enc.patch4(opStart+1, delta-5);
    }

    @Override public void asm(CodeGen code, SB sb) {
        sb.p(_name).p("  ");
        for( int i=0; i<nargs(); i++ )
            sb.p(code.reg(in(i+2))).p("  ");
        sb.unchar(2);
    }
}
