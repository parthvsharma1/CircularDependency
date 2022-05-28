import org.junit.Test;
import org.parth.A;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class circulardepTest {
    @Test
    public void Test1()
    {
        System.out.println("in a test");
        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("ApplicationContext.xml");
        A a = (A) ac.getBean("a");
        a.doSomeThing();
        ac.close();
    }
}
