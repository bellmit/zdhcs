package net.northking.atp;

import net.northking.atp.db.enums.RuEngineJobStatus;
import net.northking.atp.entity.ExecTaskEntity;
import net.northking.atp.enums.ExecuteResult;
import net.northking.atp.utils.UUIDUtil;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:bootstrap.yml"})
public class DemoTest {

    @Test
    public void uuid() {
        System.out.println(UUIDUtil.getUUIDWithoutDash());

    }
}
