package com.bankx.demo.common.base;

import java.io.Serializable;

/**
 * Base request object.
 *
 * 中文：
 * 所有入参对象的基础父类。
 * 当前版本先保留为空，后续可扩展公共校验、traceId、幂等键等字段。
 */
public abstract class BaseRequest implements Serializable {
}
