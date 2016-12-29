package SharedData;

import java.io.Serializable;

public class Request implements Serializable {
    private String type;
    private String args;
    
    public Request(String type, String args){
        this.type = type;
        this.args = args;
    }
    
    public String getType(){
        return type;
    }
    
    public String getArgs(){
        return this.args;
    }
    
}
