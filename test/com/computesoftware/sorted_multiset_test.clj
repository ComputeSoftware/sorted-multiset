(ns com.computesoftware.sorted-multiset-test
  (:require
    [clojure.test :refer :all]
    [com.computesoftware.sorted-multiset :as sm]))

(defn k-comparator
  [x y]
  (compare (:k x) (:k y)))

(deftest sorted-multiset-test
  (is (= (sm/sorted-multiset) (sm/sorted-multiset)))
  (is (= (sm/sorted-multiset 1) (sm/sorted-multiset 1)))
  (is (= (sm/sorted-multiset)
        (disj (sm/sorted-multiset 1) 1)))
  (is (= (sm/sorted-multiset 1)
        (conj (sm/sorted-multiset) 1)))
  (is (= (list 1 2 3)
        (seq (sm/sorted-multiset 3 2 1))))
  (is (= (list
           {:k 0 :a "a"}
           {:k 0 :a "b"}
           {:k 1 :a "c"})
        (seq (sm/sorted-multiset-by k-comparator
               {:k 0 :a "a"}
               {:k 0 :a "b"}
               {:k 1 :a "c"})))))
