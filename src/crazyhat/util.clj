(ns crazyhat.util
  (:import java.io.File)
  (:require [clojure.string :as st]
            [clojure.java.io :as io]))


(defn pathjoin [a b]
  (str (st/replace a #"/$" "")
       "/"
       (st/replace b #"^/" "")))


(defn dirname [path]
  (if (.contains path "/")
    (st/join "/" (drop-last (.split path "\\/")))
    ""))


(defn basename [path]
  (let [path (last (.split path "\\/"))]
    (if (.contains path ".")
      (st/join "." (drop-last (.split path "\\.")))
      path)))


(defn copy-file [from to]
  (io/make-parents to)
  (with-open [outfile (io/output-stream (File. to))
              infile (io/input-stream from)]
    (io/copy infile outfile)))


(defn extension [path]
  (let [no-dir (last (.split path "\\/"))]
    (if (.contains no-dir ".")
      (last (.split no-dir "\\.")))))
