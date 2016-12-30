package com.github.vbro.cocoyam;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Created by vbro on 17 September 2016.
 */
public class IndexedSet<ElementType> extends AbstractSet<ElementType> implements Set<ElementType>, Cloneable {

    // all elements of this IndexedSet must be an instance of elementClass
    protected Class elementClass;

    // all elements of this IndexedSet are contained in inventory
    protected Set<ElementType> inventory = Collections.emptySet();

    // maps method to a map of values -> objects
    protected Map<String, Map<Object, Set<ElementType>>> index = Collections.emptyMap();

    // maps method name to Method
    protected transient  Map<String, Method> indexedMethods = Collections.emptyMap();

    /**
     * This protected zero-argument constructor is for Serialized subclasses
     * of IndexedSet.
     */
    protected IndexedSet() {}

    /**
     * Creates an IndexedSet that indexes all methods in methods
     * @param elementClass Class type of elements in this set
     * @param methods collection of Methods to index
     * @throws IllegalArgumentException if any of the methods cannot be called from
     * elementClass, is not public, is not zero-argument or has void return type
     */
    public IndexedSet(Class elementClass, Collection<Method> methods) {
        for (Method method : methods) {
            if (!method.getDeclaringClass().isAssignableFrom(elementClass)) {
                throw new IllegalArgumentException(String.format(
                        "Method %s cannot be called from an object of class %s",
                        method.toString(), elementClass.getName()));
            }
            if (method.getParameterCount() > 0 || !Modifier.isPublic(method.getModifiers())) {
                throw new IllegalArgumentException(String.format(
                        "Method %s should be a zero-argument method", method.toString()));
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                throw new IllegalArgumentException(String.format(
                        "Method %s should be a public method", method.toString()));
            }
        }
        constructIndexedSet(elementClass, methods);
    }

    /**
     * Creates an IndexedSet that indexes all methods in methods
     * @param elementClass Class type of elements in this set
     * @param methodNames array of method names to index
     * @throws IllegalArgumentException if any of the methods cannot be called from
     * elementClass, is not public, is not zero-argument or has void return type
     */
    public IndexedSet(Class elementClass, String[] methodNames) {
        constructIndexedSet(elementClass, getMethods(elementClass, Arrays.asList(methodNames)));
    }

    /**
     * Creates an IndexedSet that indexes all the accessible zero-argument
     * methods in elementClass that have non-void return types.
     * @param elementClass Class type of elements in this set
     */
    public IndexedSet(Class elementClass) {
        this(elementClass, false);
    }

    /**
     * Creates an IndexedSet that indexes all the accessible zero-argument
     * methods in elementClass that have non-void return types.
     * If checkSerializableReturnType then there is an additional requirement
     * that the method's return type be Serializable.
     * @param elementClass
     * @param checkSerializableReturnType
     */
    protected IndexedSet(Class elementClass, boolean checkSerializableReturnType) {
        Set<Method> methods = new HashSet();
        for (Method m : elementClass.getMethods()) {
            Class returnType = m.getReturnType();
            if (m.getParameterCount() == 0 && !m.getReturnType().equals(Void.TYPE) &&
                    (!checkSerializableReturnType || returnType.isPrimitive() ||
                            Serializable.class.isAssignableFrom(returnType))) {
                methods.add(m);
            }
        }
        constructIndexedSet(elementClass, methods);
    }

    /**
     * Helper method to initialize the fields of this IndexedSet
     */
    private void constructIndexedSet(Class elementClass, Collection<Method> methods) {
        // check if elementClass is instance of ElementType
        if (methods.size() == 0) {
            throw new IllegalArgumentException("List of methods to index is empty");
        }
        this.index = new HashMap<>();
        this.inventory = new HashSet<>();
        this.indexedMethods = new HashMap<>();
        for (Method method : methods) {
            if (method.getReturnType().equals(Void.TYPE)) {
                throw new IllegalArgumentException(String.format(
                        "Method %s should have a non-void return type", method.toString()));
            }
            this.index.put(method.getName(), new HashMap<>());
            this.indexedMethods.put(method.getName(), method);
        }
        this.elementClass = elementClass;
    }

