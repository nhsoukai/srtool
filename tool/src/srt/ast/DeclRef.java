package srt.ast;


public class DeclRef extends Expr {

	private String name;
    private boolean fresh=false;

	public DeclRef(String name) {
		this(name, null);
	}
	
	public DeclRef(String name, NodeInfo nodeInfo) {
		super(nodeInfo);
		this.name = name;
	}

	public String getName() {
		return name;
	}


    public void setIsFresh(){
        fresh=true;
    }

    public boolean isFresh(){
        return fresh;
    }


    @Override
    public boolean equals(Object dr){
        boolean same=false;
            if(dr!=null && dr instanceof DeclRef){
                same= this.name.equals(((DeclRef)dr).getName());
            }
        return same;
    }

}
