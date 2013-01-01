# crazyhat

Another Simple Static Blog Engine ... written in Clojure

<img
src="https://raw.github.com/eigenhombre/crazyhat/master/crazyhat.png"
alt="Crazy Hat" title="Crazy Hat" align="right" />

`crazyhat` **[work in progress]** is a simple static blog generation
engine similar to [Jekyll](https://github.com/mojombo/jekyll), but
which affords more ease in working with a heirarchy of
pages/directories, with each directory reflected as a page in the
generated site, and with the possibility to navigate to related areas
more easily. Any page can be a blog, and there is an aggregate feed
for all blog posts as well.

### Building

To build using [Leiningen](https://github.com/technomancy/leiningen),
clone and `cd` to this project, then:

    lein uberjar

### Running

To run,

    java -jar <build-dir>/crazyhat-app.jar <dest-dir>

`build-dir` will probably be `./target` if you have just built the jar
inside the `crazyhat` directory..

`dest-dir` is a directory with your site's files. It should have a
subdirectory `markup` in it which contains markdown files to be
converted to HTML, as well as any image, CSS or JavaScript files.
`markup` can have any number of subdirectories, sub-subdirectories,
etc.; Markdown files in any of these are processed into HTML files in
`<dest-dir>/site`, preserving the directory heirarchy in `markup.`
Other files are copied to the `site` dir when they appear.[1]

### Testing

While the application is running, files can be browsed at
[http://localhost:8080/](http://localhost:8080/), with subdirectories
specified as extra terms in the URL.

[1] In a future version the copy operation may be changed to a hard
link (supported in Java 7).

### Deployment:

    rsync -vurt <dest-dir>/site user@remotehost:path/to/deploy/dir

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
