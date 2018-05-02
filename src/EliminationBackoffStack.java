import java.util.EmptyStackException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class EliminationBackoffStack<T> extends LockFreeClass<T>
{
    //in book
    private static class RangePolicy
    {
        int maxRange;
        int currentRange = 1;

        RangePolicy(int maxRange)
        {
            this.maxRange = maxRange;
        }

        public void recordEliminationSuccess()
        {
            if (currentRange < maxRange)
                currentRange++;
        }

        public void recordEliminationTimeout()
        {
            if (currentRange > 1)
                currentRange--;
        }

        public int getRange()
        {
            return currentRange;
        }
    }

    private boolean blocking = false;
    private EliminationArray<T> eliminationArray;
    private static ThreadLocal<RangePolicy> policy;

    public EliminationBackoffStack(final int exchangerCapacity, int exchangerWaitDuration, boolean blocking) {
        super(blocking);
        this.blocking = blocking;
        eliminationArray = new EliminationArray<T>(exchangerCapacity, exchangerWaitDuration);
        policy = new ThreadLocal<RangePolicy>() {
            protected synchronized RangePolicy initialValue() {
                return new RangePolicy(exchangerCapacity);
            }
        };
    }

    public boolean push(T value) {
        RangePolicy rangePolicy = policy.get();
        Node node = new Node(value);

        while (true) {
            if (this.tryPush(node))
                return true;
            else {
                try {
                    T otherValue = eliminationArray.visit(value,
                            rangePolicy.getRange());
                    if (otherValue == null) {
                        rangePolicy.recordEliminationSuccess();
                        return true;
                    }
                } catch (TimeoutException e) {
                    rangePolicy.recordEliminationTimeout();
                }
            }
        }
    }

    public T pop() {
        RangePolicy rangePolicy = policy.get();

        while (true) {
            Node returnNode = tryPop();
            if (returnNode != null)
                return returnNode.value;
            else {
                try {
                    T otherValue = eliminationArray.visit(null,
                            rangePolicy.getRange());
                    if (otherValue != null) {
                        rangePolicy.recordEliminationSuccess();
                        return otherValue;
                    } else if(!blocking){
                        return null;
                    }
                } catch (TimeoutException e) {
                    rangePolicy.recordEliminationTimeout();
                }

                if(!blocking){
                    return null;
                }
            }
        }
    }
}
