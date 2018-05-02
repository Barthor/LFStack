
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicStampedReference;
//modelled after LockFreeExchanger in book chapter 11
public class LockFreeExchanger<T>
{
    static final int EMPTY = 0, WAITING = 1, BUSY = 2;
    AtomicStampedReference<T> slot = new AtomicStampedReference<T>(null, 0);
    public T exchange(T myItem, long timeout, TimeUnit unit) throws TimeoutException
    {
        //time bound implementation
        long nanos = unit.toNanos(timeout);
        long timeBound = System.nanoTime() + nanos;
        int[] stampholder = {EMPTY};
        while (true)
        {
            if (System.nanoTime() > timeBound)
            {
                throw new TimeoutException();
            }
            T yrItem = slot.get(stampholder);
            int stamp = stampholder[0];
            switch(stamp)
            {
                //3 possible states
                case EMPTY:
                    //
                    if (slot.compareAndSet(yrItem, myItem, EMPTY, WAITING))
                    {
                        while (System.nanoTime() < timeBound)
                        {
                            yrItem = slot.get(stampholder);
                            if (stampholder[0] == BUSY)
                            {
                                slot.set(null, EMPTY);
                                return yrItem;
                            }
                        }
                        if (slot.compareAndSet(myItem, null, WAITING, EMPTY))
                        {
                            throw new TimeoutException();
                        }
                        else
                        {
                            yrItem = slot.get(stampholder);
                            slot.set(null, EMPTY);
                            return yrItem;
                        }
                    }
                    break;

                case WAITING:
                        if (slot.compareAndSet(yrItem, myItem, WAITING, BUSY))
                            return yrItem;
                        break;

                case BUSY:
                    break;
                default: //impossible
            }
        }
    }
}
