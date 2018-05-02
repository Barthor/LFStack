public abstract class Stack<T> implements SequentialCollection<T>
{
    //abstract class idea also taken from github link
    @Override
    public final void add(T t) {
        push(t);
    }

    @Override
    public final T get() {
        return pop();
    }

    public abstract boolean push(T t);

    public abstract T pop();
}