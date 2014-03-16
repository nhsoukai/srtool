package srt.ast;

import java.util.Collections;
import java.util.List;

public class InvariantList extends Node {
	
	public InvariantList(Invariant[] invars) {
		this(invars, null);
	}
	
	public InvariantList(List<Invariant> invars) {
		this(invars.toArray(new Invariant[invars.size()]), null);
	}
	
	public InvariantList(Invariant[] invars, NodeInfo nodeInfo) {
		super(nodeInfo);
        Collections.addAll(children, invars);
	}
	
	@SuppressWarnings("unchecked")
	public List<Invariant> getInvariants() {
		return (List<Invariant>) children.clone();
	}
}
