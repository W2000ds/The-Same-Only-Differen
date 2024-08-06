
public class callfunction {
    public void cout(){
        System.out.println("call succeed");
        hi();
    }

    public void hi()
    {
        System.out.println("hi");
        no();
    }

    private void no()
    {
        System.out.println("no");
    }

    public int getFloat(){
        return 1;
    }
}
