package bgu.spl181.net.data;

import bgu.spl181.net.data.IdataObjects.Container;
import bgu.spl181.net.data.IdataObjects.DataObject;
import bgu.spl181.net.data.users.*;
import com.google.gson.Gson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by avielber on 1/11/18.
 */
public abstract class AbstractDataBase<T extends DataObject> {
    private List<T> _all = null;

    protected List<T> getAll() {
        if (_all == null) {
            _all = readAll();
        }

        return _all;
    }

    protected List<T> setAll(List<T> newAll) {
        _all = newAll;
        return _all;
    }

    protected abstract List<T> readAll();

    protected abstract List<T> writeAll();

    /**
     * The method checks whether a dataObject exists in the data base,
     * it returns true if the check was successful and the jada object exists.
     * Therefor it is possible that the method will return false,
     * even though the data does exist in the data base (because of unexpected failure).
     *
     *
     * @param existsSuchThat the predicate which specifies the existence of a data object in the data base
     * @return true if the data object exists in the data base or false otherwise.
     */
    public boolean exists(Predicate<T> existsSuchThat) {
        List<T> all = getAll();

        if (all == null) {
            return false;
        }

        List<T> specific = all.stream()
                .filter(existsSuchThat)
                .collect(Collectors.toList());

        return specific.size() == 1;
    }

    /**
     * @param getBy the predicate which specifies which data object to get from the data base
     * @return the dataObject from the data base or null if failed
     */
    public T get(Predicate<T> getBy) {
        List<T> all = getAll();

        if (all == null) {
            return null;
        }

        List<T> specific = all.stream()
                .filter(getBy)
                .collect(Collectors.toList());

        if (specific.size() != 1) {
            return null;
        }

        return specific.get(0);
    }

    /**
     * @param dataObject the data object to add into data base
     * @return the added dataObject or null if addition failed
     */
    public T add(T dataObject) {
        if (getAll().add(dataObject)) {
            if (writeAll() == null) {
                return null;
            }
            return dataObject;
        }

        return null;
    }

    /**
     * @param deleteBy the predicate which specifies which data object to delete from the data base
     * @return the deleted dataObject or null if failed
     */
    public T delete(Predicate<T> deleteBy) {
        List<T> all = getAll();

        if (all == null) {
            return null;
        }

        List<T> deleted = all.stream()
                .filter(deleteBy)
                .collect(Collectors.toList());

        if (deleted.size() != 1) {
            return null;
        }

        all = setAll(all.stream()
                .filter(deleteBy.negate())
                .collect(Collectors.toList()));
        if (all == null) {
            return null;
        }

        if (writeAll() == null) {
            return null;
        }
        return deleted.get(0);
    }

    /**
     * @param dataObject the data object to update in the data base.
     * @param updateBy the predicate which specifies which data object to update in the data base
     * @return the updated dataObject or null if failed
     */
    public T update(T dataObject, Predicate<T> updateBy) {
        List<T> all = getAll();

        if (all == null) {
            return null;
        }

        List<T> old = all.stream()
                .filter(updateBy)
                .collect(Collectors.toList());

        if (old.size() != 1) {
            return null;
        }

        setAll(all.stream()
                .filter(updateBy.negate())
                .collect(Collectors.toList()));

        all = getAll();

        if (all == null) {
            return null;
        }

        if (all.add(dataObject)) {
            if (writeAll() == null) {
                return null;
            }

            return dataObject;
        }

        writeAll(); // update failed: only deletion of old data succeeded. what to do know? (write into database?)
        return null;
    }


    protected abstract String getJsonPath();

}
