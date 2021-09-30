(ns com.computesoftware.sorted-multiset
  (:refer-clojure :exclude [subseq rsubseq])
  (:import (clojure.lang PersistentTreeMap IPersistentSet IPersistentMap SeqIterator RT)))

(defn ^:private throw-unsupported []
  (throw (UnsupportedOperationException.)))

(deftype SortedMultiset [^IPersistentMap _meta
                         ^PersistentTreeMap sorted-k->vs
                         ^IPersistentSet all-vset]

  Object
  (toString [this]
    (str (seq this)))

  (hashCode [this]
    (hash sorted-k->vs))

  (equals [this that]
    (= this that))

  clojure.lang.IHashEq
  (hasheq [this]
    (hash sorted-k->vs))

  clojure.lang.IMeta
  (meta [this] _meta)

  clojure.lang.IObj
  (withMeta [this meta]
    (SortedMultiset. meta sorted-k->vs all-vset))

  clojure.lang.Counted
  (count [this]
    (count all-vset))

  clojure.lang.IPersistentCollection
  (cons [this x]
    (SortedMultiset.
      _meta
      (update sorted-k->vs x (fnil conj #{}) x)
      (conj #{} x)))

  (empty [this]
    (SortedMultiset. _meta (empty sorted-k->vs) (empty all-vset)))

  (equiv [this that]
    (and
      (set? that)
      (== (count this) (count that))
      (every? #(contains? this %) that)))

  clojure.lang.Seqable
  (seq [this]
    (some->> sorted-k->vs vals seq (mapcat identity)))

  clojure.lang.Sorted
  (seq [this ascending?]
    (mapcat val (.seq sorted-k->vs ascending?)))

  (seqFrom [this k ascending?]
    (mapcat val (.seqFrom sorted-k->vs k ascending?)))

  (entryKey [this entry]
    entry)

  (comparator [this]
    (.comparator sorted-k->vs))

  clojure.lang.Reversible
  (rseq [this]
    (mapcat val (rseq sorted-k->vs)))

  clojure.lang.ILookup
  (valAt [this v]
    (get all-vset v nil))

  (valAt [this v not-found]
    (get all-vset v not-found))

  clojure.lang.IPersistentSet
  (disjoin [this v]
    (if-let [vs (get sorted-k->vs v)]
      (if (= 1 (count vs))
        (SortedMultiset. _meta (dissoc sorted-k->vs v) (disj all-vset v))
        (SortedMultiset. _meta (update sorted-k->vs v disj v) (disj all-vset v)))
      this))

  (contains [this k]
    (contains? all-vset k))

  (get [this k]
    (get all-vset k))

  clojure.lang.IReduce
  (reduce [this f]
    (case (count all-vset)
      0 (f)
      1 (first all-vset)
      (reduce f (seq this))))

  (reduce [this f init]
    (reduce-kv
      (fn [acc _ vs]
        (reduce f acc vs))
      init sorted-k->vs))

  clojure.lang.IFn
  (invoke [this k]
    (get all-vset k))

  ;(applyTo [this args]
  ;  (let [n (RT/boundedLength args 1)]
  ;    (case n
  ;      0 (throw (clojure.lang.ArityException.
  ;                 n (.. this (getClass) (getSimpleName))))
  ;      1 (.invoke this (first args))
  ;      2 (throw (clojure.lang.ArityException.
  ;                 n (.. this (getClass) (getSimpleName)))))))
  ;
  ;clojure.lang.IEditableCollection
  ;(asTransient [this]
  ;  (->AVLTransientSet (.asTransient avl-map)))

  java.io.Serializable

  ;; https://docs.oracle.com/javase/8/docs/api/java/util/TreeSet.html
  ;; The behavior of a set is well-defined even if its ordering is inconsistent with equals; it just fails to obey the general contract of the Set interface.
  java.util.Set
  (add [this o] (throw-unsupported))
  (remove [this o] (throw-unsupported))
  (addAll [this c] (throw-unsupported))
  (clear [this] (throw-unsupported))
  (retainAll [this c] (throw-unsupported))
  (removeAll [this c] (throw-unsupported))

  (containsAll [this c]
    (every? #(contains? all-vset %) (iterator-seq (.iterator c))))

  (size [this]
    (count this))

  (isEmpty [this]
    (zero? (count this)))

  (iterator [this]
    (SeqIterator. (seq this)))

  (toArray [this]
    (RT/seqToArray (seq this)))

  (^objects toArray [this ^objects a]
    (RT/seqToPassedArray (seq this) a)))

(defn sorted-multiset
  "Returns a new sorted Multiset with supplied keys. Any equal keys are handled
  as if by repeated uses of conj."
  [& keys]
  (reduce conj (SortedMultiset. {} (sorted-map) #{}) keys))

(defn sorted-multiset-by
  "Returns a new sorted Multiset with supplied keys, using the supplied
  comparator. Any equal keys are handled as if by repeated uses of conj."
  [comparator & keys]
  (reduce conj (SortedMultiset. {} (sorted-map-by comparator) #{}) keys))

(def ^:private mk-bound-fn @#'clojure.core/mk-bound-fn)

(defn subseq
  "sc must be a sorted collection, test(s) one of <, <=, > or
  >=. Returns a seq of those entries with keys ek for
  which (test (.. sc comparator (compare ek key)) 0) is true"
  ([^clojure.lang.Sorted sc test key]
   (let [include (mk-bound-fn sc test key)]
     (if (#{> >=} test)
       (drop-while (complement include) (. sc seq true))
       (take-while include (. sc seq true)))))
  ([^clojure.lang.Sorted sc start-test start-key end-test end-key]
   (when-let [s (subseq sc start-test start-key)]
     (take-while (mk-bound-fn sc end-test end-key) s))))

(comment (sc.api/defsc 2)
  (include {:k 1, :a "c"}))

(defn rsubseq
  "sc must be a sorted collection, test(s) one of <, <=, > or
  >=. Returns a reverse seq of those entries with keys ek for
  which (test (.. sc comparator (compare ek key)) 0) is true"
  ([^clojure.lang.Sorted sc test key]
   (let [include (mk-bound-fn sc test key)]
     (if (#{< <=} test)
       (drop-while (complement include) (. sc seqFrom key false))
       (take-while include (. sc seq false)))))
  ([^clojure.lang.Sorted sc start-test start-key end-test end-key]
   (when-let [s (rsubseq sc start-test start-key)]
     (drop-while (complement (mk-bound-fn sc end-test end-key)) s))))

(comment (sc.api/defsc 1)
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
  (subseq sm > {:k 0}))
