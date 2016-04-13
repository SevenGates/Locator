package mah.sys.locator;

import java.util.Observable;

/**
 * Created by Alex on 13-Apr-16.
 */
public abstract class ObservableRunnable<T> extends Observable implements Runnable {

    T data;

    @Override
    public abstract void run();

    public T getData() {
        return data;
    }
}
