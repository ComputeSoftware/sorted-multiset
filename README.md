# sorted-multiset

Persistent sorted Multiset with multiplicity capped at 1 and Set value equivalence.

## Use Case

Default [Java TreeSet semantics](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/TreeSet.html)
define elements that are deemed equal by `compareTo` as equal from the standpoint of the set. In some cases, it is
useful to allow elements deemed equal by `compareTo` to reside in the set.

## Release Information

```clojure
com.computesoftware/sorted-multiset {:mvn/version "0.1.1"}
```

## Example usage

```clojure
(require '[com.computesoftware.sorted-multiset :as sm])

(sm/sorted-multiset-by #(compare (:k %1) (:k %2))
  {:k 0
   :v "a"}
  {:k 1
   :v "b"}
  {:k 0
   :v "c"})
=> #{{:k 0, :v "a"} {:k 0, :v "c"} {:k 1, :v "b"}}
```

This library has differing equality semantics that Java Treeset. Clojure's `subseq`
and `rsubseq` [assume Java Treeset equality semantics](https://github.com/clojure/clojure/blob/b8132f92f3c3862aa6cdd8a72e4e74802a63f673/src/clj/clojure/core.clj#L5129-L5131)
, which this library does not follow. As a result, both of those functions do not always return correct results.
Therefore, this library provides its own `subseq` and `rsubseq` functions that will work as expected.

```clojure
(def sm
  (sorted-multiset-by #(compare (:k %1) (:k %2))
    {:k 0
     :v "a"}
    {:k 1
     :v "b"}
    {:k 1
     :v "c"}
    {:k 0
     :v "c"}))
=> #'com.computesoftware.sorted-multiset/sm

;; INCORRECT: The first element should not have been included.
(subseq sm > {:k 0})
=> ({:k 0, :v "c"} {:k 1, :v "b"} {:k 1, :v "c"})

;; CORRECT: We get the expected result using sorted-multiset's subseq.
(sm/subseq sm > {:k 0})
=> ({:k 1, :v "b"} {:k 1, :v "c"})
```
