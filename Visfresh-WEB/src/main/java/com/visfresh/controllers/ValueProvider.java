/**
 *
 */
package com.visfresh.controllers;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ValueProvider<K, V extends Comparable<V>> {
    V getValue(K k);
}
