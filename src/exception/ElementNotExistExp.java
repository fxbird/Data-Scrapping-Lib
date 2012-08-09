package exception;

public class ElementNotExistExp extends Exception{
    public ElementNotExistExp(String id) {
        super("Element[id='"+id+"'] doesn't exists");
    }


}
