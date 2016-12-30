package com.github.vbro.cocoyam;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by vbro on 03 October 2016.
 */
public class IndexedSerializableSet<ElementType extends Serializable> extends IndexedSet<ElementType>
        implements Serializable {

    /**
     * Zero-argument constructor to meet requirements of Serializable classes
     */
    protected IndexedSerializableSet() {

    }

    public IndexedSerializableSet(Class elementClass) {
        super(elementClass, true);
        checkReturnValueSerializable();
    }

    public IndexedSerializableSet(Class elementClass, Collection<Method> methods) {
        super(elementClass, methods);
        checkReturnValueSerializable();
    }

    public IndexedSerializableSet(Class elementClass, String[] methodNames) {
        super(elementClass, methodNames);
        checkReturnValueSerializable();
    }

    /**
     * Custom method to deserialize an IndexedSerializableSet
     * @param ois
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        this.elementClass = (Class) ois.readObject();
        this.inventory = (Set) ois.readObject();
        this.index = (Map) ois.readObject();

        this.indexedMethods = new HashMap();
        for (Method method : getMethods(this.elementClass, this.index.keySet())) {
            this.indexedMethods.put(method.getName(), method);
        }
    }

    /**
     * Custom method to serialize this IndexedSerializableSet
     * @param oos
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(this.elementClass);
        oos.writeObject(this.inventory);
        oos.writeObject(this.index);
    }

    /**
     * Method called from constructors of IndexedSerializableSet to ensure
     * that return types of indexed methods are serializable
     */
    private void checkReturnValueSerializable() {
        for (Method method : this.indexedMethods.values()) {
            Class returnType = method.getReturnType();
            if(!(returnType.isPrimitive() || Serializable.class.isAssignableFrom(returnType))) {
                throw new IllegalArgumentException(String.format(
                        "Return type (%s) of method (%s) is not a sub class nor sub interface of java.io.Serializable.",
                        returnType.getName(), method.getName()));
            }
        }
    }

}
