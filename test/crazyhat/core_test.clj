(ns crazyhat.core-test
  (:import java.io.File)
  (:use clojure.test
        midje.sweet
        crazyhat.core
        crazyhat.util)
  (:require [clojure.java.io :as io]))


(facts "about extension"
       (extension "")        => nil
       (extension "x")       => nil
       (extension "x.y")     => "y"
       (extension "z/x.y")   => "y"
       (extension "z.b/x.y") => "y"
       (extension "z.b/x")   => nil)

(facts "about dirname"
       (dirname "x/y/z")   => "x/y"
       (dirname "x/y")     => "x"
       (dirname "x")       => ""
       (dirname
        (dirname "x/y/z")) => "x")


(facts "about splitext"
       (splitext "x.y")      => ["x" "y"]
       (splitext "x")        => ["x" ""]
       (splitext "/x/y/z.f") => ["/x/y/z" "f"]
       (splitext "/x/y/z")   => ["/x/y/z" ""]
       (splitext "/x/y.z/p.q/r.s.t") => ["/x/y.z/p.q/r.s" "t"])


(facts "about pathjoin"
       (pathjoin "a" "b")  => "a/b"
       (pathjoin "a/" "b") => "a/b"
       (pathjoin "a" "/b") => "a/b"
       (pathjoin "a" "b" "c") => "a/b/c")


(defn fake-watcher
  "Simulate behavior of core/watcher for testing"
  [root]
  (let [s (file-seq (File. root))
        filenames (map #(.getPath %) s)]
    (for [ext (map name extensions-to-update)
          f filenames
          :when (= ext (extension f))]
      (File. f))))


(defn delete-file-recursively
  "Delete file f. If it's a directory, recursively delete all its
contents. Raise an exception if any deletion fails unless silently is
true. [stolen/modified from clojure-contrib]"
  [f]
  (if (.isDirectory f)
    (doseq [child (.listFiles f)]
      (delete-file-recursively child)))
  (.delete f))

;; Test harness where we actually create files and check corresponding
;; results:
(def root "/tmp/testdir")
(def markup (pathjoin root "markup"))
(def site (pathjoin root "site"))

(defn str-contains?
  "C.f. https://groups.google.com/forum/?fromgroups=#!topic/clojure/VlKrgA3Dco4"
  [^String big ^String little]
  (not (neg? (.indexOf big little))))

(defn check-contents
  [filename]
  (let [[filen ext] (splitext filename)
        contents (slurp filename)]
    (case ext
      "html" (do
               (facts
                (str-contains? contents "Header") => true
                (str-contains? contents "!DOCTYPE") => true
                (str-contains? contents "site.css") => true
                ))
      nil)))

(do
  (delete-file-recursively (File. root))
  (doseq [fname ["somedir/to/this/file/fake.jpg"
                 ["index.md" "index.html" "# Header\n\nbody"]
                 "fake.png"
                 "site.css"
                 "media/css/ancillary.css"]]
    (let [src (pathjoin markup (if (vector? fname) (first fname)  fname))
          dst (pathjoin   site (if (vector? fname) (second fname) fname))]
      (io/make-parents src)
      (spit src (if (vector? fname) (nth fname 2) "fake stuff"))
      (handle-update (fake-watcher markup) markup site root)
      (fact (File. dst) => #(.exists %))
      (check-contents dst))))
