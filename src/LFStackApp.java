
import java.util.Random;
import java.util.concurrent.*;

public class LFStackApp
{
    public static void main(String[] args) throws ExecutionException, InterruptedException
    {
        //during fixing my old lockfreestack solution i found that the driver class found here
        //was a much clearer version of what I was trying to do so I modified it to use to set up using the data structures

        SequentialCollection<Object> collection = null;
        //test data structure that we'll operate
        collection = new EliminationBackoffStack<Object>(2, 10, false);
        //number of threads
        int threadCount = 2;
        //operations to perform
        int opCount = 10000000;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new Thread(createRunnable(collection, (opCount + threadCount) / threadCount));
        }
        double executeTimeMS = System.nanoTime();
        for (Thread thread : threads)
        {
            thread.start();
        }
        for (Thread thread : threads)
        {
            try
            {
                thread.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        executeTimeMS = (double) (System.nanoTime() - executeTimeMS) / 1000000000.0;
        System.out.println("" + executeTimeMS);
        System.gc();
    }
    public static Runnable createRunnable(
            final SequentialCollection<Object> collection, final int increments) {
        return new Runnable()
        {
            @Override
            public void run()
            {
                Random rand = new Random();
                for (int i = 0; i < increments; i++)
                {
                    double op = rand.nextDouble();
                    if (op <= 0.6)
                    {
                        collection.add(new Object());
                    } else
                    {
                        collection.get();
                    }
                }
            }
        };
    }
}
