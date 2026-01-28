package top.flowerstardream.base;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import top.flowerstardream.base.ao.req.BaseStatusChangeREQ;
import top.flowerstardream.base.test.service.ITestService;
import top.flowerstardream.base.state.BaseStatus;

/**
 * @Author: 花海
 * @Date: 2025/12/16/16:17
 * @Description: 基础测试类
 */
@SpringBootTest
@MapperScan("top.flowerstardream.base.test.mapper")
@SpringJUnitConfig
@Slf4j
public class BaseTest {

    @Resource
    private ITestService testService;

    @Test
    public void test() {
        BaseStatusChangeREQ req = BaseStatusChangeREQ.builder()
                .id(1L)
                .status(BaseStatus.ENABLE.getCode())
                .build();
        testService.startOrStop(req);
        log.info("运行正常");
    }
}
