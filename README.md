# crazyhat

Another Simple Static Blog Engine ... written in Clojure

![crazyhat](crazyhat.png)
`crazyhat` **[work in progress]** is a simple static blog generation engine similar to [Jekyll](https://github.com/mojombo/jekyll), but which affords more ease in working with a heirarchy of pages/directories, with each directory reflected as a page in the generated site, and with the possibility to navigate to related areas more easily.  Any page can be a blog, and there is an aggregate feed for all blog posts as well.

### Building

To build using [Leiningen](https://github.com/technomancy/leiningen), clone and `cd` to this project, then:

    lein uberjar

### Running

To run,

    java -jar <target-dir>/crazyhat-app.jar <site-dir>

`target-dir` will probably be `./target` if you have just built the jar.

`site-dir` should have a subdirectory `markdown` in it which contains markdown files to be converted to HTML.

When any Markdown files in `site-dir/markdown` are updated, corresponding HTML files will be automatically be made in `site-dir/site`.  This includes subdirectories.

Static files (`.jpg`s, at the moment) are copied to the `site` dir when they appear.  In a future version the copy operation may be changed to a hard link (supported in Java 7).

While the application is running, files can be browsed at [http://localhost:8080/](http://localhost:8080/), with subdirectories specified as extra terms in the URL.

### Deployment:

    rsync -vurt <site-dir>/site user@remotehost:path/to/deploy/dir

## License

Copyright © 2012 John Jacobsen

Distributed under the Eclipse Public License, the same as Clojure.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF THIRD PARTY RIGHTS. IN
NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
OR OTHER DEALINGS IN THE SOFTWARE.
