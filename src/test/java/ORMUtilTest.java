import lk.ijse.dep.orm.ORMUtil;
import org.junit.Test;

import java.util.Properties;

public class ORMUtilTest {
    @Test
    public void init(){
        ORMUtil.init(new Properties(), Customer.class);
    }
}
