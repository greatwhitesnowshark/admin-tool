/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

public class Pointer<T extends Object> {

    private T object;

    /**
     * Creates a new Pointer object without an existing object reference.
     */
    public Pointer() {
    }

    /**
     * Creates a new Pointer object with an existing object reference.
     *
     * @param object The object to store in the first available pointer index
     */
    public Pointer(T object) {
        this.object = object;
    }

    /**
     * Gets the object by first-index on the pointer-chain.
     *
     * @return The stored object
     */
    public T Get() {
        return this.object;
    }

    /**
     * Sets the pointer's object to a new object
     *
     * @param object The new object to replace the stored value
     */
    public void Set(T object) {
        this.object = object;
    }

    /**
     * Checks to see if a given object is contained by the pointer.
     *
     * @param object The object to check against
     * @return true if the given object was found inside the pointer; otherwise false
     */
    public boolean Contains(T object) {
        return this.object.equals(this.object);
    }
}