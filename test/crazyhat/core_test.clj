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


(let [markup "/tmp/testdir/markup"
      site "/tmp/testdir/site"
      index_md (str markup "/index.md")
      fakejpg (str markup "/somedir/fake.jpg")
      newjpg (str site "/somedir/fake.jpg")
      index_html (str site "/index.html")]
  (io/make-parents index_md)
  (io/make-parents fakejpg)
  (spit index_md "# Some stuff")
  (spit fakejpg "xxxx")
  (handle-update [(File. index_md)
                  (File. fakejpg)] markup site)
  (fact (File. index_html) => #(.exists %))
  (fact (File. newjpg) => #(.exists %)))

