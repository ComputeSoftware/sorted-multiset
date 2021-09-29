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
