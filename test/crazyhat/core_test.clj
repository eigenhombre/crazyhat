(ns crazyhat.core-test
  (:import java.io.File)           
  (:use clojure.test
        midje.sweet
        crazyhat.core
        crazyhat.util
        clojure.pprint)
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [net.cgrand.enlive-html :as html]
            [watchtower.core :as w]))


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
       (pathjoin "a") => "a"
       (pathjoin "/a") => "/a"
       (pathjoin "a" "b")  => "a/b"
       (pathjoin "a/" "b") => "a/b"
       (pathjoin "a" "/b") => "a/b"
       (pathjoin "/a" "b") => "/a/b"
       (pathjoin "a" "b" "c") => "a/b/c")



;; Test harness where we actually create files and check corresponding
;; results:
(def root-dir "/tmp/testdir")
(def markup-dir (pathjoin root-dir "markup"))
(def site-dir (pathjoin root-dir "site"))


(defn post-names-in-str
  [contents]
  (map html/text (html/select (-> contents
                                  java.io.StringReader.
                                  html/html-resource) [:body :ul :li])))

(defn urls-for-posts
  "
  Look for things in the form
  <body>...<ul>...<li><a class='blogpost' href='post1'>post 1</a></li>...
  "
  [contents]
  (map (comp :href :attrs)
       (html/select (-> contents
                        java.io.StringReader.
                        html/html-resource) [:body :ul :li :a.blogpost])))


(def file-list
  [{:source "somedir/to/this/file/fake.jpg"}
   {:source "fake.png"}
   {:source "site.css"}
   {:source "media/css/ancillary.css"}

   {:source "index.md"         :dest "index.html"
    :body "# Header\n\nbody"   :posts ["blogpost" "blogpost2"]}

   {:source "blogpost.md"      :dest "blogpost.html"
    :body "A sample blog post" :posts ["blogpost" "blogpost2"]}

   {:source "blogpost2.md"     :dest "blogpost2.html"
    :body "Another blog post"  :posts ["blogpost" "blogpost2"]}])


(defn is-jpg
  [path]
  (let [[_ ext] (splitext path)]
    (= (s/lower-case ext) "jpg")))


(facts "about is-jpg"
       (is-jpg "") => falsey
       (is-jpg "foo.jpg") => truthy
       (is-jpg "FOO.JPG") => truthy)


(facts "about thumb-path"
       (thumb-path "") => ""
       (thumb-path "x.png") => "x-inline.png"
       (thumb-path "x.jpg") => "x-inline.jpg"
       (thumb-path "x.JPG") => "x-inline.JPG"
       (thumb-path "/x/y/z.jpg") => "/x/y/z-inline.jpg")


(defn make-test-file [fmap]
  (let [path (pathjoin markup-dir (:source fmap))
        content (get fmap :body "fake stuff")]
    (io/make-parents path)
    (if (is-jpg path)
      (copy-file "resources/test-image.jpeg" path)
      (spit path content))))


(defn destination-path-for [fmap]
  (pathjoin site-dir (get fmap :dest (:source fmap))))

(defn source-path-for [fmap]
  (pathjoin site-dir (:source fmap)))

(defn posts-for [fmap]
  (get fmap :posts []))

(defn path-exists [path] (-> path File. .exists))


(fact "Creating thumbnails works"
      (.exists (File. "/tmp/tmp.jpg")) => truthy
      (against-background
       (before :facts
               (make-thumbnail "resources/test-image.jpeg"
                               "/tmp/tmp.jpg" 100))
       (after :checks
              (io/delete-file "/tmp/tmp.jpg"))))


(defn image-width [path]
  (let [img (javax.imageio.ImageIO/read (File. path))]
    (.getWidth img)))


(against-background
 [(before :contents
          (do
            ;; Clean test directory
            (delete-file-recursively (File. root-dir))
            ;; Make test files:
            (doseq [fmap file-list]
              (make-test-file fmap))
            ;; Handle the output file creation:
            (handle-update! (wanted-file-seq markup-dir)
                           markup-dir site-dir root-dir)))]
 (doseq [f file-list]
   (let [destpath (destination-path-for f)
         srcpath (source-path-for f)
         [filen ext] (splitext destpath)
         contents (slurp destpath)]
     (fact "Desired output file exists"
           (path-exists destpath) => truthy)
     (case ext
       "html"
       (facts "about HTML files"
              contents => (contains "!DOCTYPE")
              contents => (contains "site.css")
              (post-names-in-str contents) => (just (posts-for f))
              (count (urls-for-posts contents)) => (count (posts-for f)))
       "jpg"
       (facts "about generated JPG files"
              (path-exists srcpath) => truthy
              (path-exists destpath) => truthy
              (path-exists (thumb-path destpath)) => truthy
              (< (image-width (thumb-path destpath))
                 (image-width destpath)) => truthy)
       ;; else:
       nil))))
