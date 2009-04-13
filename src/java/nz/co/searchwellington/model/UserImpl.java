package nz.co.searchwellington.model;




public class UserImpl implements User {

    int id;
    String username;
    String password;
   
  
    public UserImpl() {
    }

    
    public UserImpl(String username, String password) {     
        this.username = username;
        this.password = password;        
    }


    
    


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getPassword() {
        return password;
    }



    public void setPassword(String password) {
        this.password = password;
    }



    public String getUsername() {
        return username;
    }



    public void setUsername(String username) {
        this.username = username;
    }
 
    
    
   
}