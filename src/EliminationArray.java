import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//In Book chapter 11
public class EliminationArray<T>
{
    //duration can vary
    private int duration = 25;
    private LockFreeExchanger<T>[] exchanger;
    private Random random;

    //@SuppressWarnings("unchecked")
    EliminationArray(int capacity, int duration)
    {
        this.duration = duration;
        exchanger = (LockFreeExchanger<T>[]) new LockFreeExchanger[capacity];
        for (int i = 0; i < capacity; i++)
        {
            exchanger[i] = new LockFreeExchanger<T>();
        }
        random = new Random();
    }

    public T visit(T value, int range) throws TimeoutException {
        int slot = random.nextInt(range);
        return (exchanger[slot].exchange(value, duration, TimeUnit.MILLISECONDS));
    }
}

