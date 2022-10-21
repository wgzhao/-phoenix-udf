package com.cxzq.ds;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.phoenix.expression.Expression;
import org.apache.phoenix.expression.LiteralExpression;
import org.apache.phoenix.schema.types.PInteger;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestTradeDateFunction
{
    private static String evaluate(final Expression expr)
    {
        final ImmutableBytesWritable ptr = new ImmutableBytesWritable();
        assertTrue(expr.evaluate(null, ptr));
        return (String) expr.getDataType().toObject(ptr);
    }

    @Test
    public void testSimpleNumber() throws Exception {
        final LiteralExpression interval = LiteralExpression.newConstant(4, PInteger.INSTANCE);
        assertEquals("20210827", evaluate(new TradeDateFunction(Collections.singletonList(interval))));
    }
}
