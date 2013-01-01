(ns crazyhat.core-test
  (:use clojure.test
        midje.sweet
        crazyhat.core))

(fact "Extension on empty path"             (extension "") => nil)
(fact "Extension on simple path"            (extension "x") => nil)
(fact "Extension on simple path 2"          (extension "x.y") => "y")
(fact "Extension with dirs"                 (extension "z/x.y") => "y")
(fact "Extension with dotted dirs"          (extension "z.b/x.y") => "y")
(fact "Extension with dotted dirs (tricky)" (extension "z.b/x") => nil)

(fact (dirname "x/y/z") => "x/y")
(fact (dirname "x/y") => "x")
(fact (dirname "x") => "")
