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

(facts "about basename"
       (basename "x.y")      => "x"
       (basename "x")        => "x"
       (basename "/x/y/z.f") => "z"
       (basename "/x/y/z")   => "z")

(facts "about appendpath"
       (appendpath "a" "b")  => "a/b"
       (appendpath "a/" "b") => "a/b"
       (appendpath "a" "/b") => "a/b")

(defn fake-watcher [root]
  "Simulate behavior of core/watcher for testing"
  (let [s (file-seq (File. root))
        filenames (map #(.getPath %) s)]
    (for [ext (map name extensions-to-update)
          f filenames
          :when (= ext (extension f))]
      (File. f))))

;; Test harness where we actually create files and check corresponding
;; results:
(let [markup "/tmp/testdir/markup"
      site "/tmp/testdir/site"
      index_md (str markup "/index.md")
      fakejpg (str markup "/somedir/fake.jpg")
      newjpg (str site "/somedir/fake.jpg")
      fakepng (str markup "/somedir/fake.png")
      newpng (str site "/somedir/fake.png")
      index_html (str site "/index.html")]
  (io/make-parents index_md)
  (io/make-parents fakejpg)
  (io/make-parents fakepng)
  (spit index_md "# Some stuff")
  (spit fakejpg "xxxx")
  (spit fakepng "xxxx")
  (handle-update (fake-watcher markup) markup site)
  (fact (File. index_html) => #(.exists %))
  (fact (File. newjpg) => #(.exists %))
  (fact (File. newpng) => #(.exists %)))

