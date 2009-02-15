package nz.co.searchwellington.model;

public class CommentImpl implements Comment {

    int id;
    String title;
   
    public CommentImpl() {
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    
    
}
