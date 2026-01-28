package top.flowerstardream.base.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.flowerstardream.base.mapper.BaseMapperX;
import top.flowerstardream.base.test.TestEO;

/**
 * @Author: 花海
 * @Date: 2025/12/17/01:25
 * @Description: 测试Mapper
 */
@Mapper
public interface TestMapper extends BaseMapperX<TestEO> {
}
