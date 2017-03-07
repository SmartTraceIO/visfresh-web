/**
 *
 */
package com.visfresh.utils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@FunctionalInterface
public interface ToStringer<M> {
    String toString(M m);
}
