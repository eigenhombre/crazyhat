(ns crazyhat.core-test
  (:import java.io.File)
  (:use clojure.test
        midje.sweet
        crazyhat.core
        crazyhat.util)
  (:require [clojure.java.io :as io]
            [net.cgrand.enlive-html :as enlive]))


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

(defn posts-in-str
  [contents]
  (map (comp first :content)
       (-> contents
           java.io.StringReader.
           enlive/html-resource
           (enlive/select [:body :div#posts])
           first
           :content
           first
           :content)))


(defn check-contents
  [filename desired-posts]
  (let [[filen ext] (splitext filename)
        contents (slurp filename)]
    (case ext
      "html" (do
               (facts
                (str-contains? contents "!DOCTYPE") => true
                (str-contains? contents "site.css") => true
;;                (posts-in-str contents) => desired-posts YOU ARE HERE
                ))
      nil)))

(def files-to-create
  ["somedir/to/this/file/fake.jpg"
   ["index.md"    "index.html"    "# Header\n\nbody"   ["blogpost"]]
   ["blogpost.md" "blogpost.html" "A sample blog post" []]
   "fake.png"
   "site.css"
   "media/css/ancillary.css"])

(do
  (delete-file-recursively (File. root))
  ;; Make test files:
  (doseq [fname files-to-create]
    (let [src (pathjoin markup (if (vector? fname) (first fname)  fname))
          content (if (vector? fname) (nth fname 2) "fake stuff")]
      (io/make-parents src)
      (spit src content)))

  ;; Handle the output file creation:
  (handle-update (fake-watcher markup) markup site root)

  ;; Check resulting content:
  (doseq [fname files-to-create]
    (let [dst (pathjoin site (if (vector? fname) (second fname) fname))
          desired_posts (if (vector? fname) (nth fname 3) [])]
      (fact (File. dst) => #(.exists %))
      (check-contents dst desired_posts))))
