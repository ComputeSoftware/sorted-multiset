(ns build
  "sorted-multiset's build script.
  clojure -T:build ci
  clojure -T:build deploy
  Run tests via:
  clojure -X:test
  For more information, run:
  clojure -A:deps -T:build help/doc"
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]
            [org.corfield.build :as bb]))

(def lib 'com.computesoftware/sorted-multiset)
(defn- the-version [patch] (format "0.1.%s" patch))
(def version (the-version (b/git-count-revs nil)))
(def snapshot (the-version "999-SNAPSHOT"))

(defn test "Run all the tests." [opts]
  (bb/run-tests opts)
  opts)

(defn jar "Build lib jar." [opts]
  (-> (assoc opts :lib lib :version version)
    (bb/clean)
    (bb/jar))
  opts)

(defn deploy "Deploy the JAR to Clojars." [opts]
  (-> opts
    (assoc :lib lib :version (if (:snapshot opts) snapshot version))
    (bb/deploy)))
