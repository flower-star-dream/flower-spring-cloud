package top.flowerstardream.base.test.service.Impl;

import org.springframework.stereotype.Service;
import top.flowerstardream.base.service.Impl.BaseServiceImpl;
import top.flowerstardream.base.test.TestEO;
import top.flowerstardream.base.test.mapper.TestMapper;
import top.flowerstardream.base.test.service.ITestService;

/**
 * @Author: 花海
 * @Date: 2025/12/17/01:25
 * @Description: 测试服务实现类
 */
@Service
public class TestServiceImpl extends BaseServiceImpl<TestMapper, TestEO> implements ITestService {
}
