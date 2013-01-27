# crazyhat

Another Simple Static Blog Engine ... written in Clojure

<img
src="https://raw.github.com/eigenhombre/crazyhat/master/crazyhat.png"
alt="Crazy Hat" title="Crazy Hat" align="right" />

Crazyhat **[work in progress]** is a simple static blog generation
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

### Testing Your Site

While the application is running, files can be browsed at
[http://localhost:8080/](http://localhost:8080/), with subdirectories
specified as extra terms in the URL.

[1] In a future version the copy operation may be changed to a hard
link (supported in Java 7).

### Deployment

    rsync -vurt <dest-dir>/site user@remotehost:path/to/deploy/dir

For more details on how to use the application, see the [tutorial](#tutorial), below.

## FAQ

**Q**: Why not write a Rails/Django/Ring/... app?

A: Because:

- I don't have any dynamic content except for what JavaScript can
easily provide.  Basically I just need to serve text and pictures.
- Static content is very fast to serve.
- Deployment is trivial - just rsync and use Mongoose/NGINX, rather
than having to set up databases, mod-blah in Apache, etc., etc. -- a
lot of incidental complexity for little gain.
- I write Python/Django all day in my current day job.  Time for something
else for personal stuff.

**Q**: Why not just use Jekyll?

A: I like Jekyll, but I prefer to have multiple blogs in one site,
which Jekyll doesn't really support easily.  I want:

1. to have the navigation bar for each page updated automagically based on
neighbors and children in directory structure;
1. to allow any page to be its own blog;
1. extensive, automated image processing [more on this to come];
1. to customize Clojure code (simple & beautiful) rather than Ruby
(somewhat pretty but not simple);
1. to minimize the amount of HTML and CSS I have to write; I'd rather
write in [better](https://github.com/weavejester/hiccup)
[DSLs](https://github.com/paraseba/cssgen).
1. Complete unit test coverage to make ruthless refactoring possible
at any point in the code development.

Many features of Crazyhat are taken from my Django application
[Coriolis](http://www.npxdesigns.com/projects/coriolis/).

**Q**: What's with the name?

A: When I was typing `lein new`, I paused for a moment to think of a
new name.  Someone walked into the room and said, "You're wearing your
crazy new hat!"  And so it was....

## <a id="tutorial"></a>Tutorial

We'll assume for the purposes of this tutorial that you're in the root
directory of the source tree and have built the Jar file in `./target`
(see building instructions at top).

Make a toplevel directory for your blog:

    mkdir myblog

Start the application and point it to the new directory:

    java -jar target/crazyhat-app.jar myblog

Fire up your browser and point it to localhost:8080.  You should get a 404-not-found page.

### Creating your first content

In a new terminal window, cd to myblog and make the markup directory.

    cd /path/to/crazyhat/myblog
    mkdir markup
    cd markup

Create a file `index.md` file in that directory, as follows:

    # Welcome!

    Welcome to my new blog.  Crazy hats for everyone!

You should see output in your Crazyhat app showing that it discovered
and processed the new file.  Hit reload in your browser: the 404 error
message should be gone now, and you should see some brand-new HTML content
rendered in its place.

### Adding an Image

Adding images is easy.  You can steal mine for the purposes of the test.  In your toplevel `markup` dir,

    wget https://raw.github.com/eigenhombre/crazyhat/master/crazyhat.png --no-check-certificate

Add the following to `index.md`:

    ![image](crazyhat.png)

Reload your browser and you should see the picture on your new page.

### Styling your new site

To reduce the fuglyness of your site, you can add a css file.  Put the following in `markup/site.css`:

    body {
      font-size: 14px;
      font-family: verdana, arial, sans-serif;
      color: #222;
      background-color: #f7f7f7;
    }

    h1 {
      font-size: 20px;
    }

For the moment, the HTML produced by Crazyhat links to `/site.css`
in every HTML page.  When you reload your browser, you should see the
new styling take effect.

### Adding Blog Posts

To create blog posts inside your home dir, simply make new markdown
files.  For example, let's create `first-blog-post.md`:

    # First Post to My New Blog

    This is my first blog post.  There are many like it, but this one
    is mine.

Upon reloading `index.html`, you should see a link to the blog post,
which, when clicked, displays the new page.  (**This behavior is still
under construction**).

### Special handling for pictures

As shown above, you can easily insert pictures. However, Crazyhat
provides some special handling which makes this a bit easier. 

Images copied into the `markup` directory (or subdirectories) will
cause copies to be placed in the appropriate `site` directory, along
with smaller thumbnail images for display in Web pages. A special
markup syntax is supported by which you may easily include both a
thumbnail with caption, and a larger-size image (**TODO**).

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
