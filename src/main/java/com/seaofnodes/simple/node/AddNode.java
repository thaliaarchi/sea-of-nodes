package com.seaofnodes.simple.node;

import com.seaofnodes.simple.type.*;

public class AddNode extends Node {
    public AddNode(Node lhs, Node rhs) { super(null, lhs, rhs); }

    @Override public String label() { return "Add"; }

    @Override public String glabel() { return "+"; }

    @Override
    StringBuilder _print1(StringBuilder sb) {
        in(1)._print0(sb.append("("));
        in(2)._print0(sb.append("+"));
        return sb.append(")");
    }


    @Override
    public Type compute() {
        if( in(1)._type instanceof TypeInteger i0 &&
            in(2)._type instanceof TypeInteger i1 ) {
            if (i0.isConstant() && i1.isConstant())
                return TypeInteger.constant(i0.value()+i1.value());
            return i0.meet(i1);
        }
        return Type.BOTTOM;
    }

    @Override
    public Node idealize () {
        Node lhs = in(1);
        Node rhs = in(2);
        Type t1 = lhs._type;
        Type t2 = rhs._type;

        // Already handled by peephole constant folding
        assert !(t1.isConstant() && t2.isConstant());

        // Add of 0.  We do not check for (0+x) because this will already
        // canonicalize to (x+0)
        if( t2 instanceof TypeInteger i && i.value()==0 )
            return lhs;

        // Add of same to a multiply by 2
        if( lhs==rhs )
            return new MulNode(lhs,new ConstantNode(TypeInteger.constant(2)).peephole());

        // Goal: a left-spine set of adds, with constants on the rhs (which then fold).

        // Move non-adds to RHS
        if( !(lhs instanceof AddNode) && rhs instanceof AddNode )
            return swap12();

        // Now we might see (add add non) or (add non non) or (add add add) but never (add non add)

        // Do we have  x + (y + z) ?
        // Swap to    (x + y) + z
        // Rotate (add add add) to remove the add on RHS
        if( rhs instanceof AddNode add )
            return new AddNode(new AddNode(lhs,add.in(1)).peephole(), add.in(2));

        // Now we might see (add add non) or (add non non) but never (add non add) nor (add add add)
        if( !(lhs instanceof AddNode) )
            // Rotate; look for (add (phi cons) con/(phi cons))
            return spline_cmp(lhs,rhs) ? swap12() : phiCon(this,true);

        // Now we only see (add add non)

        // Do we have (x + con1) + con2?
        // Replace with (x + (con1+con2) which then fold the constants
        if( lhs.in(2)._type.isConstant() && t2.isConstant() )
            return new AddNode(lhs.in(1),new AddNode(lhs.in(2),rhs).peephole());


        // Do we have ((x + (phi cons)) + con) ?
        // Do we have ((x + (phi cons)) + (phi cons)) ?
        // Push constant up through the phi: x + (phi con0+con0 con1+con1...)
        Node phicon = phiCon(this,true);
        if( phicon!=null ) return phicon;

        // Now we sort along the spline via rotates, to gather similar things together.

        // Do we rotate (x + y) + z
        // into         (x + z) + y ?
        if( spline_cmp(lhs.in(2),rhs) )
            return new AddNode(new AddNode(lhs.in(1),rhs).peephole(),lhs.in(2));

        return null;
    }

    // Rotation is only valid for associative ops, e.g. Add, Mul, And, Or.
    // Do we have ((phi cons)|(x + (phi cons)) + con|(phi cons)) ?
    // Push constant up through the phi: x + (phi con0+con0 con1+con1...)
    static Node phiCon(Node op, boolean rotate) {
        Node lhs = op.in(1);
        Node rhs = op.in(2);
        // LHS is either a Phi of constants, or another op with Phi of constants
        PhiNode lphi = pcon(lhs);
        if( rotate && lphi==null && lhs.nIns() > 2 ) {
            // Only valid to rotate constants if both are same associative ops
            if( lhs.getClass() != op.getClass() ) return null;
            lphi = pcon(lhs.in(2)); // Will rotate with the Phi push
        }
        if( lphi==null ) return null;

        // RHS is a constant or a Phi of constants
        if( !(rhs instanceof ConstantNode con) && pcon(rhs)==null )
            return null;

        // If both are Phis, must be same Region
        if( rhs instanceof PhiNode && lphi.in(0) != rhs.in(0) )
            return null;

        // Note that this is the exact reverse of Phi pulling a common op down
        // to reduce total op-count.  We don't get in an endless push-up
        // push-down peephole cycle because the constants all fold first.
        Node[] ns = new Node[lphi.nIns()];
        ns[0] = lphi.in(0);
        // Push constant up through the phi: x + (phi con0+con0 con1+con1...)
        for( int i=1; i<ns.length; i++ )
            ns[i] = op.copy(lphi.in(i), rhs instanceof PhiNode ? rhs.in(i) : rhs).peephole();
        String label = lphi._label + (rhs instanceof PhiNode rphi ? rphi._label : "");
        Node phi = new PhiNode(label,ns).peephole();
        // Rotate needs another op, otherwise just the phi
        return lhs==lphi ? phi : op.copy(lhs.in(1),phi);
    }

    static PhiNode pcon(Node op) {
        return op instanceof PhiNode phi && phi.allCons() ? phi : null;
    }

    // Compare two off-spline nodes and decide what order they should be in.
    // Do we rotate ((x + hi) + lo) into ((x + lo) + hi) ?
    // Generally constants always go right, then Phi-of-constants, then muls, then others.
    // Ties with in a category sort by node ID.
    // TRUE if swapping hi and lo.
    static boolean spline_cmp( Node hi, Node lo ) {
        if( lo._type.isConstant() ) return false;
        if( hi._type.isConstant() ) return true ;

        if( lo instanceof PhiNode && lo.allCons() ) return false;
        if( hi instanceof PhiNode && hi.allCons() ) return true ;

        if( lo instanceof PhiNode && !(hi instanceof PhiNode) ) return true;
        if( hi instanceof PhiNode && !(lo instanceof PhiNode) ) return false;

        // Same category of "others"
        return lo._nid > hi._nid;
    }

    @Override Node copy(Node lhs, Node rhs) { return new AddNode(lhs,rhs); }
}
