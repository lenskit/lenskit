package org.grouplens.lenskit.eval.traintest;

import com.google.common.base.Supplier;

/**
 * This class allows us to extend the functionality of the Supplier<T>
 * factory type class.
 * 
 * @author hugof
 *
 * @param <T>
 */
interface SwitchedSupplier<T> extends Supplier<T> {
      public void setGuessCandidates(boolean t);
      public boolean guessCandidates();
}
