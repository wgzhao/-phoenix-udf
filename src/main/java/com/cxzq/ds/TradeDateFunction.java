package com.cxzq.ds;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.phoenix.compile.KeyPart;
import org.apache.phoenix.expression.Expression;
import org.apache.phoenix.expression.function.ScalarFunction;
import org.apache.phoenix.parse.FunctionParseNode.Argument;
import org.apache.phoenix.parse.FunctionParseNode.BuiltInFunction;
import org.apache.phoenix.schema.tuple.Tuple;
import org.apache.phoenix.schema.types.PDataType;
import org.apache.phoenix.schema.types.PInteger;
import org.apache.phoenix.schema.types.PVarchar;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@BuiltInFunction(name = TradeDateFunction.NAME, args = {
        @Argument(allowedTypes = {PInteger.class}, isConstant = true, defaultValue = "1")
})
public class TradeDateFunction
        extends ScalarFunction
{
    public static final String NAME = "TRADE_DATE";
    private int interval = 1;
    private static final DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyyMMdd");

    public TradeDateFunction()
    {
    }

    public TradeDateFunction(final List<Expression> children)
            throws SQLException
    {
        super(children);
        init();
    }

    private void init()
    {
        final ImmutableBytesWritable raw = new ImmutableBytesWritable();
        if (getChildren().get(0).evaluate(null, raw)) {
            this.interval = (Integer) PInteger.INSTANCE.toObject(raw);
        }
    }

    @Override
    public boolean evaluate(Tuple tuple, ImmutableBytesWritable ptr)
    {
        // Get the child argument and evaluate it first
        final Expression arg = getChildren().get(0);
        if (!arg.evaluate(tuple, ptr)) {
            return false;
        }
        if (this.interval < 1) {
            this.interval = 1;
        }
        String lastTradeDate;
        if (this.interval == 1) {
            lastTradeDate = CloseDateUtil.getLastExchangeDay(LocalDate.now().format(pattern));
        }
        else {
            lastTradeDate = CloseDateUtil.getPeriodExchangeDay(LocalDate.now().format(pattern), -this.interval);
        }
        if (lastTradeDate == null) {
            return false;
        }

        ptr.set(PVarchar.INSTANCE.toBytes(lastTradeDate));
        return true;
    }

    @Override
    public PDataType getDataType()
    {
        return PVarchar.INSTANCE;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    /**
     * Determines whether or not a function may be used to form the start/stop
     * key of a scan
     *
     * @return the zero-based position of the argument to traverse into to look
     * for a primary key column reference, or {@value #NO_TRAVERSAL} if
     * the function cannot be used to form the scan key.
     */
    public int getKeyFormationTraversalIndex()
    {
        return NO_TRAVERSAL;
    }

    /**
     * Manufactures a KeyPart used to construct the KeyRange given a constant
     * and a comparison operator.
     *
     * @param childPart the KeyPart formulated for the child expression at the
     * {@link #getKeyFormationTraversalIndex()} position.
     * @return the KeyPart for constructing the KeyRange for this function.
     */
    public KeyPart newKeyPart(KeyPart childPart)
    {
        return null;
    }

    /**
     * Determines whether or not the result of the function invocation will be
     * ordered in the same way as the input to the function. Returning YES
     * enables an optimization to occur when a GROUP BY contains function
     * invocations using the leading PK column(s).
     *
     * @return YES if the function invocation will always preserve order for the
     * inputs versus the outputs and false otherwise, YES_IF_LAST if the
     * function preserves order, but any further column reference would
     * not continue to preserve order, and NO if the function does not
     * preserve order.
     */
    public OrderPreserving preservesOrder()
    {
        return OrderPreserving.NO;
    }
}
