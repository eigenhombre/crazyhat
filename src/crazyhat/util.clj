(ns crazyhat.util
  (:import java.io.File)
  (:require [clojure.string :as st]
            [clojure.java.io :as io]))


(defn pathjoin [& args]
  (st/join "/"
           (map (comp #(st/replace % #"/$" "")
                      #(st/replace % #"^/" ""))
                args)))


(defn dirname [path]
  (if (.contains path "/")
    (st/join "/" (drop-last (.split path "\\/")))
    ""))


(defn splitext [path]
  (let [parts (.split path "\\.")]
    (if (= 1 (count parts))
      [path ""]
      [(st/join "." (drop-last parts))
       (last parts)])))


(defn copy-file [from to]
  (io/make-parents to)
  (with-open [outfile (io/output-stream (File. to))
              infile (io/input-stream from)]
    (io/copy infile outfile)))


(defn extension [path]
  (let [no-dir (last (.split path "\\/"))]
    (if (.contains no-dir ".")
      (last (.split no-dir "\\.")))))
