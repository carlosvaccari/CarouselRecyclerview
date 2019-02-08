package cvaccari.com.br.carouselrecyclerview;

import android.util.SparseArray;

import java.util.ArrayList;

public class MultiSparseArray<E> {

    SparseArray<ArrayList<E>> mArray;
    int mSize = 0;

    public MultiSparseArray(int initialCapacity) {
        mArray = new SparseArray<>(initialCapacity);
    }

    public void put(int key, E value) {
        ArrayList<E> values = mArray.get(key);
        if (values == null) values = new ArrayList<>();
        values.add(value);
        ++mSize;
        mArray.put(key, values);
    }

    public E pop(int key) {
        ArrayList<E> values = mArray.get(key);
        if (values != null && values.size() > 0) {
            E value = values.get(0);
            values.remove(0);
            --mSize;
            return value;
        }
        return null;
    }

    public int size() {
        return mArray.size();
    }

    public ArrayList<E> valuesAt(int index) {
        return mArray.valueAt(index);
    }
}
