(ns crazyhat.util
  (:import java.io.File)
  (:require [clojure.string :as st]
            [clojure.java.io :as io]))


(defn pathjoin [& args]
  "Join any sequence of directory names, correctly handling duplicate
'/' characters, including leading '/'."
  (let [base (st/join "/"
                      (map (comp #(st/replace % #"/$" "")
                                 #(st/replace % #"^/" ""))
                           args))]
    (if (= (first (first args)) \/)
      (str "/" base)
      base)))


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


(defn extension [path]
  (let [no-dir (last (.split path "\\/"))]
    (if (.contains no-dir ".")
      (last (.split no-dir "\\.")))))


(defn copy-file [from to]
  (io/make-parents to)
  (with-open [outfile (io/output-stream (File. to))
              infile (io/input-stream from)]
    (io/copy infile outfile)))


(defn delete-file-recursively
  "Delete file f. If it's a directory, recursively delete all its
contents. Raise an exception if any deletion fails unless silently is
true. [stolen/modified from clojure-contrib]"
  [f]
  (if (.isDirectory f)
    (doseq [child (.listFiles f)]
      (delete-file-recursively child)))
  (.delete f))


(defn str-contains?
  "C.f. https://groups.google.com/forum/?fromgroups=#!topic/clojure/VlKrgA3Dco4"
  [^String big ^String little]
  (not (neg? (.indexOf big little))))


(defn get_resource [nm]
  "See http://stackoverflow.com/questions/2044394/
   how-to-load-program-resources-in-clojure"
  (ClassLoader/getSystemResource nm))


(defn thumb-path
  [path]
  (if-not (seq path)
    ""
    (let [[base ext] (splitext path)]
      (str base "-inline." ext))))


(defn make-thumbnail [filename new-filename width]
  "
  Adapted from http://briancarper.net/blog/465/
  "
  (let [img (javax.imageio.ImageIO/read (File. filename))
        imgtype (java.awt.image.BufferedImage/TYPE_INT_ARGB)
        width (min (.getWidth img) width)
        height (* (/ width (.getWidth img)) (.getHeight img))
        simg (java.awt.image.BufferedImage. width height imgtype)
        g (.createGraphics simg)]
    (.drawImage g img 0 0 width height nil)
    (.dispose g)
    (javax.imageio.ImageIO/write simg "png" (File. new-filename))))
