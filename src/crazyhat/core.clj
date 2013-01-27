(ns crazyhat.core
  (:gen-class)
  (:import [java.io File])
  (:require [watchtower.core :as w]
            [ring.adapter.jetty :as j]
            [ring.util.response :as resp]
            [clojure.string :as st]
            [markdown.core :as md]
            [clojure.java.io :as io]
            crazyhat.util)
  (:use crazyhat.util
        [hiccup.core :only [html]]
        [hiccup.element :only [link-to]]
        [hiccup.page :only [html5 include-css]]))


(defn dest-file-extension
  [x]
  (case x
    "md" "html"
    x))


(defn new-filenames-and-extensions
  [files rootdir]
  (map (fn [F]
         (let [path (.getPath F)
               path-in-markup-dir (st/replace path (pathjoin rootdir "markup/") "")
               ext (extension path)
               newext (dest-file-extension ext)
               newfile-oldext (pathjoin rootdir "site" path-in-markup-dir)
               [newf _] (splitext newfile-oldext)
               newfile (str newf "." newext)]
           [path newfile newext]))
       files))


(defn handle-copy!
  [oldfile newfile verbose]
  (if verbose (println oldfile "==copy==>" newfile))
  (copy-file oldfile newfile))


(defn wrap-html
  [posts maincontent]
  (letfn [(link-for-post [p]
            [:li (link-to {:class "blogpost"}
                          (str p ".html")
                          p)])]
    (html
     (html5
      [:head (include-css "/site.css")]
      [:body
       [:div#content maincontent]
       [:div#posts [:ul (map link-for-post posts)]]]))))


(defn is-md [f]
  (= (extension f) "md"))


(defn posts-for-file
  "
  Find blog posts in same directory as <filename> by actually listing
  directory (FIXME: inefficient)
  "
  [filename]
  (->> filename
       dirname
       File.
       .listFiles
       seq
       (map #(.getName %))
       (filter is-md)
       (remove #(= "index.md" %))
       (map #(first (st/split % #"\.")))))


(defn handle-html!
  [oldfile newfile verbose]
  (if verbose (println oldfile "==HTML==>" newfile))
  (->> oldfile
       slurp
       md/md-to-html-string
       (wrap-html (posts-for-file oldfile))
       (spit newfile)))


(defn handle-update!
  [files srcdir destdir rootdir & more]
  (let [verbose (:verbose (apply hash-map more))
        newfiles (new-filenames-and-extensions files rootdir)]
    (doseq [[oldfile newfile ext] newfiles]
      (io/make-parents newfile)
      (case ext
        "html" (handle-html! oldfile newfile verbose)
        ("png" "css") (handle-copy! oldfile newfile verbose)
        ("jpg") (do
                  (handle-copy! oldfile newfile verbose)
                  (make-thumbnail oldfile (thumb-path newfile) 300))
        (println "Don't know what to do with" oldfile "!!!")))))        


(def extensions-to-update [:md :jpg :png :css])


(defn wanted-file-seq
  "Simulate behavior of core/watcher for testing"
  [root]
  (let [s (file-seq (File. root))
        filenames (map #(.getPath %) s)]
    (for [ext (map name extensions-to-update)
          f filenames
          :when (= ext (extension f))]
      (File. f))))


(defn watcher
  [root]
  (let [src (pathjoin root "markup")
        dest (pathjoin root "site")]
    (w/watcher [src]
               (w/rate 100)
               (w/file-filter w/ignore-dotfiles)
               (w/file-filter (apply w/extensions extensions-to-update))
               (w/on-change (fn [_] (handle-update! (wanted-file-seq root)
                                                    src dest root :verbose true))))))


(defn server
  [root]
  (letfn [(serve-stat [req]
            (print "Serving" (:uri req) ": ")
            (let [r (resp/file-response (:uri req)
                                        {:root (pathjoin root "site")})]
              (println r)
              r))]
    (future (j/run-jetty serve-stat {:port 8080 :join? false}))))


(defn -main
  [ & args ]
  (case (count args)
    0 (println "Please supply a directory for your site toplevel")
    1 (let [[path] args]
        (if (.exists (File. path))
          (do
            (watcher path)
            (server path))
          (println (format "Directory %s doesn't exist!" path))))
    (println "Please supply only one directory name")))
