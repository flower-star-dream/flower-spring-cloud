package top.flowerstardream.base.mapper;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ReflectionUtils;
import top.flowerstardream.base.annotation.Query;
import top.flowerstardream.base.result.PageResult;

import java.util.Collection;
import java.util.List;

/**
 * @Author: 花海
 * @Date: 2025/12/09/23:09
 * @Description: 自动查询条件、分页
 */
public interface BaseMapperX<T> extends BaseMapper<T> {

    /**
     * 统一入口：分页 + 自动查询条件
     * 默认物理分页
     */
    default PageResult<T> autoPage(Object dto, Boolean isPhysicalPaging) {
        Boolean physical = isPhysicalPaging == null ? Boolean.TRUE : isPhysicalPaging;
        /* ---------- 1. 分页参数 ---------- */
        int page = (int) ReflectUtil.getFieldValue(dto, "page");
        int pageSize = (int) ReflectUtil.getFieldValue(dto, "pageSize");
        if (page <= 0) {
            page = 1;
        }
        if (pageSize <= 0) {
            pageSize = 10;
        }

        /* ---------- 2. 统一构造 QueryWrapper ---------- */
        QueryWrapper<T> wrapper = buildWrapper(dto);

        /* ---------- 3. 执行查询 ---------- */
        if (physical) {
            // 物理分页：只查当前页，total 自动回填
            Page<T> mpPage = new Page<>(page, pageSize);
            mpPage = selectPage(mpPage, wrapper);
            return new PageResult<>(mpPage.getTotal(), mpPage.getRecords());
        } else {
            // 逻辑分页：一次性查出全部，内存截断
            List<T> all = selectList(wrapper);
            Long total = (long) all.size();
            return new PageResult<>(total, all);
        }
    }

    /* 抽取公共 wrapper 构造，可复用 */
    default QueryWrapper<T> buildWrapper(Object dto) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        ReflectionUtils.doWithFields(dto.getClass(), field -> {
            if (field.isAnnotationPresent(Query.Ignore.class)) {
                return;
            }
            ReflectionUtils.makeAccessible(field);
            Object val = ReflectionUtils.getField(field, dto);
            if (ObjectUtils.isEmpty(val)) {
                return;
            }

            String column = StrUtil.toUnderlineCase(field.getName());
            Query.Condition c = field.getAnnotation(Query.Condition.class);
            // 解析 SpEL
            ExpressionParser parser = new SpelExpressionParser();
            StandardEvaluationContext ctx = new StandardEvaluationContext(dto);
            Object leftVal = null;
            Object rightVal = null;
            if (c == null) {
                wrapper.eq(column, val);
            } else {
                switch (c.value()) {
                    case "like": wrapper.like(column, val); break;
                    case "ge":   wrapper.ge(column, val); break;
                    case "le":   wrapper.le(column, val); break;
                    case "in":
                        if (val instanceof Collection) {
                            wrapper.in(column, (Collection<?>) val);
                        }
                        break;
                    case "between":
                        leftVal = parser.parseExpression(c.left()).getValue(ctx);
                        rightVal = parser.parseExpression(c.right()).getValue(ctx);
                        // 2. 空值保护（任意一边没给就跳过整个 between）
                        if (ObjectUtils.isEmpty(leftVal) || ObjectUtils.isEmpty(rightVal)) {
                            return;
                        }
                        wrapper.between(column, leftVal, rightVal);
                        break;
                    case "notIn":
                        if (val instanceof Collection) {
                            wrapper.notIn(column, (Collection<?>) val);
                        }
                        break;
                    case "notBetween":
                        leftVal = parser.parseExpression(c.left()).getValue(ctx);
                        rightVal = parser.parseExpression(c.right()).getValue(ctx);
                        // 2. 空值保护（任意一边没给就跳过整个 between）
                        if (ObjectUtils.isEmpty(leftVal) || ObjectUtils.isEmpty(rightVal)) {
                            return;
                        }
                        wrapper.notBetween(column, leftVal, rightVal);
                        break;
                    default: wrapper.eq(column, val);
                }
            }
        });
        return wrapper;
    }
}