    /**
     * @return a Collection of all the public, zero-argument methods of clazz
     * @throws IllegalArgumentException if there is no public, zero-argument
     * method in clazz for any of the methodNames, or if method return type is void
     */
    protected Collection<Method> getMethods(Class clazz, Collection<String> methodNames) {
        Set<Method> methods = new HashSet();
        for (String methodName : methodNames) {
            try {
                methods.add(clazz.getMethod(methodName));
            }
            catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(String.format(
                    "Class %s does not have a public, zero-argument method named: %s",
                    clazz.getName(), methodName));
            }
        }
        return methods;
    }

    /**
     * @see {@link java.util.Set#add(java.lang.Object)}
     * In addition, element is added to the index.
     * @param element element to add to this set
     * @return
     */
    public boolean add(ElementType element) {
        return add(element, true);
    }

    /**
     * Adds element to this set, with option to index the element.
     * @param element element to add to this set
     * @param shouldIndex if shouldIndex, element is added to index.
     * @return true if this set did not already contain element,
     * false otherwise.
     */
    public boolean add(ElementType element, boolean shouldIndex) {
        if (element == null) {
            throw new NullPointerException("IndexedSet does not accept null elements.");
        }
        if (this.inventory.contains(element)) {
            // element already contained, nothing to do
            return false;
        }

        if (!this.elementClass.isInstance(element)) {
            throw new IllegalArgumentException(String.format(
                    "%s is not an instance of %s", element.toString(), this.elementClass.getName()));
        }

        if (shouldIndex) {
            addToIndex(element);
        }
        return this.inventory.add(element);
    }

    /**
     * Adds element to the index of this IndexedSet
     */
    private void addToIndex(ElementType element) {
        for (Method method : this.indexedMethods.values()) {
            try {
                Object value = method.invoke(element);
                Map<Object, Set<ElementType>> valuesMap = this.index.get(method.getName());
                if (valuesMap.containsKey(value)) {
                    valuesMap.get(value).add(element);
                } else {
                    Set elementSet = new HashSet<>();
                    elementSet.add(element);
                    valuesMap.put(value, elementSet);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                continue;
            }
        }
    }

    /**
     * Clears the contents of the index.
     * This has no effects on the set of elements in this Set.
     */
    public void clearIndex() {
        for (String methodName : this.indexedMethods.keySet()) {
            this.index.put(methodName, new HashMap());
        }
    }

    /**
     * Recreates the index.
     * This has no effects on the set of elements in this Set.
     */
    public void reIndex() {
        this.clearIndex();
        for (ElementType element : this.inventory) {
            addToIndex(element);
        }
    }

    /**
     * see {@link Set#addAll(java.util.Collection)}
     * Elements added to the set will also be added to the index.
     */
    public boolean addAll(Collection<? extends ElementType> c) {
        return addAll(c, true);
    }

    /**
     * see {@link Set#addAll(java.util.Collection)}
     * If shouldIndex, elements added to the set will also be added to the index.
     */
    public boolean addAll(Collection<? extends ElementType> c, boolean shouldIndex) {
        boolean changed = false;
        for (ElementType element : c) {
            boolean b = this.add(element, shouldIndex);
            changed = changed || b;
        }
        return changed;
    }

    /**
     * see {@link Set#clear()}
     * This also clears the index.
     */
    public void clear() {
        this.clear(true);
    }

    /**
     * see {@link Set#clear()}
     * If shouldClearIndex, this also clears the index.
     */
    public void clear(boolean shouldClearIndex) {
        if (shouldClearIndex) {
            clearIndex();
        }
        this.inventory = new HashSet();
    }

    /**
     * see {@link Set#remove(java.lang.Object)}
     * Element removed will also be removed from the index.
     */
    public boolean remove(Object o) {
        return remove(o, true);
    }

    /**
     * see {@link Set#remove(java.lang.Object)}
     * If shouldRemoveFromIndex, element removed will also
     * be removed from the index.
     */
    public boolean remove(Object o, boolean shouldRemoveFromIndex) {
        if (shouldRemoveFromIndex) {
            removeFromIndex(o);
        }
        return this.inventory.remove(o);
    }

    /**
     * Removes Object o from the index.
     */
    private void removeFromIndex(Object o) {
        for (Map<Object, Set<ElementType>> valuesMap : this.index.values()) {
            for (Set<ElementType> elementSet : valuesMap.values()) {
                elementSet.remove(o);
            }
        }
    }

    /**
     * see {@link Set#retainAll(java.util.Collection)}
     * Elements removed will also be removed from the index.
     */
    public boolean retainAll(Collection<?> c) {
        return this.retainAll(c, true);
    }

    /**
     * see {@link Set#retainAll(java.util.Collection)}
     * If shouldRemoveFromIndex, elements removed will also
     * be removed from the index.
     */
    public boolean retainAll(Collection<?> c, boolean shouldRemoveFromIndex) {
        boolean changed = false;
        Set<ElementType> copy = new HashSet<>(this.inventory);
        for (ElementType element : copy) {
            if (!c.contains(element)) {
                boolean b = this.remove(element, shouldRemoveFromIndex);
                changed = changed || b;
            }
        }
        return changed;
    }

    /**
     * see {@link Set#removeAll(java.util.Collection)}
     * Elements removed will also be removed from the index.
     */
    public boolean removeAll(Collection<?> c) {
        return this.removeAll(c, true);
    }


    /**
     * see {@link Set#removeAll(java.util.Collection)}
     * If shouldRemoveFromIndex, elements removed will also
     * be removed from the index.
     */
    public boolean removeAll(Collection<?> c, boolean shouldRemoveFromIndex) {
        boolean changed = false;
        for (Object element : c) {
            boolean b = this.remove(element, shouldRemoveFromIndex);
            changed = changed || b;
        }
        return changed;
    }

    /**
     * @return true if o is an instance of IndexedSet and this.equals(o)
     * and the indexed methods and index for both sets are equal.
     */
    public boolean equalsComparingIndexes(Object o) {
        if (o instanceof IndexedSet) {
            IndexedSet otherSet = (IndexedSet)o;
            return (this.equals(otherSet) &&
                    this.indexedMethods.equals(otherSet.indexedMethods) &&
                    this.index.equals(otherSet.index));
        }
        else {
            return false;
        }
    }

    /**
     * see {@link Set#size()}
     */
    public int size() {
        return this.inventory.size();
    }

    /**
     * see {@link Set#isEmpty()}
     */
    public boolean isEmpty() {
        return this.inventory.isEmpty();
    }

    /**
     * see {@link Set#contains(java.lang.Object)}
     */
    public boolean contains(Object o) {
        return this.inventory.contains(o);
    }

    /**
     * see {@link Set#iterator()}
     */
    public Iterator<ElementType> iterator() {
        return this.inventory.iterator();
    }

    /**
     * see {@link Set#toArray()}
     */
    public Object[] toArray() {
        return this.inventory.toArray();
    }

    /**
     * see {@link Set#toArray(java.lang.Object[])}
     */
    public <T> T[] toArray(T[] a) {
        return this.inventory.toArray(a);
    }

    /**
     * see {@link Set#containsAll(java.util.Collection)}
     */
    public boolean containsAll(Collection<?> c) {
        return this.inventory.containsAll(c);
    }

    /**
     * @return a Set of elements in this set that returns the given value when
     * method is invoked, based on the current state of the index.
     * @throws IllegalArgumentException if method is not in the index
     */
    public Set<ElementType> get(Method method, Object value) {
        if (this.indexedMethods.values().contains(method)) {
            return get(method.getName(), value);
        }
        else {
            throw new IllegalArgumentException(String.format("Method \"%s\" is not indexed", method.toString()));
        }
    }

    /**
     * @return a Set of elements in this set that returns the given value when
     * method methodName is invoked, based on the current state of the index.
     * @throws IllegalArgumentException if method methodName is not in the index
     */
    public Set<ElementType> get(String methodName, Object value) {
        Set<ElementType> s = new HashSet();
        if (this.index.keySet().contains(methodName)) {
            Map<Object, Set<ElementType>> methodValues = this.index.get(methodName);
            if (methodValues.containsKey(value)) {
                s.addAll(methodValues.get(value));
            }
            return s;
        }
        else {
            throw new IllegalArgumentException(String.format("Method \"%s\" is not indexed", methodName));
        }
    }

    /**
     * @return true if there is a method with name methodName that is indexed by this set
     */
    public boolean isIndexed(String methodName) {
        return this.index.containsKey(methodName);
    }

    /**
     * @return true if method is indexed by this set
     */
    public boolean isIndexed(Method method) {
        return this.indexedMethods.containsValue(method);
    }

    /**
     * see {@link java.lang.Object#clone()}
     */
    public IndexedSet<ElementType> clone() {
        IndexedSet<ElementType> cloneSet = new IndexedSet(this.elementClass, this.indexedMethods.values());
        cloneSet.inventory = new HashSet(this.inventory);
        for (String methodName : this.index.keySet()) {
            Map<Object, Set<ElementType>> thisMap = this.index.get(methodName);
            if (!thisMap.isEmpty()) {
                Map<Object, Set<ElementType>> cloneMap = cloneSet.index.get(methodName);
                for (Object key : thisMap.keySet()) {
                    cloneMap.put(key, new HashSet(thisMap.get(key)));
                }
            }
        }
        return cloneSet;
    }

    /**
     * @return see {@link java.util.AbstractCollection#toString()}
     */
    public String toString() {
        return this.inventory.toString();
    }

    /**
     * @return a human-readable string representation of the elements and index this set
     */
    public String toStringHumanReadable() {
        StringBuilder sb = new StringBuilder();
        sb.append("elements:");
        sb.append(String.join(",", this.inventory.stream().map(i -> i.toString()).collect(Collectors.toList())));
        sb.append("\n");

        sb.append("index:\n");
        for (String methodName : this.index.keySet()) {
            sb.append("\t" + methodName + ":\n");
            Map<Object, Set<ElementType>> map = this.index.get(methodName);
            for (Object value : map.keySet()) {
                sb.append("\t\t" + value + ":\t");
                Set<ElementType> items = map.get(value);
                String itemsString = String.join(",", items.stream().map(i -> i.toString())
                        .collect(Collectors.toList()));
                sb.append(itemsString + "\n");
            }
        }
        return sb.toString();
    }

    /**
     * @return a json string representation of the elements and index this set
     */
    public String toStringJson() {
        Map<String, Object> jsonObject = new HashMap();
        jsonObject.put("elements", this.inventory);
        jsonObject.put("index", this.index);
        return JsonUtil.toJsonString(jsonObject);
    }

    /**
     * @return a pretty-printed json string representation of the elements and index this set
     */
    public String toStringJsonPrettyPrint() {
        Map<String, Object> jsonObject = new HashMap();
        jsonObject.put("elements", this.inventory);
        jsonObject.put("index", this.index);
        return JsonUtil.toJsonStringPrettyPrint(jsonObject);
    }
}
